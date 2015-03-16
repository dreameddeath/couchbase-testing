package com.dreameddeath.core.tools.annotation.exception;

import javax.lang.model.element.Element;

/**
 * Created by ceaj8230 on 07/03/2015.
 */
public class AnnotationProcessorException extends Exception{
    private Element _element;
    public AnnotationProcessorException(Element elt,String message){
        super(message);
        _element = elt;
    }
}
