package com.hemayun.sandbox.bblwheel;

import com.hemayun.bblwheel.BblWheelGrpc;
import com.hemayun.bblwheel.Bblwheel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import sun.misc.ThreadGroupUtils;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by apple on 16/12/2.
 */
public class BblwheelClient {

    public static final String BBLWHEEL_ENDPOINTS = "BBLWHEEL_ENDPOINTS";
    public static final String DEFAULT_BBLWHEEL_ENDPOINTS = "127.0.0.1:23790";
    private static int DEFAULT_KEEPALIVE_TTL = 30;
    private final ManagedChannelBuilder<?> channelBuilder;
    private ManagedChannel channel;
    private BblWheelGrpc.BblWheelStub stub;
    private BblWheelGrpc.BblWheelBlockingStub blockStub;
    private final Once once = new Once();
    private final Once ronce = new Once();
    private ServiceInstance provider;
    private volatile boolean run;

    private static String getEnv(String key, String def) {
        String v = System.getenv(key);
        if (v == null || "".equals(v.trim())) return System.getProperty(key, def);
        return v;
    }

    public static BblwheelClient newClient() {
        String endpoints[] = getEnv(BBLWHEEL_ENDPOINTS, DEFAULT_BBLWHEEL_ENDPOINTS).split("[,，、 ]");
        if (endpoints.length == 0) throw new RuntimeException("ENV BBLWHEEL_ENDPOINTS NOT FOUND");
        return new BblwheelClient(endpoints);
    }

    private BblwheelClient(String[] endpoints) {
        this(ManagedChannelBuilder.forAddress(
                endpoints[0].split(":", 2)[0],
                Integer.parseInt(endpoints[0].split(":", 2)[1])
        ).usePlaintext(true));
    }

    private BblwheelClient(ManagedChannelBuilder<?> channelBuilder) {
        this.channelBuilder = channelBuilder;
        channel = channelBuilder.build();
        blockStub = BblWheelGrpc.newBlockingStub(channel);
        stub = BblWheelGrpc.newStub(channel);

    }

    private Bblwheel.RegisterResult result;

    public Bblwheel.RegisterResult register(ServiceInstance provider) {
        this.provider = provider;
        while (true) {
            try {
                result = blockStub.register(provider.getService());
                break;
            } catch (Throwable e) {
                e.printStackTrace();

            }
            try {
                TimeUnit.SECONDS.sleep(5);
                if (channel != null) {
                    channel.shutdownNow();
                }
                channel = channelBuilder.build();
                blockStub = BblWheelGrpc.newBlockingStub(channel);
                stub = BblWheelGrpc.newStub(channel);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return Bblwheel.RegisterResult.newBuilder().setDesc(e.getMessage()).build();
            }
        }
        ronce.once(new Runnable() {
            @Override
            public void run() {
                if ("SUCCESS".equalsIgnoreCase(result.getDesc())) {
                    run = true;
                    Thread keepalive = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (run) {
                                try {
                                    keepAlive(provider);
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                                try {
                                    TimeUnit.SECONDS.sleep(5);
                                    while (true) {
                                        try {
                                            Bblwheel.RegisterResult result = blockStub.register(provider.getService());
                                            if ("SUCCESS".equalsIgnoreCase(result.getDesc())) {
                                                break;
                                            } else {
                                                TimeUnit.SECONDS.sleep(3);
                                                continue;
                                            }
                                        } catch (Throwable e) {
                                            e.printStackTrace();
                                        }
                                        TimeUnit.SECONDS.sleep(3);
                                        if (channel != null) {
                                            channel.shutdownNow();
                                        }
                                        channel = channelBuilder.build();
                                        blockStub = BblWheelGrpc.newBlockingStub(channel);
                                        stub = BblWheelGrpc.newStub(channel);
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    return;
                                }
                            }
                        }
                    });
                    keepalive.setName("keepalive-" + provider.getService().getName() + "-" + provider.getService().getID());
                    //keepalive.setDaemon(true);
                    keepalive.start();

                }
            }
        });
        return result;
    }

    public void keepAlive(ServiceInstance provider) {
        StreamObserver<Bblwheel.Event> streamObserver = new StreamObserver<Bblwheel.Event>() {
            @Override
            public void onNext(Bblwheel.Event event) {
                ServiceInstance.BblwheelListener ll = provider.getBblwheelListener();
                switch (event.getType()) {
                    case DISCOVERY:
                        if (ll != null) {
                            ll.onDiscovery(event.getService());
                        }
                        break;
                    case CONFIGUPDATE:
                        if (ll != null && event.getItem() != null) {
                            ll.onConfigUpdated(event.getItem().getKey(), event.getItem().getValue());
                        }
                        break;
                    case CONTROL:
                        if (ll != null && event.getItem() != null) {
                            ll.onControl(event.getCommand());
                        }
                        break;
                    case EXEC:
                        if (ll != null && event.getItem() != null) {
                            ll.onExec(event.getCommand());
                        }
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
                System.out.println("Event Stream onCompleted");
            }
        };
        provider.getService().toBuilder().setStatus(Bblwheel.Service.Status.INIT).build();
        StreamObserver<Bblwheel.Event> streamReq = stub.events(streamObserver);
        while (run) {
            if (channel.isShutdown() || channel.isTerminated()) {
                return;
            }
            Runtime rt = Runtime.getRuntime();
            long freeMem = rt.freeMemory();
            Bblwheel.Stats stats = provider.getService().getStats().toBuilder()
                    .setFreeMem(freeMem / 1024)
                    .setUsedMem((rt.totalMemory() - freeMem) / 1024)
                    .setUpTime(ManagementFactory.getRuntimeMXBean().getUptime() / 1000)
                    .setLastActiveTime(System.currentTimeMillis())
                    .setThreads(ThreadGroupUtils.getRootThreadGroup().activeCount())
                    .putAllOther(provider.getStats())
                    .build();
            Bblwheel.Service srv = provider.getService().toBuilder().setStats(stats).build();
            provider.service=srv;
            streamReq.onNext(Bblwheel.Event.newBuilder()
                    .setType(Bblwheel.Event.EventType.KEEPALIVE)
                    .setService(srv)
                    .build());
            try {
                TimeUnit.SECONDS.sleep(DEFAULT_KEEPALIVE_TTL - 10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public void unregister() {
        if (provider != null) {
            provider.getService().toBuilder().setStatus(Bblwheel.Service.Status.OFFLINE).build();
            blockStub.unregister(provider.getService());
        }

    }

    public Map<String, Bblwheel.Config> lookupConfig(String[] deps) {
        Bblwheel.LookupConfigReq req = Bblwheel.LookupConfigReq.getDefaultInstance();
        for (String n : deps) {
            req.toBuilder().addDependentConfigs(n);
        }
        req = req.toBuilder().build();
        return blockStub.lookupConfig(req).getConfigsMap();
    }

    public List<Bblwheel.Service> lookService(String[] deps) {
        Bblwheel.LookupServiceReq req = Bblwheel.LookupServiceReq.getDefaultInstance();
        for (String n : deps) {
            req.toBuilder().addDependentServices(n);
        }
        req = req.toBuilder().build();
        return blockStub.lookupService(req).getServicesList();
    }

    public void updateConfig(Bblwheel.Config conf) {
        Bblwheel.UpdateConfigReq req = Bblwheel.UpdateConfigReq
                .newBuilder()
                .setServiceID(provider.getService().getID())
                .setServiceName(provider.getService().getName())
                .setConfig(conf)
                .build();
        blockStub.updateConfig(req);
    }

    public void shutdown() {
        once.once(new Runnable() {
            @Override
            public void run() {
                run = false;
                if (channel != null) {
                    channel.shutdownNow();
                }
            }
        });
    }

//    synchronized final ManagedChannel connect(List<String> endpoints) {
//        return ManagedChannelBuilder.forTarget("etcd")
//                .nameResolverFactory(new SimpleEtcdNameResolverFactory(
//                        endpoints.stream()
//                                .map(BblwheelClient::endpointToUri)
//                                .collect(Collectors.toList())
//                ))
//                .usePlaintext(true).build();
////        return channel;
//    }
//
//    static URI endpointToUri(String endpoint) {
//        try {
//            if (!endpoint.startsWith("http://")) {
//                endpoint = "http://" + endpoint;
//            }
//            return new URI(endpoint);
//        } catch (URISyntaxException e) {
//            throw new IllegalArgumentException(e);
//        }
//    }
}
