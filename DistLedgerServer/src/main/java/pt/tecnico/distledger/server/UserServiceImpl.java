package pt.tecnico.distledger.server;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import static io.grpc.Status.INVALID_ARGUMENT;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private final ServerState serverState;
    private final String qualifier;
    private int prev;

    public UserServiceImpl(ServerState serverState, String qualifier) {
        this.serverState = serverState;
        this.qualifier = qualifier;
    }

    public boolean isPrimary(String qualifier) {
        return qualifier.equals("A");
    }

    @Override
    public void createAccount(UserDistLedger.CreateAccountRequest request, StreamObserver<UserDistLedger.CreateAccountResponse> responseObserver) {
        String userId = request.getUserId();
        this.prev = request.getPrevTS(0);

        if (isPrimary(qualifier)) {
            if (serverState.isServerOff())
                responseObserver.onError(INVALID_ARGUMENT.withDescription("UNAVAILABLE").asRuntimeException());
            else if (serverState.isUser(userId))
                responseObserver.onError(INVALID_ARGUMENT.withDescription("This user already has an account").asRuntimeException());
            else {
                try {
                    serverState.createAccount(userId);
                }
                catch (StatusRuntimeException e) {
                    responseObserver.onError(INVALID_ARGUMENT.withDescription("Secondary UNAVAILABLE").asRuntimeException());
                    return;
                }
                responseObserver.onNext(UserDistLedger.CreateAccountResponse.newBuilder().setTS(0,0).build());
                responseObserver.onCompleted();
            }
        }
        else
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Server is in secondary mode").asRuntimeException());
    }

    // deleteAccount not supported for phase 3
    /*@Override
    public void deleteAccount(UserDistLedger.DeleteAccountRequest request, StreamObserver<UserDistLedger.DeleteAccountResponse> responseObserver) {
        String userId = request.getUserId();

        if (isPrimary(qualifier)) {
            if (serverState.isServerOff())
                responseObserver.onError(INVALID_ARGUMENT.withDescription("UNAVAILABLE").asRuntimeException());
            else if (!serverState.isUser(userId))
                responseObserver.onError(INVALID_ARGUMENT.withDescription("This user doesn't exist").asRuntimeException());
            else if (serverState.getBalance(userId) != 0)
                responseObserver.onError(INVALID_ARGUMENT.withDescription("Account can't be deleted with balance greater than 0").asRuntimeException());
            else if (userId.equals("broker"))
                responseObserver.onError(INVALID_ARGUMENT.withDescription("Broker account can't be deleted").asRuntimeException());
            else {
                try {
                    serverState.deleteAccount(userId);
                }
                catch (StatusRuntimeException e) {
                    responseObserver.onError(INVALID_ARGUMENT.withDescription("Secondary UNAVAILABLE").asRuntimeException());
                    return;
                }
                responseObserver.onNext(UserDistLedger.DeleteAccountResponse.getDefaultInstance());
                responseObserver.onCompleted();
            }
        }
        else
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Server is in secondary mode").asRuntimeException());
    }*/

    @Override
    public void balance(UserDistLedger.BalanceRequest request, StreamObserver<UserDistLedger.BalanceResponse> responseObserver) {
        String userId = request.getUserId();
        this.prev = request.getPrevTS(0);

        if (serverState.isServerOff())
            responseObserver.onError(INVALID_ARGUMENT.withDescription("UNAVAILABLE").asRuntimeException());
        else if (!serverState.isUser(userId))
            responseObserver.onError(INVALID_ARGUMENT.withDescription("This user doesn't exist").asRuntimeException());
        else {
            Integer balance = serverState.getBalance(userId);

            responseObserver.onNext(UserDistLedger.BalanceResponse.newBuilder().setValue(balance).setValueTS(0,0).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void transferTo(UserDistLedger.TransferToRequest request, StreamObserver<UserDistLedger.TransferToResponse> responseObserver) {
        String accountFrom = request.getAccountFrom();
        String accountTo = request.getAccountTo();
        Integer amount = request.getAmount();
        Integer amountToWithdraw = -request.getAmount();
        this.prev = request.getPrevTS(0);

        if (isPrimary(qualifier)) {
            if (serverState.isServerOff())
                responseObserver.onError(INVALID_ARGUMENT.withDescription("UNAVAILABLE").asRuntimeException());
            else if (!serverState.isUser(accountFrom) || !(serverState.getBalance(accountFrom) >= amount) || !serverState.isUser(accountTo))
                responseObserver.onError(INVALID_ARGUMENT
                        .withDescription("Input has to be valid users and balance greater than amount to transfer").asRuntimeException());
            else if (amount <= 0)
                responseObserver.onError(INVALID_ARGUMENT.withDescription("Amount to transfer has to be a positive value").asRuntimeException());
            else if (accountFrom.equals(accountTo))
                responseObserver.onError(INVALID_ARGUMENT.withDescription("Source account can't be the same as destination account").asRuntimeException());
            else {
                try {
                    serverState.transferTo(accountFrom, accountTo, amount, amountToWithdraw);
                }
                catch (StatusRuntimeException e) {
                    responseObserver.onError(INVALID_ARGUMENT.withDescription("Secondary UNAVAILABLE").asRuntimeException());
                    return;
                }
                responseObserver.onNext(UserDistLedger.TransferToResponse.newBuilder().setTS(0,0).build());
                responseObserver.onCompleted();
            }
        }
        else
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Server is in secondary mode").asRuntimeException());
    }
}
