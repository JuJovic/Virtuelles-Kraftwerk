## Build stage
FROM maven:3.8.1-openjdk-11-slim AS clientbuild

RUN mkdir -p /app
COPY ./component /app/
COPY ./common-code/src/main/java /app/src/main/java
COPY ./common-code/src/main/thrift/gen-java /app/
WORKDIR /app
RUN mvn package

## Run stage
FROM openjdk:11.0-jre-slim
EXPOSE 6543/udp

RUN mkdir -p /app
# COPY target/component-1.0-SNAPSHOT.jar /app
COPY --from=clientbuild /app/target/component-1.0-SNAPSHOT.jar /app
CMD [ "java", "-jar", "/app/component-1.0-SNAPSHOT.jar" ]
