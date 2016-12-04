package com.hemayun.sandbox.bblwheel;

import com.hemayun.bblwheel.Bblwheel;

/**
 * Created by apple on 16/11/14.
 */
public interface Selector {

    Bblwheel.Service select(String serviceName, String key);

    void addService(Bblwheel.Service srv);

    void removeService(String id, String serviceName);

    class Factory {

    }
}
