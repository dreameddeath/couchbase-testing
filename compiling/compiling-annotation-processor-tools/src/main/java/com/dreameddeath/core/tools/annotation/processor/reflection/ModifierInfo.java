package com.dreameddeath.core.tools.annotation.processor.reflection;

/**
 * Created by CEAJ8230 on 16/03/2015.
 */
public enum ModifierInfo {
    PUBLIC,
    PRIVATE,
    PROTECTED;

    public static ModifierInfo valueOf(javax.lang.model.element.Modifier modifier){
        switch(modifier){
            case PUBLIC:
                return PUBLIC;
            case PRIVATE:
                return PRIVATE;
            case PROTECTED:
                return PROTECTED;
        }
        return null;
    }
}
