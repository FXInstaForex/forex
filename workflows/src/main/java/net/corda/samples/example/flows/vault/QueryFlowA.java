package net.corda.samples.example.flows.vault;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.samples.example.states.PartyABalanceState;

import java.util.ArrayList;
import java.util.List;


@InitiatingFlow
@StartableByRPC
public class QueryFlowA extends FlowLogic <List<StateAndRef<PartyABalanceState>>> {


    private final Party otherPartyName; // The name of the other node to query against

    public QueryFlowA(Party otherPartyName) {
        this.otherPartyName = otherPartyName;

    }

    @Override
    @Suspendable
    public List<StateAndRef<PartyABalanceState>> call() throws FlowException {
        double Valutamountresult =0;
        StateAndRef<PartyABalanceState> response;
        List<StateAndRef<PartyABalanceState>> queryResults= new ArrayList<StateAndRef<PartyABalanceState>>();
        FlowSession otherPartySession = initiateFlow(otherPartyName);
        System.out.println("Inside QueryFlowB -------------->");
        try {
             queryResults = otherPartySession.receive(List.class).unwrap(data -> (List<StateAndRef<PartyABalanceState>>) data);
            for (StateAndRef<PartyABalanceState> output : queryResults) {
                response = output;
                PartyABalanceState pstate = output.getState().getData();
                Valutamountresult = pstate.getAmount();
                System.out.println("getting aount from another node ----------------------------------------------------$$" + Valutamountresult);

            }
        } catch (Throwable ex) {
            // Handle any exceptions or errors
            ex.printStackTrace();
            // You can also log the error or display a more meaningful error message to the users.
        }


        return queryResults;
    }
}









