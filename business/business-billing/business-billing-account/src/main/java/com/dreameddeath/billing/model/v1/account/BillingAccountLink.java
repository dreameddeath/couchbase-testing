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

package com.dreameddeath.billing.model.v1.account;


import com.dreameddeath.core.business.model.BusinessDocumentLink;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.query.annotation.QueryExpose;

@QueryExpose(rootPath = "", notDirecltyExposed = true,superClassGenMode = SuperClassGenMode.UNWRAP)
public class BillingAccountLink extends BusinessDocumentLink<BillingAccount> {
    
    public BillingAccountLink(){}
    public BillingAccountLink (BillingAccount ba){
        super(ba);
    }
    public BillingAccountLink(BillingAccountLink srcLink){
        super(srcLink);
    }
    
    @Override
    public String toString(){
        String result = "{\n"+super.toString()+",\n";
        result+="}\n";
        return result;
    }
}