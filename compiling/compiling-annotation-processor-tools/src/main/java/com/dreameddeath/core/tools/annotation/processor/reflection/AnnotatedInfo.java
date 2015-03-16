package com.dreameddeath.core.tools.annotation.processor.reflection;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * Created by ceaj8230 on 07/03/2015.
 */
public class AnnotatedInfo {
    private AnnotatedElement _annotElt=null;
    private Element _element=null;


    public AnnotatedInfo(AnnotatedElement elt){
        _annotElt =elt;
    }

    public AnnotatedInfo(Element elt){
        _element= elt;
    }

    public <A extends Annotation> A getAnnotation(Class<A> clazz){
        if(_annotElt!=null){
            return _annotElt.getAnnotation(clazz);
        }
        else{
            return _element.getAnnotation(clazz);
        }
    }

    public <A extends Annotation> A[] getAnnotationByType(Class<A> clazz){
        if(_annotElt!=null){
            return _annotElt.getAnnotationsByType(clazz);
        }
        else{
            return _element.getAnnotationsByType(clazz);
        }
    }
}
