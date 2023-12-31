package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.samples.example.contracts.BalanceContractPartyA;
import net.corda.samples.example.states.B;

import static java.util.Collections.emptyList;

@InitiatingFlow
@StartableByRPC
public class UpdateFlowB extends FlowLogic<Void> {
    private final double amount ;
    private final Party lender;

    private final Party borrower;


    public UpdateFlowB(double amount, Party lender, Party borrower) {
        this.amount = amount;

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
        final TransactionBuilder transactionBuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
                .addOutputState(new B(amount, lender,borrower))
                .addCommand(new BalanceContractPartyA.Commands.UpdateBalance(), lender.getOwningKey(),borrower.getOwningKey());

        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        transactionBuilder.verify(getServiceHub());

        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
        subFlow(new FinalityFlow(signedTransaction, emptyList()));

        // Optionally, you can also store the state in your own vault
       // getServiceHub().getVaultService().trackBy(PartyABalanceState.class).getUpdates().subscribe(update ->
             //   {
                    //final PartyABalanceState balanceState = update.getProduced().get(0).getState().getData();
                    // Do something with the updated balance state
             //   }
       // );

        return null;
    }


}

