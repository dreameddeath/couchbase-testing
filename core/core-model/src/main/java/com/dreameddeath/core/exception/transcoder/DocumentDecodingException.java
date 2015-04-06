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

package com.dreameddeath.core.exception.transcoder;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by ceaj8230 on 16/09/2014.
 */
public class DocumentDecodingException extends RuntimeException{
    private byte[] _data;
    public DocumentDecodingException(String message, byte[] data,Throwable e){
        super(message,e);
        _data = data;
    }

    public String getMessage(){
        StringBuilder result = new StringBuilder(super.getMessage());
        result.append(" with data ");
        try{
            result.append(new String(_data,"UTF-8"));
        }
        catch(UnsupportedEncodingException e){
            result.append(Arrays.toString(_data));
        }
        return result.toString();
    }
}
