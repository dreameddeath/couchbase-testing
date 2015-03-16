package com.dreameddeath.core.tools.annotation.processor.reflection;

import javax.lang.model.element.VariableElement;
import java.lang.reflect.Field;

/**
 * Created by ceaj8230 on 07/03/2015.
 */
public class FieldInfo extends MemberInfo {
    private final AbstractClassInfo _parent;
    private String _name;
    private VariableElement _variableElement=null;
    private Field _field=null;
    private ParameterizedTypeInfo _typeInfo=null;

    protected void init(){
        if(_field!=null){
            _name = _field.getName();
            _typeInfo = new ParameterizedTypeInfo(_field.getGenericType());
        }

        if(_variableElement!=null){
            _name = _variableElement.getSimpleName().toString();
            _typeInfo = new ParameterizedTypeInfo(_variableElement.asType());
        }
    }

    public FieldInfo(ClassInfo parent,VariableElement element){
        super(parent,element);
        _parent = parent;
        _variableElement=element;
        init();
    }

    public FieldInfo(ClassInfo parent,Field field){
        super(parent,field);
        _parent = parent;
        _field = field;
        init();
    }

    public String getName(){
        return _name;
    }

    public String getFullName(){
        return _parent.getFullName()+"."+getName();
    }

    public ParameterizedTypeInfo getType() {
        return _typeInfo;
    }

}
