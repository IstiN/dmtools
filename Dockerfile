FROM eclipse-temurin:23-jre

# Set working directory
WORKDIR /app

# Build arguments  
ARG GEMINI_API_KEY
ARG GEMINI_DEFAULT_MODEL=gemini-2.5-flash-preview-05-20

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV GEMINI_API_KEY=${GEMINI_API_KEY}
ENV GEMINI_DEFAULT_MODEL=${GEMINI_DEFAULT_MODEL}

# Copy the JAR file
COPY dmtools-appengine.jar /app/app.jar

# Create a startup script that logs environment variables for debugging
RUN echo '#!/bin/bash' > /app/start.sh && \
    echo 'echo "=== ENVIRONMENT VARIABLES DEBUG ===" ' >> /app/start.sh && \
    echo 'echo "SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE"' >> /app/start.sh && \
    echo 'echo "PORT=$PORT"' >> /app/start.sh && \
    echo 'echo "APP_BASE_URL=$APP_BASE_URL"' >> /app/start.sh && \
    echo 'echo "--- OAuth2 Configuration ---"' >> /app/start.sh && \
    echo 'echo "GOOGLE_CLIENT_ID=$GOOGLE_CLIENT_ID"' >> /app/start.sh && \
    echo 'echo "GOOGLE_CLIENT_SECRET length: ${#GOOGLE_CLIENT_SECRET}"' >> /app/start.sh && \
    echo 'echo "GITHUB_CLIENT_ID=$GITHUB_CLIENT_ID"' >> /app/start.sh && \
    echo 'echo "GITHUB_CLIENT_SECRET length: ${#GITHUB_CLIENT_SECRET}"' >> /app/start.sh && \
    echo 'echo "MICROSOFT_CLIENT_ID=$MICROSOFT_CLIENT_ID"' >> /app/start.sh && \
    echo 'echo "MICROSOFT_CLIENT_SECRET length: ${#MICROSOFT_CLIENT_SECRET}"' >> /app/start.sh && \
    echo 'echo "--- Other Config ---"' >> /app/start.sh && \
    echo 'echo "JWT_SECRET length: ${#JWT_SECRET}"' >> /app/start.sh && \
    echo 'echo "GEMINI_API_KEY length: ${#GEMINI_API_KEY}"' >> /app/start.sh && \
    echo 'echo "=== STARTING APPLICATION ===" ' >> /app/start.sh && \
    echo 'exec java -Xmx768m -XX:+UseContainerSupport -Dfile.encoding=UTF-8 -jar /app/app.jar' >> /app/start.sh && \
    chmod +x /app/start.sh

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Use the startup script
CMD ["/app/start.sh"] 