#FROM ubuntu:22.04
#FROM gradle:latest AS BUILD
#RUN chmod +x gradlew
#RUN ./gradlew build

#FROM ubuntu:latest
FROM eclipse-temurin:8-jdk-jammy
#RUN mkdir -p /etc/app
WORKDIR /etc
RUN mkdir -p ./app/VMconnection
COPY /VMconnection /etc/app/VMconnection/

#FROM eclipse-temurin:8-jdk-jammy
#ENTRYPOINT ["java", "-jar", "/etc/app/VMconnection/PartyA/corda.jar"]
CMD ["/bin/bash","-c","/etc/app/VMconnection/scripts/runPartyA.bash"]
