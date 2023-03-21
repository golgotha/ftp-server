FROM openjdk:17.0.1-slim

MAINTAINER Media Stream AG

ENV JAVA_OPTS="-Xms128m -Xmx256m"

RUN mkdir /app
COPY build/libs/*.jar /app/app.jar

CMD exec java ${JAVA_OPTS} -Djava.security.egd="file:/dev/./urandom" -Djava.awt.headless=true -jar "/app/app.jar"
