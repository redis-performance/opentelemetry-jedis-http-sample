
build:
	cd opentelemetry-jedis-http-impl && mvn clean package && cd ..

run:
	java -jar -Dredis-host=localhost opentelemetry-jedis-http-impl/target/opentelemetry-jedis-http-impl-1.0-SNAPSHOT-jar-with-dependencies.jar

benchmark:
	k6 run benchmarks/sample-get.js

start-docker:
	@docker-compose -f docker-compose.yml up

stop-docker:
	@docker-compose -f docker-compose.yml down

build-docker:
	@docker-compose -f docker-compose.yml build

