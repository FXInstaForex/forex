package net.corda.samples.example.states;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.samples.example.contracts.BalanceContractPartyB;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(BalanceContractPartyB.class)
public class B implements ContractState {
    private final double  amount;
    private final Party lender;
    private final Party borrower;

    public B(double amount, Party lender, Party borrower) {
        this.amount = amount;
        this.lender = lender;
        this.borrower=borrower;
    }

    public double getAmount() {
        return amount;
    }

    public Party getlender() {
        return lender;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(lender,borrower);
    }
}
