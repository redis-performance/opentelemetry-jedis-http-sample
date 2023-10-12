# Alpine Linux with OpenJDK JRE
FROM maven:3.6.3-openjdk-8 as builder
WORKDIR /opentelemetry-jedis-http-impl
COPY . .
RUN mvn package

FROM openjdk:8-jre-alpine
WORKDIR /
COPY --from=builder /opentelemetry-jedis-http-impl/target/opentelemetry-jedis-http-impl-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
RUN wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent-all.jar
EXPOSE 7777
ENTRYPOINT ["java", "-javaagent:opentelemetry-javaagent-all.jar", "-Dotel.exporter=jaeger", "-Dotel.exporter.jaeger.endpoint=jaeger:14250", "-Dotel.exporter.jaeger.service.name=otel-jedis", "-jar", "app.jar"]
