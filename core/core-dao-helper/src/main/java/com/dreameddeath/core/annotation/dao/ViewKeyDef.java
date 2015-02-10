package com.dreameddeath.core.annotation.dao;

import com.dreameddeath.core.model.view.IViewKeyTranscoder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ceaj8230 on 10/02/2015.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewKeyDef {
    Class type();
    Class<? extends IViewKeyTranscoder> transcoder();
}
