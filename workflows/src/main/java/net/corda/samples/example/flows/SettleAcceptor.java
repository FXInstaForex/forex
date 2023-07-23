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
            private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) throws FlowException {
                super(otherPartyFlow, progressTracker);
            }

            @Override
            protected void checkTransaction(SignedTransaction stx) {
//                requireThat(require -> {
//                    ContractState output = stx.getTx().getOutputs().get(0).getData();
//                    require.using("This must be an IOU transaction.", output instanceof IOUState);
//                    IOUState iou = (IOUState) output;
//                    require.using("I won't accept IOUs with a value over 100.", iou.getValue() <= 100);
//                    return null;
//                });
//            }
            }

            final SignTxFlow signTxFlow = new SignTxFlow(otherPartySession, SignTransactionFlow.Companion.tracker());





        }
        return  subFlow(new ReceiveFinalityFlow(otherPartySession));
    }
}