package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.utilities.ProgressTracker;


@InitiatingFlow
@StartableByRPC
public class SettleBalancesForB extends FlowLogic<Void> {
    private final double buyAmount ;
    private final Party lender;
    private final double sellAmount ;

private final Party borrower;



    public SettleBalancesForB(double buyAmount, Party lender, double sellAmount, Party borrower) {
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

        System.out.println("Updating balances for Party B starts");
        subFlow(new SettleFlow(buyAmount,lender,borrower, "CONSUMED"));
        System.out.println("Updating balances for Party B ends");
        System.out.println("----------------------------------------------");
        System.out.println("Updating balances for nostro B starts");
        subFlow(new SettleNostroFlow(sellAmount,lender,borrower));
        System.out.println("Updating balances for nostro B ends");
        return null;
    }


}

