package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;

@InitiatedBy(SettleFlowNostroA.class)
public  class SettleFlowNostroAAcceptor extends FlowLogic<SignedTransaction> {

    private final FlowSession otherPartySession;

    public SettleFlowNostroAAcceptor(FlowSession otherPartySession) {
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
                    System.out.println("settle flow a acceptor`````````````````````````````");
                }
            }

            subFlow(new SignTxFlow(otherPartySession));

            return subFlow(new ReceiveFinalityFlow(otherPartySession));
        }
}