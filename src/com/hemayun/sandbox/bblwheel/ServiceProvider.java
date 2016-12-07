package com.hemayun.sandbox.bblwheel;

import com.hemayun.bblwheel.Bblwheel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by apple on 16/12/2.
 */
public class ServiceProvider {

    public volatile Bblwheel.Service service;
    public Bblwheel.Config config;
    private BblwheelListener bblwheelListener;
    private BblwheelClient wheel = BblwheelClient.newClient();

    public String Error = "ok";

    public Map<String,String> stats = new ConcurrentHashMap<>();

    public ServiceProvider() {
        this.service = Bblwheel.Service.getDefaultInstance();
        this.config = Bblwheel.Config.getDefaultInstance();
    }

    public String getID() {
        return service.getID();
    }

    public ServiceProvider setID(String ID) {
        service = service.toBuilder().setID(ID).build();
        return this;
    }

    public String getName() {
        return service.getName();
    }

    public ServiceProvider setName(String name) {
        service = service.toBuilder().setName(name).build();
        return this;
    }

    public ServiceProvider setTags(String[] tags) {
       service = service.toBuilder().addAllTags(Arrays.asList(tags)).build();
        return this;
    }

    public String getAddress() {
        return service.getAddress();
    }

    public ServiceProvider setAddress(String address) {
        service = service.toBuilder().setAddress(address).build();
        return this;
    }

    public String getDataCenter() {
        return service.getDataCenter();
    }

    public ServiceProvider setDataCenter(String dataCenter) {
        service = service.toBuilder().setDataCenter(dataCenter).build();
        return this;
    }

    public String getNode() {
        return service.getNode();
    }

    public ServiceProvider setNode(String node) {
        service = service.toBuilder().setNode(node).build();
        return this;
    }

    public String getPID() {
        return service.getPID();
    }

    public ServiceProvider setPID(String PID) {
        service = service.toBuilder().setPID(PID).build();
        return this;
    }

    public int getWeight() {
        return service.getWeight();
    }

    public ServiceProvider setWeight(int weight) {
        service = service.toBuilder().setWeight(weight).build();
        return this;
    }

    public boolean isSingle() {
        return service.getSingle();
    }

    public ServiceProvider setSingle(boolean single) {
        service = service.toBuilder().setSingle(single).build();
        return this;
    }


    public ServiceProvider setDependentServices(String[] dependentServices) {
        service = service.toBuilder().addAllDependentServices(Arrays.asList(dependentServices)).build();
        return this;
    }


    public ServiceProvider setDependentConfigs(String[] dependentConfigs) {
        service = service.toBuilder().addAllDependentConfigs(Arrays.asList(dependentConfigs)).build();
        return this;
    }

    public ServiceProvider setStatus(Bblwheel.Service.Status status) {
        service = service.toBuilder().setStatus(status).build();
        return this;
    }

    public Bblwheel.Service.Status getStatus() {
        return service.getStatus();
    }

    public ServiceProvider addConfig(String key , String value) {
        config = config.toBuilder().addItems(Bblwheel.ConfigEntry.newBuilder()
                .setKey(key)
                .setValue(value)
                .build()).build();
        return this;
    }

    public ServiceProvider setStats(String key , String value) {
        stats.put(key,value);
        return this;
    }

    public Map<String,String> getStats() {
        return stats;
    }


    public Bblwheel.Service getService() {
        return service;
    }

    public ServiceProvider online() {
        service = service.toBuilder().setStatus(Bblwheel.Service.Status.ONLINE).build();
        return this;
    }

    public void register() {
         wheel.register(this);
    }

    public void unregister() {
        wheel.shutdown();
    }
    public ServiceProvider syncConfig() {
        wheel.updateConfig(config);
        return this;
    }

    public List<Bblwheel.Service> findService(String[] deps) {
        return wheel.lookService(deps);
    }

    public Map<String, Bblwheel.Config> findConfig(String[] deps) {
        return wheel.lookupConfig(deps);
    }

    public ServiceProvider setBblwheelListener(BblwheelListener bblwheelListener) {
        this.bblwheelListener = bblwheelListener;
        return this;
    }

    public BblwheelListener getBblwheelListener() {
        return bblwheelListener;
    }


    public interface BblwheelListener {
        void onDiscovery(Bblwheel.Service service);

        void onConfigUpdated(String key, String value);

        void onControl(String cmd);

        void onExec(String cmd);
    }
}
