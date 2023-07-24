package net.corda.samples.example.flows.vault;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;


@InitiatingFlow
    @StartableByRPC
    public class QueryFlow extends FlowLogic <StateAndRef> {


    private final Party lender; // The name of the other node to query against
    private final Party borrower;

    private final double buyAmount;

    private final double sellAmount;

    public QueryFlow(Party lender, Party borrower, double buyAmount, double sellAmount) {
        this.lender = lender;
        this.borrower = borrower;
        this.buyAmount = buyAmount;
        this.sellAmount = sellAmount;
    }

    @Override
    @Suspendable
    public StateAndRef call() throws FlowException {
//            double Valutamountresult =0;
//            StateAndRef response;
//            response= null;
//            // Get the party for the other node
//            //Party otherParty = getServiceHub().getIdentityService().wellKnownPartyFromX500Name(CordaX500Name.parse(otherPartyName));
//
//            // Start the query flow and get the result
//            FlowSession otherPartySession = initiateFlow(otherPartyName);
//            System.out.println("Inside QueryFlow -------------->");
//
//            List<StateAndRef> queryResults=otherPartySession.receive(List.class).unwrap(data -> (List<StateAndRef>) data);
//            for (StateAndRef output : queryResults) {
//                response=output;
//                PartyABalanceState pstate = (PartyABalanceState)output.getState().getData();
//                Valutamountresult=pstate.getAmount();
//                System.out.println("getting amount from another node ----------------------------------------------------$$"+Valutamountresult);
//
//            }
//
//            return response;


        return null;
    }


}






