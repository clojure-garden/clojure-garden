FROM clojure:openjdk-17-lein-slim-bullseye AS base

WORKDIR /backend

COPY modules/backend/project.clj .
RUN lein deps
COPY modules/backend .

FROM base AS test
RUN lein test

FROM base AS build
RUN lein uberjar

FROM eclipse-temurin:17-alpine AS stage

WORKDIR /app

EXPOSE 8080

COPY --from=build /backend/target/*-standalone.jar backend.jar

CMD ["java", "-XX:+UseContainerSupport","-XX:MaxRAMPercentage=85", "-XX:+UseZGC", "-jar", "backend.jar"]
