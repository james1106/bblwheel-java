package com.hemayun.sandbox.bblwheel;

import com.hemayun.bblwheel.Bblwheel;
import com.hemayun.bblwheel.Bblwheel.Service;
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
    public void addService(Bblwheel.Service srv) {
        Set<Service> ins = instances.get(srv.getName());
        if (ins == null) {
            lock.lock();
            ins = new HashSet<Service>();
            lock.unlock();
            instances.put(srv.getName(), ins);
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
                    if (srv.getID().equals(id)) {
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
                if (srv.getStatus()== Service.Status.ONLINE) {
                    onlineService.add(srv);
                }
            }
            return onlineService.toArray(new Service[onlineService.size()]);
        } finally {
            lock.unlock();
        }
    }

}
