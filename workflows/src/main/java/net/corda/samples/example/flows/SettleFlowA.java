package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import net.corda.samples.example.contracts.BalanceContractPartyA;
import net.corda.samples.example.flows.vault.QueryFlowListA;
import net.corda.samples.example.states.PartyABalanceState;

import java.util.List;
import java.util.stream.Collectors;


@InitiatingFlow
@StartableByRPC
public class SettleFlowA extends FlowLogic<SignedTransaction> {

    private final Party issuer;





    public SettleFlowA( Party issuer) {

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
    public SignedTransaction call() throws FlowException {
        double Valutamountresult=0;

        List<StateAndRef<PartyABalanceState>> responseAList= subFlow(new QueryFlowListA(issuer));
        TransactionBuilder builder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0));


        try {
            for (StateAndRef<PartyABalanceState> output : responseAList) {

                PartyABalanceState pstate = output.getState().getData();
                Valutamountresult = Valutamountresult+pstate.getAmount();
                 //   builder.addOutputState(newPartyBalanceStateA, BalanceContractPartyA.BalanceContractPartyAID)
                builder  .addInputState(output)
                        .addCommand(new BalanceContractPartyA.Commands.UpdateBalance(), new PartyABalanceState(Valutamountresult,issuer, Vault.StateStatus.CONSUMED).getParticipants().stream()
                                .map(AbstractParty::getOwningKey)
                                .collect(Collectors.toList()));

            }

           builder.addOutputState(new PartyABalanceState(Valutamountresult,issuer, Vault.StateStatus.CONSUMED), BalanceContractPartyA.BalanceContractPartyAID);
        } catch (Throwable ex) {
            // Handle any exceptions or errors
            ex.printStackTrace();
            // You can also log the error or display a more meaningful error message to the users.
        }


      //  progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(builder);

        // Step 3: Collect signatures from other participants

        List<FlowSession> sessions = new PartyABalanceState(Valutamountresult,issuer, Vault.StateStatus.CONSUMED).getParticipants().stream()
                .filter(party -> !party.equals(getOurIdentity()))
                .map(this::initiateFlow)
                .collect(Collectors.toList());
        SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, sessions));

        // Step 4: Finalize the transaction
       // progressTracker.setCurrentStep(FINALIZING_TRANSACTION);

        System.out.println("Final Balance after netting for Party A"+Valutamountresult);
      //  progressTracker.setCurrentStep(FINALIZING_TRANSACTION);

        return subFlow(new FinalityFlow(fullySignedTransaction, sessions));

    }


}

