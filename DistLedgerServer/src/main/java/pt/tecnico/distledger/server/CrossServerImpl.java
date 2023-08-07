package pt.tecnico.distledger.server;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import static io.grpc.Status.INVALID_ARGUMENT;

public class CrossServerImpl extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {

    private final ServerState serverState;
    private int prev;

    public CrossServerImpl(ServerState serverState) {
        this.serverState = serverState;
    }

    @Override
    public void propagateState(CrossServerDistLedger.PropagateStateRequest request, StreamObserver<CrossServerDistLedger.PropagateStateResponse> responseObserver) {
        DistLedgerCommonDefinitions.LedgerState ledgerState = request.getState();
        this.prev = request.getReplicaTS(0);
        DistLedgerCommonDefinitions.Operation operation = ledgerState.getLedger(0);

        if (serverState.isServerOff()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("UNAVAILABLE").asRuntimeException());
            return;
        }
        serverState.addOperation(operation);

        responseObserver.onNext(CrossServerDistLedger.PropagateStateResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
