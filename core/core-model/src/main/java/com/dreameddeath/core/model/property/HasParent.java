package com.dreameddeath.core.model.property;


import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by ceaj8230 on 07/09/2014.
 */
public interface HasParent {
    public HasParent getParentElement();
    public void setParentElement(HasParent parent);

    public static class Helper {
        public static <T extends HasParent> T getFirstParentOfClass(HasParent src, Class<T> clazz) {
            if (src != null) {
                if (clazz.isAssignableFrom(src.getClass())) {
                    return (T) (src);
                } else {
                    return getFirstParentOfClass(src.getParentElement(), clazz);
                }
            }
            return null;
        }

        public static CouchbaseDocument getParentDocument(HasParent src){
            if (src != null) {
                if (src instanceof CouchbaseDocument) {
                    return (CouchbaseDocument) (src);
                } else {
                    return getParentDocument(src.getParentElement());
                }
            }
            return null;
        }

        public static void dirtyParentDocument(HasParent src){
            CouchbaseDocument doc = getParentDocument(src);
            if(doc!=null){
                doc.getBaseMeta().setStateDirty();
            }
        }
    }
}