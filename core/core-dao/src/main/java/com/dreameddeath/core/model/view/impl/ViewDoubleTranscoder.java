package com.dreameddeath.core.model.view.impl;

import com.dreameddeath.core.exception.view.ViewDecodingException;
import com.dreameddeath.core.exception.view.ViewEncodingException;
import com.dreameddeath.core.model.view.IViewTranscoder;

/**
 * Created by CEAJ8230 on 27/12/2014.
 */
public class ViewDoubleTranscoder implements IViewTranscoder<Double> {
    @Override
    public Object encode(Double key) throws ViewEncodingException {
        return key;
    }

    @Override
    public Double decode(Object value) throws ViewDecodingException {
        return ((Number)value).doubleValue();
    }
}
