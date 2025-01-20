package com.github.istin.dmtools.job;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractJob<Params> implements Job<Params>{

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Class<Params> getParamsClass() {
        return (Class<Params>) getTemplateParameterClass(getClass());
    }

    private Class<?> getTemplateParameterClass(Class<?> clazz) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0) {
                return (Class<?>) typeArguments[0];
            }
        }
        throw new IllegalArgumentException("Class does not have a template parameter.");
    }

}
