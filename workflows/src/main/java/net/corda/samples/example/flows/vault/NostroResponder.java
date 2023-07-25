package net.corda.samples.example.flows.vault;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.samples.example.states.NostroState;
import net.corda.samples.example.states.PartyBalanceStateB;

import java.util.List;

;

@InitiatedBy(NostroFlow.class)
public class NostroResponder extends FlowLogic<List<StateAndRef<NostroState>>> {


    private final FlowSession otherPartySession;

    public NostroResponder(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
    }

    @Override
    @Suspendable
    public List<StateAndRef<NostroState>>  call() throws FlowException {
        double Valutamount =0;
        // Define the vault query criteria (customize as needed)
        //QueryCriteria queryCriteria = new QueryCriteria(Vault.StateStatus.UNCONSUMED);
        System.out.println("QI am inside NostroState responder function ----------");
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        // Perform the vault query on this node
        List<StateAndRef<NostroState>> queryResults = getServiceHub().getVaultService().queryBy(NostroState.class, queryCriteria).getStates();


        // Send the query results back to the initiating node
        otherPartySession.send(queryResults);
        return queryResults;
    }
}


