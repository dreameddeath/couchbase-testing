package com.dreameddeath.party.process;

import com.dreameddeath.core.event.TaskProcessEvent;
import com.dreameddeath.core.process.common.AbstractJob;
import com.dreameddeath.core.process.document.DocumentCreateTask;
import com.dreameddeath.core.model.process.EmptyJobResult;
import com.dreameddeath.party.model.base.Organization;
import com.dreameddeath.party.model.base.Party;
import com.dreameddeath.party.model.base.Person;
import com.dreameddeath.party.model.process.CreatePartyRequest;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class CreatePartyJob extends AbstractJob<CreatePartyRequest,EmptyJobResult> {
    @Override
    public CreatePartyRequest newRequest(){return new CreatePartyRequest();}
    @Override
    public EmptyJobResult newResult(){return new EmptyJobResult();}

    @Override
    public boolean init(){
        addTask(new CreatePartyTask());
        return false;
    }

    @Override
    public boolean when(TaskProcessEvent evt){
        return false;
    }

    public static class CreatePartyTask extends DocumentCreateTask<Party>{
        @Override
        public Party buildDocument(){
            Party result;
            CreatePartyRequest req = getParentJob(CreatePartyJob.class).getRequest();
            if(req.type == CreatePartyRequest.Type.person){
                Person person=newEntity(Person.class);

                person.setFirstName(req.person.firstName);
                person.setLastName(req.person.lastName);

                result = person;
            }
            else{
                Organization organization = newEntity(Organization.class);
                organization.setBrand(req.organization.brand);
                organization.setTradingName(req.organization.tradingName);
                result = organization;
            }

            return result;
        }
    }
}
