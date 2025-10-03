package com.example.stt.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface TranscriptionService {
  TranscriptionResult transcribe(MultipartFile file) throws IOException;
}
