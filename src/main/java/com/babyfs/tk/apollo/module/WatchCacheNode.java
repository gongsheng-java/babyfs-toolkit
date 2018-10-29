package com.babyfs.tk.apollo.module;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class WatchCacheNode {

    private WatchType watchType;
    private Class<?> classType;
    private Consumer consumer;
    private Field field;
    private Class<?> fieldClass;
    private Object originObject;

    public WatchCacheNode(Class<?> classType, Consumer consumer) {
        this.classType = classType;
        this.consumer = consumer;
        this.watchType = WatchType.TYPE;
    }

    public WatchCacheNode(Field field, Class<?> fieldClass, Object originObject, Consumer consumer) {
        this.consumer = consumer;
        this.field = field;
        this.fieldClass = fieldClass;
        this.originObject = originObject;
        this.watchType = WatchType.FIELD;
    }

    public WatchCacheNode(Consumer consumer){
        this.watchType = WatchType.PLAIN;
        this.consumer = consumer;
    }

    public WatchType getWatchType() {
        return watchType;
    }

    public void setWatchType(WatchType watchType) {
        this.watchType = watchType;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public void setClassType(Class<?> classType) {
        this.classType = classType;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Class<?> getFieldClass() {
        return fieldClass;
    }

    public void setFieldClass(Class<?> fieldClass) {
        this.fieldClass = fieldClass;
    }

    public Object getOriginObject() {
        return originObject;
    }

    public void setOriginObject(Object originObject) {
        this.originObject = originObject;
    }
}
