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

package com.dreameddeath.core.dao.model.view.impl;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.view.ViewQuery;
import com.dreameddeath.core.dao.exception.view.ViewDecodingException;
import com.dreameddeath.core.dao.model.view.IViewKeyTranscoder;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Christophe Jeunesse on 02/01/2018.
 */
public class ViewDateTimeKeyTranscoder implements IViewKeyTranscoder<DateTime> {
    private JsonArray convertDateTime(DateTime value){

        return JsonArray.create()
                .add(value.year())
                .add(value.monthOfYear())
                .add(value.dayOfMonth())
                .add(value.hourOfDay())
                .add(value.minuteOfHour())
                .add(value.secondOfMinute());
    }


    @Override
    public void key(ViewQuery query, DateTime value) {
        query.key(convertDateTime(value));
    }

    @Override
    public void keys(ViewQuery query, Collection<DateTime> value) {
        query.keys(
                value.stream()
                        .map(this::convertDateTime)
                        .collect(
                                JsonArray::create,
                                JsonArray::add,
                                (array,array2)->array2.forEach(array::add)
                                )
        );
    }

    @Override
    public void startKey(ViewQuery query, DateTime value) {
        query.startKey(convertDateTime(value));
    }

    @Override
    public void endKey(ViewQuery query, DateTime value) {
        query.endKey(convertDateTime(value));
    }

    @Override
    public Object encode(DateTime key){
        return convertDateTime(key);
    }

    @Override
    public DateTime decode(Object value) throws ViewDecodingException {
        if(value instanceof Iterable){
            return getDateTime((Iterable) value);
        }
        else if(value instanceof String){
            return getDateTime(JsonArray.fromJson((String) value));
        }
        throw new ViewDecodingException(value,"The value isn't an iterable",null);
    }

    public static DateTime getDateTime(Iterable value) {
        DateTime dateTime = new DateTime();
        Iterator iterator = value.iterator();
        int pos=0;
        while(iterator.hasNext()){
            switch (pos){
                case 0:dateTime = dateTime.withYearOfEra(Integer.parseInt(iterator.next().toString()));break;
                case 1:dateTime = dateTime.withMonthOfYear(Integer.parseInt(iterator.next().toString()));break;
                case 2:dateTime = dateTime.withDayOfMonth(Integer.parseInt(iterator.next().toString()));break;
                case 3:dateTime = dateTime.withHourOfDay(Integer.parseInt(iterator.next().toString()));break;
                case 4:dateTime = dateTime.withMinuteOfHour(Integer.parseInt(iterator.next().toString()));break;
                case 5:dateTime = dateTime.withSecondOfMinute(Integer.parseInt(iterator.next().toString()));break;
            }
            ++pos;
        }
        return dateTime;
    }
}
