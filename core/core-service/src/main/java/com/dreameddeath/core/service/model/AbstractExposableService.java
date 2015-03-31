package com.dreameddeath.core.service.model;

import com.dreameddeath.core.service.registrar.IRestEndPointDescription;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;

/**
 * Created by CEAJ8230 on 15/01/2015.
 */
public abstract class AbstractExposableService {
    public AbstractExposableService(){
        ServiceRegistrar.addService(this);
    }
    
    private IRestEndPointDescription endPoint;
    public void setEndPoint(IRestEndPointDescription obj){
        endPoint = obj;
    }

    public IRestEndPointDescription getEndPoint() {
        return endPoint;
    }
}
