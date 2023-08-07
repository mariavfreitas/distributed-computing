package pt.tecnico.distledger.server;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;

import java.util.ArrayList;
import java.util.List;

import static io.grpc.Status.INVALID_ARGUMENT;

public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {

    private final ServerState serverState;
    private String qualifier;
    VectorClock prev;

    public AdminServiceImpl(ServerState serverState, String qualifier) {
        this.serverState = serverState;
        this.qualifier = qualifier;
    }

    @Override
    public void activate(AdminDistLedger.ActivateRequest request, StreamObserver<AdminDistLedger.ActivateResponse> responseObserver) {
        serverState.setServerOn();

        responseObserver.onNext(AdminDistLedger.ActivateResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deactivate(AdminDistLedger.DeactivateRequest request, StreamObserver<AdminDistLedger.DeactivateResponse> responseObserver) {
        serverState.setServerOff();

        responseObserver.onNext(AdminDistLedger.DeactivateResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    /* This method iterates through the operations list in serverState,
     * associates it with each operation type defined in proto, builds
     * and saves it to a new operations list */
    public DistLedgerCommonDefinitions.LedgerState convertOpToProto() {
        List<DistLedgerCommonDefinitions.Operation> operationList = new ArrayList<>();
        List<Operation> serverStateList = serverState.getLedger();
        DistLedgerCommonDefinitions.LedgerState ledgerState;

        for (Operation operation : serverStateList) {
            if (operation.getClass().equals(CreateOp.class)) {
                DistLedgerCommonDefinitions.Operation op = DistLedgerCommonDefinitions.Operation.newBuilder()
                        .setType(DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT)
                        .setUserId(operation.getAccount())
                        .build();
                operationList.add(op);
            }

            else if (operation.getClass().equals(DeleteOp.class)) {
                DistLedgerCommonDefinitions.Operation op = DistLedgerCommonDefinitions.Operation.newBuilder()
                        .setType(DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT)
                        .setUserId(operation.getAccount())
                        .build();
                operationList.add(op);
            }

            else if (operation.getClass().equals(TransferOp.class)) {
                DistLedgerCommonDefinitions.Operation op = DistLedgerCommonDefinitions.Operation.newBuilder()
                        .setType(DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO)
                        .setUserId(operation.getAccount())
                        .setDestUserId(((TransferOp) operation).getDestAccount())
                        .setAmount(((TransferOp) operation).getAmount())
                        .build();
                operationList.add(op);
            }

            else {
                DistLedgerCommonDefinitions.Operation op = DistLedgerCommonDefinitions.Operation.newBuilder()
                        .setType(DistLedgerCommonDefinitions.OperationType.OP_UNSPECIFIED)
                        .build();
            }
        }
        ledgerState = DistLedgerCommonDefinitions.LedgerState.newBuilder().addAllLedger(operationList).build();
        return ledgerState;
    }


    @Override
    public void getLedgerState(AdminDistLedger.getLedgerStateRequest request, StreamObserver<AdminDistLedger.getLedgerStateResponse> responseObserver) {

        if (serverState.isServerOff())
            responseObserver.onError(INVALID_ARGUMENT.withDescription("UNAVAILABLE").asRuntimeException());
        else {
            AdminDistLedger.getLedgerStateResponse response = AdminDistLedger
                    .getLedgerStateResponse
                    .newBuilder()
                    .setLedgerState(convertOpToProto())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public synchronized void gossip(AdminDistLedger.GossipRequest request, StreamObserver<AdminDistLedger.GossipResponse> responseObserver) {
        if (serverState.isServerOff())
            responseObserver.onError(INVALID_ARGUMENT.withDescription("UNAVAILABLE").asRuntimeException());
        else {
            // FAZER PROPAGATE //TODO
            responseObserver.onNext(AdminDistLedger.GossipResponse.getDefaultInstance());
            responseObserver.onCompleted();
        }
    }
}
