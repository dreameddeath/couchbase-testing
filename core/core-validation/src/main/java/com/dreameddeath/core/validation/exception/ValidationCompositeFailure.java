/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.validation.exception;

import com.dreameddeath.core.dao.exception.validation.ValidationFailure;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.HasParent;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class ValidationCompositeFailure extends ValidationFailure {
    private final HasParent docElt;
    private final AccessibleObject field;
    private final Long iterablePos;
    private final List<ValidationFailure> childList=new ArrayList<>();

    public ValidationCompositeFailure(HasParent docElt, AccessibleObject field, String message, List<ValidationFailure> listChildException){
        super(message);
        this.docElt = docElt;
        this.field = field;
        this.childList.addAll(listChildException);
        this.iterablePos=null;
    }

    public ValidationCompositeFailure(HasParent docElt, String message){
        super(message);
        this.docElt = docElt;
        this.field=null;
        this.iterablePos=null;
    }


    public ValidationCompositeFailure(HasParent docElt, String message, List<ValidationFailure> listChildException){
        super(message);
        this.docElt = docElt;
        this.childList.addAll(listChildException);
        this.field=null;
        this.iterablePos=null;
    }

    public ValidationCompositeFailure(HasParent docElt, Long iterablePos, String message, List<ValidationFailure> listChildException){
        super(message);
        this.docElt = docElt;
        this.iterablePos = iterablePos;
        this.childList.addAll(listChildException);
        this.field=null;
    }

    public ValidationCompositeFailure(HasParent docElt, Long iterablePos, String message){
        super(message);
        this.docElt = docElt;
        this.iterablePos = iterablePos;
        this.field=null;
    }


    public ValidationCompositeFailure(HasParent docElt, AccessibleObject field, String message){
        super(message);
        this.docElt = docElt;
        this.field = field;
        this.iterablePos=null;
    }

    public ValidationCompositeFailure(HasParent docElt, AccessibleObject field, String message, Throwable e){
        super(message,e);
        this.docElt = docElt;
        this.field = field;
        this.iterablePos=null;
    }

    public ValidationCompositeFailure(HasParent docElt, AccessibleObject field, Throwable e){
        super(e);
        this.docElt = docElt;
        this.field = field;
        this.iterablePos=null;
    }

    public ValidationCompositeFailure(HasParent docElt, AccessibleObject field, Object value, String message){
        super(message);
        this.docElt = docElt;
        this.field = field;
        this.iterablePos=null;
    }

    public ValidationCompositeFailure(HasParent docElt, AccessibleObject field, Object value, String message, Throwable e){
        super(message,e);
        this.docElt = docElt;
        this.field = field;
        this.iterablePos=null;
    }

    public ValidationCompositeFailure(HasParent docElt, AccessibleObject field, Object value, Throwable e){
        super(e);
        this.docElt = docElt;
        this.field = field;
        this.iterablePos=null;
    }


    public synchronized ValidationCompositeFailure addChildElement(ValidationFailure e){
        if(e instanceof ValidationCompositeFailure && !e.hasError()){
            return this;
        }
        this.childList.add(e);
        return this;
    }

    public <T extends Throwable> T findException(Class<T> exceptionClass){
        for(ValidationFailure e : childList){
            if(e.getCause()!=null && exceptionClass.isAssignableFrom(e.getCause().getClass())){
                return (T) e.getCause();
            }
            else if(e instanceof ValidationCompositeFailure){
                T result = ((ValidationCompositeFailure) e).findException(exceptionClass);
                if(result!=null){
                    return result;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("StringBufferMayBeStringBuilder")
    public String formatValidationIssues(AccessibleObject parentField,int level){
        StringBuilder buf=new StringBuilder(level*2);
        boolean hasSubBlock=false;
        for(int i=0;i<level;++i){buf.append("  ");}

        if(field==null){
            buf.append(docElt.getClass().getSimpleName()).append(" ");
            if(docElt instanceof CouchbaseDocument){
                buf.append("The document [");
                String key = ((CouchbaseDocument) docElt).getBaseMeta().getKey();
                if(key!=null){ buf.append(key); }
                else{ buf.append("NEW");}
                buf.append("] ");
            }
            else{
                buf.append("The object ");
            }
            hasSubBlock=true;
        }
        else if(!field.equals(parentField)){
            if(field instanceof Field){
                buf.append("The field '");
                DocumentProperty docProp = field.getAnnotation(DocumentProperty.class);
                if(docProp!=null){buf.append(docProp.value());}
                else {buf.append(((Field) field).getName());}
                buf.append("' ");
                hasSubBlock=true;
            }
            else if(field instanceof Method){
                buf.append("The method '");
                buf.append(((Method)field).getName()).append("()' ");
                hasSubBlock=true;
            }
        }
        else if(iterablePos!=null){
            buf.append("[").append(iterablePos).append("] ");
            hasSubBlock=true;
        }

        buf.append("<").append(super.getMessage()).append(">");

        if(hasSubBlock){ buf.append(" {\n"); }

        if(childList!=null) {
            for (ValidationFailure a_childList : childList) {
                if(a_childList instanceof ValidationCompositeFailure) {
                    buf.append(((ValidationCompositeFailure) a_childList).formatValidationIssues(field, level + 1));
                }
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

    @Override
    public boolean hasError() {
        return childList.stream().filter(ValidationFailure::hasError).count()>0 || this.getCause()!=null;
    }
}
