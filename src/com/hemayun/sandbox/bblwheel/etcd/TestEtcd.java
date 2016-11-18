package com.hemayun.sandbox.bblwheel.etcd;

import com.coreos.jetcd.EtcdClient;
import com.coreos.jetcd.EtcdClientBuilder;
import com.coreos.jetcd.EtcdKV;
import com.coreos.jetcd.api.*;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.resolver.SimpleEtcdNameResolverFactory;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolver;
import io.grpc.stub.StreamObserver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by apple on 16/11/15.
 */
public class TestEtcd {
    static String[] endpoints = new String[]{"http://localhost:2379"};
    static ManagedChannel channel;

    public static void main(String[] args) throws Exception {
//        byte[] prefix = ByteString.copyFromUtf8("/test_key").toByteArray();
//        System.out.println(ByteString.copyFrom(getPrefixEnd(prefix)).toStringUtf8());
        testKV();
        new Thread() {
            public void run() {

                while (true) {
                    try {
                        testKeeplive();
                    } catch (Throwable e) {
                    }
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }

            }
        }.start();
        testLock();
        new Thread() {
            public void run() {
                while (true) {
                    try {
                        testWatch();
                    } catch (Throwable e) {
                    }
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }.start();
    }


    // next prefix does not exist (e.g., 0xffff);
    // default to WithFromKey policy

    private static byte[] getPrefixEnd(byte[] prefix) {
        byte[] end = new byte[]{0};
        for (int i = prefix.length - 1; i >= 0; i--) {
            if (prefix[i] < 0xff) {
                prefix[i] = (byte) (prefix[i] + 1);
                end = new byte[i + 1];
                System.arraycopy(prefix, 0, end, 0, end.length);
                break;
            }
        }
        return end;
    }

    public static void testKV() throws Exception {
        EtcdClient client = EtcdClientBuilder.newBuilder().endpoints(endpoints).build();

        EtcdKV kvClient = client.getKVClient();

        ByteString key = ByteString.copyFromUtf8("/test_key");
        ByteString value = ByteString.copyFromUtf8("test_value");

        kvClient.put(key, value).get();

        ByteString key1 = ByteString.copyFromUtf8("/test1_key1");
        ByteString value1 = ByteString.copyFromUtf8("test_value1");

// put the key-value
        kvClient.put(key1, value1).get();

// get the value
        GetOption otp = GetOption.newBuilder().withRange(ByteString.copyFrom(getPrefixEnd(key.toByteArray()))).build();
        System.out.println("end " + ByteString.copyFrom(getPrefixEnd(key.toByteArray())).toStringUtf8());
        ListenableFuture<RangeResponse> getFeature = kvClient.get(key, otp);

        RangeResponse response = getFeature.get();
        System.out.println(response.getCount());
        System.out.println(response.getKvsCount());
        for (int i = 0; i < response.getKvsCount(); i++) {
            System.out.println(response.getKvs(i).getKey().toStringUtf8() + " " + response.getKvs(i).getValue().toStringUtf8());
        }

        //assertEquals(response.getKvsCount(), 1);
        //assertEquals(response.getKvs(0).getValue().toStringUtf8(), "test_value");
// delete the key
        kvClient.delete(key).get();
        kvClient.delete(key1).get();
    }

    public static void testKeeplive() throws InterruptedException {
        ManagedChannel ch = defaultChannelBuilder(simpleNameResolveFactory(Arrays.asList(endpoints)));
        KVGrpc.KVBlockingStub kvstub = KVGrpc.newBlockingStub(ch);
        LeaseGrpc.LeaseBlockingStub blockStub = LeaseGrpc.newBlockingStub(ch);
        LeaseGrpc.LeaseStub stub = LeaseGrpc.newStub(ch);
        LeaseGrantResponse resp = blockStub.leaseGrant(LeaseGrantRequest.newBuilder().setTTL(10).build());
        StreamObserver<LeaseKeepAliveResponse> respStream = new StreamObserver<LeaseKeepAliveResponse>() {
            @Override
            public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
                System.out.println("LeaseKeepAliveResponse " + leaseKeepAliveResponse.getID() + " " + leaseKeepAliveResponse.getTTL());
                PutRequest req = PutRequest
                        .newBuilder()
                        .setKey(ByteString.copyFromUtf8("/testkey"))
                        .setValue(ByteString.copyFromUtf8("testvalue"))
                        .setLease(resp.getID()).build();
                kvstub.put(req);
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("testKeeplive error");
                ch.shutdownNow();
                try {
                    ch.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.err.println("testKeeplive onCompleted");
            }
        };
        StreamObserver<LeaseKeepAliveRequest> reqStream = stub.leaseKeepAlive(respStream);
        while (true) {
            if (ch.isShutdown() || ch.isTerminated()) {
                reqStream.onCompleted();
                return;
            }
            reqStream.onNext(LeaseKeepAliveRequest.newBuilder().setID(resp.getID()).build());
            TimeUnit.SECONDS.sleep(10);
        }
    }

    public static void testLock() throws Exception {
//        ManagedChannel ch = defaultChannelBuilder(simpleNameResolveFactory(Arrays.asList(endpoints))).build();
//        EtcdLease lease = new EtcdLeaseImpl(ch, Optional.empty());
//
//        lease.keepAlive(0, new EtcdLease.EtcdLeaseHandler() {
//            @Override
//            public void onKeepAliveRespond(LeaseKeepAliveResponse leaseKeepAliveResponse) {
//                System.out.println("onKeepAliveRespond " + leaseKeepAliveResponse.getHeader().getRevision());
//            }
//
//            @Override
//            public void onLeaseExpired(long l) {
//                System.out.println("onLeaseExpired " + l);
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                System.out.println(throwable);
//            }
//        });
//
//        lease.startKeepAliveService();
    }

    public static void testWatch() throws InterruptedException {
        ManagedChannel chh = defaultChannelBuilder(simpleNameResolveFactory(Arrays.asList(endpoints)));
        WatchGrpc.WatchStub watcher = WatchGrpc.newStub(chh);
        StreamObserver<WatchRequest> reqStream = null;
        StreamObserver<WatchResponse> respStream = new StreamObserver<WatchResponse>() {
            @Override
            public void onNext(WatchResponse resp) {
                System.out.println("onNext WatchIs" + resp.getWatchId());
                System.out.println("Canceled " + resp.getCanceled());
                System.out.println("CompactRevision " + resp.getCompactRevision());
                System.out.println("Created " + resp.getCreated());
                System.out.println("EventCount " + resp.getEventsCount());
                for (Event e : resp.getEventsList()) {
                    System.out.println("Event " + e.getType().name() + " " + e.getKv().getKey().toStringUtf8() + " " + e.getKv().getValue().toStringUtf8());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                chh.shutdownNow();
                try {
                    System.out.println("termination " + chh.awaitTermination(10, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("watcher " + chh.isShutdown() + " " + chh.isTerminated());
            }

            @Override
            public void onCompleted() {
                System.out.println("WatchResponse onCompleted");
            }
        };

        reqStream = watcher.watch(respStream);

        WatchRequest req = WatchRequest
                .newBuilder()
                .setCreateRequest(
                        WatchCreateRequest
                                .newBuilder()
                                .setKey(ByteString.copyFromUtf8("/testwatch"))
                                .setPrevKv(true))
                .build();
        WatchRequest req1 = WatchRequest
                .newBuilder()
                .setCreateRequest(
                        WatchCreateRequest
                                .newBuilder()
                                .setKey(ByteString.copyFromUtf8("/aaawatch"))
                                .setPrevKv(true))
                .build();
        reqStream.onNext(req);
        reqStream.onNext(req1);
        while (true) {
            if (chh.isShutdown() || chh.isTerminated()) {
                reqStream.onCompleted();
                return;
            }
            TimeUnit.SECONDS.sleep(10);
        }
    }

    static final ManagedChannel connect(List<String> endpoints) throws Exception {
        return ManagedChannelBuilder.forTarget("etcd")
                .nameResolverFactory(new SimpleEtcdNameResolverFactory(
                        endpoints.stream()
                                .map(TestEtcd::endpointToUri)
                                .collect(Collectors.toList())
                ))
                .usePlaintext(true).build();
    }

    static final NameResolver.Factory simpleNameResolveFactory(List<String> endpoints) {
        return new SimpleEtcdNameResolverFactory(
                endpoints.stream()
                        .map(TestEtcd::endpointToUri)
                        .collect(Collectors.toList())
        );
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

    synchronized static ManagedChannel defaultChannelBuilder(NameResolver.Factory factory) {
        if (channel != null && !channel.isShutdown()) {
            return channel;
        }
        channel = ManagedChannelBuilder.forTarget("etcd")
                .nameResolverFactory(factory)
                .usePlaintext(true).build();
        return channel;
    }
}
