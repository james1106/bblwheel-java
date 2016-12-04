# bblwheel-java

###如何运行与测试
1. [ 编译运行bblwheel](https://github.com/gqf2008/bblwheel)
2. [下载etcdctl工具](https://github.com/coreos/etcd/releases/)
3. 运行Leopard
4. 用etcdctl工具跟踪key的变化
    - ./etcdctl watch --prefix=true /v1/bblwheel/service
5. 用etcdctl工具试着注册、变更服务和配置并观察Leopard程序控制台输出结果
    - 注册依赖服务A 001 ./etcdctl put /v1/bblwheel/service/register/serviceA/001 < serviceA_001.json
    - 注册依赖服务A 002 ./etcdctl put /v1/bblwheel/service/register/serviceA/002 < serviceA_002.json
    - 注册依赖服务B 001./etcdctl put /v1/bblwheel/service/register/serviceB/001 < serviceB_001.json
    - 注册依赖服务B 002 ./etcdctl put /v1/bblwheel/service/register/serviceB/002 < serviceB_002.json
    - 变更服务状态A 00 ./etcdctl put /v1/bblwheel/service/register/serviceA/001 < serviceA_001_offline.json
    - 变更服务配置stop ./etcdctl put /v1/bblwheel/service/config/testService1/001/stop 1
    - 变更服务配置start ./etcdctl put /v1/bblwheel/service/config/testService1/001/start 1
    - 变更服务配置restart ./etcdctl put /v1/bblwheel/service/config/testService1/001/restart 1
    
###例子  
  
```
package com.hemayun.sandbox.bblwheel;

import com.hemayun.bblwheel.Bblwheel;
import com.hemayun.bblwheel.Bblwheel.Config;
import com.hemayun.bblwheel.Bblwheel.Service;
import com.hemayun.sandbox.bblwheel.SimpleService.SimpleService;
import com.hemayun.sandbox.bblwheel.selector.HashSelector;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.UUID;

public class Leopard {

    static Service provider;

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        RPCServer server = new RPCServer(6543);
        server.register(new SimpleService());
        Once once = new Once();
        Selector selector = HashSelector.create();
        //创建服务实例
        ServiceInstance instance = new ServiceInstance()
                .setID("001")
                .setName("testService1")
                .setTags(new String[]{"author: gqf", "mail: gao.qingfeng@gmail.com"})
                .setAddress("http://127.0.0.1:7321,grpc://0.0.0.0:7323,tcp://0.0.0.0:7325")
                .setDataCenter("aliyun-huadong2-shanghai")
                .setNode("cloud-test-001")
                .setSingle(true)
                .setDependentServices(new String[]{"serviceA", "serviceB"})
                .setDependentConfigs(new String[]{"common/db1", "common/redis1", "testService1/001"})
                .setPID(ManagementFactory.getRuntimeMXBean().getName())
                .setStatus(Service.Status.INIT)

                .addConfig("leopard.webapp", "/var/hemayun/webapp/testService1")
                .addConfig("leopard.path", "/")
                .addConfig("leopard.connectors", "0.0.0.0:7321:1:1:30000")
                .addConfig("backlog", "4096")
                .addConfig("writeTimeout", "30000")
                .addConfig("readTimeout", "30000")
                .addConfig("maxWorkThread", "200")
                .addConfig("tcp.listenAddr", "tcp://0.0.0.0:7325")
                .addConfig("grpc.listenAddr", "grpc://0.0.0.0:7323")
                .addConfig("http.listenAddr", "http://0.0.0.0:7321")
                .addConfig("leopard.webapp", "/var/hemayun/webapp/testService1")
                .addConfig("leopard.webapp", "/var/hemayun/webapp/testService1")
                .addConfig("leopard.webapp", "/var/hemayun/webapp/testService1")

                .setStats("Name", "" + ManagementFactory.getRuntimeMXBean().getName())
                .setStats("Name", "" + ManagementFactory.getRuntimeMXBean().getName())
                .setStats("StartTime", "" + ManagementFactory.getRuntimeMXBean().getStartTime())
                .setStats("ManagementSpecVersion", "" + ManagementFactory.getRuntimeMXBean().getManagementSpecVersion())
                .setStats("SpecName", "" + ManagementFactory.getRuntimeMXBean().getSpecName())
                .setStats("SpecVendor", "" + ManagementFactory.getRuntimeMXBean().getSpecVendor())
                .setStats("VmName", "" + ManagementFactory.getRuntimeMXBean().getVmName())
                .setStats("VmVendor", "" + ManagementFactory.getRuntimeMXBean().getVmVendor())
                .setStats("InputArguments", "" + ManagementFactory.getRuntimeMXBean().getInputArguments());
        instance.setBblwheelListener(new ServiceInstance.BblwheelListener() {
            @Override
            public void onDiscovery(Service srv) {
                System.out.println("onDiscovery\n"+srv);
                if (srv.getStatus() == Service.Status.ONLINE) {
                    selector.addService(srv);
                } else {
                    selector.removeService(srv.getID(), srv.getName());
                }
                System.out.println("Select Service " + selector.select(srv.getName(), UUID.randomUUID().toString()));
            }

            @Override
            public void onConfigUpdated(String key, String value) {
                System.out.println("onConfigUpdated " + key + "=" + value);
            }

            @Override
            public void onControl(String cmd) {
                System.out.println("onControl " + cmd);
            }

            @Override
            public void onExec(String cmd) {
                System.out.println("onExec " + cmd);
            }
        });
        Bblwheel.RegisterResult result = instance.register();//注册服务
        System.out.println(result);
        if ("SUCCESS".equalsIgnoreCase(result.getDesc())) {
            for (Service srv : result.getServiceList()) {
                if (srv.getStatus() == Service.Status.ONLINE) {
                    selector.addService(srv);
                } else {
                    selector.removeService(srv.getID(), srv.getName());
                }
            }
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    instance.unregister();

                }
            });
            once.once(new Runnable() {
                @Override
                public void run() {
                    try {
                        server.start();
                        instance.online();
                        server.join();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}

```