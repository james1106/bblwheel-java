package com.hemayun.sandbox.bblwheel.SimpleService;

import com.google.protobuf.ByteString;
import com.hemayun.bblwheel.RpcGrpc;
import com.hemayun.bblwheel.RpcOuterClass;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;

/**
 * Created by apple on 16/12/2.
 */
public class SimpleService extends RpcGrpc.RpcImplBase {

    @Override
    public void call(RpcOuterClass.Request request, StreamObserver<RpcOuterClass.Response> responseObserver) {
        System.out.println("call "+request);
        RpcOuterClass.Response resp = RpcOuterClass.Response.newBuilder()
                .setID(request.getID())
                .setClientID(request.getClintID())
                .setTimestamp(System.currentTimeMillis())
                .setContent(ByteString.copyFromUtf8("content"))
                .setStatus(200)
                .setStatusText("OK")
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}
