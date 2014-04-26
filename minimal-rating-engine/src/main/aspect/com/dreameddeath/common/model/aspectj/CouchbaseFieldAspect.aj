package com.dreameddeath.common.model.aspectj;


import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.dreameddeath.common.model.CouchbaseDocumentArrayList;
import com.dreameddeath.common.model.CouchbaseDocumentElement;
import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentLink;
//import com.dreameddeath.common.annotation.CollectionField;
import java.lang.reflect.ParameterizedType;



public aspect CouchbaseFieldAspect { 
	
    before() : (call(* remove*(..)) || call(* add*(..)) || call(* clear())) && target(CouchbaseDocumentArrayList+) && within(CouchbaseDocumentElement+) {
		CouchbaseDocument source=((CouchbaseDocumentElement)thisJoinPoint.getThis()).getParentDocument();
        if(source!=null){
            source.setStateDirty();
        }
        
        //System.out.println("Changing array in "+thisJoinPoint.getThis().getClass().getName()+" of "+
        //        thisJoinPoint.getTarget().getClass().getName()+"."+thisJoinPoint.getSignature().getName());
	}

    before(Object o) : set(@JsonProperty (!List) *) && args(o) && within(CouchbaseDocumentElement+) {
        CouchbaseDocument source=((CouchbaseDocumentElement)thisJoinPoint.getThis()).getParentDocument();
        if(source!=null){
            source.setStateDirty();
        }
        //System.out.println("Changing value of "+thisJoinPoint.getThis().getClass().getName()+"."+thisJoinPoint.getSignature().getName());
	}
    
    before(CouchbaseDocumentLink o) : set(CouchbaseDocumentLink+ *) && args(o) && within(CouchbaseDocumentElement+) {
        o.setParentElement((CouchbaseDocumentElement)thisJoinPoint.getThis());
        System.out.println("Set Link value of "+thisJoinPoint.getThis().getClass().getName()+"."+thisJoinPoint.getSignature().getName());
	} 

}