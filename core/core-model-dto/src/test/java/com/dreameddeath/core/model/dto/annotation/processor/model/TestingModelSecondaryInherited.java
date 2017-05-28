package com.dreameddeath.core.model.dto.annotation.processor.model;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;

/**
 * Created by CEAJ8230 on 28/05/2017.
 */

@DocumentEntity(domain = "test",version = "1.0")
public class TestingModelSecondaryInherited extends TestingModel {
    @DocumentProperty
    public String inheritedSecondaryStrValue;
}
