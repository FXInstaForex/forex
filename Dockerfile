#FROM ubuntu:22.04
#FROM gradle:latest AS BUILD
#RUN chmod +x gradlew
#RUN ./gradlew build

FROM ubuntu:latest
RUN mkdir -p /etc/app
COPY /VMConnection /etc/app/
ENTRYPOINT ["java", "-jar", "/etc/app/VMConnection/PartyA/docker.jar"]
CMD ["java", "-jar", "/etc/app/VMConnection/PartyA/docker.jar"]
