package com.dreameddeath.core.transcoder.json;

import com.dreameddeath.core.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.util.CouchbaseDocumentStructureReflection;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

public class CouchbaseDocumentIntrospector extends JacksonAnnotationIntrospector implements
        Versioned {

    @Override
    public Version version() {
        return VersionUtil.versionFor(getClass());
    }


    @Override
    /**
     * Helper method for constructing standard {@link com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder}
     * implementation.
     */
    protected StdTypeResolverBuilder _constructStdTypeResolverBuilder() {
        return new DocumentTypeResolverBuilder();
    }

    @Override
    public PropertyName findNameForSerialization(Annotated a)
    {
        PropertyName name = super.findNameForSerialization(a);
        if(name==null) {
            if (a instanceof AnnotatedMethod){
                AnnotatedMethod am = (AnnotatedMethod) a;
                if(CouchbaseDocumentStructureReflection.isReflexible(am.getDeclaringClass())) {
                    if (am.getName().startsWith("get") && (am.getName().length() > 3)) {
                        CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClass(am.getDeclaringClass());
                        CouchbaseDocumentFieldReflection fieldReflection = structureReflection.getDeclaredFieldByGetterName(am.getName());
                        if (fieldReflection != null) {
                            name = new PropertyName(fieldReflection.getName());
                        }
                    }
                }
            }
            else if(a instanceof AnnotatedField){
                AnnotatedField af = (AnnotatedField) a;
                if(CouchbaseDocumentStructureReflection.isReflexible(af.getDeclaringClass())) {
                    CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClass(af.getDeclaringClass());
                    CouchbaseDocumentFieldReflection fieldReflection = structureReflection.getDeclaredField(af.getAnnotated());
                    if (fieldReflection != null && fieldReflection.isPureField()) {
                        name = new PropertyName(fieldReflection.getName());
                    }
                }
            }
        }

        return name;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a)
    {
        PropertyName name = super.findNameForDeserialization(a);
        if(name==null) {
            if (a instanceof AnnotatedMethod){
                AnnotatedMethod am = (AnnotatedMethod) a;
                if(CouchbaseDocumentStructureReflection.isReflexible(am.getDeclaringClass())) {
                    if (am.getName().startsWith("set")) {
                        CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClass(am.getDeclaringClass());
                        CouchbaseDocumentFieldReflection fieldReflection = structureReflection.getDeclaredFieldBySetterName(am.getName());
                        if (fieldReflection != null) {
                            name = new PropertyName(fieldReflection.getName());
                        }
                    }
                }
            }
            else if(a instanceof AnnotatedField){
                AnnotatedField af = (AnnotatedField) a;
                if(CouchbaseDocumentStructureReflection.isReflexible(af.getDeclaringClass())) {
                    CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClass(af.getDeclaringClass());
                    CouchbaseDocumentFieldReflection fieldReflection = structureReflection.getDeclaredField(af.getAnnotated());
                    if (fieldReflection != null && fieldReflection.isPureField()) {
                        name = new PropertyName(fieldReflection.getName());
                    }
                }
            }
        }

        return name;
    }
}