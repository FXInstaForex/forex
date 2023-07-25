package net.corda.samples.example.states;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.samples.example.contracts.PartyBNostroContract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(PartyBNostroContract.class)
public class PartyBNostroState implements ContractState {
    private final double  amount;

    private final Party issuer;
    private final String status;
    public PartyBNostroState(double amount, Party issuer, String status) {
        this.amount = amount;
        this.issuer = issuer;
        this.status = status;
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
