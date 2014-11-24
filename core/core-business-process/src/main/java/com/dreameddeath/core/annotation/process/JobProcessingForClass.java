package com.dreameddeath.core.annotation.process;

import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.process.service.IJobProcessingService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by CEAJ8230 on 24/11/2014.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JobProcessingForClass {
    Class<? extends AbstractJob> value();
}
