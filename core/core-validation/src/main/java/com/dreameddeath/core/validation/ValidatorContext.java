package com.dreameddeath.core.validation;

import com.dreameddeath.core.model.property.HasParent;
import com.dreameddeath.core.session.ICouchbaseSession;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ceaj8230 on 20/11/2014.
 */
public class ValidatorContext {
    private ICouchbaseSession _session;
    private List<HasParent> _stack = new LinkedList<HasParent>();

    public ICouchbaseSession getSession(){return _session;}
    public void setSession(ICouchbaseSession session){ _session = session;}

    public void push(HasParent parent){
        _stack.add(0,parent);
    }

    public HasParent head(){
        return _stack.get(0);
    }

    public HasParent pop(){
        return _stack.remove(0);
    }

    public static ValidatorContext buildContext(ICouchbaseSession session){
        ValidatorContext context = new ValidatorContext();
        context.setSession(session);
        return context;
    }
}