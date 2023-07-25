package net.corda.samples.example.flows.vault;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.samples.example.states.PartyABalanceState;

import java.util.List;

;

@InitiatedBy(QueryFlowListA.class)
public class QueryResponderFlowA extends FlowLogic<List<StateAndRef<PartyABalanceState>>> {


    private final FlowSession otherPartySession;

    public QueryResponderFlowA(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
    }

    @Override
    @Suspendable
    public List<StateAndRef<PartyABalanceState>>  call() throws FlowException {
        double Valutamount =0;
        // Define the vault query criteria (customize as needed)
        //QueryCriteria queryCriteria = new QueryCriteria(Vault.StateStatus.UNCONSUMED);
        System.out.println("QI am inside PartyABalanceState responder function ----------");
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        // Perform the vault query on this node
        List<StateAndRef<PartyABalanceState>> queryResults = getServiceHub().getVaultService().queryBy(PartyABalanceState.class, queryCriteria).getStates();


        // Send the query results back to the initiating node
        otherPartySession.send(queryResults);
        return queryResults;
    }
}


