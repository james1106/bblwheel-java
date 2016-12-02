package com.hemayun.sandbox.bblwheel;

import com.coreos.jetcd.resolver.SimpleEtcdNameResolverFactory;
import com.hemayun.bblwheel.BblWheelGrpc;
import com.hemayun.bblwheel.Bblwheel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by apple on 16/12/2.
 */
public class BblwheelClient {
//    public static final String BBLWHEEL_ENDPOINTS = "BBLWHEEL_ENDPOINTS";
//
//    public static final String DEFAULT_BBLWHEEL_ENDPOINTS = "127.0.0.1:23790";
//    private final static String endpoints[] = getEnv(BBLWHEEL_ENDPOINTS, DEFAULT_BBLWHEEL_ENDPOINTS).split("[,，、 ]");
    private final ManagedChannel channel;
    private final BblWheelGrpc.BblWheelStub stub;
    private final BblWheelGrpc.BblWheelBlockingStub blockStub;
    private final Once once = new Once();
    private Bblwheel.Service provider;
//    static {
//        if (endpoints.length == 0) throw new RuntimeException("ENV BBLWHEEL_ENDPOINTS NOT FOUND");
//    }
    private static String getEnv(String key, String def) {
        String v = System.getenv(key);
        if (v == null || "".equals(v.trim())) return System.getProperty(key, def);
        return v;
    }


    public BblwheelClient(String[] endpoints) {
        this(ManagedChannelBuilder.forAddress(
                endpoints[0].split(":",2)[0],
                Integer.parseInt(endpoints[0].split(":",2)[1])
        ).usePlaintext(true));
    }

    public BblwheelClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockStub = BblWheelGrpc.newBlockingStub(channel);
        stub = BblWheelGrpc.newStub(channel);
    }

    public void register(Bblwheel.Service provider) {
        StreamObserver<Bblwheel.Event> streamObserver = new StreamObserver<Bblwheel.Event>() {
            @Override
            public void onNext(Bblwheel.Event event) {

                switch (event.getType()) {
                    case KEEPALIVE:
                        break;
                    case DISCOVERY:
                        break;
                    case REGISTER_RESULT:
                        break;
                    case CONFIGUPDATE:
                        break;
                }

            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                channel.shutdownNow();
                try {
                    channel.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCompleted() {

            }
        };
        stub.register(provider,streamObserver);
        provider= provider.toBuilder().setStatus(Bblwheel.Service.Status.INIT).build();
        this.provider=provider;
    }


    public void unregister() {
        if(provider!=null){
            provider= provider.toBuilder().setStatus(Bblwheel.Service.Status.OFFLINE).build();
            blockStub.unregister(provider);
        }

    }

    public void online() {
        if(provider!=null) {
           provider= provider.toBuilder().setStatus(Bblwheel.Service.Status.ONLINE).build();
            blockStub.online(provider);
        }
    }

    public void updateConfig(Bblwheel.Config conf) {
        Bblwheel.UpdateConfigReq req = Bblwheel.UpdateConfigReq
                .newBuilder()
                .setServiceID(provider.getID())
                .setServiceName(provider.getName())
                .setConfig(conf)
                .build();
        blockStub.updateConfig(req);
    }

    public void updateStats() {
        if (provider!=null){
            blockStub.updateStatus(provider);
        }
    }



    public void shutdown() {
        once.once(new Runnable() {
            @Override
            public void run() {
                if (channel!=null){
                    channel.shutdownNow();
                }
            }
        });
    }

    synchronized final ManagedChannel connect(List<String> endpoints) {
        return ManagedChannelBuilder.forTarget("etcd")
                .nameResolverFactory(new SimpleEtcdNameResolverFactory(
                        endpoints.stream()
                                .map(BblwheelClient::endpointToUri)
                                .collect(Collectors.toList())
                ))
                .usePlaintext(true).build();
//        return channel;
    }

    static URI endpointToUri(String endpoint) {
        try {
            if (!endpoint.startsWith("http://")) {
                endpoint = "http://" + endpoint;
            }
            return new URI(endpoint);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
