package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.AttachmentResolutionException;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.TransactionResolutionException;
import net.corda.core.contracts.TransactionVerificationException;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.samples.example.contracts.BalanceContractPartyA;
import net.corda.samples.example.contracts.PartyANostroContract;
import net.corda.samples.example.states.PartyABalanceState;
import net.corda.samples.example.states.PartyANostroState;

import static java.util.Collections.emptyList;


@InitiatingFlow
@StartableByRPC
public class SettleBalancesForA extends FlowLogic<Void> {
    private final double buyAmount ;
    private final Party lender;
    private final double sellAmount ;

private final Party borrower;



    public SettleBalancesForA(double buyAmount, Party lender, double sellAmount, Party borrower) {
        this.buyAmount = buyAmount;
        this.lender = lender;
        this.sellAmount = sellAmount;


        this.borrower = borrower;
    }

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

    @Override
    @Suspendable
    public Void call() throws FlowException {

        System.out.println("Settling Balances for Party A starts");
        updateLedgerforPartyA(sellAmount,borrower);
        System.out.println("Settling Balances for Party A ends");
        System.out.println("----------------------------------------------");
        System.out.println("Settling Balances for Party A Nostro  starts");
        updateNostroLedgerforPartyA(buyAmount,borrower);
        System.out.println("Settling Balances for Party A Nostro ends");
        return null;
    }



    @Suspendable
    public void updateLedgerforPartyA(double amount, Party party) throws TransactionVerificationException, AttachmentResolutionException, TransactionResolutionException {

        double finalamout=0;

        //System.out.println("Inside updateLedgerforPartyA");
        // final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        final StateAndRef<PartyABalanceState> iouStateAndRef = getServiceHub().getVaultService()
                .queryBy(PartyABalanceState.class, queryCriteria)
                .getStates().get(0);
        final PartyABalanceState oldIOUState = iouStateAndRef.getState().getData();
        finalamout = (oldIOUState.getAmount()) - (amount);
        final PartyABalanceState newIOUState = new PartyABalanceState(finalamout, party, Vault.StateStatus.CONSUMED);
        System.out.println("Amount before settelment for Party A  ::"+oldIOUState.getAmount());
        System.out.println("Amount after final settelment for Party A ::"+finalamout);


        //progressTracker.setCurrentStep(FINALIZING_TRANSACTION);

        /////////////////////////////////////////////////////
        final TransactionBuilder txnBuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
                .addInputState(iouStateAndRef)
                .addOutputState(newIOUState)
                .addCommand(new BalanceContractPartyA.Commands.UpdateBalance(), party.getOwningKey());
       // progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        txnBuilder.verify(getServiceHub());
        //progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txnBuilder);
        try {
        //    progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
            subFlow(new FinalityFlow(signedTx, emptyList()));
        } catch (FlowException e) {
            throw new RuntimeException(e);
        }




    }


    @Suspendable
// STOPSHIP: 22-07-2023
    public void updateNostroLedgerforPartyA(double amount, Party party) throws TransactionVerificationException, AttachmentResolutionException, TransactionResolutionException {
        double finalamout=0;
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        final StateAndRef<PartyANostroState> iouStateAndRef = getServiceHub().getVaultService()
                .queryBy(PartyANostroState.class, queryCriteria)
                .getStates().get(0);

        final PartyANostroState oldIOUState = iouStateAndRef.getState().getData();
        finalamout = (oldIOUState.getAmount()) + (amount);
        System.out.println("Amount before settelment for Party A Nostro  ::"+oldIOUState.getAmount());
        System.out.println("Amount after final settelment for Party A Nostro::"+finalamout);
        final PartyANostroState newIOUState = new PartyANostroState(finalamout, party, "CONSUMED");
final TransactionBuilder txnBuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
                .addInputState(iouStateAndRef)
                .addOutputState(newIOUState)
                .addCommand(new PartyANostroContract.Commands.UpdateBalance(), party.getOwningKey());

        txnBuilder.verify(getServiceHub());

        final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txnBuilder);
        try {
           subFlow(new FinalityFlow(signedTx, emptyList()));
        } catch (FlowException e) {
            throw new RuntimeException(e);
        }


    }
}

