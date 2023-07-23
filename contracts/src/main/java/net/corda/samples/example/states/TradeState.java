package net.corda.samples.example.states;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.samples.example.contracts.ProposalAndTradeContract;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@BelongsToContract(ProposalAndTradeContract.class)
public class TradeState implements LinearState {
    private Party buyer;
    private Party seller;
    private UniqueIdentifier linearId;

    private String buyCurrency;
    private BigDecimal buyAmount;
    private String sellCurrency;
    private BigDecimal sellAmount;
    private String tradeId;
    private Date settlmentDate;

    private BigDecimal spotRate;


    @ConstructorForDeserialization
    public TradeState( Party buyer, Party seller,String buyCurrency,BigDecimal buyAmount,String sellCurrency,BigDecimal sellAmount,String tradeId,Date settlmentDate, BigDecimal spotRate,UniqueIdentifier linearId) {

        this.buyer = buyer;
        this.seller = seller;
        this.buyCurrency=buyCurrency;
        this.buyAmount=buyAmount;
        this.sellCurrency=sellCurrency;
        this.sellAmount=sellAmount;
        this.tradeId=tradeId;
        this.settlmentDate=settlmentDate;
        this.spotRate=spotRate;
        this.linearId = linearId;
    }

    public TradeState( Party buyer, Party seller,String buyCurrency,BigDecimal buyAmount,String sellCurrency,BigDecimal sellAmount,String tradeId,Date settlmentDate,BigDecimal spotRate) {

        this.buyer = buyer;
        this.seller = seller;
        this.buyCurrency=buyCurrency;
        this.buyAmount=buyAmount;
        this.sellCurrency=sellCurrency;
        this.sellAmount=sellAmount;
        this.tradeId=tradeId;
        this.settlmentDate=settlmentDate;
        this.spotRate=spotRate;
        this.linearId = new UniqueIdentifier();
    }

    public Party getBuyer() {
        return buyer;
    }

    public Party getSeller() {
        return seller;
    }

    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public BigDecimal getBuyAmount() {
        return buyAmount;
    }

    public BigDecimal getSellAmount() {
        return sellAmount;
    }

    public BigDecimal getSpotRate() {
        return spotRate;
    }

    public String getBuyCurrency() {
        return buyCurrency;
    }

    public String getSellCurrency() {
        return sellCurrency;
    }

    public String getTradeId() {
        return tradeId;
    }

    public Date getSettlmentDate() {
        return settlmentDate;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(buyer,seller);
    }
}
