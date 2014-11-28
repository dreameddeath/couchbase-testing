package com.dreameddeath.core.transcoder.json;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.utils.Helper;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.util.ClassUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by ceaj8230 on 07/11/2014.
 */
public class CouchbaseDocumentTypeIdResolver extends TypeIdResolverBase{
    private static final String VERSION="\\d+\\.\\d+\\.\\d+";
    private static final String ID="(\\w+)/(\\w+)/("+VERSION+")";
    private static final String FORMAT="%s/%s/%s";
    private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION);
    private static final Pattern ID_PATTERN = Pattern.compile(ID);

    private JavaType _baseType;
    private String _domain;


    public  CouchbaseDocumentTypeIdResolver() {
        super(null, null);
    }

    @Override
    public void init(JavaType baseType){
        _baseType =baseType;
        DocumentDef annot =_baseType.getRawClass().getAnnotation(DocumentDef.class);
        if(annot!=null){
            _domain = annot.domain();
        }
    }

    @Override
    public String idFromValue(Object value){
        DocumentDef annot=value.getClass().getAnnotation(DocumentDef.class);
        if(annot!=null){
            String name = annot.name();
            if("".equals(name)){
                name =value.getClass().getSimpleName();
            }
            return String.format(FORMAT,annot.domain(),name,annot.version());
        }
        else{
            throw new RuntimeException("Need the DocumentRef annotation on class "+ value.getClass().getName());
        }
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType){
        return null;
    }

    @Override @Deprecated
    public String idFromBaseType() {
        return null;
    }

    @Override
    public JavaType typeFromId(String id) {
        return null;
    }

    public JavaType typeFromId(DatabindContext context, String id) {
        Matcher parseResult = ID_PATTERN.matcher(id);
        if(parseResult.matches()) {
            String domain = parseResult.group(1).toString();
            String name = parseResult.group(2).toString();
            String version = parseResult.group(3).toString();
            String filename = Helper.getDocumentEntityFilename(domain, name, version);
            InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(is));
            try {
                String className = fileReader.readLine();
                return context.getTypeFactory().constructType(ClassUtil.findClass(className));
            }
            catch(ClassNotFoundException|IOException e){
                throw  new RuntimeException("Cannot find/read file <"+filename+"> for id <"+id+">");
            }
        }
        throw  new RuntimeException("Cannot parse id <"+id+">");
            //getClass().getClassLoader().getResource()
        //context.getTypeFactory().constructFromCanonical()
    }

    @Override
    public JsonTypeInfo.Id getMechanism(){
        return JsonTypeInfo.Id.CUSTOM;
    }
}
