package com.dreameddeath.core.exception;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by ceaj8230 on 20/11/2014.
 */
public class DuplicateAttachedTaskException extends Exception {
    private String _taskUid;
    private String _jobKey;
    private CouchbaseDocument _doc;


    public DuplicateAttachedTaskException(CouchbaseDocument doc, String jobKey,String taskUid){
        this(doc,jobKey,taskUid,"The task <"+taskUid+"> is already existing of job <"+jobKey+">");
    }

    public DuplicateAttachedTaskException(CouchbaseDocument doc,String jobKey, String taskUid,  String message){
        super(message);
        _doc = doc;
        _jobKey = jobKey;
        _taskUid = taskUid;
    }

}
