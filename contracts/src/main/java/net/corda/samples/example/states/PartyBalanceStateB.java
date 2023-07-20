package net.corda.samples.example.states;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.samples.example.contracts.BalanceContractPartyB;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(BalanceContractPartyB.class)
public class PartyBalanceStateB implements ContractState {
    private final double  amount;
    private final Party owner;

    public PartyBalanceStateB(double amount, Party owner) {
        this.amount = amount;
        this.owner = owner;
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
