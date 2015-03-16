package com.dreameddeath.core.tools.annotation.processor.testing;

import java.io.Closeable;
import java.util.Map;

/**
 * Created by ceaj8230 on 09/03/2015.
 */
public interface GenericsTestExistingClass<TREQ,TRES extends Map<String,String> & Closeable> {
    public void methodVoid();

}
