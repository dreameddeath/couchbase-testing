package com.dreameddeath.common.model.aspectj;


import java.util.Collection;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.dreameddeath.common.annotation.CouchbaseField;
import com.dreameddeath.common.annotation.CouchbaseEntity;
import com.dreameddeath.common.annotation.CouchbaseCollectionField;
import com.dreameddeath.common.model.CouchbaseDocumentArrayList;
import com.dreameddeath.common.model.CouchbaseDocumentList;
import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentLink;

public aspect CouchbaseFieldAspect { 
	
    before() : (call(* (@CouchbaseCollectionField *).add*(..))||call(* (@CouchbaseCollectionField *).clear())||call(* (@CouchbaseCollectionField *).remove(..))) && within(CouchbaseDocument+ || CouchbaseDocumentLink+) {
		//System.out.println("Match of "+thisJoinPoint.getSignature().getName());
        ((CouchbaseDocument)thisJoinPoint.getThis()).setStateDirty();
	} 
    
    
    /*after() : (call(* remove*(..)) || call(* add*(..)) || call(* clear())) && target(CouchbaseDocumentArrayList+) && within(CouchbaseDocument+ || CouchbaseDocumentLink+) {
		((CouchbaseDocument)thisJoinPoint.getThis()).setStateDirty();
        //System.out.println("Changing array of "+thisJoinPoint.getSignature().getName());
	} */
    
	before(Object o) : set(@(CouchbaseField || JsonProperty) (!@CouchbaseCollectionField *) *) && args(o) && within(CouchbaseDocument+) {
        ((CouchbaseDocument)thisJoinPoint.getThis()).setStateDirty();
        //System.out.println("Changing value of "+thisJoinPoint.getThis().getClass().getName()+"."+thisJoinPoint.getSignature().getName());
	} 

    before(Object o) : set(@(CouchbaseField || JsonProperty) (!@CouchbaseCollectionField *) *) && args(o) && within(CouchbaseDocumentLink+) {
        CouchbaseDocument source=((CouchbaseDocumentLink)thisJoinPoint.getThis()).getSourceObject();
        if(source!=null){
            ((CouchbaseDocumentLink)thisJoinPoint.getThis()).getSourceObject().setStateDirty();
        }
        //System.out.println("Changing link value of "+thisJoinPoint.getThis().getClass().getName()+"."+thisJoinPoint.getSignature().getName());
	} 

}