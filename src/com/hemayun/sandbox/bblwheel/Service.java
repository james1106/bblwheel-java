package com.hemayun.sandbox.bblwheel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by apple on 16/11/14.
 */
public class Service {

    private static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .disableInnerClassSerialization()
            .setPrettyPrinting()
            .create();
    public String ID;
    public String Address;
    public String DataCenter;
    public String Node;
    public int Weight;
    public String Name;
    public String[] Tags;
    public String[] Dependencies = new String[]{};
    public int InstanceNum;
    public String PID;

    public transient Config Config = new Config();

    public transient Statistics Statistics = new Statistics();
    @SerializedName("Status")
    private volatile Status status = Status.INIT;
    private transient DiscoveryListener discoveryListener;
    private transient StatisticsCallback statisticsCallback;
    private transient ConfigListener configListener;

    public static Service fromString(String from) {
        return gson.fromJson(from, Service.class);
    }

    public boolean isOnline() {
        return status == Status.ONLINE;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status newStatus) {
        this.status = newStatus;
    }

    public String toString() {
        return gson.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;

        if (!ID.equals(service.ID)) return false;
        return Name.equals(service.Name);

    }

    @Override
    public int hashCode() {
        int result = ID.hashCode();
        result = 31 * result + Name.hashCode();
        return result;
    }

    public StatisticsCallback getStatisticsCallback() {
        return statisticsCallback;
    }

    public void setStatisticsCallback(StatisticsCallback statisticsCallback) {
        this.statisticsCallback = statisticsCallback;
    }

    public ConfigListener getConfigListener() {
        return configListener;
    }

    public void setConfigListener(ConfigListener configListener) {
        this.configListener = configListener;
    }

    public DiscoveryListener getDiscoveryListener() {
        return discoveryListener;
    }

    public void setDiscoveryListener(DiscoveryListener discoveryListener) {
        this.discoveryListener = discoveryListener;
    }

    public enum Status {
        INIT, ONLINE, MAINTENANCE, OFFLINE, FAULT
    }


    public interface StatisticsCallback {
        void statistics(Statistics stat);
    }

    public interface ConfigListener {
        void onConfigUpdated(String key, String value);
    }

    public interface DiscoveryListener {
        void onDiscovery(Service service);
    }

    public static class Statistics {
        public volatile long count;
        public volatile long upTime;
        public volatile long usedMem;
        public volatile long freeMem;
        public volatile long threads;
        public volatile long avgRespTime;
        public volatile long minRespTime;
        public volatile long maxRespTime;
        public volatile long lastActiveTime;
        public Map<String, Object> other = new ConcurrentHashMap<String, Object>();

        public String toString() {
            return gson.toJson(this);
        }
    }

    public static class Config {

        final private Map<String, String> items = new HashMap<String, String>();

        public void setValue(String key, String val) {
            items.put(key, val);
        }

        public String getValue(String name, String defValue) {
            return items.getOrDefault(name, defValue);
        }

        public boolean hasKey(String name) {
            return items.containsKey(name);
        }

        public Iterator<Map.Entry<String, String>> iterator() {
            return items.entrySet().iterator();
        }


        public void dump() {
            for (Map.Entry<String, String> kv : items.entrySet()) {
                System.out.println(kv.getKey() + " = " + kv.getValue());
            }
        }

        public int intValue(String name, int defValue) {
            if (items.containsKey(
                    name
            )) {
                return Integer.parseInt(items.get(name));
            }
            return defValue;
        }


        public long longValue(String name, long defValue) {
            if (items.containsKey(name)) {
                return Long.parseLong(items.get(name));
            }
            return defValue;
        }

        public float floatValue(String name, float defValue) {
            if (items.containsKey(
                    name
            )) {
                return Float.parseFloat(items.get(name));
            }
            return defValue;
        }

        public double doubleValue(String name, double defValue) {
            if (items.containsKey(
                    name
            )) {
                return Double.parseDouble(items.get(name));
            }
            return defValue;
        }
    }
}
