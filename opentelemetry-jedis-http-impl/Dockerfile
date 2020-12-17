# Alpine Linux with OpenJDK JRE
FROM maven:3.6.3-openjdk-8 as builder
WORKDIR /opentelemetry-jedis-http-impl
COPY . .
RUN mvn package

FROM openjdk:8-jre-alpine
WORKDIR /
COPY --from=builder /opentelemetry-jedis-http-impl/target/opentelemetry-jedis-http-impl-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
EXPOSE 7777
ENTRYPOINT ["java", "-jar", "app.jar"]
