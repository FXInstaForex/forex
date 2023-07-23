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
@InitiatedBy(QueryFlow.class)
public class QueryResponderFlow extends FlowLogic<Double> {


    private final FlowSession otherPartySession;

    public QueryResponderFlow(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
    }

    @Override
    @Suspendable
    public Double  call() throws FlowException {
        double Valutamount =0;
        // Define the vault query criteria (customize as needed)
        //QueryCriteria queryCriteria = new QueryCriteria(Vault.StateStatus.UNCONSUMED);
        System.out.println("QI am inside call function ----------");
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        // Perform the vault query on this node
        List<StateAndRef<PartyABalanceState>> queryResults = getServiceHub().getVaultService().queryBy(PartyABalanceState.class, queryCriteria).getStates();


        // Send the query results back to the initiating node
        otherPartySession.send(queryResults);
        return Valutamount;
    }
}
