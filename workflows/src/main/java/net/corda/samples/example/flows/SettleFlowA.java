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
import net.corda.samples.example.states.PartyABalanceState;

import java.util.Collections;
import java.util.List;


@InitiatingFlow
@StartableByRPC
public class SettleFlowA extends FlowLogic<Void> {
    private final double tranAmt ;
    private final Party issuer;





    public SettleFlowA(double tranAmt, Party issuer) {
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
        double Valutamountresult=0;
        List<StateAndRef<PartyABalanceState>> responseAList= subFlow(new QueryFlowA(issuer));

        try {
            for (StateAndRef<PartyABalanceState> output : responseAList) {

                PartyABalanceState pstate = output.getState().getData();
                Valutamountresult = Valutamountresult+pstate.getAmount();
                System.out.println("getting aount from another node ----------------------------------------------------$$" + Valutamountresult);

            }
            System.out.println("final Valutamountresult"+Valutamountresult);
        } catch (Throwable ex) {
            // Handle any exceptions or errors
            ex.printStackTrace();
            // You can also log the error or display a more meaningful error message to the users.
        }

        final PartyABalanceState newIOUState = new PartyABalanceState(Valutamountresult,issuer);
       final TransactionBuilder transactionBuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
                .addOutputState(newIOUState)
                //.addInputState(responseB)
                .addCommand(new BalanceContractPartyB.Commands.UpdateBalance(), issuer.getOwningKey());
        System.out.println("Inside settleflowA :: before GENERATING_TRANSACTION");
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        transactionBuilder.verify(getServiceHub());
        System.out.println("Inside settleflowA :: after verifying _TRANSACTION");
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);
        System.out.println("Inside settleflowA :: signedTransaction"+signedTransaction);

        progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
        subFlow(new FinalityFlow(signedTransaction, Collections.emptyList()));

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

