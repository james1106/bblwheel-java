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
        void onConfigUpdated(Config.Item item);
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

        final private Map<String, Config.Item> items = new HashMap<String, Config.Item>();

        public void setValue(String key, String val) {
            items.put(key, new Config.Item(key, val));
        }

        public String getValue(String name, String defValue) {
            return items.getOrDefault(name, new Config.Item(name, defValue)).value;
        }

        public boolean hasKey(String name) {
            return items.containsKey(name);
        }

        public Iterator<Item> iterator() {
            return items.values().iterator();
        }


        public void dump() {
            for (Config.Item i : items.values()) {
                System.out.println(i.name + " = " + i.value);
            }
        }

        public int intValue(String name, int defValue) {
            if (items.containsKey(
                    name
            )) {
                return items.get(name).intValue();
            }
            return defValue;
        }


        public long longValue(String name, long defValue) {
            if (items.containsKey(
                    name
            )) {
                return items.get(name).longValue();
            }
            return defValue;
        }

        public float floatValue(String name, float defValue) {
            if (items.containsKey(
                    name
            )) {
                return items.get(name).floatValue();
            }
            return defValue;
        }

        public double ldoubleValue(String name, double defValue) {
            if (items.containsKey(
                    name
            )) {
                return items.get(name).doubleValue();
            }
            return defValue;
        }


        public static class Item {
            public String name;
            public String value;

            Item() {

            }

            public Item(String key, String val) {
                this.name = key;
                this.value = val;
            }

            public int intValue() {
                return Integer.parseInt(value);
            }

            public long longValue() {
                return Long.parseLong(value);
            }

            public float floatValue() {
                return Float.parseFloat(value);
            }

            public double doubleValue() {
                return Double.parseDouble(value);
            }
        }
    }
}
