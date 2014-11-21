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
