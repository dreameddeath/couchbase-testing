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

package com.dreameddeath.core.process.exception;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 20/11/2014.
 */
public class DuplicateAttachedTaskException extends Exception {
    private String taskUid;
    private String jobKey;
    private CouchbaseDocument doc;


    public DuplicateAttachedTaskException(CouchbaseDocument doc, String jobKey,String taskUid){
        this(doc,jobKey,taskUid,"The task <"+taskUid+"> is already existing of job <"+jobKey+">");
    }

    public DuplicateAttachedTaskException(CouchbaseDocument doc,String jobKey, String taskUid,  String message){
        super(message);
        this.doc = doc;
        this.jobKey = jobKey;
        this.taskUid = taskUid;
    }

}
