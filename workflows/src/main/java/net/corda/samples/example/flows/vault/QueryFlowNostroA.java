package net.corda.samples.example.flows.vault;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.samples.example.states.PartyANostroState;
import net.corda.samples.example.states.PartyBNostroState;

import java.util.List;


@InitiatingFlow
@StartableByRPC
public class QueryFlowNostroA extends FlowLogic <StateAndRef> {


    private final Party otherPartyName; // The name of the other node to query against

    public QueryFlowNostroA(Party otherPartyName) {
        this.otherPartyName = otherPartyName;

    }

    @Override
    @Suspendable
    public StateAndRef call() throws FlowException {
        StateAndRef res=null;
        double Valutamountresult=0;
        // Get the party for the other node
        //Party otherParty = getServiceHub().getIdentityService().wellKnownPartyFromX500Name(CordaX500Name.parse(otherPartyName));

        // Start the query flow and get the result
        FlowSession otherPartySession = initiateFlow(otherPartyName);
        System.out.println("Inside QueryFlow -------------->");
        List<StateAndRef> queryResults=otherPartySession.receive(List.class).unwrap(data -> (List<StateAndRef>) data);
        for (StateAndRef output : queryResults) {
            res=output;
            PartyBNostroState pstate = (PartyBNostroState) output.getState().getData();
            Valutamountresult=pstate.getAmount();
            System.out.println("getting aount from another node ----------------------------------------------------$$"+Valutamountresult);

        }

        return res;
    }
}









