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
import net.corda.samples.example.contracts.NostroContract;
import net.corda.samples.example.contracts.PartyBNostroContract;
import net.corda.samples.example.flows.vault.NostroFlow;
import net.corda.samples.example.flows.vault.QueryFlowB;
import net.corda.samples.example.states.NostroState;
import net.corda.samples.example.states.PartyBNostroState;
import net.corda.samples.example.states.PartyBalanceStateB;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@InitiatingFlow
@StartableByRPC
public class SettleNostroFlow extends FlowLogic<SignedTransaction> {
    private final double tranAmt ;
    private final Party lender;

    private final Party borrower;



    public SettleNostroFlow(double tranAmt, Party lender, Party borrower) {
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
        StateAndRef<NostroState> responseB= subFlow(new NostroFlow(lender));
        final NostroState oldIOUState =  responseB.getState().getData();

        double previousAmount = oldIOUState.getAmount();
        double  amount= previousAmount + (tranAmt);
        final NostroState newIOUState = new NostroState(amount,getOurIdentity(),"CONSUMED");
        System.out.println("Amount before settelment for Party B Nostro  ::"+oldIOUState.getAmount());
        System.out.println("Amount after final settelment for Party B Nostro::"+amount);
//
        TransactionBuilder builder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
                .addOutputState(newIOUState, NostroContract.NostroContractID)
                .addInputState(responseB)
                .addCommand(new NostroContract.Commands.UpdateBalance(), newIOUState.getParticipants().stream()
                        .map(AbstractParty::getOwningKey)
                        .collect(Collectors.toList()));

     //   progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(builder);

        // Step 3: Collect signatures from other participants

        List<FlowSession> sessions = newIOUState.getParticipants().stream()
                .filter(party -> !party.equals(getOurIdentity()))
                .map(this::initiateFlow)
                .collect(Collectors.toList());
        SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, sessions));

        // Step 4: Finalize the transaction
     //   progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
        return subFlow(new FinalityFlow(fullySignedTransaction, sessions));


    }


}

