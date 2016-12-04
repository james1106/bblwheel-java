package com.hemayun.sandbox.bblwheel;

import com.hemayun.bblwheel.RpcGrpc;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.NettyServerBuilder;

import java.io.IOException;

/**
 * Created by apple on 16/12/2.
 */
public class RPCServer {

    private Server server;
    private int port;
    public RPCServer(int port) {
        this.port=port;
    }

    public void register(BindableService service){
        server = NettyServerBuilder.forPort(port)
                .addService(service)
                .build();
    }
    public void start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                RPCServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void join() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
