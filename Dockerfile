FROM openjdk:11-jre-slim

ARG SERVER_PORT=80
ARG JAR_FILE=target/*.jar
ARG VOL_DIR=/favorites
ARG FAVORITES_FILE=stations.yml

COPY ${JAR_FILE} app.jar

ENV SERVER_PORT=${SERVER_PORT}
EXPOSE ${SERVER_PORT}/tcp

VOLUME ${VOL_DIR}
ENV FAVORITES_FILE=${VOL_DIR}/${FAVORITES_FILE}

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
