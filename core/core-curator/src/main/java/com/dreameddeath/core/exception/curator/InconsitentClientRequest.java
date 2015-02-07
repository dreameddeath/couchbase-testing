package com.dreameddeath.core.exception.curator;

/**
 * Created by CEAJ8230 on 06/02/2015.
 */
public class InconsitentClientRequest extends Exception {
    private String _existingNameSpace;
    private String _requestedNameSpace;

    public InconsitentClientRequest(String existingNameSpace,String requestedNameSpace){
        super("The requested namespace <"+requestedNameSpace+"> is inconsistent with "+existingNameSpace);
        _existingNameSpace = existingNameSpace;
        _requestedNameSpace = requestedNameSpace;
    }


    public String getExistingNameSpace() {
        return _existingNameSpace;
    }

    public String getRequestedNameSpace() {
        return _requestedNameSpace;
    }
}
