package net.corda.samples.example.states;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.samples.example.contracts.BalanceContractPartyB;
import net.corda.samples.example.contracts.NostroContract;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(NostroContract.class)
public class NostroState implements ContractState {
    private final double  amount;
    private final Party owner;
    private final  String status;

    public NostroState(double amount, Party owner, String status) {
        this.amount = amount;
        this.owner = owner;
        this.status = status;
    }


    public double getAmount() {
        return amount;
    }

    public Party getOwner() {
        return owner;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner);
    }
}
