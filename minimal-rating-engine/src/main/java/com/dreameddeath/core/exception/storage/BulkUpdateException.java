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
        StringBuffer buf=new StringBuffer();

        buf.append("<").append(super.getMessage()).append(">");

        if(_childList!=null) {
            for (int childPos = 0; childPos < _childList.size(); ++childPos) {
                buf.append(_childList.get(childPos).formatValidationIssues());
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
