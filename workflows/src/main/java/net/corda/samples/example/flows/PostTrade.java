package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.*;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.samples.example.contracts.BalanceContractPartyA;
import net.corda.samples.example.contracts.IOUContract;
import net.corda.samples.example.contracts.PartyANostroContract;
import net.corda.samples.example.flows.vault.NostroFlow;
import net.corda.samples.example.flows.vault.QueryFlowB;
import net.corda.samples.example.states.*;

import java.math.BigDecimal;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.Collections.emptyList;

@InitiatingFlow
@StartableByRPC
public class PostTrade extends FlowLogic<Void> {

    private final SignedTransaction signedTransaction;
    private static final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction");
    private static final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Verifying transaction");
    private static final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction");
    private static final ProgressTracker.Step FINALIZING_TRANSACTION = new ProgressTracker.Step("Finalizing transaction");

    private final ProgressTracker progressTracker = new ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            FINALIZING_TRANSACTION
    );

    public PostTrade(SignedTransaction signedTransaction) {
        this.signedTransaction = signedTransaction;
    }

    @Override
    @Suspendable
    public Void call() throws FlowException {
        boolean flag = false;
        Party otherParty = null;
        Date settlementDate;
        List<TransactionState<ContractState>> outputs = signedTransaction.getTx().getOutputs();
        BigDecimal buyAmount = new BigDecimal(0);
        BigDecimal sellAmount=new BigDecimal(0);
        String date = "";
        // Iterate over the outputs and extract the participants (nodes)
        List<AbstractParty> participants = null;
        for (TransactionState<ContractState> output : outputs) {
            participants = output.getData().getParticipants();
            TradeState tradeState= (TradeState) output.getData().getParticipants();
            //IOUState iouState = (IOUState) output.getData();
            buyAmount = tradeState.getBuyAmount();
            sellAmount=tradeState.getSellAmount();
settlementDate=tradeState.getSettlmentDate();

        }
//        for (AbstractParty node : participants) {
//            if (!(participants.equals(getOurIdentity()))) {
//                otherParty = (Party) node;
//            }
//        }

        flag = validateNodesAuthenticity(participants);
        System.out.println("flag after validateNodesAuthenticity called" + flag);

        //if (flag) {
        //sign transaction
        signTransaction(signedTransaction);
        updateBalances(buyAmount.doubleValue(),sellAmount.doubleValue(),date,otherParty);



        // } else {
        //  throw new FlowException("Node authenticity failed..transaction failed");
        //}





        return null;
    }
@Suspendable
    public void updateBalances(double buyAmount,double sellAmount,String date, Party otherParty) throws FlowException {

        double balanceAmtA = getAmountfromVaultforPartyA();


    double balanceAmtNostroA = getAmountfromVaultforPartyANostro();


    System.out.println("=====================================================");
    System.out.println("balanceAmtA==============="+balanceAmtA+"================");

    System.out.println("=====================================================");
    System.out.println("balanceAmtNostroA==============="+balanceAmtNostroA+"================");
    StateAndRef < PartyBalanceStateB > partybBal= subFlow(new QueryFlowB(otherParty));
        double balanceAmtB = partybBal.getState().getData().getAmount();



    System.out.println("balanceAmtB==============="+balanceAmtB+"================");


    StateAndRef<NostroState> partybBalNostro= subFlow(new NostroFlow(otherParty));
    System.out.println("partybBalNostro==============="+partybBalNostro+"================");
    double balanceAmtNostroB = partybBalNostro.getState().getData().getAmount();

    System.out.println("balanceAmtNostroB==============="+balanceAmtNostroB+"================");

    System.out.println("=====================================================");
        if (balanceAmtB <= sellAmount) {
            System.out.println("Balance amount is less than the request amount ,Hence declining the transaction");
        } else {
            System.out.println("Balance amount is sufficient  for requested amount to lend ,Hence proceeding the transaction");
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-YYYY");
            String strDate = format.format(new Date());
            if (date.equalsIgnoreCase(strDate)) {
                System.out.println("==========Trade date is equal to current Date =============================");
                System.out.println("==========settling party a starts =============================");
                updateNostroLedgerforPartyA(buyAmount,getOurIdentity());
                updateLedgerforPartyA(sellAmount,getOurIdentity());
                //updateA(amount,getOurIdentity());
                System.out.println("==========settling party a ends =============================");
                System.out.println("==========settling party b start =============================");
               subFlow(new SettleFlow(buyAmount,otherParty,getOurIdentity())) ;
                System.out.println("==========settling party b end =============================");
                subFlow(new SettleNostroFlow(sellAmount,otherParty));
                System.out.println("==========settling party b SettleFlow  ends =============================");
            } else {
                //netting flow
                System.out.println("Netting to be done when settlement date is same as current date ");
            }


        }
    }

    public void signTransaction(SignedTransaction signedTransaction) throws TransactionVerificationException, AttachmentResolutionException, TransactionResolutionException {

        List<TransactionState<ContractState>> outputs = signedTransaction.getTx().getOutputs();
        BigDecimal buyAmount = new BigDecimal(0);
        BigDecimal sellAmount=new BigDecimal(0);
        Date settlementDate = null;
        Party buyer=null;
        Party seller=null;
        String buyCurrency = "";
         String sellCurrency="";
        String tradeId="";
        BigDecimal spotRate= new BigDecimal(0);
UniqueIdentifier linearId=null;
        // Iterate over the outputs and extract the participants (nodes)
        List<AbstractParty> participants = null;
        for (TransactionState<ContractState> output : outputs) {
            participants = output.getData().getParticipants();
            TradeState tradeState= (TradeState) output.getData().getParticipants();
            //IOUState iouState = (IOUState) output.getData();
            buyAmount = tradeState.getBuyAmount();
            sellAmount=tradeState.getSellAmount();
            settlementDate=tradeState.getSettlmentDate();
buyer=tradeState.getBuyer();
buyCurrency=tradeState.getBuyCurrency();
sellCurrency=tradeState.getSellCurrency();
tradeId=tradeState.getTradeId();
spotRate=tradeState.getSpotRate();
linearId=tradeState.getLinearId();

        }
        final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
        System.out.println("Other party........" + seller);
         TradeState tradeState= new TradeState(buyer,seller,buyCurrency,buyAmount,sellCurrency,sellAmount,tradeId,settlementDate,spotRate,linearId);


            final Command<IOUContract.Commands.Create> txCommand = new Command<>(
                new IOUContract.Commands.Create(),
                Arrays.asList(tradeState.getSeller().getOwningKey(), tradeState.getBuyer().getOwningKey()));
        final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(tradeState, IOUContract.ID)
                .addCommand(txCommand);

        // Stage 2.
        // Verify that the transaction is valid.
        txBuilder.verify(getServiceHub());

        // Stage 3.
        // Sign the transaction.
        final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

        System.out.println("partSignedTx..........." + partSignedTx);
    }




    public boolean validateNodesAuthenticity(List<AbstractParty> nodes) throws FlowException {
        boolean validateNodeFlag = false;
        for (AbstractParty node : nodes) {
            if (isWellKnownNodeIdentity(node.nameOrNull())) {
                System.out.println("The node's identity is known and trusted" + node.nameOrNull());

                validateNodeFlag = true;
            } else {
                System.out.println("The node's identity is not known or not trusted");
                throw new FlowException("The node's identity is not known or not trusted");
            }

            if(validateNodeFlag){
                verifyNodeIntegrity(node.nameOrNull(), node.getOwningKey());
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
System.out.println("`````````````````"+iouState.getCountry().getName().toString());
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
@Suspendable
    public void updateLedgerforPartyA(double euramount, Party party) throws TransactionVerificationException, AttachmentResolutionException, TransactionResolutionException {

        double finalamout=0;

        System.out.println("Inside updateLedgerforPartyA");
       // final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        final StateAndRef<PartyABalanceState> iouStateAndRef = getServiceHub().getVaultService()
                .queryBy(PartyABalanceState.class, queryCriteria)
                .getStates().get(0);
    System.out.println("Inside updateLedgerforPartyA");
        final PartyABalanceState oldIOUState = iouStateAndRef.getState().getData();
        finalamout = (oldIOUState.getAmount()) - euramount;
    System.out.println("Inside updateLedgerforPartyA :: amount"+euramount);
        final PartyABalanceState newIOUState = new PartyABalanceState(finalamout, party);
    System.out.println("Inside updateLedgerforPartyA :: oldIOUState.getAmount())"+oldIOUState.getAmount());
    System.out.println("Inside updateLedgerforPartyA :: finalamout"+finalamout);;
    System.out.println("Inside updateLedgerforPartyA :: iouStateAndRef"+iouStateAndRef.toString());


        //progressTracker.setCurrentStep(FINALIZING_TRANSACTION);

        /////////////////////////////////////////////////////
    final TransactionBuilder txnBuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
            .addInputState(iouStateAndRef)
            .addOutputState(newIOUState)
            .addCommand(new BalanceContractPartyA.Commands.UpdateBalance(), party.getOwningKey());
    progressTracker.setCurrentStep(GENERATING_TRANSACTION);
   txnBuilder.verify(getServiceHub());
    progressTracker.setCurrentStep(SIGNING_TRANSACTION);
    final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txnBuilder);
    try {
        progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
        subFlow(new FinalityFlow(signedTx, emptyList()));
    } catch (FlowException e) {
        throw new RuntimeException(e);
    }




    }

   
@Suspendable
// STOPSHIP: 22-07-2023
    public void updateNostroLedgerforPartyA(double usdamount, Party party) throws TransactionVerificationException, AttachmentResolutionException, TransactionResolutionException {
        double finalamout=0;
        System.out.println("Inside updateNostroLedgerforPartyA");

        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        final StateAndRef<PartyANostroState> iouStateAndRef = getServiceHub().getVaultService()
                .queryBy(PartyANostroState.class, queryCriteria)
                .getStates().get(0);

        final PartyANostroState oldIOUState = iouStateAndRef.getState().getData();
        finalamout = (oldIOUState.getAmount()) + (usdamount);
        System.out.println("Inside updateNostroLedgerforPartyA :: amount"+usdamount);
        System.out.println("Inside updateNostroLedgerforPartyA :: oldIOUState.getAmount())"+oldIOUState.getAmount());
        System.out.println("Inside updateNostroLedgerforPartyA :: finalamout"+finalamout);
        final PartyANostroState newIOUState = new PartyANostroState(finalamout, party);

        System.out.println("Inside updateNostroLedgerforPartyA :: iouStateAndRef"+iouStateAndRef.toString());

    final TransactionBuilder txnBuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
            .addInputState(iouStateAndRef)
            .addOutputState(newIOUState)
            .addCommand(new PartyANostroContract.Commands.UpdateBalance(), party.getOwningKey());
System.out.println("before verifying");
    txnBuilder.verify(getServiceHub());
    System.out.println("after verifying");
    final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txnBuilder);
    try {
System.out.println("1111111111111111111111111111111............try block for nostro A");
        subFlow(new FinalityFlow(signedTx, emptyList()));
    } catch (FlowException e) {
        System.out.println("00000000000000000000111111111111111............catch block for nostro A"+e.getMessage());
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


    public double getAmountfromVaultforPartyA() {
        double balanceAmt = 0;
        System.out.println("------getAmountfromVaultforPartyA--------");


        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        //owner will be party initiating tansaction owner  is
        Vault.Page<PartyABalanceState> results = getServiceHub().getVaultService().queryBy(PartyABalanceState.class);
        List<StateAndRef<PartyABalanceState>> states = results.getStates();
        for (StateAndRef<PartyABalanceState> state : states) {
            PartyABalanceState iouState = state.getState().getData();
            System.out.println("Balance Amount ---PartyA" + iouState.getAmount());
            balanceAmt = iouState.getAmount();
        }

        return balanceAmt;

    }
    public double getAmountfromVaultforPartyANostro() {
        double balanceAmt = 0;
        System.out.println("------getAmountfromVaultforPartyANostro--------");


        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        //owner will be party initiating tansaction owner  is
        Vault.Page<PartyANostroState> results = getServiceHub().getVaultService().queryBy(PartyANostroState.class);
        List<StateAndRef<PartyANostroState>> states = results.getStates();
        for (StateAndRef<PartyANostroState> state : states) {
            PartyANostroState iouState = state.getState().getData();
            System.out.println("Balance Amount---PartyANostroState" + iouState.getAmount());
            balanceAmt = iouState.getAmount();
        }

        return balanceAmt;

    }

}
