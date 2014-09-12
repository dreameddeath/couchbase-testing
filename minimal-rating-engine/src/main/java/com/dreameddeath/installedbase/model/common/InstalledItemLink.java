package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by ceaj8230 on 31/08/2014.
 */
public class InstalledItemLink extends BaseCouchbaseDocumentElement {
    /**
     *  id : Target item id
     */
    @DocumentProperty("id")
    private Property<String> _id = new StandardProperty<String>(InstalledItemLink.this);
    /**
     *  type : The type of link
     */
    @DocumentProperty("type")
    private Property<Type> _type = new StandardProperty<Type>(InstalledItemLink.this);
    /**
     *  direction : Direction of the link
     */
    @DocumentProperty("direction")
    private Property<Direction> _direction = new StandardProperty<Direction>(InstalledItemLink.this);
    /**
     *  status : Link Status
     */
    @DocumentProperty("status")
    private Property<InstalledStatus> _status = new StandardProperty<InstalledStatus>(InstalledItemLink.this);

    // id accessors
    public String getId() { return _id.get(); }
    public void setId(String val) { _id.set(val); }
    // type accessors
    public Type getType() { return _type.get(); }
    public void setType(Type val) { _type.set(val); }
    // direction accessors
    public Direction getDirection() { return _direction.get(); }
    public void setDirection(Direction val) { _direction.set(val); }
    // status accessors
    public InstalledStatus getStatus() { return _status.get(); }
    public void setStatus(InstalledStatus val) { _status.set(val); }

    public enum Type{
        RELIES, //The item is relying on the targetted item
        BRINGS, //The item has been automatically added the targetted item
        MIGRATE,//The item has been migrated to another item
        MOVED //The item has been moved to a new Parent
    }

    public enum Direction{
        FROM,
        TO
    }

}
