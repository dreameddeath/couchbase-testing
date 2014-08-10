package com.dreameddeath.party.process;


import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.event.TaskProcessEvent;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.model.process.DocumentCreateTask;
import com.dreameddeath.party.model.Organization;
import com.dreameddeath.party.model.Party;
import com.dreameddeath.party.model.Person;
import com.dreameddeath.party.model.process.CreatePartyRequest;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class CreatePartyJob extends AbstractJob {
    @DocumentProperty("request")
    public CreatePartyRequest request;

    public boolean when(TaskProcessEvent evt){
        return false;
    }

    public boolean init(){
        addTask(new CreatePartyTask());
        return false;
    }

    public static class CreatePartyTask extends DocumentCreateTask<Party>{
        public Party buildDocument(){
            Party result;
            CreatePartyRequest req = getParentJob(CreatePartyJob.class).request;
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
