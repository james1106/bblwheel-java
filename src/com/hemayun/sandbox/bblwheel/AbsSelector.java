package com.hemayun.sandbox.bblwheel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by apple on 16/11/17.
 */
public abstract class AbsSelector implements Selector {

    public static final int MAX_SERVICE_INSTANCE = 1024;

    private Map<String, Set<Service>> instances = new ConcurrentHashMap<String, Set<Service>>();

    private Lock lock = new ReentrantLock();

    @Override
    public void addService(Service srv) {
        Set<Service> ins = instances.get(srv.Name);
        if (ins == null) {
            lock.lock();
            ins = new HashSet<Service>();
            lock.unlock();
            instances.put(srv.Name, ins);
        }
        lock.lock();
        try {
            if (ins.size() >= MAX_SERVICE_INSTANCE) return;
            ins.add(srv);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeService(String id, String serviceName) {
        Set<Service> ins = instances.get(serviceName);
        if (ins != null) {
            lock.lock();
            try {
                for (Iterator<Service> iter = ins.iterator(); iter.hasNext(); ) {
                    Service srv = iter.next();
                    if (srv.ID.equals(id)) {
                        iter.remove();
                        break;
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    protected Service[] findService(String serviceName) {
        Set<Service> ins = instances.get(serviceName);
        if (ins == null) {
            lock.lock();
            ins = new HashSet<Service>();
            lock.unlock();
            instances.put(serviceName, ins);
        }
        lock.lock();
        List<Service> onlineService = new ArrayList<Service>();
        try {
            for (Service srv : ins) {
                if (srv.isOnline()) {
                    onlineService.add(srv);
                }
            }
            return onlineService.toArray(new Service[onlineService.size()]);
        } finally {
            lock.unlock();
        }
    }

}
