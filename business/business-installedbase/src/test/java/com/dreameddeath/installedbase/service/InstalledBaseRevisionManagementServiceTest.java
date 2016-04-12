package com.dreameddeath.installedbase.service;

import com.dreameddeath.core.date.MockDateTimeServiceImpl;
import org.joda.time.DateTime;
import org.junit.Before;

/**
 * Created by Christophe Jeunesse on 11/04/2016.
 */
public class InstalledBaseRevisionManagementServiceTest {
    private static final DateTime refDate = DateTime.parse("2016/01/01-00:00:00");
    private InstalledBaseRevisionManagementService service;

    @Before
    public void buildInstalledBaseRevisionManagementService(){
        service = new InstalledBaseRevisionManagementService();
        service.setDateTimeService(new MockDateTimeServiceImpl(MockDateTimeServiceImpl.Calculator.fixedCalculator(refDate)));
    }


}