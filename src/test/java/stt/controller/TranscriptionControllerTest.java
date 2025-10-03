package com.example.stt.controller;

import com.example.stt.service.TranscriptionResult;
import com.example.stt.service.TranscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TranscriptionController.class)
public class TranscriptionControllerTest {
  @Autowired MockMvc mvc;
  @MockBean TranscriptionService service;

  @Test
  void healthWorks() throws Exception {
    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/health"))
      .andExpect(status().isOk())
      .andExpect(content().string("OK"));
  }

  @Test
  void transcribeReturnsResult() throws Exception {
    MockMultipartFile audio = new MockMultipartFile("file","test.wav", MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy".getBytes());
    when(service.transcribe(any())).thenReturn(new TranscriptionResult("ok", "{\"text\":\"hello world\"}"));

    mvc.perform(multipart("/api/transcribe").file(audio))
       .andExpect(status().isOk())
       .andExpect(jsonPath("$.status").value("ok"))
       .andExpect(jsonPath("$.transcript").exists());

    verify(service, times(1)).transcribe(any());
  }
}
