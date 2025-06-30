FROM openjdk:23-jdk-slim

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
# ENV GEMINI_API_KEY - should be passed at runtime
# ENV GEMINI_DEFAULT_MODEL - should be passed at runtime

# Copy the JAR file
COPY dmtools-appengine.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Dpolyglot.engine.WarnInterpreterOnly=false", "-Xmx512m", "-Xms256m", "-server", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-Dserver.port=8080", "-jar", "app.jar"] 