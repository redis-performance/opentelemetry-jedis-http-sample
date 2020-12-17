# Alpine Linux with OpenJDK JRE
FROM openjdk:8-jre-alpine
WORKDIR /
COPY opentelemetry-jedis-http-impl/target/opentelemetry-jedis-http-impl-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
EXPOSE 7777
# ENTRYPOINT ["java", "-jar", "app.jar"]
CMD "java" "-jar" "app.jar"
