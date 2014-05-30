package com.dreameddeath.common.service;

import com.dreameddeath.common.model.process.AbstractTask;

/**
 * Created by ceaj8230 on 21/05/2014.
 */
public interface TaskProcessingService<T extends AbstractTask> {
    public void init(T task);
    public void process(T task);
    public void retry(T task);
    public void finalize(T task);
    public void cleanup(T task);

    public void execute(T task);
    public boolean isRunnable(T task);
}
