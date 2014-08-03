package com.dreameddeath.common.storage;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.annotation.JsonTypeResolver;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class CouchbaseDocumentIntrospector extends JacksonAnnotationIntrospector implements
        Versioned {

    @Override
    public Version version() {
        return VersionUtil.versionFor(getClass());
    }

//    @Override
//    protected TypeResolverBuilder<?> _findTypeResolver(MapperConfig<?> config,
//                                                       Annotated ann, JavaType baseType)
//    {
//        // First: maybe we have explicit type resolver?
//        TypeResolverBuilder<?> b;
//        JsonTypeInfo info = ann.getAnnotation(JsonTypeInfo.class);
//        JsonTypeResolver resAnn = ann.getAnnotation(JsonTypeResolver.class);
//
//        if (resAnn != null) {
//            if (info == null) {
//                return null;
//            }
//            /* let's not try to force access override (would need to pass
//             * settings through if we did, since that's not doable on some
//             * platforms)
//             */
//            b = config.typeResolverBuilderInstance(ann, resAnn.value());
//        } else { // if not, use standard one, if indicated by annotations
//            if (info == null) {
//                return null;
//            }
//            // bit special; must return 'marker' to block use of default typing:
//            if (info.use() == JsonTypeInfo.Id.NONE) {
//                return _constructNoTypeResolverBuilder();
//            }
//            b = _constructStdTypeResolverBuilder();
//        }
//        // Does it define a custom type id resolver?
//        JsonTypeIdResolver idResInfo = ann.getAnnotation(JsonTypeIdResolver.class);
//        TypeIdResolver idRes = (idResInfo == null) ? null
//                : config.typeIdResolverInstance(ann, idResInfo.value());
//        if (idRes != null) { // [JACKSON-359]
//            idRes.init(baseType);
//        }
//        b = b.init(info.use(), idRes);
//        /* 13-Aug-2011, tatu: One complication wrt [JACKSON-453]; external id
//         *   only works for properties; so if declared for a Class, we will need
//         *   to map it to "PROPERTY" instead of "EXTERNAL_PROPERTY"
//         */
//        JsonTypeInfo.As inclusion = info.include();
//        if (inclusion == JsonTypeInfo.As.EXTERNAL_PROPERTY && (ann instanceof AnnotatedClass)) {
//            inclusion = JsonTypeInfo.As.PROPERTY;
//        }
//        b = b.inclusion(inclusion);
//        b = b.typeProperty(info.property());
//        Class<?> defaultImpl = info.defaultImpl();
//        if (defaultImpl != JsonTypeInfo.None.class) {
//            b = b.defaultImpl(defaultImpl);
//        }
//        b = b.typeIdVisibility(info.visible());
//        return b;
//    }

    @Override
    /**
     * Helper method for constructing standard {@link TypeResolverBuilder}
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
                if(am.getName().startsWith("get") && (am.getName().length()>3)){
                    String fieldName = am.getName().substring(3,4).toLowerCase()+am.getName().substring(4);

                    for(Field field: am.getDeclaringClass().getDeclaredFields()) {
                        DocumentProperty fieldProp = field.getAnnotation(DocumentProperty.class);
                        if(fieldProp==null) continue;
                        if(am.getName().equals(fieldProp.getter())){
                            name = new PropertyName(fieldProp.value());
                            break;
                        }
                        else if(fieldName.equals(fieldProp.value())){
                            name=new PropertyName(fieldProp.value());
                        }
                    }
                }
            }
            else if(a instanceof AnnotatedField){
                AnnotatedField af = (AnnotatedField) a;
                DocumentProperty fieldProp = af.getAnnotation(DocumentProperty.class);
                if(((af.getModifiers() & Modifier.PUBLIC)!=0)&&(fieldProp!=null)){
                    name=new PropertyName(fieldProp.value());
                }
            }
        }

        return name;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a)
    {
        PropertyName name = super.findNameForSerialization(a);
        if(name==null) {
            DocumentProperty annotation= a.getAnnotation(DocumentProperty.class);
            if (a instanceof AnnotatedMethod){
                AnnotatedMethod am = (AnnotatedMethod) a;
                if(am.getName().startsWith("set")) {
                    for(Field field: am.getDeclaringClass().getDeclaredFields()) {
                        DocumentProperty fieldProp = field.getAnnotation(DocumentProperty.class);
                        if((fieldProp!=null) && (am.getName().equals(fieldProp.setter()))){
                            name = new PropertyName(fieldProp.value());
                        }
                    }
                }
            }
            else if(a instanceof AnnotatedField){
                AnnotatedField af = (AnnotatedField) a;
                DocumentProperty fieldProp = af.getAnnotation(DocumentProperty.class);
                if(((af.getModifiers() & Modifier.PUBLIC)!=0)&&(fieldProp!=null)){
                    name=new PropertyName(fieldProp.value());
                }
            }
        }

        return name;
    }
}