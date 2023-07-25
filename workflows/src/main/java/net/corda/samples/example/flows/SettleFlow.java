package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.samples.example.contracts.BalanceContractPartyB;
import net.corda.samples.example.flows.vault.QueryFlowB;
import net.corda.samples.example.states.PartyBalanceStateB;

import java.util.List;
import java.util.stream.Collectors;


@InitiatingFlow
@StartableByRPC
public class SettleFlow extends FlowLogic<SignedTransaction> {
    private final double tranAmt ;
    private final Party lender;

private final Party borrower;





    public SettleFlow(double tranAmt, Party lender, Party borrower, String status) {
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
    public SignedTransaction call() throws FlowException {
        StateAndRef< PartyBalanceStateB > responseB= subFlow(new QueryFlowB(lender));
        final PartyBalanceStateB oldIOUState = responseB.getState().getData();
        double previousAmount = oldIOUState.getAmount();
        double  amount= previousAmount - (tranAmt);
        final PartyBalanceStateB newPartyBalanceStateB = new PartyBalanceStateB(amount,lender,"CONSUMED");

       // System.out.println("Inside settlementInitiatorB :: tranAmt"+tranAmt);
        //System.out.println("Inside settlementInitiatorB ::previousAmount)"+previousAmount);
        System.out.println("Amount before settelment for Party B   ::"+oldIOUState.getAmount());
        System.out.println("Amount after final settelment for Party B ::"+amount);
///.addInputState(responseB)
TransactionBuilder builder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0));
                builder.addOutputState(newPartyBalanceStateB, BalanceContractPartyB.BalanceContractPartyBID)
                        .addInputState(responseB)
                .addCommand(new BalanceContractPartyB.Commands.UpdateBalance(), newPartyBalanceStateB.getParticipants().stream()
                        .map(AbstractParty::getOwningKey)
                        .collect(Collectors.toList()));

                       //  progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(builder);
        
        // Step 3: Collect signatures from other participants

        List<FlowSession> sessions = newPartyBalanceStateB.getParticipants().stream()
                .filter(party -> !party.equals(getOurIdentity()))
                .map(this::initiateFlow)
                .collect(Collectors.toList());
        SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, sessions));
        
        // Step 4: Finalize the transaction




return subFlow(new FinalityFlow(fullySignedTransaction, sessions));
    }


}

