package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.v1.common.InstalledValue;
import com.dreameddeath.installedbase.process.model.v1.ValueUpdateResult;

/**
 * Created by Christophe Jeunesse on 08/04/2016.
 */
public class InstalledAttributeUpdateToApply {
    private InstalledValue value;
    private ValueUpdateResult updateResult;

    public InstalledValue getValue() {
        return value;
    }

    public void setValue(InstalledValue value) {
        this.value = value;
    }

    public ValueUpdateResult getUpdateResult() {
        return updateResult;
    }

    public void setUpdateResult(ValueUpdateResult updateResult) {
        this.updateResult = updateResult;
    }
}
