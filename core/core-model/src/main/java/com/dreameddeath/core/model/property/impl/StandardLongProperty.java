/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.property.HasParent;
import com.dreameddeath.core.model.property.NumericProperty;

/**
 * Created by ceaj8230 on 14/08/2014.
 */
public class StandardLongProperty extends StandardProperty<Long> implements NumericProperty<Long> {
    public StandardLongProperty(HasParent parent){  super(parent);}
    public StandardLongProperty(HasParent parent, Number defaultValue){super(parent,defaultValue.longValue());}

    public StandardLongProperty inc(Number byVal){set(get()+byVal.longValue());return this;}
    public StandardLongProperty dec(Number byVal){set(get()-byVal.longValue());return this;}
    public StandardLongProperty mul(Number byVal){set(get()*byVal.longValue());return this;}
    public StandardLongProperty div(Number byVal){set(get()/byVal.longValue());return this;}
}
