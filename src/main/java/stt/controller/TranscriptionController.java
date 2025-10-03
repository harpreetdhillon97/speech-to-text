package com.example.stt.controller;

import com.example.stt.service.TranscriptionService;
import com.example.stt.service.TranscriptionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class TranscriptionController {

  private final TranscriptionService service;
  private final Logger log = LoggerFactory.getLogger(TranscriptionController.class);

  public TranscriptionController(TranscriptionService service) {
    this.service = service;
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("OK");
  }

  @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<TranscriptionResult> transcribe(@RequestPart("file") MultipartFile file) {
    System.out.println("Received file: " + file.getOriginalFilename());
    if (file == null || file.isEmpty()) {
      return ResponseEntity.badRequest().body(new TranscriptionResult("error", "No file uploaded"));
    }

    try {
      TranscriptionResult result = service.transcribe(file);
      return ResponseEntity.ok(result);
    } catch (IllegalArgumentException ex) {
      log.warn("Unsupported input: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
              .body(new TranscriptionResult("error", ex.getMessage()));
    } catch (IOException ex) {
      log.error("IO error during transcription", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new TranscriptionResult("error", "Internal error processing audio"));
    } catch (Exception ex) {
      log.error("Unexpected error", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new TranscriptionResult("error", "Unexpected server error"));
    }
  }

  // @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  //   public ResponseEntity<String> transcribe(@RequestParam("file") MultipartFile file) {
  //       try {
  //           System.out.println("✅ Received file: " + file.getOriginalFilename());
  //           return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
  //       } catch (Exception e) {
  //           e.printStackTrace();
  //           return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
  //       }
  //   }
}
