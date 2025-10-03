# Speech-to-Text Service Design Document

## Executive Summary

This document outlines the design and implementation of an open-source Speech-to-Text (STT) service built for the ZoomInfo SE3 Take-Home assignment. The service provides HTTP API endpoints for converting audio files to text using the Vosk open-source speech recognition engine.

## Table of Contents

1. [Business Requirements](#business-requirements)
2. [Technical Architecture](#technical-architecture)
3. [System Design](#system-design)
4. [Implementation Details](#implementation-details)
5. [API Specification](#api-specification)
6. [Deployment Strategy](#deployment-strategy)
7. [Future Enhancements](#future-enhancements)

## Business Requirements

### Original Assignment Requirements
- Build an open-source speech-to-text service
- Expose HTTP API endpoints for audio transcription
- Use open-source solutions (Vosk, Whisper, etc.)
- Provide health check endpoints
- Include CI/CD pipeline configuration
- Support Docker deployment
- Provide comprehensive documentation

### Functional Requirements
- **Audio File Processing**: Accept various audio formats (MP3, WAV, M4A, etc.)
- **Speech Recognition**: Convert audio to text with reasonable accuracy
- **REST API**: Provide RESTful endpoints for service interaction
- **Health Monitoring**: Expose health check endpoints for monitoring
- **Error Handling**: Graceful error handling with appropriate HTTP status codes

### Non-Functional Requirements
- **Performance**: Process audio files efficiently with minimal latency
- **Scalability**: Support containerized deployment for horizontal scaling
- **Reliability**: Robust error handling and logging
- **Maintainability**: Clean, well-documented code following best practices
- **Portability**: Docker-based deployment for environment consistency

## Technical Architecture

### Technology Stack
- **Runtime**: Java 17 (OpenJDK)
- **Framework**: Spring Boot 3.1.6
- **Speech Recognition**: Vosk 0.3.45 (Open-source speech recognition toolkit)
- **Audio Processing**: FFmpeg (Audio format conversion and normalization)
- **Build Tool**: Maven 3.x
- **Containerization**: Docker with Docker Compose
- **Testing**: JUnit 5, Mockito 5.3.1

### Architecture Patterns
- **Layered Architecture**: Clear separation between controller, service, and configuration layers
- **Dependency Injection**: Spring's IoC container for loose coupling
- **Strategy Pattern**: TranscriptionService interface for potential multiple implementations
- **Template Method**: Standardized audio processing pipeline

## System Design

### High-Level Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client        │    │   Spring Boot   │    │   Vosk Engine   │
│   Application   │───▶│   REST API      │───▶│   Speech        │
│                 │    │                 │    │   Recognition   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   FFmpeg        │
                       │   Audio         │
                       │   Processing    │
                       └─────────────────┘
```

### Component Architecture

#### 1. Controller Layer (`TranscriptionController`)
- **Responsibility**: HTTP request handling and response formatting
- **Key Features**:
  - Multipart file upload handling
  - Input validation and error response formatting
  - HTTP status code management
  - Request logging

#### 2. Service Layer (`VoskTranscriptionService`)
- **Responsibility**: Core business logic and audio processing
- **Key Features**:
  - Audio format conversion using FFmpeg
  - Vosk speech recognition integration
  - Temporary file management
  - Error handling and cleanup

#### 3. Configuration Layer (`VoskConfig`)
- **Responsibility**: Application configuration and bean management
- **Key Features**:
  - Vosk model initialization
  - Resource management
  - Environment-specific configurations

### Data Flow

1. **File Upload**: Client uploads audio file via POST `/api/transcribe`
2. **Validation**: Controller validates file presence and format
3. **Temporary Storage**: File saved to temporary location
4. **Audio Conversion**: FFmpeg converts audio to 16kHz mono PCM WAV
5. **Speech Recognition**: Vosk processes converted audio
6. **Result Processing**: JSON response formatted and returned
7. **Cleanup**: Temporary files removed

## Implementation Details

### Audio Processing Pipeline

```java
// Simplified processing flow
MultipartFile → Temp File → FFmpeg Conversion → Vosk Recognition → JSON Result
```

### Error Handling Strategy

| Error Type | HTTP Status | Response |
|------------|-------------|----------|
| No file uploaded | 400 Bad Request | Error message |
| Unsupported format | 415 Unsupported Media Type | Format error |
| Processing failure | 500 Internal Server Error | Generic error |
| FFmpeg failure | 415 Unsupported Media Type | Conversion error |


## API Specification

### Health Check Endpoint

```http
GET /api/health
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: text/plain

OK
```

**cURL Example:**
```bash
curl -X GET https://speechtotext-2sm4.onrender.com/api/health
```

### Transcription Endpoint

```http
POST /api/transcribe
Content-Type: multipart/form-data
```

**Request Model:**
- **Method**: POST
- **Content-Type**: multipart/form-data
- **Form Parameter**: `file` (required) - Audio file binary data
- **Supported Formats**: MP3, WAV, M4A, FLAC, OGG, WEBM
- **File Size Limit**: 1MB (configurable)

### Postman Setup for Speech-to-Text API
To test your deployed Speech-to-Text API in **Postman**, follow these exact steps:
---
**1. Method & URL**
* **Method**: `POST`
* **URL**:
  ```
  https://speechtotext-2sm4.onrender.com/api/transcribe
  ```
---
**2. Request Body**
* Go to **Body** → select **form-data**
* Add a key/value pair:
  * **Key**: `file` (must match what your controller expects)
  * **Type**: `File`
  * **Value**: Upload a `.wav` file from your computer
---
**3. Send Request**

* Click **Send**
* You should receive a JSON response with the transcription result.
---

**Request:**
```
file: [audio file binary data]
```

**Success Response Model:**
```json
{
  "status": "ok",
  "transcript": "{\"text\":\"transcribed speech content\"}"
}
```

**Success Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": "ok",
  "transcript": "{\"text\":\"transcribed speech content\"}"
}
```

**Error Response Models:**

*Bad Request (400):*
```json
{
  "status": "error",
  "transcript": "No file uploaded"
}
```

*Unsupported Media Type (415):*
```json
{
  "status": "error",
  "transcript": "Unsupported audio or ffmpeg conversion failed"
}
```

*Internal Server Error (500):*
```json
{
  "status": "error",
  "transcript": "Internal error processing audio"
}
```

**Error Responses:**
```http
HTTP/1.1 400 Bad Request
{
  "status": "error",
  "transcript": "No file uploaded"
}

HTTP/1.1 415 Unsupported Media Type
{
  "status": "error",
  "transcript": "Unsupported audio or ffmpeg conversion failed"
}

HTTP/1.1 500 Internal Server Error
{
  "status": "error",
  "transcript": "Internal error processing audio"
}
```

**CURL Examples:**

*Basic transcription request:*
```bash
curl -X POST https://speechtotext-2sm4.onrender.com/api/transcribe \
  -F "file=@/path/to/audio.wav" \
  -H "Content-Type: multipart/form-data"
```

*Transcription with different audio formats:*
```bash
# MP3 file
curl -X POST https://speechtotext-2sm4.onrender.com/api/transcribe \
  -F "file=@/path/to/recording.mp3"

# WAV file
curl -X POST https://speechtotext-2sm4.onrender.com/api/transcribe \
  -F "file=@/path/to/speech.wav"

# M4A file
curl -X POST https://speechtotext-2sm4.onrender.com/api/transcribe \
  -F "file=@/path/to/voice_memo.m4a"
```

*Testing with Docker deployment:*
```bash
# Production deployment
curl -X POST https://speechtotext-2sm4.onrender.com/api/transcribe \
  -F "file=@/path/to/audio_file.mp3"
```

*Error handling example:*
```bash
# This will return 400 Bad Request
curl -X POST https://speechtotext-2sm4.onrender.com/api/transcribe

# This will return 415 Unsupported Media Type (if file format not supported)
curl -X POST https://speechtotext-2sm4.onrender.com/api/transcribe \
  -F "file=@document.pdf"
```

## Deployment Strategy

### CI/CD Pipeline

The service includes GitHub Actions configuration for:
- **Build Automation**: Maven compilation and testing
- **Docker Image Building**: Automated container creation
- **Quality Gates**: Test execution and coverage reporting
- **Deployment**: Automated deployment to staging/production


## Future Enhancements

### Short-term Improvements
1. **Multiple Model Support**: Support for different language models
2. **Async Processing**: Queue-based processing for large files
3. **Result Caching**: Cache transcription results for duplicate files
5. **Enhanced Error Handling**: More granular error codes and messages

### Long-term Enhancements
1. **WebSocket Support**: Real-time streaming transcription
2. **Multiple Engine Support**: Whisper, DeepSpeech integration
3. **Audio Format Detection**: Automatic format detection and conversion