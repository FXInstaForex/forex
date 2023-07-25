package net.corda.samples.example.flows.vault;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.samples.example.states.PartyBalanceStateB;

import java.util.ArrayList;
import java.util.List;


@InitiatingFlow
    @StartableByRPC
    public class QueryFlowListB extends FlowLogic <List<StateAndRef<PartyBalanceStateB>>> {

    private final Party otherPartyName; // The name of the other node to query against

    public QueryFlowListB(Party otherPartyName) {
        this.otherPartyName = otherPartyName;

    }
    @Override
    @Suspendable
    public List<StateAndRef<PartyBalanceStateB>> call() throws FlowException {
        double Valutamountresult =0;
        StateAndRef<PartyBalanceStateB> response;
        List<StateAndRef<PartyBalanceStateB>> queryResults= new ArrayList<StateAndRef<PartyBalanceStateB>>();
        FlowSession otherPartySession = initiateFlow(otherPartyName);
        System.out.println("Inside QueryFlowA -------------->");
        try {
            queryResults = otherPartySession.receive(List.class).unwrap(data -> (List<StateAndRef<PartyBalanceStateB>>) data);

        } catch (Throwable ex) {
            // Handle any exceptions or errors
            ex.printStackTrace();
            // You can also log the error or display a more meaningful error message to the users.
        }


        return queryResults;
    }


}






