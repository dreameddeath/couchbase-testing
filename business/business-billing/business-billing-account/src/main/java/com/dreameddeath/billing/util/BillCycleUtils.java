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

package com.dreameddeath.billing.util;

import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 03/08/2014.
 */
public class BillCycleUtils {
    public static DateTime CalcCycleEndDate(DateTime startDate, Integer bDom,Integer cycleLengh){
        DateTime endDate = startDate.withTime(0,0,0,0).plusMonths(cycleLengh);

        while(endDate.dayOfMonth().get()!=bDom){
            endDate = endDate.minusDays(1);
        }
        return endDate;
    }
}
