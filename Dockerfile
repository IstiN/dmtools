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

# Create startup script
RUN echo '#!/bin/bash\nexec java -Dpolyglot.engine.WarnInterpreterOnly=false -Xmx512m -Xms256m -server -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Dserver.port=${PORT:-8080} -Dserver.address=0.0.0.0 -jar app.jar' > /start.sh && chmod +x /start.sh

# Run the application
ENTRYPOINT ["/start.sh"] 