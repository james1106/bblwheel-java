package com.hemayun.sandbox.bblwheel.etcd;

import java.util.concurrent.TimeUnit;

/**
 * Created by apple on 16/11/16.
 */
public class Const {
    public static final String SERVICE_REGISTER_PREFIX = "/v1/bblwheel/service/register";

    public static final String SERVICE_GRANT_PREFIX = "/v1/bblwheel/service/grant";

    public static final String SERVICE_CONFIG_PREFIX = "/v1/bblwheel/service/config";

    public static final String SERVICE_STATISTICS_PREFIX = "/v1/bblwheel/service/statistics";

    public static final String ETCD_ENDPOINTS = "ETCD_ENDPOINTS";

    public static final String DEFAULT_ETCD_ENDPOINTS = "127.0.0.1:2379";

    public static final long DEFAULT_KEEPALIVE_TTL = 30;

    public static final TimeUnit TIME_UNIT_SECOND = TimeUnit.SECONDS;
}
