package com.dreameddeath.common.service;

import com.dreameddeath.common.model.process.AbstractJob;

/**
 * Created by ceaj8230 on 21/05/2014.
 */
public interface JobProcessingService<T extends AbstractJob> {
    public void init(T job);
    public void preprocess(T job);
    public void process(T job);
    public void postprocess(T job);
    public void cleanup(T job);
    public void execute(T job);
}
