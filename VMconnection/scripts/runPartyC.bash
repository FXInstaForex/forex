cd /etc/app/forex-v1/VMconnection/PartyC
java -jar corda.jar &
P1=$!
sleep 30
cd /etc/app/forex-v1;
./gradlew clean runPartyCServer &
P2=$!
wait $P1 $P2
