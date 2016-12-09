package com.hemayun.sandbox.bblwheel.selector;

import com.hemayun.bblwheel.Bblwheel;
import com.hemayun.bblwheel.Bblwheel.Service;
import com.hemayun.sandbox.bblwheel.AbsSelector;
import com.hemayun.sandbox.bblwheel.MurmurHash3;
import com.hemayun.sandbox.bblwheel.Selector;

/**
 * Created by apple on 16/11/15.
 */
public class HashSelector extends AbsSelector {
    private static HashSelector selector = new HashSelector();

    private HashSelector() {
    }

    public static Selector create() {
        return selector;
    }

    @Override
    public Bblwheel.Service select(String serviceName, String key) {
        Service[] ins = findService(serviceName);
        if (ins.length == 0) return null;
        if (ins.length == 1) return ins[0];
        Service srv = ins[Math.abs((int) (murmurHash(key) % (long) ins.length))];
        srv.getStats().toBuilder().setCount(srv.getStats().getCount()+1);
        return srv;
    }

    public long murmurHash(String key) {
        return MurmurHash3.murmurhash3_x86_32(key, 0, key.length(), 1);
    }

    public long bkdrHash(String str) {
        long seed = 131;
        long hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash * seed) + str.charAt(i);
        }
        return hash;
    }
}
