package net.corda.samples.example.flows;
import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;

import net.corda.core.identity.Party;
import net.corda.samples.example.states.PartyANostroState;

import java.util.ArrayList;
import java.util.List;


    @InitiatingFlow
    @StartableByRPC
    public class QueryFlowListNostroA extends FlowLogic <List<StateAndRef<PartyANostroState>>> {


        private final Party otherPartyName; // The name of the other node to query against

        public QueryFlowListNostroA(Party otherPartyName) {
            this.otherPartyName = otherPartyName;

        }

        @Override
        @Suspendable
        public List<StateAndRef<PartyANostroState>> call() throws FlowException {
            double Valutamountresult =0;
            StateAndRef<PartyANostroState> response;
            List<StateAndRef<PartyANostroState>> queryResults= new ArrayList<StateAndRef<PartyANostroState>>();
            FlowSession otherPartySession = initiateFlow(otherPartyName);
            System.out.println("Inside QueryFlowA -------------->");
            try {
                queryResults = otherPartySession.receive(List.class).unwrap(data -> (List<StateAndRef<PartyANostroState>>) data);

            } catch (Throwable ex) {
                // Handle any exceptions or errors
                ex.printStackTrace();
                // You can also log the error or display a more meaningful error message to the users.
            }


            return queryResults;
        }
    }









