package com.dreameddeath.core.service.context;

/**
 * Created by CEAJ8230 on 17/03/2015.
 */
public interface IGlobalContextTranscoder {
    public String encode(IGlobalContext ctxt);
    public IGlobalContext decode(String encodedContext);
}
