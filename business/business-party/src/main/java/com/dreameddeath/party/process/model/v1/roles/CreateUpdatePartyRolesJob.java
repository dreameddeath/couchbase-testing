/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.party.process.model.v1.roles;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.couchbase.core.process.remote.annotation.Request;
import com.dreameddeath.couchbase.core.process.remote.annotation.RestExpose;
import com.dreameddeath.couchbase.core.process.remote.annotation.Result;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 09/05/2016.
 */
@DocumentEntity @RestExpose(rootPath = "partyjobs/createupdateroles",domain = "party",name = "createupdatepartyrolesjob")
public class CreateUpdatePartyRolesJob extends AbstractJob {
    /**
     *  roles : roles to create or update
     */
    @DocumentProperty("roleRequests") @Request
    private ListProperty<CreateUpdateRoleRequest> roleRequests = new ArrayListProperty<>(CreateUpdatePartyRolesJob.this);
    /**
     *  results : result of roles create
     */
    @DocumentProperty("results") @Result
    private ListProperty<CreateUpdateRoleResult> results = new ArrayListProperty<>(CreateUpdatePartyRolesJob.this);

    /**
     * Getter of roles
     * @return the whole (immutable) list of roles
     */
    public List<CreateUpdateRoleRequest> getRoleRequests() { return roleRequests.get(); }
    /**
     * Setter of roles
     * @param vals the new collection of values
     */
    public void setRoleRequests(Collection<CreateUpdateRoleRequest> vals) { roleRequests.set(vals); }
    /**
     * Add a new entry to the property roles
     * @param val the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addRoleRequest(CreateUpdateRoleRequest val){ return roleRequests.add(val); }
    /**
     * Add a new entry to the property roles at the specified position
     * @param index the new entry to be added
     * @param val the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addRoleRequest(int index,CreateUpdateRoleRequest val){ return roleRequests.add(val); }
    /**
     * Remove an entry to the property roles
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeRoleRequest(CreateUpdateRoleRequest val){ return roleRequests.remove(val); }
    /**
     * Remove an entry to the property roles at the specified position
     * @param index the position of element to be removed
     * @return the entry removed if any
     */
    public CreateUpdateRoleRequest removeRoleRequest(int index){ return roleRequests.remove(index); }

    /**
     * Getter of results
     * @return the whole (immutable) list of results
     */
    public List<CreateUpdateRoleResult> getResults() { return results.get(); }
    /**
     * Setter of results
     * @param newResults the new collection of results
     */
    public void setResults(Collection<CreateUpdateRoleResult> newResults) { results.set(newResults); }
    /**
     * Add a new entry to the property results
     * @param newResults the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addResults(CreateUpdateRoleResult newResults){ return results.add(newResults); }
    /**
     * Add a new entry to the property results at the specified position
     * @param index the new entry to be added
     * @param newResults the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addResults(int index,CreateUpdateRoleResult newResults){ return results.add(newResults); }
    /**
     * Remove an entry to the property results
     * @param oldResults the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeResults(CreateUpdateRoleResult oldResults){ return results.remove(oldResults); }
    /**
     * Remove an entry to the property results at the specified position
     * @param index the position of element to be removed
     * @return the entry removed if any
     */
    public CreateUpdateRoleResult removeResults(int index){ return results.remove(index); }


}
