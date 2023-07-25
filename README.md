# CorDapp Example

# CorDapp Example

This repo is the implementation of the tutorial CorDapp. 

You can find the tutorial for this example [here](https://docs.r3.com/en/platform/corda/4.10/community/tutorial-cordapp.html).

Pre-requisite to Run FX Flow 

There are four pieces of required software for CorDapp development:
•	Java 8 JDK
•	IntelliJ IDEA
•	Git
•	Gradle
Use below commands to install required software’s in VM :
1. sudo apt-get update
2.sudo apt-get install openjdk-8-jdk
3.export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
4.docker pull corda/corda-zulu-java1.8-4.6:latest
5.sudo apt-get  install gradle 8.1.1
6.sudo snap install docker
7.sudo apt-get  install docker-compose
8. sudo apt-get install openssh-server
•	sudo service ssh status---to check status
•	sudo service ssh restart ---to restart ssh

Steps to Run FX Flow

1.	Clone the repository: 
•	Git clone the Code from https://github.com/FXInstaForex/forex.git
•	Command:
Git clone https://github.com/FXInstaForex/forex.git
•	Branch Name: PreAndPostTrade
gradle clean deployNodes(intellij) or gradlew.bat clean deployNodes(terminal)
2.	Download “corda-tools-network-bootstrapper-4.10-RC05.jar” from https://software.r3.com/ui/native/corda-releases/net/corda/corda-tools-network-bootstrapper/4.10-RC05/
3.	Create folder “VMConnection”(any name of your choice) and copy “corda-tools-network-bootstrapper-4.10-RC05.jar”  to this folder.
4.	Copy contracts-0.1.jar and workflows-0.1.jar from any node folder and paste in “VMConnection”
•	Ex: <Inside project folder >/build/nodes/PartyA/cordapps
•	Copy PartyB_node.conf, PartyB_node.conf,Notary_conf from <Project Folder >/cordapp-                    example/build/nodes and paste in “VMConnection”.
             Open each *_node.conf file and update p2pAddress, rpcSettings:adminAdress and admin
Ex:
 
•	P2pAddress is the ip of vm where you will be starting that node
5.	Open terminal and navigate to VMConnection folder
•	Execute below command:(update the jar name to the version you have downloaded)
 
6.	Copy “drivers” folder from node folder and paste in  VMConnection/PartyA
•	Ex: samples-java/Basic/cordapp-example/build/nodes/PartyA
•	Repeat step for every nodes to copy the drivers folder.

Copy files from local to VM
7.	scp -r ./PartyA cloud_user@34.220.75.169:/home/cloud_user/samples-java/Basic/cordapp-example
•	<mention ip where you want to start the node: ip in *_node_conf and in above path should be same>
•	Similarly Copy other nodes on the vm.



VM Steps
1.   Open cmd and connect to vm 
2.ssh cloud_user@35.87.136.13 and provide password 
3.Navigate to node folder where the folder from local in copied
•	Ex: ~/samples-java/Basic/cordapp-example/PartyA
•	Execute below command:
 

Deploy and Run Nodes In Corda

1.	Run below commands to run deploy corda nodes and execute transactions(Flow commands):
Go to the project folder where you have colined the project and run below command on terminal.
gradlew.bat clean deployNodes

 
              call build\nodes\runnodes.bat 

               
2.	Nodes are started using above command and popped up as below :
 

     3.Upate balances using below commands on Nodes opened in step 2 :
flow start PartyAUpdateBalanceFlow amount: 100000, issuer: "O=PartyA, L=London, C=GB", status: "UNCONSUMED"
flow start PartyAUpdateNostroBalanceFlow amount: 50,issuer: "O=PartyA, L=London, C=GB", status: "UNCONSUMED"

 

flow start PartyBUpdateBalanceFlow amount: 1000, issuer: "O=PartyB, L=New York, C=US", status: "UNCONSUMED"

flow start PartyBUpdateNostroBalanceFlow amount: 50,issuer: "O=PartyB, L=New York, C=US", status: "UNCONSUMED"

 
4.Run below queries to see balances in VALUT :E.g for 1 command run.
run vaultQuery contractStateType: net.corda.samples.example.states.PartyABalanceState
run vaultQuery contractStateType: net.corda.samples.example.states.PartyANostroState
run vaultQuery contractStateType: net.corda.samples.example.states.NostroState
run vaultQuery contractStateType: net.corda.samples.example.states.PartyBalanceStateB

 

Settlement Flow 
1.run below commands to start settlement flow : 
flow start SettleBalancesForB buyAmount: 100, lender: "O=PartyB, L=New York, C=US",sellAmount: 100, borrower: "O=PartyA, L=London, C=GB"
flow start SettleBalancesForA buyAmount: 100, lender: "O=PartyB, L=New York, C=US",sellAmount: 200, borrower: "O=PartyA, L=London, C=GB"

 

 

 
Netting Flow
•	Run below commands to start Netting Flow 
flow start  NettingFlowA  borrower: "O=PartyA, L=London, C=GB" , lender: "O=PartyB, L=New York, C=US"
flow start  NettingFlowB  borrower: "O=PartyA, L=London, C=GB" , lender: "O=PartyB, L=New York, C=US"

 

 

 



 

 
