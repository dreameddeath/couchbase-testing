package com.dreameddeath.party.process.model;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.model.process.EmptyJobResult;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.process.business.model.DocumentCreateTask;
import com.dreameddeath.party.model.base.Party;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
@DocumentDef(domain = "party",version="1.0.0")
public class CreatePartyJob extends AbstractJob<CreatePartyRequest,EmptyJobResult> {
    @Override
    public CreatePartyRequest newRequest(){return new CreatePartyRequest();}
    @Override
    public EmptyJobResult newResult(){return new EmptyJobResult();}

    @DocumentDef(domain="party",version="1.0.0")
    public static class CreatePartyTask extends DocumentCreateTask<Party>{
    }
}
