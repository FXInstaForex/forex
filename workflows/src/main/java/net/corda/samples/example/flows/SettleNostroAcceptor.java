package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;

@InitiatedBy(SettleNostroFlow.class)
public  class SettleNostroAcceptor extends FlowLogic<SignedTransaction> {

    private final FlowSession otherPartySession;

    public SettleNostroAcceptor(FlowSession otherPartySession) {
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
                    System.out.println("settle nostro acceptor`````````````````````````````");
                }
            }

            subFlow(new SignTxFlow(otherPartySession));

            return subFlow(new ReceiveFinalityFlow(otherPartySession));
        }
}