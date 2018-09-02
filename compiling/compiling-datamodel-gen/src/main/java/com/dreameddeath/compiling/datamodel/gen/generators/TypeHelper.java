package com.dreameddeath.compiling.datamodel.gen.generators;

import com.dreameddeath.compiling.datamodel.gen.model.ModelDef;
import com.dreameddeath.core.java.utils.StringUtils;
import com.google.common.base.Preconditions;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.joda.time.DateTime;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

public class TypeHelper {
    private final String separatorPattern = "[<>,]";
    public String getPackage(ModelDef modelDef,SubType subType){
        return getEffectiveClassName(getCoreClassName(modelDef),subType).packageName();
    }

    public TypeName getTypeName(ModelDef model, Queue<String> tokens,SubType subType) {
        String type = tokens.poll();
        Preconditions.checkState(type!=null && !type.equals(""),"unexpected part");
        ClassName typeName = getTypeName(model, subType, type);
        String nextToken = tokens.peek();
        if(nextToken!=null && nextToken.equals("<")){
            tokens.poll();
            ArrayList<TypeName> params = new ArrayList<>();
            do{
                params.add(getTypeName(model,tokens,subType));
                nextToken = tokens.poll();
                Preconditions.checkState(
                        nextToken!=null && (nextToken.equals(">") || nextToken.equals(",")),
                        "Bad parameterized type structure for %s",typeName
                );
            }
            while(!nextToken.equals(">"));
            Preconditions.checkState(nextToken.equals(">"),"Bad parameterized type structure for %s",typeName);
            return ParameterizedTypeName.get(typeName,params.stream().toArray(TypeName[]::new));
        }
        else{
            return typeName;
        }


    }

    private ClassName getTypeName(ModelDef model, SubType subType, String type) {
        switch (type.toLowerCase()){
            case "string": case "str":
                return ClassName.get(String.class);
            case "int": case "integer": case "long":
                return ClassName.get(Long.class);
            case "double": case "float": case "bigdecimal":
                return ClassName.get(BigDecimal.class);
            case "date":
                return ClassName.get(Date.class);
            case "datetime":
                return ClassName.get(DateTime.class);
            case "uuid":
                return ClassName.get(UUID.class);
            case "boolean": case "bool":
                return ClassName.get(Boolean.class);
            case "map":
                return ClassName.get(Map.class);
            case "list":
                return ClassName.get(List.class);
            case "set":
                return ClassName.get(Set.class);
            default:
                String effectiveType = type;
                if(effectiveType.startsWith("*")){
                    effectiveType = effectiveType.substring(1);
                }
                if(effectiveType.contains(".")){
                    String packageName = effectiveType.substring(0,effectiveType.lastIndexOf("."));
                    String className = effectiveType.substring(effectiveType.lastIndexOf(".")+1);
                    return getEffectiveClassName(ClassName.get(packageName,className),subType);
                }
                else{
                    return getEffectiveClassName(ClassName.get(getPackage(model, SubType.BASE),effectiveType),subType);
                }
        }
    }

    public TypeName getTypeName(ModelDef model, String type,SubType subType){
        try {
            String[] parts = type.split("(?<="+separatorPattern+")|(?="+separatorPattern+")");
            Queue<String> tokensQueue = new LinkedList<>();
            for (String part : parts) {
                tokensQueue.add(part.trim());
            }

            TypeName typeName = getTypeName(model, tokensQueue, subType);
            while (tokensQueue.size() > 0) {
                String poll = tokensQueue.poll();
                Preconditions.checkState(poll.trim().length() == 0, "Bad type format");
            }
            return typeName;
        }
        catch(Throwable e){
            throw new RuntimeException("Cannot parse type "+type,e);
        }
    }


    public ClassName getEffectiveClassName(ClassName origClassName, SubType subType) {
        switch (subType){
            case INTERFACE:
                return ClassName.get(origClassName.packageName(),"I"+origClassName.simpleName());
            case BUILDER:
                return getEffectiveClassName(origClassName,SubType.INTERFACE).nestedClass("Builder");
            case IMPL:
                return ClassName.get(origClassName.packageName()+".impl",origClassName.simpleName());
            case IMPL_BUILDER:
                return getEffectiveClassName(origClassName,SubType.IMPL).nestedClass("Builder");
            case BASE:
                return origClassName;
        }
        throw new IllegalStateException();
    }

    public ClassName getCoreClassName(ModelDef modelDef) {
        StringBuilder packageNameSb = new StringBuilder();

        File file = new File(modelDef.relativeFilename);
        String nameFromFileName = file.getName();
        file = file.getParentFile();

        while(file!=null){

            if(packageNameSb.length()>0){
                packageNameSb.insert(0,'.');
            }
            packageNameSb.insert(0,file.getName());
            file = file.getParentFile();
        }

        if(StringUtils.isNotEmptyAfterTrim(modelDef.packageName)){
            if(packageNameSb.length()>0){
                packageNameSb.append('.');
            }
            packageNameSb.append(modelDef.packageName.trim());
        }
        if(StringUtils.isNotEmptyAfterTrim(modelDef.name)){
            return ClassName.get(packageNameSb.toString(),modelDef.name.trim());
        }
        else{
            return ClassName.get(packageNameSb.toString(),StringUtils.capitalizeFirst(nameFromFileName));
        }
    }

    public enum SubType{
        INTERFACE,
        BUILDER,
        IMPL,
        IMPL_BUILDER,
        BASE
    }
}
