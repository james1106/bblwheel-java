package com.hemayun.sandbox.bblwheel;

import com.hemayun.bblwheel.Bblwheel.Config;
import com.hemayun.bblwheel.Bblwheel.Service;
import com.hemayun.sandbox.bblwheel.SimpleService.SimpleService;
import com.hemayun.sandbox.bblwheel.selector.HashSelector;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Leopard {

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
        ServiceProvider provider = new ServiceProvider()
                //设置服务提供者信息
                .setID("001")//服务ID
                .setName("testService1")//服务名称
                .setTags(new String[]{"author: gqf", "mail: gao.qingfeng@gmail.com"})//服务标签
                .setAddress("http://127.0.0.1:7321,grpc://0.0.0.0:7323,tcp://0.0.0.0:7325")//服务地址
                .setDataCenter("aliyun-huadong2-shanghai")//所属数据中心
                .setNode("cloud-test-001")//所属服务节点
                .setSingle(true)//是否单例
                .setDependentServices(new String[]{"serviceA", "serviceB"})//依赖服务
                .setDependentConfigs(new String[]{"common/db1", "common/redis1", "testService1/001"})//依赖配置
                .setPID(ManagementFactory.getRuntimeMXBean().getName())//服务进程标示
                .setStatus(Service.Status.INIT)
                //添加默认配置
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
                //设置统计数据
                .setStats("Name", "" + ManagementFactory.getRuntimeMXBean().getName())
                .setStats("Name", "" + ManagementFactory.getRuntimeMXBean().getName())
                .setStats("StartTime", "" + ManagementFactory.getRuntimeMXBean().getStartTime())
                .setStats("ManagementSpecVersion", "" + ManagementFactory.getRuntimeMXBean().getManagementSpecVersion())
                .setStats("SpecName", "" + ManagementFactory.getRuntimeMXBean().getSpecName())
                .setStats("SpecVendor", "" + ManagementFactory.getRuntimeMXBean().getSpecVendor())
                .setStats("VmName", "" + ManagementFactory.getRuntimeMXBean().getVmName())
                .setStats("VmVendor", "" + ManagementFactory.getRuntimeMXBean().getVmVendor())
                .setStats("InputArguments", "" + ManagementFactory.getRuntimeMXBean().getInputArguments());
        provider.setBblwheelListener(new ServiceProvider.BblwheelListener() {//设置回调接口
            @Override
            public void onDiscovery(Service srv) {//当服务发现的时候回调
                System.out.println("onDiscovery\n"+srv);
                if (srv.getStatus() == Service.Status.ONLINE) {
                    selector.addService(srv);
                } else {
                    selector.removeService(srv.getID(), srv.getName());
                }
                System.out.println("Select Service " + selector.select(srv.getName(), UUID.randomUUID().toString()));
            }

            @Override
            public void onConfigUpdated(String key, String value) {//当配置变更的时候回调
                System.out.println("onConfigUpdated " + key + "=" + value);
            }

            @Override
            public void onControl(String cmd) {//当收到控制命令的时候回调
                System.out.println("onControl " + cmd);
            }

            @Override
            public void onExec(String cmd) {//当收到执行外部命令的时候回调
                System.out.println("onExec " + cmd);
            }
        });
        //读取依赖的配置
        Map<String,Config> config = provider.findConfig(new String[]{"common/db1",provider.getName()+"/"+provider.getID()});
        //获取依赖服务，如果服务没有被授权则不回被返回，当服务器端完成授权后则会通知到客户端
        List<Service> depServices= provider.findService(new String[]{"serviceA","serviceB"});
        //与配置中心同步配置
       // instance.syncConfig();
        //启动服务
        once.once(new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //注册服务并保存心跳
        provider.register();
        //更新服务状态
        provider.online();
        server.join();
        //注销服务
        provider.unregister();
    }
}
