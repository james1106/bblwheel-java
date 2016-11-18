package com.hemayun.sandbox.bblwheel.etcd;

import com.coreos.jetcd.api.*;
import com.coreos.jetcd.resolver.SimpleEtcdNameResolverFactory;
import com.google.protobuf.ByteString;
import com.hemayun.sandbox.bblwheel.Service;
import com.hemayun.sandbox.bblwheel.Wheel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by apple on 16/11/14.
 */
public class EtcdWheel extends Wheel implements SignalHandler {
    private static EtcdWheel ourInstance;
    private final String endpoints[] = getEnv(Const.ETCD_ENDPOINTS, Const.DEFAULT_ETCD_ENDPOINTS).split("[,，、 ]");
    private StreamObserver<WatchRequest> watchStream;
    private StreamObserver<LeaseKeepAliveRequest> keepAliveStream;
    private volatile boolean run;

    private EtcdWheel() {
        if (endpoints.length == 0) throw new WheelException("ENV ETCD_ENDPOINTS NOT FOUND");
        //Signal.handle(new Signal("TERM"), this);    // kill -15 common kill
        //Signal.handle(new Signal("INT"), this);    // Ctrl+c
        // Signal.handle(new Signal("SIGKILL"), this);    // kill
        run = true;
    }

    public static synchronized EtcdWheel getInstance() {
        if (ourInstance == null) {
            ourInstance = new EtcdWheel();
        }
        return ourInstance;

    }

    private static String getEnv(String key, String def) {
        String v = System.getenv(key);
        if (v == null || "".equals(v.trim())) return System.getProperty(key, def);
        return v;
    }

    private static ByteString getPrefixEnd(ByteString key) {
        byte[] prefix = key.toByteArray();
        byte[] end = new byte[]{0};
        for (int i = prefix.length - 1; i >= 0; i--) {
            if (prefix[i] < 0xff) {
                prefix[i] = (byte) (prefix[i] + 1);
                end = new byte[i + 1];
                System.arraycopy(prefix, 0, end, 0, end.length);
                break;
            }
        }
        return ByteString.copyFrom(end);
    }

    private void closeChannel() {
        run = false;
        if (watchStream != null) {
            watchStream.onCompleted();
            watchStream = null;
        }
        if (keepAliveStream != null) {
            keepAliveStream.onCompleted();
            keepAliveStream = null;
        }
//        if (channel != null) {
//            channel.shutdownNow();
//            channel = null;
//        }
    }

    @Override
    public void register(Service provider) {

        syncConfig(provider);

        //TODO 1.防止重复注册 2.检查已注册实例数量约束
        Thread keepAliveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (run) {
                    try {
                        registerAndKeeplive(provider);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });
        keepAliveThread.setName("KeepAliveWorker-" + provider.Name + "-" + provider.ID);
        //keepAliveThread.setDaemon(true);
        keepAliveThread.start();

        Thread watchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (run) {
                    try {
                        startWatcher(provider);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });
        watchThread.setName("WatcherWorker-" + provider.Name + "-" + provider.ID);
        //watchThread.setDaemon(true);
        watchThread.start();
    }

    private void notifyEvent(Service provider, Event e) {
        Service.DiscoveryListener dl = provider.getDiscoveryListener();
        Service.ConfigListener cl = provider.getConfigListener();
        if (dl == null && cl == null) return;
        Event.EventType type = e.getType();
        KeyValue kv = e.getKv();
        String key = kv.getKey().toStringUtf8();
        String value = kv.getValue().toStringUtf8();
        String confPrefix = createConfigKey(provider.Name, provider.ID).toStringUtf8();
        if (key.startsWith(Const.SERVICE_REGISTER_PREFIX) && dl != null) {
            try {
                Service srv = Service.fromString(value);
                //TODO 服务id 服务名字和key要对应
                if (srv.ID == null || srv.Name == null) return;
                if (type == Event.EventType.DELETE) {
                    srv.setStatus(Service.Status.OFFLINE);
                }
                dl.onDiscovery(srv);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        } else if (key.startsWith(confPrefix) && cl != null) {
            String name = key.substring(confPrefix.length() + 1);
            if (type == Event.EventType.PUT) {
                cl.onConfigUpdated(new Service.Config.Item(name, value == null ? "" : value));
            } else if (type == Event.EventType.DELETE) {
                cl.onConfigUpdated(new Service.Config.Item(name, ""));
            }
        }
    }

    private void startWatcher(Service provider) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        ManagedChannel ch = connect(Arrays.asList(endpoints));
        WatchGrpc.WatchStub watcher = WatchGrpc.newStub(ch);
        StreamObserver<WatchResponse> respStream = new StreamObserver<WatchResponse>() {
            @Override
            public void onNext(WatchResponse resp) {
                for (Event e : resp.getEventsList()) {
                    if (e.getType() == Event.EventType.DELETE || e.getType() == Event.EventType.PUT) {
                        notifyEvent(provider, e);
                    } else {
                        System.out.println("Warning.Event " + e.getType().name() + " " + e.getKv().getKey().toStringUtf8() + " " + e.getKv().getValue().toStringUtf8());
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                try {
                    ch.shutdownNow();
                    ch.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }

            }

            @Override
            public void onCompleted() {
                latch.countDown();
                System.out.println("WatchResponse onCompleted");
            }
        };

        watchStream = watcher.watch(respStream);
        ByteString watchKey = createConfigKey(provider.Name, provider.ID, "/");
        System.out.println("Watch " + watchKey.toStringUtf8());
        WatchRequest watchConf = WatchRequest
                .newBuilder()
                .setCreateRequest(
                        WatchCreateRequest
                                .newBuilder()
                                .setKey(watchKey)
                                .setRangeEnd(getPrefixEnd(watchKey)))
                .build();

        watchStream.onNext(watchConf);

        for (String name : provider.Dependencies) {
            watchKey = createRegisterKey(name, "/");
            System.out.println("Watch " + watchKey.toStringUtf8());
            WatchRequest watchService = WatchRequest
                    .newBuilder()
                    .setCreateRequest(
                            WatchCreateRequest
                                    .newBuilder()
                                    .setKey(watchKey)
                                    .setRangeEnd(getPrefixEnd(watchKey)))
                    .build();
            // try{
            watchStream.onNext(watchService);
//            } catch (Throwable e) {
//                e.printStackTrace();
//                if (latch.getCount() != 0) {
//                    latch.countDown();
//                }
//            }
        }
        List<Service> list = lookupService(provider.Dependencies);
        Service.DiscoveryListener dl = provider.getDiscoveryListener();
        if (dl != null) {
            list.stream().forEach(s -> dl.onDiscovery(s));
        }
        latch.await();


    }

    private void syncConfig(Service provider) {
        Service.Config conf = lookupConfig(provider.ID, provider.Name);
        for (Iterator<Service.Config.Item> iter = provider.Config.iterator(); iter.hasNext(); ) {
            Service.Config.Item item = iter.next();
            if (!conf.hasKey(item.name)) {
                conf.setValue(item.name, item.value);
            }
        }
        provider.Config = conf;
        conf.dump();
        ManagedChannel ch = connect(Arrays.asList(endpoints));
        try {
            KVGrpc.KVBlockingStub kvstub = KVGrpc.newBlockingStub(ch);
            for (Iterator<Service.Config.Item> iter = conf.iterator(); iter.hasNext(); ) {
                Service.Config.Item item = iter.next();
                PutRequest req = PutRequest
                        .newBuilder()
                        .setKey(createConfigKey(provider.Name, provider.ID, item.name))
                        .setValue(ByteString.copyFromUtf8(item.value))
                        .build();
                kvstub.put(req);
                //System.out.println(req.getKey().toStringUtf8()+" "+req.getValue().toStringUtf8());
            }
        } finally {
            if (ch != null) {
                ch.shutdownNow();
            }
        }
    }

    private void registerAndKeeplive(Service provider) throws InterruptedException {
        ManagedChannel ch = connect(Arrays.asList(endpoints));
        KVGrpc.KVBlockingStub kvstub = KVGrpc.newBlockingStub(ch);
        LeaseGrpc.LeaseBlockingStub blockStub = LeaseGrpc.newBlockingStub(ch);
        LeaseGrpc.LeaseStub stub = LeaseGrpc.newStub(ch);
        LeaseGrantResponse resp = blockStub.leaseGrant(LeaseGrantRequest.newBuilder().setTTL(Const.DEFAULT_KEEPALIVE_TTL).build());
        StreamObserver<LeaseKeepAliveResponse> respStream = new StreamObserver<LeaseKeepAliveResponse>() {
            @Override
            public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
                //System.out.println("LeaseKeepAliveResponse " + leaseKeepAliveResponse.getID() + " " + leaseKeepAliveResponse.getTTL());
                PutRequest req = PutRequest
                        .newBuilder()
                        .setKey(createRegisterKey(provider.Name, provider.ID))
                        .setValue(ByteString.copyFromUtf8(provider.toString()))
                        .setLease(resp.getID()).build();
                kvstub.put(req);

                Service.StatisticsCallback cb = provider.getStatisticsCallback();
                if (cb != null) {
                    cb.statistics(provider.Statistics);
                    PutRequest req1 = PutRequest
                            .newBuilder()
                            .setKey(createStatisticsKey(provider.Name, provider.ID))
                            .setValue(ByteString.copyFromUtf8(provider.Statistics.toString()))
                            .setLease(resp.getID())
                            .build();
                    kvstub.put(req1);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                ch.shutdownNow();
                try {
                    ch.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCompleted() {
                //System.err.println("testKeeplive onCompleted");
            }
        };
        keepAliveStream = stub.leaseKeepAlive(respStream);
        while (run) {
            if (ch.isShutdown() || ch.isTerminated()) {
                return;
            }
            keepAliveStream.onNext(LeaseKeepAliveRequest.newBuilder().setID(resp.getID()).build());
            TimeUnit.SECONDS.sleep(Const.DEFAULT_KEEPALIVE_TTL - 5);
        }

    }

    @Override
    public void unregister(String id, String name) {
        ManagedChannel ch = connect(Arrays.asList(endpoints));
        try {
            KVGrpc.KVBlockingStub kvstub = KVGrpc.newBlockingStub(ch);
            DeleteRangeRequest.Builder builder = DeleteRangeRequest.newBuilder()
                    .setKey(ByteString.copyFromUtf8(id));
            kvstub.deleteRange(builder.build());
        } finally {
            if (ch != null) {
                ch.shutdownNow();
            }
        }
    }

    private List<Service> lookupService(String... name) {
        List<Service> list = new ArrayList<Service>();
        ManagedChannel ch = connect(Arrays.asList(endpoints));
        try {
            KVGrpc.KVBlockingStub kvstub = KVGrpc.newBlockingStub(ch);
            for (String n : name) {
                ByteString key = createRegisterKey(n, "/");
                RangeRequest req = RangeRequest
                        .newBuilder()
                        .setKey(key)
                        .setRangeEnd(getPrefixEnd(key))
                        .build();
                RangeResponse resp = kvstub.range(req);

                for (KeyValue kv : resp.getKvsList()) {
                    Service srv = parserConsumer(kv);
                    list.add(srv);
                }
            }
            return list;
        } finally {
            if (ch != null) {
                ch.shutdownNow();
            }
        }
    }

    private Service parserConsumer(KeyValue kv) {
        return Service.fromString(kv.getValue().toStringUtf8());
    }

    private Service.Config lookupConfig(String id, String name) {
        Service.Config config = new Service.Config();
        ManagedChannel ch = connect(Arrays.asList(endpoints));
        try {
            KVGrpc.KVBlockingStub kvstub = KVGrpc.newBlockingStub(ch);
            ByteString key = createConfigKey(name, id, "/");
            RangeRequest req = RangeRequest
                    .newBuilder()
                    .setKey(key)
                    .setRangeEnd(getPrefixEnd(key))
                    .build();
            RangeResponse resp = kvstub.range(req);

            for (KeyValue kv : resp.getKvsList()) {
                config.setValue(kv.getKey().substring(key.size()).toStringUtf8(), kv.getValue().toStringUtf8());
            }
            return config;
        } finally {
            if (ch != null) {
                ch.shutdownNow();
            }
        }
    }

    @Override
    public void handle(Signal sn) {
        closeChannel();
    }

    synchronized final ManagedChannel connect(List<String> endpoints) {
        // if (channel != null && !channel.isShutdown()) return channel;
        return ManagedChannelBuilder.forTarget("etcd")
                .nameResolverFactory(new SimpleEtcdNameResolverFactory(
                        endpoints.stream()
                                .map(TestEtcd::endpointToUri)
                                .collect(Collectors.toList())
                ))
                .usePlaintext(true).build();
//        return channel;
    }

    private ByteString createRegisterKey(String... suffix) {
        StringBuilder sb = new StringBuilder(Const.SERVICE_REGISTER_PREFIX);
        for (String s : suffix) {
            if ("/".equals(s))
                sb.append(s);
            else
                sb.append("/").append(s);
        }
        return ByteString.copyFromUtf8(sb.toString());
    }

    private ByteString createStatusKey(String... suffix) {
        StringBuilder sb = new StringBuilder(Const.SERVICE_STATUS_PREFIX);
        for (String s : suffix) {
            if ("/".equals(s))
                sb.append(s);
            else
                sb.append("/").append(s);
        }
        return ByteString.copyFromUtf8(sb.toString());
    }

    private ByteString createConfigKey(String... suffix) {
        StringBuilder sb = new StringBuilder(Const.SERVICE_CONFIG_PREFIX);
        for (String s : suffix) {
            if ("/".equals(s))
                sb.append(s);
            else
                sb.append("/").append(s);
        }
        return ByteString.copyFromUtf8(sb.toString());
    }

    private ByteString createStatisticsKey(String... suffix) {
        StringBuilder sb = new StringBuilder(Const.SERVICE_STATISTICS_PREFIX);
        for (String s : suffix) {
            if ("/".equals(s))
                sb.append(s);
            else
                sb.append("/").append(s);
        }
        return ByteString.copyFromUtf8(sb.toString());
    }
}
