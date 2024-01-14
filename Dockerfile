# Use Ubuntu as the base image
FROM ubuntu:20.04

# Install OpenJDK
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set the Java environment variables
ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk-amd64
ENV PATH $PATH:$JAVA_HOME/bin

# Install FFmpeg
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*7
EXPOSE 8080

RUN mkdir -p /app/logs/ app/tmp app/download app/out && chmod -R 777 /app

WORKDIR /app

COPY target/ai-content-generator-0.0.1-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]