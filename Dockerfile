FROM openjdk:23-jdk-slim

# Build arguments  
ARG GEMINI_API_KEY
ARG GEMINI_DEFAULT_MODEL=gemini-2.5-flash-preview-05-20

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV GEMINI_API_KEY=${GEMINI_API_KEY}
ENV GEMINI_DEFAULT_MODEL=${GEMINI_DEFAULT_MODEL}

# Copy the JAR file
COPY dmtools-appengine.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application directly with proper memory settings for Cloud Run
CMD exec java \
    -Dpolyglot.engine.WarnInterpreterOnly=false \
    -Xmx768m \
    -Xms256m \
    -server \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -Dserver.port=${PORT:-8080} \
    -Dserver.address=0.0.0.0 \
    -jar app.jar 