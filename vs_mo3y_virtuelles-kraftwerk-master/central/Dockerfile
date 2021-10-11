## Build stage
FROM maven:3.8.1-openjdk-11-slim AS centralbuild

RUN mkdir -p /app
COPY ./central /app/
COPY ./common-code/src/main/java /app/src/main/java
COPY ./common-code/src/main/thrift/gen-java /app/
WORKDIR /app
RUN mvn package

## Run stage
FROM openjdk:11.0-jre-slim
EXPOSE 6543/udp
EXPOSE 1234

RUN mkdir -p /app
COPY --from=centralbuild /app/target/central-1.0-SNAPSHOT.jar /app
CMD [ "java", "-jar", "/app/central-1.0-SNAPSHOT.jar" ]
