package com.dreameddeath.core.service.registrar;

/**
 * Created by CEAJ8230 on 15/01/2015.
 */
public abstract class AbstractExposableService {
    public AbstractExposableService(){
        ServiceRegistrar.addService(this);
    }

    private Object endPoint;
    public void setEndPoint(Object obj){
        endPoint = obj;
    }

    public Object getEndPoint() {
        return endPoint;
    }
}
