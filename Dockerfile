#FROM ubuntu:22.04
#FROM gradle:latest AS BUILD
#RUN chmod +x gradlew
#RUN ./gradlew build

FROM ubuntu:latest
RUN mkdir -p /etc/app
COPY /VMconnection /etc/app/

FROM eclipse-temurin:8-jdk-jammy
ENTRYPOINT ["java", "-jar", "/etc/app/VMconnection/PartyA/corda.jar"]
CMD ["java", "-jar", "/etc/app/VMconnection/PartyA/corda.jar"]
