/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.curator.exception;

/**
 * Created by Christophe Jeunesse on 06/02/2015.
 */
public class InconsitentClientRequest extends Exception {
    private String existingNameSpace;
    private String requestedNameSpace;

    public InconsitentClientRequest(String existingNameSpace,String requestedNameSpace){
        super("The requested namespace <"+requestedNameSpace+"> is inconsistent with "+existingNameSpace);
        this.existingNameSpace = existingNameSpace;
        this.requestedNameSpace = requestedNameSpace;
    }


    public String getExistingNameSpace() {
        return existingNameSpace;
    }

    public String getRequestedNameSpace() {
        return requestedNameSpace;
    }
}
