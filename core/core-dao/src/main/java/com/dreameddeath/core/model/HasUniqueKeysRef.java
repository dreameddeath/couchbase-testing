package com.dreameddeath.core.model;

import java.util.Set;

/**
 * Created by ceaj8230 on 23/11/2014.
 */
public interface HasUniqueKeysRef {
    public boolean addDocUniqKeys(String key);
    public boolean removeDocUniqKeys(String key);
    public Set<String> getRemovedUniqueKeys();
}
