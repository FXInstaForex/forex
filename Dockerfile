#FROM ubuntu:22.04
#FROM gradle:latest AS BUILD
#RUN chmod +x gradlew
#RUN ./gradlew build

FROM ubuntu:latest
RUN mkdir -p /etc/app
COPY /VMconnection /etc/app/
ENTRYPOINT ["java", "-jar", "/etc/app/VMconnection/PartyA/docker.jar"]
CMD ["java", "-jar", "/etc/app/VMconnection/PartyA/docker.jar"]
