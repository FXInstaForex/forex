package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
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

import static java.util.Collections.emptyList;

@InitiatingFlow
@StartableByRPC
public class SettleNostroFlow extends FlowLogic<Void> {
    private final double tranAmt ;
    private final Party issuer;

    public SettleNostroFlow(double tranAmt, Party issuer) {
        this.tranAmt = tranAmt;
        this.issuer = issuer;
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
        StateAndRef<NostroState> responseB= subFlow(new NostroFlow(issuer));
        final NostroState oldIOUState =  responseB.getState().getData();

        double previousAmount = oldIOUState.getAmount();
        double  amount= previousAmount + (tranAmt);
        final NostroState newIOUState = new NostroState(amount,getOurIdentity());
        System.out.println("Inside settlementInitiatorB :: tranAmt"+tranAmt);
        System.out.println("Inside settlementInitiatorB ::previousAmount)"+previousAmount);
        System.out.println("Inside settlementInitiatorB :: finalamout"+amount);

        final TransactionBuilder transactionBuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
                .addOutputState(new NostroState(amount, issuer), NostroContract.NostroContractID)
                .addInputState(responseB)
                .addCommand(new PartyBNostroContract.Commands.UpdateBalance(), issuer.getOwningKey());

        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        transactionBuilder.verify(getServiceHub());

        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
        subFlow(new FinalityFlow(signedTransaction, emptyList()));

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

