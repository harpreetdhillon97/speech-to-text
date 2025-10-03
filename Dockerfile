# Stage 1: build using official maven image (mvn available)
FROM maven:3.9.5-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy pom first to cache dependencies
COPY pom.xml ./
RUN mvn -q -B dependency:go-offline

# Copy source and build artifact
COPY src ./src
RUN mvn -q -B package -DskipTests

# # Stage 2: runtime
# FROM eclipse-temurin:17-jre-jammy
# WORKDIR /app
# COPY models ./models
# COPY --from=builder /workspace/target/*.jar app.jar
# ENV VOSK_MODEL_PATH=/app/models
# EXPOSE 8080
# CMD ["java","-jar","/app/app.jar"]

# Stage 2 Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Install ffmpeg and wget/unzip
RUN apt-get update && apt-get install -y ffmpeg wget unzip && rm -rf /var/lib/apt/lists/*

# Download small Vosk model
RUN wget -q https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip \
    && unzip vosk-model-small-en-us-0.15.zip \
    && rm vosk-model-small-en-us-0.15.zip \
    && mv vosk-model-small-en-us-0.15 model

# Copy built JAR from builder
COPY --from=builder /workspace/target/*.jar app.jar

ENV VOSK_MODEL_PATH=/app/model
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]