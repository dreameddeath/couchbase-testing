/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.exception.storage;

import java.util.List;

/**
 * Created by ceaj8230 on 13/09/2014.
 */
public class BulkUpdateException extends StorageException {
    List<BulkUpdateException> _childList;

    public BulkUpdateException(String message,List<BulkUpdateException> listChildException){
        super(message);
        _childList=listChildException;
    }

    public BulkUpdateException(String message,Throwable e){
        super(message,e);
    }
    public BulkUpdateException(String message){
        super(message);
    }

    public String formatValidationIssues(){
        StringBuilder buf=new StringBuilder();

        buf.append("<").append(super.getMessage()).append(">");

        if(_childList!=null) {
            for (BulkUpdateException a_childList : _childList) {
                buf.append(a_childList.formatValidationIssues());
            }
        }
        else if(getCause()!=null){
            buf.append(getCause().getMessage()).append("\n");
        }

        buf.append("\n");
        return buf.toString();
    }
    public String getMessage(){
        return formatValidationIssues();
    }

}
