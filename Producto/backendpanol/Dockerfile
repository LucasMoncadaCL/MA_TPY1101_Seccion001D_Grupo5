# syntax=docker/dockerfile:1.7
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

ARG JOOQ_DB_URL
ARG JOOQ_DB_USER
ARG JOOQ_DB_PASSWORD
ENV JOOQ_DB_URL=${JOOQ_DB_URL}
ENV JOOQ_DB_USER=${JOOQ_DB_USER}
ENV JOOQ_DB_PASSWORD=${JOOQ_DB_PASSWORD}

COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY src src
COPY target/generated-sources/jooq target/generated-sources/jooq

RUN test -f target/generated-sources/jooq/com/panol_project/backendpanol/jooq/tables/Category.java \
 && test -f target/generated-sources/jooq/com/panol_project/backendpanol/jooq/tables/Implement.java \
 && test -f target/generated-sources/jooq/com/panol_project/backendpanol/jooq/tables/Location.java
RUN ! grep -q 'public.category.id' target/generated-sources/jooq/com/panol_project/backendpanol/jooq/tables/Category.java \
 && ! grep -q 'public.implement.id' target/generated-sources/jooq/com/panol_project/backendpanol/jooq/tables/Implement.java \
 && ! grep -q 'public.location.id' target/generated-sources/jooq/com/panol_project/backendpanol/jooq/tables/Location.java

RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests "-Djooq.codegen.skip=true" package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/backendpanol-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
