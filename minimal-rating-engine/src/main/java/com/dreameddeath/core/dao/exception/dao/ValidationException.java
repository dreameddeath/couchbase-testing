/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.dao.exception.dao;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class ValidationException extends DaoException {
    BaseCouchbaseDocumentElement _docElt;
    AccessibleObject _field;
    Object _value;
    Long _iterablePos;
    List<ValidationException> _childList;

    public ValidationException(BaseCouchbaseDocumentElement docElt,AccessibleObject field,String message,List<ValidationException> listChildException){
        super(message);
        _docElt = docElt;
        _field = field;
        _childList = listChildException;
    }

    public ValidationException(BaseCouchbaseDocumentElement docElt,String message,List<ValidationException> listChildException){
        super(message);
        _docElt = docElt;
        _childList = listChildException;
    }

    public ValidationException(BaseCouchbaseDocumentElement docElt,Long iterablePos,String message,List<ValidationException> listChildException){
        super(message);
        _docElt = docElt;
        _iterablePos = iterablePos;
        _childList = listChildException;
    }


    public ValidationException(BaseCouchbaseDocumentElement docElt,AccessibleObject field,String message){
        super(message);
        _docElt = docElt;
        _field = field;
    }

    public ValidationException(BaseCouchbaseDocumentElement docElt,AccessibleObject field,String message,Throwable e){
        super(message,e);
        _docElt = docElt;
        _field = field;
    }

    public ValidationException(BaseCouchbaseDocumentElement docElt,AccessibleObject field,Throwable e){
        super(e);
        _docElt = docElt;
        _field = field;
    }



    public ValidationException(BaseCouchbaseDocumentElement docElt,AccessibleObject field,Object value,String message){
        super(message);
        _docElt = docElt;
        _field = field;
        _value = value;
    }

    public ValidationException(BaseCouchbaseDocumentElement docElt,AccessibleObject field,Object value,String message,Throwable e){
        super(message,e);
        _docElt = docElt;
        _field = field;
        _value = value;
    }

    public ValidationException(BaseCouchbaseDocumentElement docElt,AccessibleObject field,Object value,Throwable e){
        super(e);
        _docElt = docElt;
        _field = field;
        _value = value;
    }

    @SuppressWarnings("StringBufferMayBeStringBuilder")
    public String formatValidationIssues(AccessibleObject parentField,int level){
        StringBuffer buf=new StringBuffer(level*2);
        boolean hasSubBlock=false;
        for(int i=0;i<level;++i){buf.append("  ");}

        if(_field==null){
            buf.append(_docElt.getClass().getSimpleName()).append(" ");
            if(_docElt instanceof CouchbaseDocument){
                buf.append("The document [");
                String key = ((CouchbaseDocument) _docElt).getBaseMeta().getKey();
                if(key!=null){ buf.append(key); }
                else{ buf.append("NEW");}
                buf.append("] ");
            }
            else{
                buf.append("The object ");
            }
            hasSubBlock=true;
        }
        else if(!_field.equals(parentField)){
            if(_field instanceof Field){
                buf.append("The field ");
                DocumentProperty docProp = _field.getAnnotation(DocumentProperty.class);
                if(docProp!=null){buf.append(docProp.value());}
                else {buf.append(((Field) _field).getName());}
                buf.append(" ");
                hasSubBlock=true;
            }
            else if(_field instanceof Method){
                buf.append("The method ");
                buf.append(((Method)_field).getName()).append("() ");
                hasSubBlock=true;
            }
        }
        else if(_iterablePos!=null){
            buf.append("[").append(_iterablePos).append("] ");
            hasSubBlock=true;
        }

        buf.append("<").append(super.getMessage()).append(">");

        if(hasSubBlock){ buf.append(" {\n"); }

        if(_childList!=null) {
            for (ValidationException a_childList : _childList) {
                buf.append(a_childList.formatValidationIssues(_field, level + 1));
            }
        }
        else if(getCause()!=null){
            buf.append(getCause().getMessage()).append("\n");
        }

        if(hasSubBlock){
            for(int i=0;i<level;++i){buf.append("  ");}
            buf.append("}");
        }

        buf.append("\n");
        return buf.toString();
    }

    public String getMessage(){
        return formatValidationIssues(null,0);
    }

}
