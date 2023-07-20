package net.corda.samples.example.states;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.samples.example.contracts.BalanceContractPartyA;
import net.corda.samples.example.contracts.KYContract;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(KYContract.class)
public class KYCState implements ContractState {

    private final Party country;

    public KYCState(Party country) {
        this.country = country;
    }

    public Party getCountry() {
        return country;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(country);
    }
}
