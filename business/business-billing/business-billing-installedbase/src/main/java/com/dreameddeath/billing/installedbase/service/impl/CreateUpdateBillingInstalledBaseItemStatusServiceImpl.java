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

package com.dreameddeath.billing.installedbase.service.impl;

import com.dreameddeath.billing.installedbase.model.v1.BillingInstalledBaseItem;
import com.dreameddeath.billing.installedbase.model.v1.BillingInstalledBaseItemStatus;
import com.dreameddeath.billing.installedbase.service.ICreateUpdateBillingInstalledBaseItemStatusService;
import com.dreameddeath.billing.installedbase.service.model.v1.CreateUpdateBillingInstalledBaseAction;
import com.dreameddeath.billing.installedbase.service.model.v1.CreateUpdateBillingInstalledBaseItemResult;
import com.dreameddeath.billing.installedbase.service.model.v1.CreateUpdateBillingInstalledBaseItemStatusUpdateResult;
import com.dreameddeath.installedbase.model.v1.common.published.query.CodeResponse;
import com.dreameddeath.installedbase.model.v1.common.published.query.InstalledStatusResponse;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;


/**
 * Created by Christophe Jeunesse on 11/12/2017.
 */
public class CreateUpdateBillingInstalledBaseItemStatusServiceImpl implements ICreateUpdateBillingInstalledBaseItemStatusService {

    public CreateUpdateBillingInstalledBaseAction manageUpdateOfStatuses(BillingInstalledBaseItem billingInstalledBaseItem, List<InstalledStatusResponse> statuses, CreateUpdateBillingInstalledBaseItemResult itemUpdateResult) {
        List<InstalledStatusResponse> sortedStatusResponses = new ArrayList<>(statuses);
        sortedStatusResponses.sort(Comparator.comparing(InstalledStatusResponse::getStartDate));
        Iterator<InstalledStatusResponse> iteratorExpected = sortedStatusResponses.iterator();
        CodeResponse lastResponseCode=null;
        DateTime lastEndDate=null;
        while(iteratorExpected.hasNext()){
            InstalledStatusResponse currResponse = iteratorExpected.next();
            Preconditions.checkArgument(lastResponseCode==null || !lastResponseCode.equals(currResponse.getCode()),"Couldn't have two times the same status consecutively");
            Preconditions.checkArgument(lastEndDate==null || lastEndDate.isEqual(currResponse.getStartDate().minusSeconds(1)),"No time hole between start time and end times");
            lastResponseCode =currResponse.getCode();
            lastEndDate=currResponse.getEndDate();
        }

        ExistingItemsListWrapper existingStatusesWrapper= new ExistingItemsListWrapper(billingInstalledBaseItem,itemUpdateResult);

        int currPosExpected = 0;
        //int currPosExisting = 0;
        while(currPosExpected<sortedStatusResponses.size()){
            InstalledStatusResponse currExpected = sortedStatusResponses.get(currPosExpected);

            if(currExpected.getStartDate().isEqual(currExpected.getEndDate())){
                currPosExpected++;
                continue;
            }
            BillingInstalledBaseItemStatus.Status expectedStatus = mapStatus(currExpected.getCode());
            Preconditions.checkNotNull(expectedStatus,"The status %s isn't managed",currExpected.getCode());

            if(existingStatusesWrapper.currStatus==null){
                existingStatusesWrapper.insertStatus(currExpected);
            }
            //no matching of status
            else if(expectedStatus!=existingStatusesWrapper.currStatus.getStatus()){
                CompareDateRangeResult compareDateRangeResult = compareDateRange(currExpected, existingStatusesWrapper.currStatus);
                if(compareDateRangeResult.needCreateNewItemWhenStatusDiffers){
                    existingStatusesWrapper.insertStatus(currExpected);
                    //buildNewStatus(currExpected,billingInstalledBaseItem,existingStatusesWrapper,currPosExisting);
                }
                switch (compareDateRangeResult){
                    //nothing to do
                    case BEFORE:
                        break;
                    //need to "restart" from the expected item end date(+1)
                    case OVERLAPPING_START:
                    case EARLIER_END_DATE:
                        existingStatusesWrapper.updateStartDate(currExpected.getEndDate().plusSeconds(1));
                        break;
                    //need to clean curr existing item
                    case OLDER_END_DATE:
                    case OVERLAPPING_BOTH:
                    case EARLIER_START_DATE:
                    case EXACT:
                    case AFTER:
                        existingStatusesWrapper.cancelCurrent();
                        existingStatusesWrapper.next();
                        break;
                    //need to close the current item
                    case OVERLAPPING_END:
                    case OLDER_START_DATE:
                    case CONTAINED_INTO:
                        existingStatusesWrapper.updateEndDate(currExpected.getStartDate());
                        existingStatusesWrapper.next();
                        break;
                }
                if(compareDateRangeResult.moveForwardOnExpected){
                    ++currPosExpected;
                }
            }
            //Change of dates
            else{
                switch (compareDateRange(currExpected,existingStatusesWrapper.currStatus)){
                    case EXACT:
                        ++currPosExpected;
                        existingStatusesWrapper.next();
                        break;
                    case AFTER:
                        existingStatusesWrapper.cancelCurrent();
                        existingStatusesWrapper.next();
                        break;
                    case BEFORE:
                        existingStatusesWrapper.insertStatus(currExpected);
                        existingStatusesWrapper.next();
                        break;
                    case OVERLAPPING_START:
                    case EARLIER_END_DATE:
                    case EARLIER_START_DATE:
                    case OLDER_START_DATE:
                    case CONTAINED_INTO:
                        existingStatusesWrapper.updateStartDate(currExpected.getStartDate());
                        existingStatusesWrapper.updateEndDate(currExpected.getEndDate());
                        ++currPosExpected;
                        existingStatusesWrapper.next();
                        break;
                    case OLDER_END_DATE:
                    case OVERLAPPING_BOTH:
                    case OVERLAPPING_END:
                        existingStatusesWrapper.updateStartDate(currExpected.getStartDate());
                        existingStatusesWrapper.updateEndDate(currExpected.getEndDate());
                        existingStatusesWrapper.next();
                        break;
                }
            }
        }
        return existingStatusesWrapper.resultAction;
    }

    public CompareDateRangeResult compareDateRange(InstalledStatusResponse expected, BillingInstalledBaseItemStatus currStatus){
        if(
                expected.getEndDate().isBefore(currStatus.getStartDate())
                        || expected.getEndDate().isEqual(currStatus.getStartDate())
                ){
            return CompareDateRangeResult.BEFORE;
        }
        else if(expected.getEndDate().isBefore(currStatus.getEndDate())){
            if(expected.getStartDate().isBefore(currStatus.getStartDate())) {
                return CompareDateRangeResult.OVERLAPPING_START;
            }
            else if(expected.getStartDate().isEqual(currStatus.getStartDate())) {
                return CompareDateRangeResult.EARLIER_END_DATE;
            }
            else {
                return CompareDateRangeResult.CONTAINED_INTO;
            }
        }
        else if(expected.getEndDate().isEqual(currStatus.getEndDate())){
            if(expected.getStartDate().isBefore(currStatus.getStartDate())) {
                return CompareDateRangeResult.EARLIER_START_DATE;
            }
            else if(expected.getStartDate().isEqual(currStatus.getStartDate())) {
                return CompareDateRangeResult.EXACT;
            }
            else {
                return CompareDateRangeResult.OLDER_START_DATE;
            }
        }
        else{
            if(expected.getStartDate().isBefore(currStatus.getStartDate())) {
                return CompareDateRangeResult.OVERLAPPING_BOTH;
            }
            else if(expected.getStartDate().isEqual(currStatus.getStartDate())) {
                return CompareDateRangeResult.OLDER_END_DATE;
            }
            else if(expected.getStartDate().isBefore(currStatus.getEndDate())){
                return CompareDateRangeResult.OVERLAPPING_END;
            }
            else{
                return CompareDateRangeResult.AFTER;
            }
        }
    }

    private enum CompareDateRangeResult{
        BEFORE(true,true),//new_end before or equal curr_start
        OVERLAPPING_START(true,true),//new_start before (strict), new_end before curr_end
        OVERLAPPING_BOTH(false,false),//new_start before (strict), new_end after(strictly) curr_end
        EARLIER_END_DATE(true,true),//new start equals curr_start, new_end before(strictly) curr_end
        EARLIER_START_DATE(true,true),//new start before(strict)curr_start, new_end equals curr_end
        EXACT(true,true),
        OLDER_START_DATE(true,true),//new start after(strict)curr_start, new_end equals curr_end
        CONTAINED_INTO(false,false),//new start after(strict) curr_start, new_end before(strictly) curr_end
        OVERLAPPING_END(false,false),//new start after(strict) curr_start, new_end after(strictly) curr_end
        OLDER_END_DATE(false,false),//new start equals curr_start, new_end after(strictly) curr_end
        AFTER(false,false);//new start after or equals curr_end

        private final boolean needCreateNewItemWhenStatusDiffers;
        private final boolean moveForwardOnExpected;

        CompareDateRangeResult(boolean needCreateNewItemWhenStatusDiffers, boolean moveForwardOnExpected){
            this.needCreateNewItemWhenStatusDiffers = needCreateNewItemWhenStatusDiffers;
            this.moveForwardOnExpected=moveForwardOnExpected;
        }
    }


    private static BillingInstalledBaseItemStatus.Status mapStatus(CodeResponse origStatus){
        if(origStatus==null){
            return null;
        }

        switch (origStatus){
            case ACTIVE:return BillingInstalledBaseItemStatus.Status.ACTIVE;
            case ABORTED:return BillingInstalledBaseItemStatus.Status.CANCELLED;
            case SUSPENDED:return BillingInstalledBaseItemStatus.Status.SUSPENDED;
            case CLOSED:return BillingInstalledBaseItemStatus.Status.CLOSED;
            case REMOVED:return BillingInstalledBaseItemStatus.Status.CANCELLED;
            case INEXISTING:return BillingInstalledBaseItemStatus.Status.CANCELLED;
        }
        return null;
    }


    private static class ExistingItemsListWrapper{
        private final BillingInstalledBaseItem  existingItem;
        private final List<BillingInstalledBaseItemStatus> existingStatuses;
        private final Stack<CreateUpdateBillingInstalledBaseItemStatusUpdateResult> backedResults = new Stack<>();
        private final CreateUpdateBillingInstalledBaseItemResult itemUpdateResult;
        private CreateUpdateBillingInstalledBaseAction resultAction = CreateUpdateBillingInstalledBaseAction.UNCHANGED;

        private int currPos = -1;
        private CreateUpdateBillingInstalledBaseItemStatusUpdateResult currUpdateResult=null;
        private BillingInstalledBaseItemStatus currStatus=null;

        ExistingItemsListWrapper(BillingInstalledBaseItem currItem, CreateUpdateBillingInstalledBaseItemResult itemUpdateResult){
            this.itemUpdateResult = itemUpdateResult;
            this.existingItem = currItem;
            this.existingStatuses = new ArrayList<>(currItem.getStatuses());
            this.existingStatuses.sort(Comparator.comparing(BillingInstalledBaseItemStatus::getStartDate));
            if(this.hasNext()){
                next();
            }
        }

        public void next(){
            if(hasNext()) {
                ++currPos;
                currStatus = existingStatuses.get(currPos);
                if (this.backedResults.size() > 0) {
                    currUpdateResult = this.backedResults.pop();
                } else {
                    this.createNewUpdateResult(currStatus, CreateUpdateBillingInstalledBaseAction.UNCHANGED);
                }
            }
            else{
                ++currPos;
                currStatus=null;
                currUpdateResult=null;
            }
        }

        private void createNewUpdateResult(BillingInstalledBaseItemStatus currStatus,CreateUpdateBillingInstalledBaseAction action) {
            currUpdateResult=new CreateUpdateBillingInstalledBaseItemStatusUpdateResult();
            this.itemUpdateResult.addStatus(currUpdateResult);
            currUpdateResult.setAction(action);
            currUpdateResult.setOldStartDate(currStatus.getStartDate());
            currUpdateResult.setOldEndDate(currStatus.getEndDate());
            currUpdateResult.setStatus(currStatus.getStatus());
            resultAction=CreateUpdateBillingInstalledBaseAction.UPDATED;
        }

        public BillingInstalledBaseItemStatus insertStatus(InstalledStatusResponse status){
            BillingInstalledBaseItemStatus result = new BillingInstalledBaseItemStatus();
            result.setStatus(mapStatus(status.getCode()));
            result.setStartDate(status.getStartDate());
            result.setEndDate(status.getEndDate());

            if(currPos<0){
                ++currPos;
            }
            else{
                this.backedResults.push(this.currUpdateResult);
            }
            existingStatuses.add(currPos,result);
            existingItem.addStatuses(result);

            this.createNewUpdateResult(result,CreateUpdateBillingInstalledBaseAction.CREATED);
            resultAction=CreateUpdateBillingInstalledBaseAction.UPDATED;
            return result;
        }

        public boolean hasNext(){
            return this.currPos<(this.existingStatuses.size()-1);
        }

        public void updateStartDate(DateTime newStartDate){
            this.currStatus.setStartDate(newStartDate);
            this.currUpdateResult.setStartDate(newStartDate);
            if(this.currUpdateResult.getAction()!=CreateUpdateBillingInstalledBaseAction.CREATED){
                this.currUpdateResult.setAction(CreateUpdateBillingInstalledBaseAction.UPDATED);
            }
            resultAction=CreateUpdateBillingInstalledBaseAction.UPDATED;
        }

        public void updateEndDate(DateTime newStartDate){
            this.currStatus.setEndDate(newStartDate);
            this.currUpdateResult.setEndDate(newStartDate);
            if(this.currUpdateResult.getAction()!=CreateUpdateBillingInstalledBaseAction.CREATED){
                this.currUpdateResult.setAction(CreateUpdateBillingInstalledBaseAction.UPDATED);
            }
            resultAction=CreateUpdateBillingInstalledBaseAction.UPDATED;
        }

        public void cancelCurrent() {
            this.currStatus.setStatus(BillingInstalledBaseItemStatus.Status.CANCELLED);
            this.currUpdateResult.setAction(CreateUpdateBillingInstalledBaseAction.CANCELLED);
            resultAction=CreateUpdateBillingInstalledBaseAction.UPDATED;
        }
    }
}
