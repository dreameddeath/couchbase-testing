package com.dreameddeath.party.service;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.party.model.v1.Party;
import com.dreameddeath.party.process.model.v1.party.CreateUpdatePartyRequest;
import com.dreameddeath.party.process.model.v1.roles.CreateUpdateRoleRequest;
import com.dreameddeath.party.service.model.PartyRolesUpdateResult;
import com.dreameddeath.party.service.model.PartyUpdateResult;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 10/05/2016.
 */
public interface IPartyManagementService {
    Party managePartyCreation(ICouchbaseSession session, CreateUpdatePartyRequest request);
    PartyUpdateResult managePartyUpdate(CreateUpdatePartyRequest request, Party party);
    PartyRolesUpdateResult managePartyRolesUpdate(List<CreateUpdateRoleRequest> roleUpdateList, Party party);
}
