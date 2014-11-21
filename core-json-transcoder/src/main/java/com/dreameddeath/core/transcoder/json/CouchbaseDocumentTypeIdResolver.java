package com.dreameddeath.core.transcoder.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by ceaj8230 on 07/11/2014.
 */
public class CouchbaseDocumentTypeIdResolver extends TypeIdResolverBase {
    private static final String VERSION="(\\d+).(\\d+).(\\d+)";
    private static final String ID="(\\w+)/(\\w+)/("+VERSION+")";
    private static final String FORMAT="%s/%s/%d.%d.%d";
    private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION);
    private static final Pattern ID_PATTERN = Pattern.compile(ID);

    protected final MapperConfig<?> _config;

    /**
     * Mappings from class name to type id, used for serialization
     */
    protected final HashMap<String, String> _typeToId;

    /**
     * Mappings from type id to JavaType, used for deserialization
     */
    protected final HashMap<String, JavaType> _idToType;

    protected CouchbaseDocumentTypeIdResolver(MapperConfig<?> config, JavaType baseType,
                                 HashMap<String, String> typeToId, HashMap<String, JavaType> idToType)
    {
        super(baseType, config.getTypeFactory());
        _config = config;
        _typeToId = typeToId;
        _idToType = idToType;
    }


    /**
     * If no name was explicitly given for a class, we will just
     * use non-qualified class name
     */
    protected static String _defaultTypeId(Class<?> cls)
    {
        String n = cls.getName();
        int ix = n.lastIndexOf('.');
        return (ix < 0) ? n : n.substring(ix+1);
    }

    public static CouchbaseDocumentTypeIdResolver construct(MapperConfig<?> config, JavaType baseType,
                                               Collection<NamedType> subtypes, boolean forSer, boolean forDeser)
    {
        // sanity check
        if (forSer == forDeser) throw new IllegalArgumentException();
        HashMap<String, String> typeToId = null;
        HashMap<String, JavaType> idToType = null;

        if (forSer) {
            typeToId = new HashMap<String, String>();
        }
        if (forDeser) {
            idToType = new HashMap<String, JavaType>();
        }
        if (subtypes != null) {
            for (NamedType t : subtypes) {
                /* no name? Need to figure out default; for now, let's just
                 * use non-qualified class name
                 */
                Class<?> cls = t.getType();
                String id = t.hasName() ? t.getName() : _defaultTypeId(cls);
                if (forSer) {
                    typeToId.put(cls.getName(), id);
                }
                if (forDeser) {
                    /* 24-Feb-2011, tatu: [JACKSON-498] One more problem; sometimes
                     *   we have same name for multiple types; if so, use most specific
                     *   one.
                     */
                    JavaType prev = idToType.get(id);
                    if (prev != null) { // Can only override if more specific
                        if (cls.isAssignableFrom(prev.getRawClass())) { // nope, more generic (or same)
                            continue;
                        }
                    }
                    idToType.put(id, config.constructType(cls));
                }
            }
        }
        return new CouchbaseDocumentTypeIdResolver(config, baseType, typeToId, idToType);
    }


    @Override
    public void init(JavaType baseType){

    }

    @Override
    public String idFromValue(Object value){
        return null;
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType){
        return null;
    }

    @Override
    public String idFromBaseType(){
        return null;
    }

    @Override
    public JavaType typeFromId(String id){
        return null;
    }

    public JavaType typeFromId(DatabindContext context, String id) {
        return typeFromId(id);
    }

    @Override
    public JsonTypeInfo.Id getMechanism(){
        return JsonTypeInfo.Id.CUSTOM;
    }
}
