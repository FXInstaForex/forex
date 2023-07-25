package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.samples.example.states.PartyBNostroState;
import net.corda.samples.example.states.PartyBNostroState;

import java.util.ArrayList;
import java.util.List;


@InitiatingFlow
@StartableByRPC
public class QueryFlowListNostroB extends FlowLogic <List<StateAndRef<PartyBNostroState>>> {


    private final Party otherPartyName; // The name of the other node to query against

    public QueryFlowListNostroB(Party otherPartyName) {
        this.otherPartyName = otherPartyName;

    }

    @Override
    @Suspendable
    public List<StateAndRef<PartyBNostroState>> call() throws FlowException {
        double Valutamountresult =0;
        StateAndRef<PartyBNostroState> response;
        List<StateAndRef<PartyBNostroState>> queryResults= new ArrayList<StateAndRef<PartyBNostroState>>();
        FlowSession otherPartySession = initiateFlow(otherPartyName);
        System.out.println("Inside QueryFlowListNostroB -------------->");
        try {
            queryResults = otherPartySession.receive(List.class).unwrap(data -> (List<StateAndRef<PartyBNostroState>>) data);

        } catch (Throwable ex) {
            // Handle any exceptions or errors
            ex.printStackTrace();
            // You can also log the error or display a more meaningful error message to the users.
        }


        return queryResults;
    }
}









