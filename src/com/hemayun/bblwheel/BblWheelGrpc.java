package com.hemayun.bblwheel;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.0.0)",
    comments = "Source: bblwheel.proto")
public class BblWheelGrpc {

  private BblWheelGrpc() {}

  public static final String SERVICE_NAME = "bblwheel.BblWheel";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.hemayun.bblwheel.Bblwheel.Service,
      com.hemayun.bblwheel.Bblwheel.Event> METHOD_REGISTER =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING,
          generateFullMethodName(
              "bblwheel.BblWheel", "Register"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.hemayun.bblwheel.Bblwheel.Service.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.hemayun.bblwheel.Bblwheel.Event.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.hemayun.bblwheel.Bblwheel.Service,
      com.hemayun.bblwheel.Bblwheel.Void> METHOD_UNREGISTER =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "bblwheel.BblWheel", "Unregister"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.hemayun.bblwheel.Bblwheel.Service.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.hemayun.bblwheel.Bblwheel.Void.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.hemayun.bblwheel.Bblwheel.Service,
      com.hemayun.bblwheel.Bblwheel.Void> METHOD_ONLINE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "bblwheel.BblWheel", "Online"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.hemayun.bblwheel.Bblwheel.Service.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.hemayun.bblwheel.Bblwheel.Void.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.hemayun.bblwheel.Bblwheel.Service,
      com.hemayun.bblwheel.Bblwheel.Void> METHOD_UPDATE_STATUS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "bblwheel.BblWheel", "UpdateStatus"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.hemayun.bblwheel.Bblwheel.Service.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.hemayun.bblwheel.Bblwheel.Void.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.hemayun.bblwheel.Bblwheel.UpdateConfigReq,
      com.hemayun.bblwheel.Bblwheel.Void> METHOD_UPDATE_CONFIG =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "bblwheel.BblWheel", "UpdateConfig"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.hemayun.bblwheel.Bblwheel.UpdateConfigReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.hemayun.bblwheel.Bblwheel.Void.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BblWheelStub newStub(io.grpc.Channel channel) {
    return new BblWheelStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BblWheelBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new BblWheelBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static BblWheelFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new BblWheelFutureStub(channel);
  }

  /**
   */
  public static abstract class BblWheelImplBase implements io.grpc.BindableService {

    /**
     */
    public void register(com.hemayun.bblwheel.Bblwheel.Service request,
        io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Event> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_REGISTER, responseObserver);
    }

    /**
     */
    public void unregister(com.hemayun.bblwheel.Bblwheel.Service request,
        io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Void> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_UNREGISTER, responseObserver);
    }

    /**
     */
    public void online(com.hemayun.bblwheel.Bblwheel.Service request,
        io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Void> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ONLINE, responseObserver);
    }

    /**
     */
    public void updateStatus(com.hemayun.bblwheel.Bblwheel.Service request,
        io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Void> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_UPDATE_STATUS, responseObserver);
    }

    /**
     */
    public void updateConfig(com.hemayun.bblwheel.Bblwheel.UpdateConfigReq request,
        io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Void> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_UPDATE_CONFIG, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_REGISTER,
            asyncServerStreamingCall(
              new MethodHandlers<
                com.hemayun.bblwheel.Bblwheel.Service,
                com.hemayun.bblwheel.Bblwheel.Event>(
                  this, METHODID_REGISTER)))
          .addMethod(
            METHOD_UNREGISTER,
            asyncUnaryCall(
              new MethodHandlers<
                com.hemayun.bblwheel.Bblwheel.Service,
                com.hemayun.bblwheel.Bblwheel.Void>(
                  this, METHODID_UNREGISTER)))
          .addMethod(
            METHOD_ONLINE,
            asyncUnaryCall(
              new MethodHandlers<
                com.hemayun.bblwheel.Bblwheel.Service,
                com.hemayun.bblwheel.Bblwheel.Void>(
                  this, METHODID_ONLINE)))
          .addMethod(
            METHOD_UPDATE_STATUS,
            asyncUnaryCall(
              new MethodHandlers<
                com.hemayun.bblwheel.Bblwheel.Service,
                com.hemayun.bblwheel.Bblwheel.Void>(
                  this, METHODID_UPDATE_STATUS)))
          .addMethod(
            METHOD_UPDATE_CONFIG,
            asyncUnaryCall(
              new MethodHandlers<
                com.hemayun.bblwheel.Bblwheel.UpdateConfigReq,
                com.hemayun.bblwheel.Bblwheel.Void>(
                  this, METHODID_UPDATE_CONFIG)))
          .build();
    }
  }

  /**
   */
  public static final class BblWheelStub extends io.grpc.stub.AbstractStub<BblWheelStub> {
    private BblWheelStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BblWheelStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BblWheelStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BblWheelStub(channel, callOptions);
    }

    /**
     */
    public void register(com.hemayun.bblwheel.Bblwheel.Service request,
        io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Event> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(METHOD_REGISTER, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void unregister(com.hemayun.bblwheel.Bblwheel.Service request,
        io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Void> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_UNREGISTER, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void online(com.hemayun.bblwheel.Bblwheel.Service request,
        io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Void> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ONLINE, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateStatus(com.hemayun.bblwheel.Bblwheel.Service request,
        io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Void> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_UPDATE_STATUS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateConfig(com.hemayun.bblwheel.Bblwheel.UpdateConfigReq request,
        io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Void> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_UPDATE_CONFIG, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class BblWheelBlockingStub extends io.grpc.stub.AbstractStub<BblWheelBlockingStub> {
    private BblWheelBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BblWheelBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BblWheelBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BblWheelBlockingStub(channel, callOptions);
    }

    /**
     */
    public java.util.Iterator<com.hemayun.bblwheel.Bblwheel.Event> register(
        com.hemayun.bblwheel.Bblwheel.Service request) {
      return blockingServerStreamingCall(
          getChannel(), METHOD_REGISTER, getCallOptions(), request);
    }

    /**
     */
    public com.hemayun.bblwheel.Bblwheel.Void unregister(com.hemayun.bblwheel.Bblwheel.Service request) {
      return blockingUnaryCall(
          getChannel(), METHOD_UNREGISTER, getCallOptions(), request);
    }

    /**
     */
    public com.hemayun.bblwheel.Bblwheel.Void online(com.hemayun.bblwheel.Bblwheel.Service request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ONLINE, getCallOptions(), request);
    }

    /**
     */
    public com.hemayun.bblwheel.Bblwheel.Void updateStatus(com.hemayun.bblwheel.Bblwheel.Service request) {
      return blockingUnaryCall(
          getChannel(), METHOD_UPDATE_STATUS, getCallOptions(), request);
    }

    /**
     */
    public com.hemayun.bblwheel.Bblwheel.Void updateConfig(com.hemayun.bblwheel.Bblwheel.UpdateConfigReq request) {
      return blockingUnaryCall(
          getChannel(), METHOD_UPDATE_CONFIG, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class BblWheelFutureStub extends io.grpc.stub.AbstractStub<BblWheelFutureStub> {
    private BblWheelFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BblWheelFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BblWheelFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BblWheelFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.hemayun.bblwheel.Bblwheel.Void> unregister(
        com.hemayun.bblwheel.Bblwheel.Service request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_UNREGISTER, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.hemayun.bblwheel.Bblwheel.Void> online(
        com.hemayun.bblwheel.Bblwheel.Service request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ONLINE, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.hemayun.bblwheel.Bblwheel.Void> updateStatus(
        com.hemayun.bblwheel.Bblwheel.Service request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_UPDATE_STATUS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.hemayun.bblwheel.Bblwheel.Void> updateConfig(
        com.hemayun.bblwheel.Bblwheel.UpdateConfigReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_UPDATE_CONFIG, getCallOptions()), request);
    }
  }

  private static final int METHODID_REGISTER = 0;
  private static final int METHODID_UNREGISTER = 1;
  private static final int METHODID_ONLINE = 2;
  private static final int METHODID_UPDATE_STATUS = 3;
  private static final int METHODID_UPDATE_CONFIG = 4;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final BblWheelImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(BblWheelImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REGISTER:
          serviceImpl.register((com.hemayun.bblwheel.Bblwheel.Service) request,
              (io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Event>) responseObserver);
          break;
        case METHODID_UNREGISTER:
          serviceImpl.unregister((com.hemayun.bblwheel.Bblwheel.Service) request,
              (io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Void>) responseObserver);
          break;
        case METHODID_ONLINE:
          serviceImpl.online((com.hemayun.bblwheel.Bblwheel.Service) request,
              (io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Void>) responseObserver);
          break;
        case METHODID_UPDATE_STATUS:
          serviceImpl.updateStatus((com.hemayun.bblwheel.Bblwheel.Service) request,
              (io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Void>) responseObserver);
          break;
        case METHODID_UPDATE_CONFIG:
          serviceImpl.updateConfig((com.hemayun.bblwheel.Bblwheel.UpdateConfigReq) request,
              (io.grpc.stub.StreamObserver<com.hemayun.bblwheel.Bblwheel.Void>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    return new io.grpc.ServiceDescriptor(SERVICE_NAME,
        METHOD_REGISTER,
        METHOD_UNREGISTER,
        METHOD_ONLINE,
        METHOD_UPDATE_STATUS,
        METHOD_UPDATE_CONFIG);
  }

}
