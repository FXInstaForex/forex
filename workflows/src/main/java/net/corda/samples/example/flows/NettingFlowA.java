package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;


@InitiatingFlow
@StartableByRPC
public class NettingFlowA extends FlowLogic<SignedTransaction> {

    private final Party borrower;

    private final Party lender;




    public NettingFlowA(Party borrower, Party lender) {

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
    public SignedTransaction call() throws FlowException {
        System.out.println("Updating netting balances for Party A starts");
        subFlow(new SettleFlowA(borrower));
        System.out.println("Updating  netting balances for Party A ends");
        System.out.println("----------------------------------------------");
        System.out.println("Updating netting  balances for nostro A starts");
        subFlow(new SettleFlowNostroA(borrower));
        System.out.println("Updating netting  balances for nostro A ends");
    return null;

    }


}

