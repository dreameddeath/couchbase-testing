package com.dreameddeath.testing.dataset.runtime;

import com.dreameddeath.testing.dataset.model.Dataset;
import com.dreameddeath.testing.dataset.model.DatasetMvel;
import com.dreameddeath.testing.dataset.model.DatasetValue;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.mvel2.templates.TemplateRuntime;
import org.slf4j.LoggerFactory;

/**
 * Created by Christophe Jeunesse on 17/04/2016.
 */
public class MvelRuntimeContext {
    private final VariableResolverFactory variableResolverFactory;

    public MvelRuntimeContext(Dataset dataset){
        MapVariableResolverFactory factory=new MapVariableResolverFactory();
        factory.createVariable("log",LoggerFactory.getLogger(dataset.getClass().getName()+"."+dataset.getName()));
        factory.createVariable("globalDataset",dataset);
        factory.createVariable("globalManager",dataset.getManager());
        this.variableResolverFactory = factory;
    }

    public VariableResolverFactory getVariableResolverFactory(){
        return variableResolverFactory;
    }


    public Object execute(DatasetMvel datasetMvel){
        return MVEL.executeExpression(datasetMvel.getCompiledExpression(),this,this.getVariableResolverFactory());
    }

    public Object execute(DatasetValue datasetValue){
        if(datasetValue.isTemplate()) {
            return TemplateRuntime.execute(datasetValue.getTemplate(), this, this.getVariableResolverFactory()).toString();
        }
        else if(datasetValue.isMvel()){
            return execute(datasetValue.getMvel());
        }
        throw new RuntimeException("Inconsistent state");
    }
}
