package org.whitesource.ant.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class LoggerAntPlugin implements Logger {

    /* --- Members --- */
    
    private final Logger logger;

    /* --- Constructors --- */

    public LoggerAntPlugin(Class clazz){
        this.logger = LoggerFactory.getLogger(clazz);
    }


    @Override
    public void info(String msg) {
        this.logger.info(msg);
    }

    @Override
    public void info(String msg, Object arg) {
        this.logger.info(msg, arg);
    }

    @Override
    public void info(String msg, Object o, Object o1) {
        this.logger.info(msg, o, o1);
    }

    @Override
    public void info(String format, Object... args) {
        this.logger.info(format, args);
    }

    @Override
    public void info(String msg, Throwable t) {
        this.logger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return this.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String s) {
        this.logger.info(marker, s);
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        this.logger.info(marker, s, o);
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {
        this.logger.info(marker, s, o, o1);
    }

    @Override
    public void info(Marker marker, String s, Object... objects) {
        this.logger.info(marker, s, objects);
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        this.logger.info(marker, s, throwable);
    }

    @Override
    public void debug(String msg) {
        this.logger.debug(msg);
    }

    @Override
    public void debug(String msg, Object arg) {
        this.logger.debug(msg, arg);
    }

    @Override
    public void debug(String s, Object o, Object o1) {
        this.logger.debug(s, o, o1);
    }

    @Override
    public void debug(String format, Object... args) {
        this.logger.debug(format, args);
    }

    @Override
    public void debug(String msg, Throwable t) {
        this.logger.debug(msg, t);
    }

    @Override
    public void warn(String msg) {
        this.logger.warn(msg);
    }

    @Override
    public void warn(String msg, Object arg) {
        this.logger.warn(msg, arg);
    }

    @Override
    public void warn(String format, Object... args) {
        this.logger.warn(format, args);
    }

    @Override
    public void warn(String s, Object o, Object o1) {
        this.logger.warn(s, o, o1);
    }

    public void warn(String msg, Throwable t) {
        this.logger.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return this.logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String s) {
        this.logger.warn(marker, s);
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        this.logger.warn(marker, s, o);
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {
        this.logger.warn(marker, s, o, o1);
    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {
        this.logger.warn(marker, s, objects);
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        this.logger.warn(marker, s, throwable);
    }

    @Override
    public void error(String msg) {
        this.logger.error(msg);
    }

    @Override
    public void error(String msg, Object arg) {
        this.logger.error(msg, arg);
    }

    @Override
    public void error(String s, Object o, Object o1) {
        this.logger.error(s, o, o1);
    }

    @Override
    public void error(String format, Object... args) {
        this.logger.error(format, args);
    }

    @Override
    public void error(String msg, Throwable t) {
        this.logger.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return this.logger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String s) {
        this.logger.error(marker, s);
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        this.logger.error(marker, s, o);
    }

    @Override
    public void error(Marker marker, String msg, Object arg1, Object arg2) {
        this.logger.error(marker, msg, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String s, Object... objects) {
        this.logger.error(marker, s, objects);
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        this.logger.error(marker, s, throwable);
    }

    @Override
    public void trace(String msg) {
        this.logger.trace(msg);
    }

    @Override
    public void trace(String msg, Object arg) {
        this.logger.trace(msg, arg);
    }

    @Override
    public void trace(String s, Object o, Object o1) {
        this.logger.trace(s, o, o1);
    }

    @Override
    public void trace(String format, Object... args) {
        this.logger.trace(format, args);
    }

    @Override
    public void trace(String msg, Throwable t) {
        this.logger.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return this.logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String s) {
        this.logger.trace(marker, s);
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
        this.logger.trace(marker, s, o);
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {
        this.logger.trace(marker, s, o, o1);
    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {
        this.logger.trace(marker, s, objects);
    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
        this.logger.trace(marker, s, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return this.logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String s) {
        this.logger.debug(marker, s);
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        this.logger.debug(marker, s, o);
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {
        this.logger.debug(marker, s, o, o1);
    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {
        this.logger.debug(marker, s, objects);
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        this.logger.debug(marker, s, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    @Override
    public String getName() {
        return this.logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }
}
