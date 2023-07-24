cd /etc/app/forex-v1/VMconnection/PartyC
java -jar corda.jar &
P1=$!
cd /etc/app/forex-v1;
./gradlew clean runPartyCServer &
P2=$!
wait $P1 $P2
