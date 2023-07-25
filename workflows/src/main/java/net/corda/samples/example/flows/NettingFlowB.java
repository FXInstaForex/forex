package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;
import net.corda.samples.example.states.PartyANostroState;

import java.util.List;


@InitiatingFlow
@StartableByRPC
public class NettingFlowB extends FlowLogic<SignedTransaction> {

    private final Party borrower;

    private final Party lender;




    public NettingFlowB( Party borrower, Party lender) {

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
        System.out.println("Updating  netting balances for Party B starts");
        subFlow(new SettleNettingB(lender));
        System.out.println("Updating netting  balances for Party B ends");
        System.out.println("----------------------------------------------");
        System.out.println("Updating netting  balances for nostro B starts");
        subFlow(new SettleNettingNostroB(lender));
        System.out.println("Updating netting  balances for nostro B ends");
    return null;

    }


}

