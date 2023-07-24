# CorDapp Example

This repo is the implementation of the tutorial CorDapp. 

You can find the tutorial for this example [here](https://docs.r3.com/en/platform/corda/4.10/community/tutorial-cordapp.html).


Pre-requisite to Run FX Flow 

There are four pieces of required software for CorDapp development:
•	Java 8 JDK
•	IntelliJ IDEA
•	Git
•	Gradle
Steps to Run FX Flow

1.	Clone the repository: 
•	Git clone the Code from https://github.com/FXInstaForex/forex.git
•	Command:
Git clone https://github.com/FXInstaForex/forex.git
•	Branch Name: PreAndPostTrade

2.	Run below commands to run deploy corda nodes and execute transactions(Flow commands):
Go to the project folder where you have colined the project and run below command on terminal.
gradlew.bat clean deployNodes

 
              call build\nodes\runnodes.bat 

               

Nodes are started using above command and popped up as below :
 

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

 

 

 

