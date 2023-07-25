package net.corda.samples.obligation.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.corda.client.jackson.JacksonSupport;
import net.corda.core.contracts.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.samples.example.flows.*;
import net.corda.samples.example.flows.pretrade.AcceptanceFlow;
import net.corda.samples.example.flows.pretrade.ModificationFlow;
import net.corda.samples.example.flows.pretrade.ProposalFlow;
import net.corda.samples.example.states.IOUState;
import net.corda.samples.example.states.TradeState;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/api/iou") // The paths for HTTP requests are relative to this base path.
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(RestController.class);
    private final CordaRPCOps proxy;
    private final CordaX500Name me;

    public MainController(NodeRPCConnection rpc) {
        this.proxy = rpc.getProxy();
        this.me = proxy.nodeInfo().getLegalIdentities().get(0).getName();

    }

    /** Helpers for filtering the network map cache. */
    public String toDisplayString(X500Name name){
        return BCStyle.INSTANCE.toString(name);
    }

    private boolean isNotary(NodeInfo nodeInfo) {
        return !proxy.notaryIdentities()
                .stream().filter(el -> nodeInfo.isLegalIdentity(el))
                .collect(Collectors.toList()).isEmpty();
    }

    private boolean isMe(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().equals(me);
    }

    private boolean isNetworkMap(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().equals("Network Map Service");
    }

    @Configuration
    class Plugin {
        @Bean
        public ObjectMapper registerModule() {
            return JacksonSupport.createNonRpcMapper();
        }
    }

    @GetMapping(value = "/status", produces = TEXT_PLAIN_VALUE)
    private String status() {
        return "200";
    }

    @GetMapping(value = "/servertime", produces = TEXT_PLAIN_VALUE)
    private String serverTime() {
        return (LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC"))).toString();
    }

    @GetMapping(value = "/addresses", produces = TEXT_PLAIN_VALUE)
    private String addresses() {
        return proxy.nodeInfo().getAddresses().toString();
    }

    @GetMapping(value = "/identities", produces = TEXT_PLAIN_VALUE)
    private String identities() {
        return proxy.nodeInfo().getLegalIdentities().toString();
    }

    @GetMapping(value = "/platformversion", produces = TEXT_PLAIN_VALUE)
    private String platformVersion() {
        return Integer.toString(proxy.nodeInfo().getPlatformVersion());
    }

    @GetMapping(value = "/peers", produces = APPLICATION_JSON_VALUE)
    public HashMap<String, List<String>> getPeers() {
        HashMap<String, List<String>> myMap = new HashMap<>();

        // Find all nodes that are not notaries, ourself, or the network map.
        Stream<NodeInfo> filteredNodes = proxy.networkMapSnapshot().stream()
                .filter(el -> !isNotary(el) && !isMe(el) && !isNetworkMap(el));
        // Get their names as strings
        List<String> nodeNames = filteredNodes.map(el -> el.getLegalIdentities().get(0).getName().toString())
                .collect(Collectors.toList());

        myMap.put("peers", nodeNames);
        return myMap;
    }

    @GetMapping(value = "/notaries", produces = TEXT_PLAIN_VALUE)
    private String notaries() {
        return proxy.notaryIdentities().toString();
    }

    @GetMapping(value = "/flows", produces = TEXT_PLAIN_VALUE)
    private String flows() {
        return proxy.registeredFlows().toString();
    }

    @GetMapping(value = "/states", produces = TEXT_PLAIN_VALUE)
    private String states() {
        return proxy.vaultQuery(ContractState.class).getStates().toString();
    }

    @GetMapping(value = "/me",produces = APPLICATION_JSON_VALUE)
    private HashMap<String, String> whoami(){
        HashMap<String, String> myMap = new HashMap<>();
        myMap.put("me", me.toString());
        return myMap;
    }
    @GetMapping(value = "/ious",produces = APPLICATION_JSON_VALUE)
    public List<StateAndRef<TradeState>> getIOUs() {
        // Filter by states type: IOU.
        return proxy.vaultQuery(TradeState.class).getStates();
    }







    /**
     * Settles an IOU. Requires cash in the right currency to be able to settle.
     * Example request:
     * curl -X GET 'http://localhost:10007/api/iou/settle-iou?id=705dc5c5-44da-4006-a55b-e29f78955089&amount=98&currency=USD'
     */

    /**
     * Helper end-point to issue some cash to ourselves.
     * Example request:
     * curl -X GET 'http://localhost:10009/api/iou/self-issue-cash?amount=100&currency=USD'
     */

    @CrossOrigin("*")

    @PostMapping (value = "initialBalanceA/{amt}" , produces =  TEXT_PLAIN_VALUE  )
    public ResponseEntity<String> initialBalanceA(@PathVariable String amt) throws IllegalArgumentException {
        System.out.println("request"+Double.valueOf(amt));
        HttpHeaders header= new HttpHeaders();
        header.add("Content-Type","CORS-HEADER");
        double amount=Double.valueOf(amt);
        // Get party objects for myself and the counterparty.

        CordaX500Name partyX500Name = CordaX500Name.parse("O=PartyA, L=London, C=GB");
        System.out.println("partyX500Name..............................."+partyX500Name);
        Party issuer = proxy.wellKnownPartyFromX500Name(partyX500Name);



        //String borrower = request.getParameter("borrower");

        // Get party objects for myself and the counterparty.

//        CordaX500Name partyX500Name = CordaX500Name.parse(party);
//        Party otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name);

        // Create a new IOU state using the parameters given.
        try {
            // Start the IOUIssueFlow. We block and waits for the flow to return.
            proxy.startTrackedFlowDynamic(PartyAUpdateBalanceFlow.class,amount,issuer,"UNCONSUMED").getReturnValue().get();
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction id committed to ledger.\n ");
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
    @PostMapping (value = "initialBalanceB/{amt}" , produces =  TEXT_PLAIN_VALUE  )
    public ResponseEntity<String> initialBalanceB(@PathVariable String amt) throws IllegalArgumentException {
        System.out.println("request"+Double.valueOf(amt));

        double amount=Double.valueOf(amt);
        // Get party objects for myself and the counterparty.

        CordaX500Name partyX500Name = CordaX500Name.parse("O=PartyB, L=New York, C=US");
        System.out.println("partyX500Name..............................."+partyX500Name);
        Party issuer = proxy.wellKnownPartyFromX500Name(partyX500Name);



        //String borrower = request.getParameter("borrower");

        // Get party objects for myself and the counterparty.

//        CordaX500Name partyX500Name = CordaX500Name.parse(party);
//        Party otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name);

        // Create a new IOU state using the parameters given.
        try {
            // Start the IOUIssueFlow. We block and waits for the flow to return.
            proxy.startTrackedFlowDynamic(PartyBUpdateBalanceFlow.class,amount,issuer,"UNCONSUMED").getReturnValue().get();
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction id  committed to ledger.\n ");
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    /**
     * Displays all IOU states that only this node has been involved in.
     */
    @GetMapping(value = "my-ious",produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StateAndRef<IOUState>>> getMyIOUs() {
        List<StateAndRef<IOUState>> myious = proxy.vaultQuery(IOUState.class).getStates().stream().filter(
                it -> it.getState().getData().getLender().equals(proxy.nodeInfo().getLegalIdentities().get(0))).collect(Collectors.toList());
        return ResponseEntity.ok(myious);
    }

    @PostMapping (value = "initialNostroBalanceA/{amt}" , produces =  TEXT_PLAIN_VALUE  )
    public ResponseEntity<String> initialNostroBalanceA(@PathVariable String amt) throws IllegalArgumentException {
        System.out.println("request"+Double.valueOf(amt));

        double amount=Double.valueOf(amt);
        // Get party objects for myself and the counterparty.

        CordaX500Name partyX500Name = CordaX500Name.parse("O=PartyA, L=London, C=GB");
        System.out.println("partyX500Name..............................."+partyX500Name);
        Party issuer = proxy.wellKnownPartyFromX500Name(partyX500Name);



        //String borrower = request.getParameter("borrower");

        // Get party objects for myself and the counterparty.

//        CordaX500Name partyX500Name = CordaX500Name.parse(party);
//        Party otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name);

        // Create a new IOU state using the parameters given.
        try {
            // Start the IOUIssueFlow. We block and waits for the flow to return.
            proxy.startTrackedFlowDynamic(PartyAUpdateNostroBalanceFlow.class,amount,issuer,"UNCONSUMED").getReturnValue().get();
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction committed to ledger.\n ");
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
    @PostMapping (value = "initialNostroBalanceB/{amt}" , produces =  TEXT_PLAIN_VALUE  )
    public ResponseEntity<String> initialNostroBalanceB(@PathVariable String amt) throws IllegalArgumentException {
        System.out.println("request"+Double.valueOf(amt));

        double amount=Double.valueOf(amt);
        // Get party objects for myself and the counterparty.

        CordaX500Name partyX500Name = CordaX500Name.parse("O=PartyB, L=New York, C=US");
        System.out.println("partyX500Name..............................."+partyX500Name);
        Party issuer = proxy.wellKnownPartyFromX500Name(partyX500Name);



        //String borrower = request.getParameter("borrower");

        // Get party objects for myself and the counterparty.

//        CordaX500Name partyX500Name = CordaX500Name.parse(party);
//        Party otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name);

        // Create a new IOU state using the parameters given.
        try {
            // Start the IOUIssueFlow. We block and waits for the flow to return.
            proxy.startTrackedFlowDynamic(PartyBUpdateBalanceFlow.class,amount,issuer,"UNCONSUMED").getReturnValue().get();
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction id committed to ledger.\n " );
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping (value = "settleBalancesA/{buyAmt}/{sellAmt}" , produces =  TEXT_PLAIN_VALUE  )
    public ResponseEntity<String> settleBalancesA(@PathVariable String buyAmt,@PathVariable String sellAmt) throws IllegalArgumentException {
        System.out.println("request"+Double.valueOf(buyAmt));

        double buyAmount=Double.valueOf(buyAmt);
        double sellAmount=Double.valueOf(sellAmt);
        // Get party objects for myself and the counterparty.

        CordaX500Name partyX500Name = CordaX500Name.parse("O=PartyB, L=New York, C=US");
        System.out.println("partyX500Name..............................."+partyX500Name);
        Party lender = proxy.wellKnownPartyFromX500Name(partyX500Name);
        CordaX500Name partyX500NameB = CordaX500Name.parse("O=PartyA, L=London, C=GB");
        Party borrower = proxy.wellKnownPartyFromX500Name(partyX500NameB);



        //String borrower = request.getParameter("borrower");

        // Get party objects for myself and the counterparty.

//        CordaX500Name partyX500Name = CordaX500Name.parse(party);
//        Party otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name);

        // Create a new IOU state using the parameters given.
        try {
            // Start the IOUIssueFlow. We block and waits for the flow to return.
            proxy.startTrackedFlowDynamic(SettleBalancesForA.class,buyAmount,lender,sellAmount,borrower).getReturnValue().get();
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Balances settled for Party A ");
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
    @PostMapping (value = "settleBalancesB/{buyAmt}/{sellAmt}" , produces =  TEXT_PLAIN_VALUE  )
    public ResponseEntity<String> settleBalancesB(@PathVariable String buyAmt,@PathVariable String sellAmt) throws IllegalArgumentException {
        System.out.println("request"+Double.valueOf(buyAmt));

        double buyAmount=Double.valueOf(buyAmt);
        double sellAmount=Double.valueOf(sellAmt);
        // Get party objects for myself and the counterparty.

        CordaX500Name partyX500Name = CordaX500Name.parse("O=PartyB, L=New York, C=US");
        System.out.println("partyX500Name..............................."+partyX500Name);
        Party lender = proxy.wellKnownPartyFromX500Name(partyX500Name);
        CordaX500Name partyX500NameB = CordaX500Name.parse("O=PartyA, L=London, C=GB");
        Party borrower = proxy.wellKnownPartyFromX500Name(partyX500NameB);



        //String borrower = request.getParameter("borrower");

        // Get party objects for myself and the counterparty.

//        CordaX500Name partyX500Name = CordaX500Name.parse(party);
//        Party otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name);

        // Create a new IOU state using the parameters given.
        try {
            // Start the IOUIssueFlow. We block and waits for the flow to return.
            proxy.startTrackedFlowDynamic(SettleBalancesForB.class,buyAmount,lender,sellAmount,borrower).getReturnValue().get();
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Balances settled for Party B.\n ");
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping (value = "nettingForB" , produces =  TEXT_PLAIN_VALUE  )
    public ResponseEntity<String> nettingForB() throws IllegalArgumentException {

        // Get party objects for myself and the counterparty.

        CordaX500Name partyX500Name = CordaX500Name.parse("O=PartyB, L=New York, C=US");
        System.out.println("partyX500Name..............................."+partyX500Name);
        Party lender = proxy.wellKnownPartyFromX500Name(partyX500Name);
        CordaX500Name partyX500NameB = CordaX500Name.parse("O=PartyA, L=London, C=GB");
        Party borrower = proxy.wellKnownPartyFromX500Name(partyX500NameB);



        //String borrower = request.getParameter("borrower");

        // Get party objects for myself and the counterparty.

//        CordaX500Name partyX500Name = CordaX500Name.parse(party);
//        Party otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name);

        // Create a new IOU state using the parameters given.
        try {
            // Start the IOUIssueFlow. We block and waits for the flow to return.
            proxy.startTrackedFlowDynamic(NettingFlowB.class,lender,borrower).getReturnValue().get();
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Netting done for Party B.\n ");
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
    @PostMapping (value = "nettingForA" , produces =  TEXT_PLAIN_VALUE  )
    public ResponseEntity<String> nettingForA() throws IllegalArgumentException {

        // Get party objects for myself and the counterparty.

        CordaX500Name partyX500Name = CordaX500Name.parse("O=PartyB, L=New York, C=US");
        System.out.println("partyX500Name..............................."+partyX500Name);
        Party lender = proxy.wellKnownPartyFromX500Name(partyX500Name);
        CordaX500Name partyX500NameB = CordaX500Name.parse("O=PartyA, L=London, C=GB");
        Party borrower = proxy.wellKnownPartyFromX500Name(partyX500NameB);



        //String borrower = request.getParameter("borrower");

        // Get party objects for myself and the counterparty.

//        CordaX500Name partyX500Name = CordaX500Name.parse(party);
//        Party otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name);

        // Create a new IOU state using the parameters given.
        try {
            // Start the IOUIssueFlow. We block and waits for the flow to return.
            proxy.startTrackedFlowDynamic(NettingFlowA.class,lender,borrower).getReturnValue().get();
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Netting done for Party A.\n ");
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping (value = "accept/{id}" , produces =  TEXT_PLAIN_VALUE  )
    public ResponseEntity<String> nettingForB(@PathVariable UniqueIdentifier id) throws IllegalArgumentException {

        // Get party objects for myself and the counterparty.

        CordaX500Name partyX500Name = CordaX500Name.parse("O=PartyB, L=New York, C=US");
        System.out.println("partyX500Name..............................."+partyX500Name);
        Party lender = proxy.wellKnownPartyFromX500Name(partyX500Name);
        CordaX500Name partyX500NameB = CordaX500Name.parse("O=PartyA, L=London, C=GB");
        Party borrower = proxy.wellKnownPartyFromX500Name(partyX500NameB);

        try {
            // Start the IOUIssueFlow. We block and waits for the flow to return.
            proxy.startTrackedFlowDynamic(AcceptanceFlow.Initiator.class,id).getReturnValue().get();
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Proposal Accepted\n ");
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping (value = "modify/{id}/{spot}/{amt}" , produces =  TEXT_PLAIN_VALUE  )
    public ResponseEntity<String> modify(@PathVariable UniqueIdentifier id,@PathVariable String spot,@PathVariable String amt) throws IllegalArgumentException {
        double spotRate=Double.valueOf(spot);
        double buyAmt=Double.valueOf(amt);
        // Get party objects for myself and the counterparty.


        try {
            // Start the IOUIssueFlow. We block and waits for the flow to return.
            proxy.startTrackedFlowDynamic(ModificationFlow.Initiator.class,id,spotRate,buyAmt).getReturnValue().get();
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Proposal Accepted\n ");
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping (value = "proposal" , produces =  TEXT_PLAIN_VALUE  )
    public ResponseEntity<String> propose(@RequestBody TradeState req) throws IllegalArgumentException {

        // Get party objects for myself and the counterparty.
        System.out.println("request body"+req.toString());

        try {
            CordaX500Name partyX500Name = CordaX500Name.parse("O=PartyB, L=New York, C=US");
            System.out.println("partyX500Name..............................."+partyX500Name);
            Party buyer = proxy.wellKnownPartyFromX500Name(partyX500Name);
            // Start the IOUIssueFlow. We block and waits for the flow to return.
            proxy.startTrackedFlowDynamic(ProposalFlow.Initiator.class,req.getBuyer(),req.getSeller(), req.getBuyCurrency(),  req.getBuyAmount(),  req.getSellCurrency(),  req.getSellAmount(),  req.getTradeId(),  req.getSettlmentDate(),req.getSpotRate()).getReturnValue().get();

            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Proposal Accepted\n ");
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
