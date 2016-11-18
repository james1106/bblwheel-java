package com.hemayun.sandbox.bblwheel;

import com.hemayun.sandbox.bblwheel.selector.HashSelector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import sun.misc.ThreadGroupUtils;

import java.lang.management.ManagementFactory;
import java.util.UUID;

public class Leopard {

    private static String get(String key, String def) {
        return System.getProperty(key, def);
    }

    private static int toInt(String o, String def) {
        if (o == null || "".equals(o.trim()))
            return Integer.parseInt(def);
        else
            return Integer.parseInt(o);
    }

    private static long toLong(String o, String def) {
        if (o == null || "".equals(o.trim()))
            return Long.parseLong(def);
        else
            return Long.parseLong(o);
    }

    private static String toString(String o, String def) {
        if (o == null || "".equals(o.trim()))
            return def;
        else
            return o;
    }

    static class Jetty {
        private final Server server = new Server() {
            {
                setStopAtShutdown(true);
                // setGracefulShutdown(5000);
            }
        };

        private void buildConnector(Server server) throws Exception {
            String cs[] = get("leopard.connectors",
                    "0.0.0.0:7321:1:1:30000").split("[,]", 8);
            String as[];
            int j = (as = cs).length;
            for (int i = 0; i < j; i++) {
                String s = as[i];
                String p[] = s.split("[:]");
                String host = "0.0.0.0";
                int port = 8989;
                int acceptors = 1;
                int selectors = 1;
                int idleTimeout = 30000;
                if (p.length >= 1)
                    host = Leopard.toString(p[0], "0.0.0.0");
                if (p.length >= 2)
                    port = toInt(p[1], "8989");
                if (p.length >= 3)
                    acceptors = toInt(p[2], ""
                            + Runtime.getRuntime().availableProcessors());
                if (p.length >= 4)
                    selectors = toInt(p[3], ""
                            + Runtime.getRuntime().availableProcessors() * 2);
                if (p.length >= 5)
                    idleTimeout = toInt(p[4], "30000");
                ServerConnector con = new ServerConnector(server, acceptors, selectors);
                con.setName("LEOPARD-SERVER-CONNECTOR");
                con.setAcceptQueueSize(4096 * 2);
                con.setReuseAddress(true);
                con.setInheritChannel(true);
                server.addConnector(con);
            }

        }

        private void buildApp(Server server) {
            WebAppContext app = new WebAppContext();
            app.setContextPath(get("leopard.path", "/"));
            app.setWar(get("leopard.webapp", "app"));
            server.setHandler(app);
        }

        public Jetty start() throws Exception {
            buildConnector(server);
            buildApp(server);
            server.start();
            return this;
        }

        public void join() throws InterruptedException {
            server.dumpStdErr();
            server.join();
        }

        public void stop() {
            try {
                server.stop();
            } catch (Exception exception) {
            }
        }

    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        //启动jetty
        Jetty jetty = new Jetty().start();

        Selector selector = HashSelector.create();
        //创建服务
        Service provider = new Service();
        provider.ID = "001";
        provider.Name = "testService1";
        provider.Address = "http://127.0.0.1:7321,grpc://0.0.0.0:7323,tcp://0.0.0.0:7325";
        provider.DataCenter = "aliyun-huadong1";
        provider.Node = "cloud-test-001";
        provider.InstanceNum = 1;
        provider.Dependencies = new String[]{"serviceA", "serviceB"};//依赖的服务
        provider.PID = ManagementFactory.getRuntimeMXBean().getName();//获取进程id@hostName|userName
        provider.Config = new Service.Config() {{//设置默认配置参数
            setValue("shutdown", "0");
            setValue("start", "1");
            setValue("restart", "0");

            setValue("http.listenAddr", "http://0.0.0.0:7321");
            setValue("grpc.listenAddr", "grpc://0.0.0.0:7323");
            setValue("tcp.listenAddr", "tcp://0.0.0.0:7325");
            setValue("maxWorkThread", "20");
            setValue("readTimeout", "30000");
            setValue("writeTimeout", "30000");
            setValue("backlog", "4096");
            setValue("reuseAddress", "true");
            setValue("leopard.connectors", "0.0.0.0:7321:1:1:30000");
            setValue("leopard.path", "/");
            setValue("leopard.webapp", "/var/hemayun/webapp/testService1");
        }};

        provider.setStatisticsCallback(stat -> {//设置统计回调接口
            Runtime rt = Runtime.getRuntime();
            stat.count = provider.Statistics.count;
            stat.other.putAll(provider.Statistics.other);
            stat.avgRespTime = provider.Statistics.avgRespTime;
            stat.maxRespTime = provider.Statistics.maxRespTime;
            stat.minRespTime = provider.Statistics.minRespTime;
            stat.freeMem = rt.freeMemory() / 1024;
            stat.usedMem = (rt.totalMemory() - stat.freeMem) / 1024;
            stat.lastActiveTime = System.currentTimeMillis();
            stat.upTime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
            stat.threads = ThreadGroupUtils.getRootThreadGroup().activeCount();
            stat.other.put("Name", "" + ManagementFactory.getRuntimeMXBean().getName());
            stat.other.put("StartTime", "" + ManagementFactory.getRuntimeMXBean().getStartTime());
//                stat.other.put("ClassPath",""+ManagementFactory.getRuntimeMXBean().getClassPath());
//                stat.other.put("BootClassPath",""+ManagementFactory.getRuntimeMXBean().getBootClassPath());
            // stat.other.put("LibraryPath",""+ManagementFactory.getRuntimeMXBean().getLibraryPath());
            stat.other.put("ManagementSpecVersion", "" + ManagementFactory.getRuntimeMXBean().getManagementSpecVersion());
            stat.other.put("SpecName", "" + ManagementFactory.getRuntimeMXBean().getSpecName());
            stat.other.put("SpecVendor", "" + ManagementFactory.getRuntimeMXBean().getSpecVendor());
            stat.other.put("SpecVersion", "" + ManagementFactory.getRuntimeMXBean().getSpecVersion());
            stat.other.put("VmName", "" + ManagementFactory.getRuntimeMXBean().getVmName());
            stat.other.put("VmVendor", "" + ManagementFactory.getRuntimeMXBean().getVmVendor());
            stat.other.put("InputArguments", "" + ManagementFactory.getRuntimeMXBean().getInputArguments());
            //   stat.other.put("SystemProperties",""+ManagementFactory.getRuntimeMXBean().getSystemProperties());
            //stat.other.put("ObjectName",""+ManagementFactory.getRuntimeMXBean().getObjectName());
        });

        provider.setConfigListener((k, v) -> {//设置配置变更监听接口
            System.out.println("onConfigUpdated " + k + "=" + v);
            if ("stop".equals(k) && jetty.server.isRunning()) {
                provider.setStatus(Service.Status.MAINTENANCE);
                jetty.stop();
                try {
                    jetty.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if ("start".equals(k) && jetty.server.isStopped()) {
                try {
                    jetty.start();
                    provider.setStatus(Service.Status.ONLINE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("restart".equals(k)) {
                jetty.stop();
                try {
                    jetty.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    jetty.start();
                    provider.setStatus(Service.Status.ONLINE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        provider.setDiscoveryListener(service -> {//设置服务发现监听接口
            System.out.println("onDiscovery " + service);
            if (service.isOnline())
                selector.addService(service);
            else
                selector.removeService(service.ID, service.Name);
            System.out.println("Select Service " + selector.select(service.Name, UUID.randomUUID().toString()));
        });

        provider.setStatus(Service.Status.ONLINE);//变更服务状态

        Wheel wheel = Wheel.getWheel();
        wheel.register(provider);//注册服务
    }

}
