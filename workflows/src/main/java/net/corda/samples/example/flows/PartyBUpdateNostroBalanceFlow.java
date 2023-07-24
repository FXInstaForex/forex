package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.samples.example.contracts.NostroContract;
import net.corda.samples.example.contracts.PartyBNostroContract;
import net.corda.samples.example.states.NostroState;
import net.corda.samples.example.states.PartyBNostroState;

import static java.util.Collections.emptyList;

@InitiatingFlow
@StartableByRPC
public class PartyBUpdateNostroBalanceFlow extends FlowLogic<Void> {
    private final double amount ;
    private final Party issuer;
    public PartyBUpdateNostroBalanceFlow(double amount, Party issuer) {
        this.amount = amount;
        this.issuer = issuer;
    }

    @Override
    @Suspendable
    public Void call() throws FlowException {
        final TransactionBuilder transactionBuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
                .addOutputState(new NostroState(amount,issuer))
                .addCommand(new NostroContract.Commands.UpdateBalance(), getOurIdentity().getOwningKey());
transactionBuilder.verify(getServiceHub());

        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);
subFlow(new FinalityFlow(signedTransaction, emptyList()));


        return null;
    }


}

