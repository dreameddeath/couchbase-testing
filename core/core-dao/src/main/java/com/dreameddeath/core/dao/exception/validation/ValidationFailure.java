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

package com.dreameddeath.core.dao.exception.validation;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class ValidationFailure {
    private final Throwable e;
    private final String message;

    public ValidationFailure(Throwable e){
        this(null,e);
    }

    public ValidationFailure(String message, Throwable e){
        this.e = e;
        this.message = message;
    }

    public ValidationFailure(String message){ this(message,null);}

    public Throwable getCause(){
        return e;
    }

    public String getMessage(){
        return message;
    }

    public boolean hasError(){
        return this.getCause()!=null;
    }
}
