cd /etc/app/forex-v1/VMconnection/PartyA
java -jar corda.jar &
P1=$!
cd /etc/app/forex-v1;
./gradlew clean runPartyAServer &
P2=$!
wait $P1 $P2
