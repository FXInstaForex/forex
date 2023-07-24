package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.samples.example.contracts.BalanceContractPartyB;
import net.corda.samples.example.contracts.IOUContract;
import net.corda.samples.example.flows.vault.QueryFlowB;
import net.corda.samples.example.states.IOUState;
import net.corda.samples.example.states.PartyBalanceStateB;

import java.util.Arrays;
import java.util.Collections;


@InitiatingFlow
@StartableByRPC
public class SettleFlow extends FlowLogic<Void> {
    private final double tranAmt ;
    private final Party lender;

private final Party borrower;



    public SettleFlow(double tranAmt, Party lender, Party borrower) {
        this.tranAmt = tranAmt;
        this.lender = lender;


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
        StateAndRef< PartyBalanceStateB > responseB= subFlow(new QueryFlowB(lender));
        final PartyBalanceStateB oldIOUState = responseB.getState().getData();
        double previousAmount = oldIOUState.getAmount();
        double  amount= previousAmount - (tranAmt);
        final PartyBalanceStateB newIOUState = new PartyBalanceStateB(amount,lender);
       // System.out.println("Inside settlementInitiatorB :: tranAmt"+tranAmt);
        //System.out.println("Inside settlementInitiatorB ::previousAmount)"+previousAmount);
        System.out.println("Inside settlementInitiatorB :: finalamout"+amount);

        final Command<BalanceContractPartyB.Commands.UpdateBalance> txCommand = new Command<>(
                new BalanceContractPartyB.Commands.UpdateBalance(),
                Arrays.asList(lender.getOwningKey(), borrower.getOwningKey()));

        final TransactionBuilder transactionBuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
                .addOutputState(new PartyBalanceStateB(amount, lender),BalanceContractPartyB.BalanceContractPartyBID)
                .addInputState(responseB).addCommand(txCommand);
               // .addCommand(new BalanceContractPartyB.Commands.UpdateBalance(), lender
        //.getOwningKey());
        System.out.println("Inside settlementInitiatorB :: before GENERATING_TRANSACTION");
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        //transactionBuilder.verify(getServiceHub());
        System.out.println("Inside settlementInitiatorB :: after verifying _TRANSACTION");
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(transactionBuilder);
        System.out.println("Inside settlementInitiatorB :: signedTransaction"+partSignedTx.getTx().getOutputs().get(0).getData().getParticipants().toString());

       // progressTracker.setCurrentStep(FINALIZING_TRANSACTION);

        System.out.println("Setlling nostroflow");
      //  subFlow(new SettleNostroFlow(tranAmt,lender
        // ));
        //subFlow(new FinalityFlow(partSignedTx, Collections.emptyList()));

        FlowSession borrowerSession = initiateFlow(borrower);
        final SignedTransaction fullySignedTx = subFlow(
                new CollectSignaturesFlow(partSignedTx, ImmutableSet.of(borrowerSession), CollectSignaturesFlow.Companion.tracker()));
        System.out.println("Inside settlementInitiatorB :: fullySignedTx"+fullySignedTx.getTx().getOutputs().get(0).getData().getParticipants().toString());
        // Stage 5.
        progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
        System.out.println("Notarise and record the transaction in both parties' vaults.");
         subFlow(new FinalityFlow(fullySignedTx, ImmutableSet.of(borrowerSession)));


        return null;
    }


}

