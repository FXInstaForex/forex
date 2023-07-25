package net.corda.samples.example.states;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.samples.example.contracts.ProposalAndTradeContract;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

@BelongsToContract(ProposalAndTradeContract.class)
public class ProposalState implements LinearState {
    private Party buyer;
    private Party seller;
    private Party proposer;
    private Party proposee;
    private UniqueIdentifier linearId;
    private String buyCurrency;
    private double buyAmount;
    private String sellCurrency;
    private double sellAmount;
    private String tradeId;
    private Date settlmentDate;
    private double spotRate;

    @ConstructorForDeserialization
    public ProposalState(Party buyer, Party seller, Party proposer, Party proposee, String buyCurrency,double buyAmount,String sellCurrency,double sellAmount,String tradeId,Date settlmentDate,double spotRate,UniqueIdentifier linearId) {

        this.buyer = buyer;
        this.seller = seller;
        this.proposer = proposer;
        this.proposee = proposee;
        this.buyCurrency=buyCurrency;
        this.buyAmount=buyAmount;
        this.sellCurrency=sellCurrency;
        this.sellAmount=sellAmount;
        this.tradeId = tradeId;
        this.settlmentDate=settlmentDate;
        this.spotRate=spotRate;
        double result = spotRate * buyAmount;
        this.sellAmount=result;
        this.linearId = linearId;

    }

    public ProposalState(Party buyer, Party seller, Party proposer, Party proposee,String buyCurrency,double buyAmount,String sellCurrency,double sellAmount,String tradeId,Date settlmentDate, double spotRate) {

        this.buyer = buyer;
        this.seller = seller;
        this.proposer = proposer;
        this.proposee = proposee;
        this.buyCurrency=buyCurrency;
        this.buyAmount=buyAmount;
        this.sellCurrency=sellCurrency;
        this.tradeId = tradeId;
        this.settlmentDate=settlmentDate;
        this.spotRate=spotRate;
        double result = spotRate * buyAmount;
        this.sellAmount=result;
        this.linearId = new UniqueIdentifier();
    }

   public Party getBuyer() {
        return buyer;
    }

    public Party getSeller() {
        return seller;
    }

    public Party getProposer() {
        return proposer;
    }


    public double getBuyAmount() {
        return buyAmount;
    }

    public double getSellAmount() {
        return sellAmount;
    }

    public double getSpotRate() {
        return spotRate;
    }

    public String getBuyCurrency() {
        return buyCurrency;
    }

    public String getSellCurrency() {
        return sellCurrency;
    }

    public String getTradeId() {
        return linearId.toString();
    }

    public Date getSettlmentDate() {
        return settlmentDate;
    }

    public Party getProposee() {
        return proposee;
    }

    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants(){
        return ImmutableList.of(proposer, proposee);

    }
}
