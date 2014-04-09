package com.dreameddeath.common.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Target(ElementType.TYPE)
@Inherited
public @interface CouchbaseEntity {
}