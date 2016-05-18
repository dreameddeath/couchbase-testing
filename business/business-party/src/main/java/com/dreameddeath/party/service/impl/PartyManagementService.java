package com.dreameddeath.party.service.impl;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.party.model.v1.Organization;
import com.dreameddeath.party.model.v1.Party;
import com.dreameddeath.party.model.v1.PartyRole;
import com.dreameddeath.party.model.v1.Person;
import com.dreameddeath.party.model.v1.roles.BillingAccountPartyRole;
import com.dreameddeath.party.process.model.v1.party.CreateUpdatePartyRequest;
import com.dreameddeath.party.process.model.v1.roles.BillingAccountCreateUpdateRoleRequest;
import com.dreameddeath.party.process.model.v1.roles.CreateUpdateRoleRequest;
import com.dreameddeath.party.process.model.v1.roles.tasks.PartyRolesUpdateResult;
import com.dreameddeath.party.process.model.v1.roles.tasks.PartyUpdateResult;
import com.dreameddeath.party.service.IPartyManagementService;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 10/05/2016.
 */
public class PartyManagementService implements IPartyManagementService {
    @Override
    public Party managePartyCreation(ICouchbaseSession session,CreateUpdatePartyRequest request) {
        Party result;
        if(request.type == CreateUpdatePartyRequest.Type.person){
            result=session.newEntity(Person.class);
        }
        else{
            result = session.newEntity(Organization.class);
        }
        managePartyUpdate(request,result);
        return result;
    }

    @Override
    public PartyUpdateResult managePartyUpdate(CreateUpdatePartyRequest request, Party party) {
        PartyUpdateResult result = new PartyUpdateResult();
        if(request.type == CreateUpdatePartyRequest.Type.person){
            Preconditions.checkArgument(party instanceof Person);
            Person person = (Person)party;
            if(request.person.firstName!=null) {
                person.setFirstName(request.person.firstName);
            }
            if(request.person.lastName!=null) {
                person.setLastName(request.person.lastName);
            }
        }
        else{
            Preconditions.checkArgument(party instanceof Organization);
            Organization organization = (Organization)party;
            if(request.organization.brand!=null) {
                organization.setBrand(request.organization.brand);
            }
            if(request.organization.tradingName!=null) {
                organization.setTradingName(request.organization.tradingName);
            }
        }

        return result;
    }

    @Override
    public PartyRolesUpdateResult managePartyRolesUpdate(List<CreateUpdateRoleRequest> roleUpdateList, Party party) {
        PartyRolesUpdateResult result = new PartyRolesUpdateResult();
        for(CreateUpdateRoleRequest request:roleUpdateList){
            if(party.getUid().equals(request.getPartyId())){
                if(request instanceof BillingAccountCreateUpdateRoleRequest){
                    manageCreateUpdateBillingAccountRoles(result,(BillingAccountCreateUpdateRoleRequest)request,party);
                }
            }
        }
        return result;
    }

    private void manageCreateUpdateBillingAccountRoles(PartyRolesUpdateResult result, BillingAccountCreateUpdateRoleRequest request, Party party) {
        String baId=request.getBaId();
        BillingAccountPartyRole existingRole=null;
        for(BillingAccountPartyRole role:getPartyRoles(party,BillingAccountPartyRole.class)){
            if(role.getBaUid().equals(baId)){
                if(existingRole!=null){
                    throw new RuntimeException("Duplicate BaId "+ baId+" in party "+party.getUid());
                }
                existingRole=role;
            }
        }
        if(existingRole==null){
            existingRole = new BillingAccountPartyRole();
            party.addPartyRole(existingRole);
            existingRole.setBaUid(baId);
        }

        List<BillingAccountPartyRole.RoleType> typesToAdd=new ArrayList<>(request.getTypes().size());
        for(BillingAccountPartyRole.RoleType type :request.getTypes()){
            if(!existingRole.getRoles().contains(type)){
                typesToAdd.add(type);
            }
        }
        for(BillingAccountPartyRole.RoleType role:typesToAdd){
            existingRole.addRole(role);
        }

    }


    private <T extends PartyRole> List<T> getPartyRoles(Party party,Class<T> roleClazz){
        return party.getPartyRoles().stream().filter(role-> roleClazz.isAssignableFrom(role.getClass())).map(role->(T)role).collect(Collectors.toList());
    }

}
