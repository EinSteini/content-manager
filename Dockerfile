# Build container
FROM gradle:8.13.0-jdk21-alpine AS build
WORKDIR /app

COPY settings.gradle.kts build.gradle.kts ./
COPY domain/src/ domain/src/
COPY domain/build.gradle.kts domain/
COPY application/src/ application/src/
COPY application/build.gradle.kts application/
COPY adapters/src/ adapters/src/
COPY adapters/build.gradle.kts adapters/
COPY plugins/ plugins/
COPY main/src/ main/src/
COPY main/build.gradle.kts main/

RUN gradle clean :main:shadowJar \
    --no-daemon \
    --warning-mode all \
    -Porg.gradle.java.installations.auto-download=true \
    -Porg.gradle.java.installations.paths=/opt/java/openjdk

# Run container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/main/build/libs/*.jar app.jar

RUN addgroup -S app && adduser -S app -G app && \
    chown -R app:app /app

USER app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

EXPOSE 8443
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]