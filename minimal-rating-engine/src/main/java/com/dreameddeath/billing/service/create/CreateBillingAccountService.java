package com.dreameddeath.billing.service.create;

import com.dreameddeath.billing.model.service.CreateBillingAccountJob;
import com.dreameddeath.common.service.AbstractJobProcessingServiceImpl;

/**
 * Created by ceaj8230 on 29/05/2014.
 */
public class CreateBillingAccountService {
    public class job extends AbstractJobProcessingServiceImpl<CreateBillingAccountJob>{
        @Override
        public void init(CreateBillingAccountJob job){

        }

        @Override
        public void cleanup(CreateBillingAccountJob job){

        }
    }
}
