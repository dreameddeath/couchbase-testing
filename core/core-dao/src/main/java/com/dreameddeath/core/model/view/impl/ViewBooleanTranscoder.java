package com.dreameddeath.core.model.view.impl;

import com.dreameddeath.core.exception.view.ViewDecodingException;
import com.dreameddeath.core.exception.view.ViewEncodingException;
import com.dreameddeath.core.model.view.IViewTranscoder;

/**
 * Created by CEAJ8230 on 27/12/2014.
 */
public class ViewBooleanTranscoder implements IViewTranscoder<Boolean> {
    @Override
    public Object encode(Boolean key) throws ViewEncodingException {
        return key;
    }

    @Override
    public Boolean decode(Object value) throws ViewDecodingException {
        return Boolean.valueOf(value.toString());
    }
}
