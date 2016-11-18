package com.hemayun.sandbox.bblwheel;

import com.hemayun.sandbox.bblwheel.etcd.EtcdWheel;

/**
 * Created by apple on 16/11/14.
 */
public abstract class Wheel {

    private static final String DEFAULT_WHEEL_PROVIDER = "etcd";
    private static final String WHEEL_PROVIDER = "WHEEL_PROVIDER";
    private static Wheel wheel;

    public static synchronized Wheel getWheel() {
        if (wheel != null) return wheel;
        String providerClassName = System.getenv(WHEEL_PROVIDER);
        if (providerClassName == null || DEFAULT_WHEEL_PROVIDER.equalsIgnoreCase(providerClassName)) {
            wheel = EtcdWheel.getInstance();
            return wheel;
        }
        try {
            Class clazz = Class.forName(providerClassName);
            wheel = (Wheel) clazz.newInstance();
            return wheel;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        wheel = EtcdWheel.getInstance();
        return wheel;
    }

    public abstract void register(Service provider);

    public abstract void unregister(String id, String name);

    public static class WheelException extends RuntimeException {
        public WheelException() {
        }

        public WheelException(String message) {
            super(message);
        }

        public WheelException(String message, Throwable cause) {
            super(message, cause);
        }

        public WheelException(Throwable cause) {
            super(cause);
        }

        public WheelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    public static class RegisterException extends WheelException {

        public RegisterException() {
        }

        public RegisterException(String message) {
            super(message);
        }

        public RegisterException(String message, Throwable cause) {
            super(message, cause);
        }

        public RegisterException(Throwable cause) {
            super(cause);
        }

        public RegisterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    public static class ServiceNotFoundException extends WheelException {
        public ServiceNotFoundException() {
        }

        public ServiceNotFoundException(String message) {
            super(message);
        }

        public ServiceNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public ServiceNotFoundException(Throwable cause) {
            super(cause);
        }

        public ServiceNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

}


