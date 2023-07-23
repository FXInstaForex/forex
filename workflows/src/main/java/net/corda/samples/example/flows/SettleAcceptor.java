package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

@InitiatedBy(SettleFlow.class)
public  class SettleAcceptor extends FlowLogic<SignedTransaction> {

    private final FlowSession otherPartySession;

    public SettleAcceptor(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {

            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession otherPartySession) {
                    super(otherPartySession);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                    // Additional checks on the received transaction
                }
            }

            subFlow(new SignTxFlow(otherPartySession));


        return  subFlow(new ReceiveFinalityFlow(otherPartySession));
    }
}