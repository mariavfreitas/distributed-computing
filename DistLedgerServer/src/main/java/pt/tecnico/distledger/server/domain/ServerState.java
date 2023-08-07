package pt.tecnico.distledger.server.domain;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.server.NamingServerService;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerState {
    private List<Operation> ledger;
    ConcurrentHashMap<String, Integer> users = new ConcurrentHashMap<>();
    private boolean isServerOn;
    private static String serviceName = "DistLedger";
    DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub; /*stub  e host para o B*/
    ManagedChannel channel;

    public ServerState() {
        this.ledger = new ArrayList<>();
        this.isServerOn = true;
        users.put("broker", 1000);
    }

    public synchronized void createAccount(String userId) {
        /**/
        List<DistLedgerCommonDefinitions.Operation> op = new ArrayList<>();
        DistLedgerCommonDefinitions.LedgerState.Builder ledgerAux = DistLedgerCommonDefinitions.LedgerState.newBuilder();
        DistLedgerCommonDefinitions.Operation.Builder operation = DistLedgerCommonDefinitions.Operation.newBuilder();
        operation.setType(DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT).setUserId(userId);
        op.add(operation.build());
        DistLedgerCommonDefinitions.LedgerState ledgerState = ledgerAux.addAllLedger(op).build();
        CrossServerDistLedger.PropagateStateRequest request = CrossServerDistLedger.PropagateStateRequest.newBuilder().setState(ledgerState).build();
        /*lookup para descobrir o porto do B para fazer o propagate para la*/
        List<String> hostPort = NamingServerService.lookup(serviceName, "B");
        channel = ManagedChannelBuilder.forTarget(hostPort.get(0)).usePlaintext().build();
        stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);
        try {
            stub.propagateState(request);
        }
        catch (StatusRuntimeException e) {
            throw e;
        }

        users.put(userId, 0);
        CreateOp createOp = new CreateOp(userId);
        ledger.add(createOp);
    }

    // deleteAccount not supported for phase 3
    /*public synchronized void deleteAccount(String userId) {
        List<DistLedgerCommonDefinitions.Operation> op = new ArrayList<>();
        DistLedgerCommonDefinitions.LedgerState.Builder ledgerAux = DistLedgerCommonDefinitions.LedgerState.newBuilder();
        DistLedgerCommonDefinitions.Operation.Builder operation = DistLedgerCommonDefinitions.Operation.newBuilder();
        operation.setType(DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT).setUserId(userId);
        op.add(operation.build());
        DistLedgerCommonDefinitions.LedgerState ledgerState = ledgerAux.addAllLedger(op).build();
        CrossServerDistLedger.PropagateStateRequest request = CrossServerDistLedger.PropagateStateRequest.newBuilder().setState(ledgerState).build();
        List<String> hostPort = NamingServerService.lookup(serviceName, "B");
        channel = ManagedChannelBuilder.forTarget(hostPort.get(0)).usePlaintext().build();
        stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);
        try {
            stub.propagateState(request);
        }
        catch (StatusRuntimeException e) {
            throw e;
        }

        users.remove(userId);
        DeleteOp deleteOp = new DeleteOp(userId);
        ledger.add(deleteOp);
    }*/

    public synchronized List<Operation> getLedger() {
        return new ArrayList<>(ledger);
    }

    public synchronized boolean isUser(String userId) {
        return users.containsKey(userId);
    }

    public synchronized Integer getBalance(String userId) {
        return users.get(userId);
    }

    public synchronized void setBalance(String userId, Integer amount) {
        if (getBalance(userId) != null)
            users.computeIfPresent(userId, (key, val) -> val + amount);
    }

    public synchronized void transferTo(String accountFrom, String accountTo, Integer amount, Integer amountToWithdraw) {
        List<DistLedgerCommonDefinitions.Operation> op = new ArrayList<>();
        DistLedgerCommonDefinitions.LedgerState.Builder ledgerAux = DistLedgerCommonDefinitions.LedgerState.newBuilder();
        DistLedgerCommonDefinitions.Operation.Builder operation = DistLedgerCommonDefinitions.Operation.newBuilder();
        operation.setType(DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO).setUserId(accountFrom).setDestUserId(accountTo).setAmount(amount);
        op.add(operation.build());
        DistLedgerCommonDefinitions.LedgerState ledgerState = ledgerAux.addAllLedger(op).build();
        CrossServerDistLedger.PropagateStateRequest request = CrossServerDistLedger.PropagateStateRequest.newBuilder().setState(ledgerState).build();
        List<String> hostPort = NamingServerService.lookup(serviceName, "B");
        channel = ManagedChannelBuilder.forTarget(hostPort.get(0)).usePlaintext().build();
        stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);
        try {
            stub.propagateState(request);
        }
        catch (StatusRuntimeException e) {
            throw e;
        }

        setBalance(accountFrom, amountToWithdraw);
        setBalance(accountTo, amount);
        TransferOp transferOp = new TransferOp(accountFrom, accountTo, amount);
        ledger.add(transferOp);
    }

    public synchronized void addOperation(DistLedgerCommonDefinitions.Operation operation) {
        if (operation.getType().equals(DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT)) {
            Operation op = new CreateOp(operation.getUserId());
            ledger.add(op);
        }
        else if (operation.getType().equals(DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT)) {
            Operation op = new DeleteOp(operation.getUserId());
            ledger.add(op);
        }
        else if (operation.getType().equals(DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO)) {
            Operation op = new TransferOp(operation.getUserId(), operation.getDestUserId(), operation.getAmount());
            ledger.add(op);
        }
    }

    // Admin
    public boolean isServerOff() {
        return !isServerOn;
    }

    public synchronized void setServerOn() {
        isServerOn = true;
    }

    public synchronized void setServerOff() {
        isServerOn = false;
    }
}