package com.dreameddeath.compiling.datamodel.gen.generators;

import com.dreameddeath.compiling.datamodel.gen.model.ModelDef;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.v2.ICouchbaseDocument;
import com.dreameddeath.core.model.v2.meta.CouchbaseMetaInfo;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

public class AnnotationHelper {

    public static final String COUCHBASE_META_INFO_FIELD = "__db_meta";

    private final TypeHelper typeHelper= new TypeHelper();

    public void addAnnotationsForClass(ImplementationGenerator.Context context, ModelDef modelDef){
        if(modelDef.flags.contains(ModelDef.Flag.ENTITY)){
            manageEntity(context, modelDef);
        }
        //if(modelDef.flags.contains(ModelDef.Flag.VERSIONED))

    }

    private void manageEntity(ImplementationGenerator.Context context, ModelDef modelDef) {
        AnnotationSpec.Builder entityAnnotationBuilder = AnnotationSpec.builder(DocumentEntity.class);
        if(StringUtils.isNotEmptyAfterTrim(modelDef.domain)){
            entityAnnotationBuilder = entityAnnotationBuilder.addMember("domain","$S",modelDef.domain.trim());
        }
        if(StringUtils.isNotEmptyAfterTrim(modelDef.version)){
            entityAnnotationBuilder = entityAnnotationBuilder.addMember("version","$S",modelDef.version.trim());
        }


        context.type
                .addSuperinterface(ICouchbaseDocument.class)
                .addAnnotation(entityAnnotationBuilder.build())
                .addField(FieldSpec.builder(
                        CouchbaseMetaInfo.class, COUCHBASE_META_INFO_FIELD, Modifier.PRIVATE,Modifier.FINAL)
                        .build()
                )
                .addMethod(MethodSpec
                        .methodBuilder("getCouchbaseMetaInfo")
                        .returns(CouchbaseMetaInfo.class)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC,Modifier.FINAL)
                        .addStatement("return this.$L",COUCHBASE_META_INFO_FIELD)
                        .build()
                );

        context.constructor.addStatement("this.$L = $L.$L",
                COUCHBASE_META_INFO_FIELD,
                ImplementationGenerator.BUILDER_PARAM_NAME,
                COUCHBASE_META_INFO_FIELD);

        context.builderType.addSuperinterface(ParameterizedTypeName.get(
                    ClassName.get(ICouchbaseDocument.Builder.class),
                    TypeVariableName.get("T"))
                )
                .addField(FieldSpec
                        .builder(CouchbaseMetaInfo.class,COUCHBASE_META_INFO_FIELD, Modifier.PRIVATE)
                        .initializer("new $T().create()",CouchbaseMetaInfo.Builder.class)
                        .build()
                )
                .addMethod(MethodSpec
                        .methodBuilder("withCouchbaseMetaInfo")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(CouchbaseMetaInfo.class,"metaInfo")
                        .addStatement("this.$L = metaInfo",COUCHBASE_META_INFO_FIELD)
                        .addStatement("return this")
                        .returns(typeHelper.buildBuilderReturnType(modelDef, SubType.IMPL_BUILDER))
                        .build()
                );
        context.builderConstructor.addStatement("this.$L = $L.$L.toMutable().toModified().create()",COUCHBASE_META_INFO_FIELD,ImplementationGenerator.ORIG_ITEM_PARAM_NAME,COUCHBASE_META_INFO_FIELD);

    }
}
