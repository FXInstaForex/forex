package net.corda.samples.example.flows;

import net.corda.core.contracts.*;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;

import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.*;

import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.node.NodeInfo;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.samples.example.contracts.*;
import net.corda.samples.example.states.*;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;

import static java.util.Collections.emptyList;

@InitiatingFlow
@StartableByRPC
public class FXTrade19July extends FlowLogic<Void> {

    private final SignedTransaction signedTransaction;

    public FXTrade19July(SignedTransaction signedTransaction) {
        this.signedTransaction = signedTransaction;
    }

    @Override
    public Void call() throws FlowException {
        boolean flag = false;
        Party otherParty = null;
        List<TransactionState<ContractState>> outputs = signedTransaction.getTx().getOutputs();
        int amount = 0;
        String date = "";
        // Iterate over the outputs and extract the participants (nodes)
        List<AbstractParty> participants = null;
        for (TransactionState<ContractState> output : outputs) {
            participants = output.getData().getParticipants();
        IOUState iouState = (IOUState) output.getData();
            amount = iouState.getValue();
            date = iouState.getSettlementDate();

        }
        for (AbstractParty node : participants) {
            if (!(participants.equals(getOurIdentity()))) {
                otherParty = (Party) node;
            }
        }

        flag = validateNodesAuthenticity(participants);
        System.out.println("flag after validateNodesAuthenticity called" + flag);

        if (flag) {
            //sign transaction
            signTransaction(otherParty, amount, date);
            updateBalances(amount,date,otherParty);



        } else {
            throw new FlowException("Node authenticity failed..transaction failed");
        }





        return null;
    }

    public void updateBalances(double amount,String date, Party otherParty){
        double balanceAmtA = getAmountfromVaultforPartyA();
        double balanceAmtB = getAmountfromVaultforPartyB();
        System.out.println("=====================================================");
        System.out.println("balanceAmtA==============="+balanceAmtA+"================");
        System.out.println("balanceAmtB==============="+balanceAmtB+"================");
        System.out.println("=====================================================");
        if (balanceAmtB <= amount) {
            System.out.println("Balance amount is less than the request amount ,Hence declining the transaction");
        } else {
            System.out.println("Balance amount is sufficient  for requested amount to lend ,Hence proceeding the transaction");
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-YYYY");
            String strDate = format.format(new Date());
            if (date.equalsIgnoreCase(strDate)) {
                //settle realtime
                updateLedgerforPartyA((getAmountfromVaultforPartyA() - amount), getOurIdentity());
                updateNostroLedgerforPartyA((getAmountfromVaultforPartyB() + amount), otherParty);
        updateLedgerforPartyB((getAmountfromVaultforPartyB() + amount), otherParty);
                updateLedgerforPartyB((getAmountfromVaultforPartyB() + amount), otherParty);
            } else {
                //netting flow
                System.out.println("Netting to be done when settlement date is same as current date ");
            }


        }
    }

    public void signTransaction(Party otherParty, int amount, String settlementDate) throws TransactionVerificationException, AttachmentResolutionException, TransactionResolutionException {
        final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));

        System.out.println("Other party........" + otherParty);
        IOUState iouState = new IOUState(amount, getOurIdentity(), otherParty, new UniqueIdentifier(), settlementDate);
        final Command<IOUContract.Commands.Create> txCommand = new Command<>(
                new IOUContract.Commands.Create(),
                Arrays.asList(iouState.getLender().getOwningKey(), iouState.getBorrower().getOwningKey()));
        final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(iouState, IOUContract.ID)
                .addCommand(txCommand);

        // Stage 2.
        // Verify that the transaction is valid.
        txBuilder.verify(getServiceHub());

        // Stage 3.
        // Sign the transaction.
        final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

        System.out.println("partSignedTx..........." + partSignedTx);
    }

    public double getAmountfromVaultforPartyA() {
        double balanceAmt = 0;
        System.out.println("------getAmountfromVaultforPartyA--------");


        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        //owner will be party initiating tansaction owner  is
        Vault.Page<PartyBalanceStateA> results = getServiceHub().getVaultService().queryBy(PartyBalanceStateA.class);
        List<StateAndRef<PartyBalanceStateA>> states = results.getStates();
        for (StateAndRef<PartyBalanceStateA> state : states) {
            PartyBalanceStateA iouState = state.getState().getData();
            System.out.println("Balance Amount ---PartyA" + iouState.getAmount());
            balanceAmt = iouState.getAmount();
 }

        return balanceAmt;

    }
    public double getAmountfromVaultforPartyB() {
        double balanceAmt = 0;
        System.out.println("------getAmountfromVaultforPartyB--------");


        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        //owner will be party initiating tansaction owner  is
        Vault.Page<PartyBalanceStateB> results = getServiceHub().getVaultService().queryBy(PartyBalanceStateB.class);
        List<StateAndRef<PartyBalanceStateB>> states = results.getStates();
        for (StateAndRef<PartyBalanceStateB> state : states) {
            PartyBalanceStateB iouState = state.getState().getData();
            System.out.println("Balance Amount---PartyB" + iouState.getAmount());
            balanceAmt = iouState.getAmount();
        }

        return balanceAmt;

    }

    public boolean validateNodesAuthenticity(List<AbstractParty> nodes) throws FlowException {
        boolean validateNodeFlag = false;
        for (AbstractParty node : nodes) {
            if (isWellKnownNodeIdentity(node.nameOrNull())) {
                System.out.println("The node's identity is known and trusted" + node.nameOrNull());
                verifyNodeIntegrity(node.nameOrNull(), node.getOwningKey());
                validateNodeFlag = true;
            } else {
                System.out.println("The node's identity is not known or not trusted");
                throw new FlowException("The node's identity is not known or not trusted");
            }
        }


        return validateNodeFlag;
    }

    private boolean isWellKnownNodeIdentity(CordaX500Name identity) {
        for (NodeInfo nodeInfo : getServiceHub().getNetworkMapCache().getAllNodes()) {
            if (nodeInfo.getLegalIdentities().get(0).getName().equals(identity)) {
                return true;
            }
        }
        return false;
    }

    public boolean verifyNodeIntegrity(CordaX500Name nodeX500Name, PublicKey expectedPublicKey) throws FlowException {
        boolean verifiedFlag = false;
        // Step 1: Retrieve the Party and Certificate for the node
        Party nodeParty =  getServiceHub().getIdentityService().wellKnownPartyFromX500Name(nodeX500Name);
        if (nodeParty == null) {
            System.out.println("Node not found: " + nodeX500Name);
            verifiedFlag = false;
        }

        PartyAndCertificate nodeCertificate =  getServiceHub().getIdentityService().certificateFromKey(nodeParty.getOwningKey());
        if (nodeCertificate == null) {
            System.out.println("Certificate not found for node: " + nodeX500Name);
            verifiedFlag = false;
        } else {
            verifiedFlag = true;
            System.out.println("Certificate  found for node: " + nodeX500Name + "......" + nodeCertificate.toString());
        }

        // Step 3: Verify the node's public key
        PublicKey actualPublicKey = nodeCertificate.getOwningKey();
        if (!actualPublicKey.equals(expectedPublicKey)) {
            System.out.println("Public key mismatch for node: " + nodeX500Name);

            verifiedFlag = false;

        } else {
            verifiedFlag = true;
            System.out.println("Public key matched for node: " + nodeX500Name);
        }
        // All checks passed, node integrity verified
        System.out.println("verifiedFlag" + verifiedFlag);
        if (verifiedFlag) {
            verifiedFlag = verifyKYCtransaction(nodeX500Name);
        } else {
            verifiedFlag = false;
            throw new FlowException("Node integrity verification failed " + nodeX500Name);
        }
        System.out.println("verifiedFlag after KYC" + verifiedFlag);
        return verifiedFlag;
    }

    public boolean verifyKYCtransaction(CordaX500Name nodeX500Name) throws FlowException {
        System.out.println("I am inside verifyKYCtransaction " + nodeX500Name.toString());
        boolean verifiedKYC = false;
    String[] tokens = (nodeX500Name.toString()).split(",");
        String[] conutrytoken = tokens[2].split("=");
        List<StateAndRef<KYCState>> states= getBlacklistedCountries();
       System.out.println("countries"+states.toString());
        for (StateAndRef<KYCState> state : states) {
            KYCState iouState = state.getState().getData();

            if((iouState.getCountry().getName().toString()).equalsIgnoreCase(conutrytoken[1])){
                System.out.println("ngCountry is blacklisted for trade hence rejecti for Trade" + conutrytoken[1] );
                throw new FlowException("BlackListed country" + conutrytoken[1]);
            }
            else {
                System.out.println("Country is valid for trade " + conutrytoken[1]);
                verifiedKYC = true;


            }

        }



        return verifiedKYC;
    }

    public void updateLedgerforPartyA(double amount, Party party) {
        System.out.println("Inside updateLedgerforPartyA");
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
//        final QueryCriteria.LinearStateQueryCriteria linearStateQueryCriteria =
//                new QueryCriteria.LinearStateQueryCriteria()
//                        .withUuid(Collections.singletonList(linearId.getId()));
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        final StateAndRef<PartyBalanceStateA> iouStateAndRef = getServiceHub().getVaultService()
                .queryBy(PartyBalanceStateA.class, queryCriteria)
                .getStates().get(0);

        final PartyBalanceStateA oldIOUState = iouStateAndRef.getState().getData();
        final PartyBalanceStateA newIOUState = new PartyBalanceStateA(amount, party);
       

        final TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                .addInputState(iouStateAndRef)
                .addOutputState(newIOUState)
                .addCommand(new BalanceContractPartyA.Commands.UpdateBalance(), getOurIdentity().getOwningKey());

        // progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        // transactionBuilder.verify(getServiceHub());

        //progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        //progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
        try {
            subFlow(new FinalityFlow(signedTransaction, emptyList()));
        } catch (FlowException e) {
            throw new RuntimeException(e);
        }


    }


    public void updateNostroLedgerforPartyA(double amount, Party party) {
        System.out.println("Inside updateNostroLedgerforPartyA");
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
//        final QueryCriteria.LinearStateQueryCriteria linearStateQueryCriteria =
//                new QueryCriteria.LinearStateQueryCriteria()
//                        .withUuid(Collections.singletonList(linearId.getId()));
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        final StateAndRef<PartyANostroState> iouStateAndRef = getServiceHub().getVaultService()
                .queryBy(PartyANostroState.class, queryCriteria)
                .getStates().get(0);

        final PartyANostroState oldIOUState = iouStateAndRef.getState().getData();
        final PartyANostroState newIOUState = new PartyANostroState(amount);

        final TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                .addInputState(iouStateAndRef)
                .addOutputState(newIOUState)
                .addCommand(new PartyANostroContract.Commands.UpdateBalance(), getOurIdentity().getOwningKey());

        // progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        // transactionBuilder.verify(getServiceHub());

        //progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        //progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
        try {
            subFlow(new FinalityFlow(signedTransaction, emptyList()));
        } catch (FlowException e) {
            throw new RuntimeException(e);
        }


    }


    public void updateNostroLedgerforPartyB(double amount, Party party) {
        System.out.println("Inside updateNostroLedgerforPartyB");
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
//        final QueryCriteria.LinearStateQueryCriteria linearStateQueryCriteria =
//                new QueryCriteria.LinearStateQueryCriteria()
//                        .withUuid(Collections.singletonList(linearId.getId()));
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        final StateAndRef<PartyBNostroState> iouStateAndRef = getServiceHub().getVaultService()
                .queryBy(PartyBNostroState.class, queryCriteria)
                .getStates().get(0);

        final PartyBNostroState oldIOUState = iouStateAndRef.getState().getData();
        final PartyBNostroState newIOUState = new PartyBNostroState(amount);

        final TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                .addInputState(iouStateAndRef)
                .addOutputState(newIOUState)
                .addCommand(new PartyBNostroContract.Commands.UpdateBalance(), getOurIdentity().getOwningKey());

        // progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        // transactionBuilder.verify(getServiceHub());

        //progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        //progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
        try {
            subFlow(new FinalityFlow(signedTransaction, emptyList()));
        } catch (FlowException e) {
            throw new RuntimeException(e);
        }


    }
    public void updateLedgerforPartyB(double amount, Party party) {
        System.out.println("Inside updateLedgerforPartyB");
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
//        final QueryCriteria.LinearStateQueryCriteria linearStateQueryCriteria =
//                new QueryCriteria.LinearStateQueryCriteria()
//                        .withUuid(Collections.singletonList(linearId.getId()));
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        final StateAndRef<PartyBalanceStateB> iouStateAndRef = getServiceHub().getVaultService()
                .queryBy(PartyBalanceStateB.class, queryCriteria)
                .getStates().get(0);

        final PartyBalanceStateB oldIOUState = iouStateAndRef.getState().getData();
        final PartyBalanceStateB newIOUState = new PartyBalanceStateB(amount, party);

        final TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                .addInputState(iouStateAndRef)
                .addOutputState(newIOUState)
                .addCommand(new BalanceContractPartyB.Commands.UpdateBalance(), party.getOwningKey());

        // progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        // transactionBuilder.verify(getServiceHub());

        //progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        //progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
        try {
            subFlow(new FinalityFlow(signedTransaction, emptyList()));
        } catch (FlowException e) {
            throw new RuntimeException(e);
        }


    }

    public List<StateAndRef<KYCState>> getBlacklistedCountries() {
        System.out.println("Inside getBlacklistedCountries");
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        Vault.Page<KYCState> results = getServiceHub().getVaultService().queryBy(KYCState.class);
        List<StateAndRef<KYCState>> states = results.getStates();
        return states;
    }

}
