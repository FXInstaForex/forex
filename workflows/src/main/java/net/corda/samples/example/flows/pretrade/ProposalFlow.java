package net.corda.samples.example.flows.pretrade;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import kotlin.Unit;
import net.corda.samples.example.contracts.ProposalAndTradeContract;
import net.corda.samples.example.states.ProposalState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;

public class ProposalFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<UniqueIdentifier> {
        private Boolean isBuyer;

        private Party counterparty;
        private String buyCurrency;
        private BigDecimal buyAmount;
        private String sellCurrency;
        private BigDecimal sellAmount;
        private String tradeId;
        private Date settlmentDate;
        private BigDecimal spotRate;

        public Initiator(Boolean isBuyer, Party counterparty, String buyCurrency, BigDecimal buyAmount, String sellCurrency, BigDecimal sellAmount, String tradeId, Date settlmentDate,BigDecimal spotRate) {
            this.isBuyer = isBuyer;
            this.counterparty = counterparty;
            this.buyCurrency=buyCurrency;
            this.buyAmount=buyAmount;
            this.sellCurrency=sellCurrency;
            this.sellAmount=sellAmount;
            this.tradeId = tradeId;
            this.settlmentDate=settlmentDate;
            this.spotRate=spotRate;
        }

        @Suspendable
        @Override
        public UniqueIdentifier call() throws FlowException {
            Party buyer, seller;
            if(isBuyer){
                buyer = getOurIdentity();
                seller = counterparty;
            }else{
                buyer = counterparty;
                seller = getOurIdentity();
            }
            ProposalState output = new ProposalState(buyer, seller,getOurIdentity(),counterparty,buyCurrency,buyAmount,sellCurrency,sellAmount,tradeId,settlmentDate,spotRate);

            //Creating the command
            ProposalAndTradeContract.Commands.Propose commandType = new ProposalAndTradeContract.Commands.Propose();
            List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey(), counterparty.getOwningKey());
            Command command = new Command(commandType, requiredSigners);

            //Building the transaction
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(output, ProposalAndTradeContract.ID)
                    .addCommand(command);

            //Signing the transaction ourselves
            SignedTransaction partStx = getServiceHub().signInitialTransaction(txBuilder);

            //Gather counterparty sigs
            FlowSession counterpartySession = initiateFlow(counterparty);
            SignedTransaction fullyStx = subFlow(new CollectSignaturesFlow(partStx, ImmutableList.of(counterpartySession)));

            //Finalise the transaction
            SignedTransaction finalisedTx = subFlow(new FinalityFlow(fullyStx, ImmutableList.of(counterpartySession)));
            return finalisedTx.getTx().outputsOfType(ProposalState.class).get(0).getLinearId();
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<SignedTransaction>{
        private FlowSession counterpartySession;

        public Responder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            SignTransactionFlow signTransactionFlow = new SignTransactionFlow(counterpartySession){

                @Override
                protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {

                }
            };
            SecureHash txId = subFlow(signTransactionFlow).getId();

            SignedTransaction finalisedTx = subFlow(new ReceiveFinalityFlow(counterpartySession, txId));
            return finalisedTx;
        }
    }
}
