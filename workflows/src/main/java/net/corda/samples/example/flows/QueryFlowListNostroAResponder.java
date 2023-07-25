package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.samples.example.states.PartyANostroState;

import java.util.List;

@InitiatedBy(QueryFlowListNostroA.class)
public class QueryFlowListNostroAResponder extends FlowLogic<List<StateAndRef<PartyANostroState>>> {


    private final FlowSession otherPartySession;

    public QueryFlowListNostroAResponder(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
    }

    @Override
    @Suspendable
    public List<StateAndRef<PartyANostroState>> call() throws FlowException {
        double Valutamount =0;
        // Define the vault query criteria (customize as needed)
        //QueryCriteria queryCriteria = new QueryCriteria(Vault.StateStatus.UNCONSUMED);
        System.out.println("QI am inside PartyANostroState responder function ----------");
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        // Perform the vault query on this node
        List<StateAndRef<PartyANostroState>> queryResults = getServiceHub().getVaultService().queryBy(PartyANostroState.class, queryCriteria).getStates();


        // Send the query results back to the initiating node
        otherPartySession.send(queryResults);
        return queryResults;
    }
}
