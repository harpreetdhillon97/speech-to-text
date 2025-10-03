package com.example.stt.service;

public class TranscriptionResult {
  private String status;
  private String transcript;

  public TranscriptionResult() {}

  public TranscriptionResult(String status, String transcript) {
    this.status = status;
    this.transcript = transcript;
  }

  // getters + setters
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getTranscript() { return transcript; }
  public void setTranscript(String transcript) { this.transcript = transcript; }
}
