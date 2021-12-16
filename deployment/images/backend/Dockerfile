FROM clojure:openjdk-17-tools-deps-slim-bullseye AS builder

WORKDIR /tmp
RUN apt update && apt upgrade -y && \
    apt install -y curl make git rlwrap unzip && \
    rm -rf /var/lib/apt/lists/* \

RUN curl -sLO https://raw.githubusercontent.com/babashka/babashka/master/install && \
    chmod +x install && ./install


WORKDIR /project
COPY .git .
COPY modules/backend ./modules/backend
COPY version.tmpl .


WORKDIR /project/modules/backend
RUN bb uber



FROM eclipse-temurin:17-alpine AS runner

WORKDIR /app
COPY --from=builder /project/modules/backend/target/backend.jar backend.jar

EXPOSE 8080

CMD ["java", "-XX:+UseContainerSupport","-XX:MaxRAMPercentage=85", "-XX:+UseZGC", "-jar", "backend.jar"]
