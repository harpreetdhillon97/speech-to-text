package com.example.stt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vosk.Model;

import jakarta.annotation.PreDestroy;
import java.io.IOException;

@Configuration
public class VoskConfig {
  @Value("${VOSK_MODEL_PATH:/models/vosk-model-small-en-us-0.15}")
  private String modelPath;

  private Model model;

  @Bean
  public Model voskModel() throws IOException {
    model = new Model(modelPath);
    return model;
  }

  @PreDestroy
  public void cleanup() {
    if (model != null) {
      model.close();
    }
  }
}
