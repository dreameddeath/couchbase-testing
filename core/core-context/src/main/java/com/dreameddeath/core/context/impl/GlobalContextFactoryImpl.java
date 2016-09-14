/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.context.impl;

import com.dreameddeath.core.context.*;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.user.IUserFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 12/09/2016.
 */
public class GlobalContextFactoryImpl implements IContextFactory {
    private static final String PREFIX_UID="TID_";
    private IUserFactory userFactory;
    private final ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();

    @Autowired
    public void setUserFactory(IUserFactory userFactory) {
        this.userFactory = userFactory;
    }

    @Override
    public String encode(IGlobalContext ctxt) {
        try {
            return new String(Base64.encodeBase64(mapper.writeValueAsBytes(ctxt)));
        }
        catch(JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public IGlobalContext decode(String encodedContext) {
        try {
            IGlobalContext readContext = mapper.readValue(Base64.decodeBase64(encodedContext), GlobalContextImpl.class);
            if(readContext.userCtxt()!=null && readContext.userCtxt().getUser()==null){
                String token = readContext.userCtxt().getToken();
                IUser user=userFactory.fromToken(token);
                Preconditions.checkNotNull(token,"User and token null");
                return buildContext(new IGlobalContext.Builder()
                        .withUserContextBuilder(IUserContext.builder().withUser(user).withToken(token))
                        .withCallerContext(readContext.callerCtxt())
                        .withExternalContext(readContext.externalCtxt())
                        .withGlobalTraceId(readContext.globalTraceId()));
            }
            else {
                return readContext;
            }
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }
    }


    public static String buildDefaultTraceId(){
        return PREFIX_UID+UUID.randomUUID().toString();
    }

    @Override
    public String buildTraceId() {
        return buildDefaultTraceId();
    }

    @Override
    public IGlobalContext buildDefaultContext() {
        return buildContext(new IGlobalContext.Builder());
    }

    @Override
    public IGlobalContext buildContext(IGlobalContext.Builder builder) {
        String traceId=builder.getTraceId();
        String globalTraceId=builder.getGlobalTraceId();
        ICallerContext callerContext=null;
        IExternalCallerContext externalCallerContext=null;
        IUserContext userContext=null;
        if(traceId==null){
            traceId=buildTraceId();
        }

        if(globalTraceId==null){
            globalTraceId=traceId;
        }

        if(builder.getCallerContextBuilder()!=null){
            callerContext = new CallerContextImpl(builder.getCallerContextBuilder());
        }

        if(builder.getExternalContextBuilder()!=null){
            externalCallerContext=new ExternalContextImpl(builder.getExternalContextBuilder());
        }
        if(builder.getUserContextBuilder()!=null){
            IUser user = builder.getUserContextBuilder().getUser();
            String token=builder.getUserContextBuilder().getToken();
            if(user==null){
                Preconditions.checkNotNull(token,"Either the user or the token must be defined in the context builder");
                user = userFactory.fromToken(token);
            }
            if (token == null) {
                token=userFactory.toToken(user);
            }

            userContext = new UserContextImpl(IUserContext.builder().withUser(user).withToken(token));
        }
        else{
            IUser defaultUser=userFactory.defaultUser();
            String token=userFactory.toToken(defaultUser);
            userContext = new UserContextImpl(
                    IUserContext.builder().withUser(defaultUser).withToken(token));
        }

        //String traceId
        return new GlobalContextImpl(traceId,globalTraceId,callerContext,externalCallerContext,userContext);
    }

    @Override
    public IGlobalContext buildContext(IUser user) {
        return buildContext(new IGlobalContext.Builder().withUserContextBuilder(IUserContext.builder().withUser(user)));
    }

}
