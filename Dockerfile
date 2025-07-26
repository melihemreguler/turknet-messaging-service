# syntax=docker/dockerfile:1
FROM --platform=linux/amd64 openjdk:17-jdk-alpine AS build-project
WORKDIR /turknet-messaging-service

# Install Maven
RUN apk add --no-cache maven

# Copy project files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM --platform=linux/amd64 alpine:3.19
WORKDIR /app

RUN apk add --no-cache openjdk17-jre tzdata

# Set timezone
ENV TZ=Europe/Istanbul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV ARTIFACT_NAME="turknet-messaging-service.jar"

COPY --from=build-project /turknet-messaging-service/target/turknet-messaging-service-*.jar /app/turknet-messaging-service.jar

# Expose application port
EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar turknet-messaging-service.jar"]
