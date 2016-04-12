package com.dreameddeath.testing.dataset.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 12/04/2016.
 */
public class JsonNode {
    private List<JsonMeta> metaList=new ArrayList<>();


    public void addMeta(JsonMeta meta){
        metaList.add(meta);
    }


}
