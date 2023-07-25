package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.samples.example.contracts.PartyANostroContract;
import net.corda.samples.example.states.PartyANostroState;

import static java.util.Collections.emptyList;

@InitiatingFlow
@StartableByRPC
public class PartyAUpdateNostroBalanceFlow extends FlowLogic<Void> {
    private final double amount ;
    private final Party issuer;
    private final String status;
    public PartyAUpdateNostroBalanceFlow(double amount, Party issuer, String status) {
        this.amount = amount;
        this.issuer = issuer;
        this.status = status;
    }

    @Override
    @Suspendable
    public Void call() throws FlowException {
        final TransactionBuilder transactionBuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
                .addOutputState(new PartyANostroState(amount, issuer,status))
                .addCommand(new PartyANostroContract.Commands.UpdateBalance(), getOurIdentity().getOwningKey());
transactionBuilder.verify(getServiceHub());

        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);
subFlow(new FinalityFlow(signedTransaction, emptyList()));


        return null;
    }


}

