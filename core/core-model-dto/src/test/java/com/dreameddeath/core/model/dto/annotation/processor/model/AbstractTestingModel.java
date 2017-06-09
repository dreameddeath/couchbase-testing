package com.dreameddeath.core.model.dto.annotation.processor.model;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;

/**
 * Created by CEAJ8230 on 28/05/2017.
 */
@DocumentEntity(domain = "test",version = "1.0")
@DtoGenerate(mode = DtoInOutMode.IN)
@DtoGenerate(mode = DtoInOutMode.OUT)
public abstract class AbstractTestingModel extends CouchbaseDocument {
    @DocumentProperty
    public String strValue;

}
