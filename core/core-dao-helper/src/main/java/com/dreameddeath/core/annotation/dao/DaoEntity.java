package com.dreameddeath.core.annotation.dao;

import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by CEAJ8230 on 29/12/2014.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DaoEntity {
    Class<? extends CouchbaseDocumentDao> baseDao();
    String dbPath();
    String idFormat();
    String idPattern();
}
