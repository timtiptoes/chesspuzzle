# Build stage — gradle image has Gradle pre-installed, no network download needed
FROM gradle:8.10-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src src
RUN gradle bootJar --no-daemon

# Run stage
FROM eclipse-temurin:17-jre
RUN apt-get update && \
    apt-get install -y nginx python3 python3-pip && \
    pip3 install --no-cache-dir --break-system-packages chess && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/build/libs/starter_web-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/lichessDB.db /app/lichessDB.db
COPY board_render/main.py /app/board_render/main.py
COPY web/chesspuzzleui.html /usr/share/nginx/html/chesspuzzleui.html
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY docker/start.sh /start.sh
RUN chmod +x /start.sh && rm -f /etc/nginx/sites-enabled/default

EXPOSE 80 8081
ENTRYPOINT ["/start.sh"]
