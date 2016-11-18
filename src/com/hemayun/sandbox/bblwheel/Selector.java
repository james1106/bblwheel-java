package com.hemayun.sandbox.bblwheel;

/**
 * Created by apple on 16/11/14.
 */
public interface Selector {

    Service select(String serviceName, String key);

    void addService(Service srv);

    void removeService(String id, String serviceName);

    class Factory {

    }
}
