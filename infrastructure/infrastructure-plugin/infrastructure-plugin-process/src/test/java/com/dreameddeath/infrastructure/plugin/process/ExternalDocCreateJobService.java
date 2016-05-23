package com.dreameddeath.infrastructure.plugin.process;

/**
 * Created by Christophe Jeunesse on 23/05/2016.
 */
public class ExternalDocCreateJobService {
    public TestDocProcess createDoc(String name){
        TestDocProcess doc = new TestDocProcess();
        doc.name=name;
        return doc;
    }
}
