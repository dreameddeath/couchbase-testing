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

package com.dreameddeath.core.dao.annotation.processor.model;

import com.google.common.base.Preconditions;
import org.apache.velocity.VelocityContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christophe jeunesse on 31/01/2017.
 */
public class GlobalDef {
    private DaoDef daoDef;
    private EntityDef entity;
    private DbPathDef dbPathDef;
    private final List<CounterDef> counterDefList=new ArrayList<>();
    private final List<ViewDef> viewDefList=new ArrayList<>();


    public DaoDef getDaoDef() {
        return daoDef;
    }

    public void setDaoDef(DaoDef daoDef) {
        this.daoDef = daoDef;
    }

    public EntityDef getEntity() {
        return entity;
    }

    public void setEntity(EntityDef entity) {

        this.entity = entity;
    }

    public DbPathDef getDbPathDef() {
        return dbPathDef;
    }

    public void setDbPathDef(DbPathDef dbPathDef) {
        this.dbPathDef = dbPathDef;
    }

    public List<CounterDef> getCounterDefList() {
        return counterDefList;
    }

    public void setCounterDefList(List<CounterDef> counterDefList) {
        this.counterDefList.clear();
        this.counterDefList.addAll(counterDefList);
    }

    public List<ViewDef> getViewDefList() {
        return viewDefList;
    }

    public void setViewDefList(List<ViewDef> viewDefList) {
        this.viewDefList.clear();
        this.viewDefList.addAll(viewDefList);
    }

    public void fillContext(VelocityContext context){
        context.put("daoDef", daoDef);
        context.put("entity", entity);
        context.put("dbPath", dbPathDef);
        context.put("views", viewDefList);
        context.put("counters", counterDefList);
        long nbKeyGenCounter = counterDefList.stream()
                .filter(CounterDef::isKeyGen)
                .map(counterDef -> {
                    context.put("keyCounter", counterDef);
                    return counterDef;
                })
                .count();

        Preconditions.checkArgument(nbKeyGenCounter<=1,"Cannot have more than one key gen counter");
    }
}
