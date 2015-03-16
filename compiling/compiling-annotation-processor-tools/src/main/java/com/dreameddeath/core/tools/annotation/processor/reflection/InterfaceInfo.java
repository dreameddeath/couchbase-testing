package com.dreameddeath.core.tools.annotation.processor.reflection;

import javax.lang.model.element.TypeElement;

/**
 * Created by ceaj8230 on 07/03/2015.
 */
public class InterfaceInfo extends AbstractClassInfo {
    public InterfaceInfo(TypeElement element) {
        super(element);
    }

    public InterfaceInfo(Class<?> clazz){
        super(clazz);
    }

    @Override
    public boolean isInterface(){
        return true;
    }
}
