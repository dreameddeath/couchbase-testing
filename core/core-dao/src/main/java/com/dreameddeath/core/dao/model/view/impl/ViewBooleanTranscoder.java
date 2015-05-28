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

package com.dreameddeath.core.dao.model.view.impl;

import com.dreameddeath.core.dao.exception.view.ViewDecodingException;
import com.dreameddeath.core.dao.exception.view.ViewEncodingException;
import com.dreameddeath.core.dao.model.view.IViewTranscoder;

/**
 * Created by Christophe Jeunesse on 27/12/2014.
 */
public class ViewBooleanTranscoder implements IViewTranscoder<Boolean> {
    @Override
    public Object encode(Boolean key) throws ViewEncodingException {
        return key;
    }

    @Override
    public Boolean decode(Object value) throws ViewDecodingException {
        return Boolean.valueOf(value.toString());
    }
}
