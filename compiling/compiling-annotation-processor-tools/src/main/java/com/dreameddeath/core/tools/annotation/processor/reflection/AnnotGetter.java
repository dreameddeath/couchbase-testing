package com.dreameddeath.core.tools.annotation.processor.reflection;

import java.lang.annotation.Annotation;

/**
 * Created by CEAJ8230 on 16/03/2015.
 */
public interface AnnotGetter<T extends Annotation> {
    public Class get(T annot);
}
