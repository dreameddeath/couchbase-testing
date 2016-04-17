package com.dreameddeath.testing.dataset.model;

import org.mvel2.MVEL;
import org.mvel2.compiler.CompiledExpression;

/**
 * Created by Christophe Jeunesse on 14/04/2016.
 */
public class DatasetMvel {
    private StringBuilder sb = new StringBuilder();
    private Dataset parent=null;
    private DatasetElement parentElement=null;
    private CompiledExpression compiledExpression=null;

    public void addContent(String content){
        sb.append(content);
    }

    public void prepare(Dataset parent, DatasetElement datasetElement) {
        this.parent = parent;
        this.parentElement = datasetElement;
        compiledExpression=(CompiledExpression)MVEL.compileExpression(sb.toString(),parent.getParserContext());
    }

    public CompiledExpression getCompiledExpression() {
        return compiledExpression;
    }

}
