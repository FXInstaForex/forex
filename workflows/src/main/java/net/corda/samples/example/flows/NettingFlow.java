package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.samples.example.contracts.BalanceContractPartyB;
import net.corda.samples.example.flows.vault.QueryFlowA;
import net.corda.samples.example.flows.vault.QueryFlowB;
import net.corda.samples.example.states.PartyABalanceState;
import net.corda.samples.example.states.PartyBalanceStateB;

import java.util.Collections;
import java.util.List;


@InitiatingFlow
@StartableByRPC
public class NettingFlow extends FlowLogic<Void> {
    private final double tranAmt ;
    private final Party borrower;

    private final Party lender;




    public NettingFlow(double tranAmt, Party borrower, Party lender) {
        this.tranAmt = tranAmt;
        this.borrower = borrower;


        this.lender = lender;
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
        double Valutamountresult=0;
        StateAndRef< PartyBalanceStateB > responseB= subFlow(new QueryFlowB(lender));




//
//        final PartyBalanceStateB oldIOUState = responseB.getState().getData();
//        double previousAmount = oldIOUState.getAmount();
//        double  amount= previousAmount - (tranAmt);
//        final PartyBalanceStateB newIOUState = new PartyBalanceStateB(amount,getOurIdentity());
//        System.out.println("Inside settlementInitiatorB :: tranAmt"+tranAmt);
//        System.out.println("Inside settlementInitiatorB ::previousAmount)"+previousAmount);
//        System.out.println("Inside settlementInitiatorB :: finalamout"+amount);
//
//        final TransactionBuilder transactionBuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
//                .addOutputState(new PartyBalanceStateB(amount, lender),BalanceContractPartyB.BalanceContractPartyBID)
//                .addInputState(responseB)
//                .addCommand(new BalanceContractPartyB.Commands.UpdateBalance(), lender.getOwningKey());
//        System.out.println("Inside settlementInitiatorB :: before GENERATING_TRANSACTION");
//        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
//        //transactionBuilder.verify(getServiceHub());
//        System.out.println("Inside settlementInitiatorB :: after verifying _TRANSACTION");
//        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
//        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);
//        System.out.println("Inside settlementInitiatorB :: signedTransaction"+signedTransaction);
//
//        progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
//        subFlow(new FinalityFlow(signedTransaction, Collections.emptyList()));

        // Optionally, you can also store the state in your own vault
       //getServiceHub().getVaultService().trackBy(PartyABalanceState.class).getUpdates().subscribe();//update ->
//               {
//                    final PartyABalanceState balanceState = update.getProduced().get(0).getState().getData();
//                    // Do something with the updated balance state
//                }
//        );

        return null;
    }


}

