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

package com.dreameddeath.core.date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Created by Christophe Jeunesse on 22/01/2015.
 */
public interface IDateTimeService {
    DateTime MIN_TIME = new DateTime( 0, 1, 1, 0, 0, 0, DateTimeZone.UTC );
    DateTime MAX_TIME = new DateTime(292278993, 12, 31, 23, 59, 59);

    default boolean isMin(DateTime date){
        return date.isEqual(MIN_TIME);
    }
    default boolean isMax(DateTime date){
        return date.isEqual(MAX_TIME);
    }

    default DateTime now(){
        return DateTime.now();
    }

    default DateTime min(){
        return MIN_TIME;
    }
    default DateTime max(){
        return MAX_TIME;
    }

    DateTime getCurrentDate();

}
