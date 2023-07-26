cd /etc/app/forex-v1/VMconnection/PartyB
java -jar corda.jar &
P1=$!
#cd /etc/app/forex-v1;
#./gradlew clean runPartyBServer &
#P2=$!
wait $P1 
# $P2