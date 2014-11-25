package com.dreameddeath.core.annotation.dao;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.storage.impl.GenericCouchbaseTranscoder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by CEAJ8230 on 24/11/2014.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DaoForClass {
    Class<? extends CouchbaseDocument> value();
    Class<? extends GenericCouchbaseTranscoder> withTranscoder() default GenericCouchbaseTranscoder.class;
}
