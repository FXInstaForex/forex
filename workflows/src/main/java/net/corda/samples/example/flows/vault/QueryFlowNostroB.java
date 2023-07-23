package net.corda.samples.example.flows.vault;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.samples.example.states.PartyBNostroState;

import java.util.List;


@InitiatingFlow
@StartableByRPC
public class QueryFlowNostroB extends FlowLogic <StateAndRef<PartyBNostroState>> {


    private final Party otherPartyName; // The name of the other node to query against

    public QueryFlowNostroB(Party otherPartyName) {
        this.otherPartyName = otherPartyName;

    }

    @Override
    @Suspendable
    public StateAndRef<PartyBNostroState> call() throws FlowException {
        double Valutamountresult =0;
        StateAndRef<PartyBNostroState> response;
        response= null;
        // Get the party for the other node
        //Party otherParty = getServiceHub().getIdentityService().wellKnownPartyFromX500Name(CordaX500Name.parse(otherPartyName));

        // Start the query flow and get the result
        FlowSession otherPartySession = initiateFlow(otherPartyName);
        System.out.println("Inside QueryFlowB -------------->");
        try {
            List<StateAndRef<PartyBNostroState>> queryResults = otherPartySession.receive(List.class).unwrap(data -> (List<StateAndRef<PartyBNostroState>>) data);
            for (StateAndRef<PartyBNostroState> output : queryResults) {
                response = output;
                PartyBNostroState pstate = output.getState().getData();
                Valutamountresult = pstate.getAmount();
                System.out.println("getting aount from another node ----------------------------------------------------$$" + Valutamountresult);

            }
        } catch (Throwable ex) {
            // Handle any exceptions or errors
            ex.printStackTrace();
            // You can also log the error or display a more meaningful error message to the users.
        }


        return response;
    }
}









