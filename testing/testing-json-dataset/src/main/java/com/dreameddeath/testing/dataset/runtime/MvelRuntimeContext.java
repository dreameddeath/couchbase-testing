package com.dreameddeath.testing.dataset.runtime;

import com.dreameddeath.testing.dataset.model.Dataset;
import com.dreameddeath.testing.dataset.model.DatasetMvel;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
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
}
