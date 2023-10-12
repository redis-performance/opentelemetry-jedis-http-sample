
build:
	cd opentelemetry-jedis-http-impl && mvn clean package && cd ..

start-docker:
	@docker-compose -f docker-compose.yml up

stop-docker:
	@docker-compose -f docker-compose.yml down

build-docker:
	@docker-compose -f docker-compose.yml build

