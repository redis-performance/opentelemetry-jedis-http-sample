
build:
	cd opentelemetry-jedis-http-impl && mvn clean package && cd ..

run:
	java -jar -Dredis-host=localhost opentelemetry-jedis-http-impl/target/opentelemetry-jedis-http-impl-1.0-SNAPSHOT-jar-with-dependencies.jar

benchmark:
	k6 run benchmarks/sample-get.js

start-docker:
	@docker-compose -f docker-compose.yml up

start-docker-lite:
	@docker-compose -f docker-compose-solely-server.yml up -d

stop-docker:
	@docker-compose -f docker-compose.yml down

stop-docker-lite:
	@docker-compose -f docker-compose-solely-server.yml down

build-docker:
	@docker-compose -f docker-compose.yml build

