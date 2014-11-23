package com.dreameddeath.core.process.service;

import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.model.process.AbstractTask;

/**
 * Created by ceaj8230 on 23/11/2014.
 */
public interface ITaskProcessingService<T extends AbstractTask> {
    public boolean init(TaskContext ctxt,T task) throws TaskExecutionException;
    public boolean preprocess(TaskContext ctxt,T task) throws TaskExecutionException;
    public boolean process(TaskContext ctxt,T task) throws TaskExecutionException;
    public boolean postprocess(TaskContext ctxt,T task) throws TaskExecutionException;
    public boolean finish(TaskContext ctxt,T task) throws TaskExecutionException;
    public boolean cleanup(TaskContext ctxt,T task) throws TaskExecutionException;
}
