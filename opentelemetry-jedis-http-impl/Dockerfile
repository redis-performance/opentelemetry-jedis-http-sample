FROM maven:3.9.4-amazoncorretto-21-debian as builder
WORKDIR /opentelemetry-jedis-http-impl
COPY . .
RUN mvn clean package

FROM maven:3.9.4-amazoncorretto-21-debian
WORKDIR /
RUN  apt-get update \
  && apt-get install -y wget
COPY --from=builder /opentelemetry-jedis-http-impl/target/opentelemetry-jedis-http-impl-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
RUN wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.30.0/opentelemetry-javaagent.jar
EXPOSE 7777
ENTRYPOINT ["java", "-javaagent:opentelemetry-javaagent.jar", "-Dotel.exporter=jaeger", "-Dotel.exporter.jaeger.endpoint=jaeger:14250", "-Dotel.exporter.jaeger.service.name=otel-jedis", "-jar", "app.jar"]
