package net.corda.samples.example.flows.vault;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.samples.example.states.PartyBNostroState;

import java.util.List;

@InitiatedBy(QueryFlowNostroB.class)
public class QueryResponderFlowNostroB extends FlowLogic<List<StateAndRef<PartyBNostroState>>> {


    private final FlowSession otherPartySession;

    public QueryResponderFlowNostroB(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
    }

    @Override
    @Suspendable
    public List<StateAndRef<PartyBNostroState>>  call() throws FlowException {
        double Valutamount =0;
        // Define the vault query criteria (customize as needed)
        //QueryCriteria queryCriteria = new QueryCriteria(Vault.StateStatus.UNCONSUMED);
        System.out.println("QI am inside responder nostro b  function ----------");
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        // Perform the vault query on this node
        List<StateAndRef<PartyBNostroState>> queryResults = getServiceHub().getVaultService().queryBy(PartyBNostroState.class, queryCriteria).getStates();
        System.out.println("responder nostro b queryResults ----------"+queryResults.toString());

        // Send the query results back to the initiating node
        otherPartySession.send(queryResults);
        return queryResults;
    }
}
