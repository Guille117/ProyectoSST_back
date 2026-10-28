FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace
COPY gradle/ gradle/
COPY gradlew settings.gradle build.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends gosu \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
RUN groupadd --system app \
    && useradd --system --gid app --home-dir /app app \
    && mkdir -p /app/uploads \
    && chown -R app:app /app
COPY --from=build /workspace/build/libs/app.jar /app/app.jar
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN sed -i 's/\r$//' /usr/local/bin/docker-entrypoint.sh \
    && chmod +x /usr/local/bin/docker-entrypoint.sh
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV APP_UPLOADS_DIR=/app/uploads
ENV JAVA_OPTS="-Xms256m -Xmx768m -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"
VOLUME ["/app/uploads"]
EXPOSE 8080
ENTRYPOINT ["docker-entrypoint.sh"]
