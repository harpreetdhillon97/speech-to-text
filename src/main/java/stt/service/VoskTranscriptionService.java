package com.example.stt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.vosk.Recognizer;
import org.vosk.Model;

import javax.sound.sampled.*;
import java.io.*;

@Service
public class VoskTranscriptionService implements TranscriptionService {

  private final Model model;
  private final Logger log = LoggerFactory.getLogger(VoskTranscriptionService.class);

  public VoskTranscriptionService(Model model) {
    this.model = model;
  }

  @Override
  public TranscriptionResult transcribe(MultipartFile file) throws IOException {
    // Save uploaded file to temp file
    File tmp = File.createTempFile("upload-", "-" + file.getOriginalFilename());
    file.transferTo(tmp);

    // Convert to 16kHz mono PCM WAV using ffmpeg (must be present in container/server)
    File wav = File.createTempFile("converted-", ".wav");
    String[] ffmpegCmd = {
      "ffmpeg", "-y", "-i", tmp.getAbsolutePath(),
      "-ar", "16000", "-ac", "1", "-f", "wav", wav.getAbsolutePath()
    };

    try {
      Process p = new ProcessBuilder(ffmpegCmd).redirectErrorStream(true).start();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (InputStream is = p.getInputStream()) {
        is.transferTo(baos);
      }
      int code = p.waitFor();
      if (code != 0) {
        log.warn("ffmpeg returned code {} and output: {}", code, baos.toString());
        throw new IllegalArgumentException("Unsupported audio or ffmpeg conversion failed");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Audio conversion interrupted", e);
    }

    // Read wav and feed to Vosk recognizer
    try (AudioInputStream ais = AudioSystem.getAudioInputStream(wav)) {
      AudioFormat format = ais.getFormat();
      // Validate correct PCM encoding
      if (format.getSampleRate() != 16000.0f || format.getChannels() != 1) {
        log.debug("Converted file format: {}", format);
      }

      Recognizer recognizer = new Recognizer(model, 16000.0f);
      byte[] buffer = new byte[4096];
      int n;
      while ((n = ais.read(buffer)) >= 0) {
        if (recognizer.acceptWaveForm(buffer, n)) {
          // partial accepted
        } else {
          // partial
        }
      }
      String res = recognizer.getFinalResult();
      // result is JSON from Vosk like {"text":"..."}
      // For simplicity, return the raw JSON as transcript
      return new TranscriptionResult("ok", res);
    } catch (UnsupportedAudioFileException e) {
      throw new IllegalArgumentException("Unsupported audio file");
    } finally {
      tmp.delete();
      wav.delete();
    }
  }
}
