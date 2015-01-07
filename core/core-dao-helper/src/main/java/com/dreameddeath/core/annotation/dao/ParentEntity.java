package com.dreameddeath.core.annotation.dao;

import com.dreameddeath.core.model.document.CouchbaseDocument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by CEAJ8230 on 06/01/2015.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParentEntity {
    Class<? extends CouchbaseDocument> c();
    String keyPath();
    String separator();
}
