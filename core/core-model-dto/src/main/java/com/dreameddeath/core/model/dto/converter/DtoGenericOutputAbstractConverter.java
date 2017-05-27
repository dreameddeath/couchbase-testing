package com.dreameddeath.core.model.dto.converter;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.dto.converter.model.DtoConverterDef;
import com.dreameddeath.core.model.entity.model.EntityDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by CEAJ8230 on 18/04/2017.
 */
public class DtoGenericOutputAbstractConverter<TDOC,TTYPE> extends DtoGenericAbstractConverter<IDtoOutputConverter<TDOC,TTYPE>,TDOC,TTYPE> implements IDtoOutputConverter<TDOC,TTYPE> {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());


    public DtoGenericOutputAbstractConverter(Class<TDOC> docClass, Class<TTYPE> typeClass, String version) {
        super(docClass, typeClass, version);
    }

    @Override
    public TTYPE convertToOutput(TDOC doc) {
        Class<? extends TDOC> docClass = (Class)doc.getClass();

        for(EntityConverterForClass<IDtoOutputConverter<TDOC,TTYPE>,TDOC, TTYPE> converter:getListConverters()){
            if(converter.getDocClass().isAssignableFrom(docClass)) {
                return converter.getConverter().convertToOutput(doc);
            }
        }
        throw new RuntimeException("Cannot find converter for "+doc.getClass());

    }

    @Override
    protected IDtoOutputConverter<TDOC, TTYPE> getEffectiveConverter(DtoConverterFactory factory, DtoConverterDef dtoConverterDef, String version,EntityDef currEntityDef) {
        if (dtoConverterDef.getOutputClass() != null && (version == null || version.equals(dtoConverterDef.getConverterVersion()))) {
            try {
                @SuppressWarnings("unchecked")
                Class<TDOC> childDocClass = AbstractClassInfo.getClassInfo(currEntityDef.getClassName()).getCurrentClass();

                @SuppressWarnings("unchecked")
                Class<TTYPE> outputClass = AbstractClassInfo.getClassInfo(dtoConverterDef.getOutputClass()).getCurrentClass();

                if(StringUtils.isEmpty(version)){
                    return factory.getDtoOutputConverter(childDocClass, outputClass);
                }
                else {
                    return factory.getDtoOutputConverter(childDocClass, outputClass, version);
                }
            }
            catch (ClassNotFoundException e){
                LOG.error("Error during init of converter ",e);
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
