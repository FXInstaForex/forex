package net.corda.samples.example.states;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.samples.example.contracts.PartyANostroContract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(PartyANostroContract.class)
public class PartyANostroState implements ContractState {
    private final double  amount;

    private final Party issuer;
    public PartyANostroState(double amount, Party issuer) {
        this.amount = amount;
        this.issuer = issuer;
    }

    public double getAmount() {
        return amount;
    }


    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(issuer);
    }
}
