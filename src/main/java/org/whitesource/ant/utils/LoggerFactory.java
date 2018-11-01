package org.whitesource.ant.utils;

public class LoggerFactory {

    public static LoggerAntPlugin getLogger(Class clazz) {
        return new LoggerAntPlugin(clazz);
    }
}
