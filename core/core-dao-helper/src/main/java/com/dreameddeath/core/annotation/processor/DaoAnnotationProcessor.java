package com.dreameddeath.core.annotation.processor;


import com.dreameddeath.core.annotation.dao.*;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.model.view.IViewKeyTranscoder;
import com.dreameddeath.core.model.view.IViewTranscoder;
import com.dreameddeath.core.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.util.CouchbaseDocumentStructureReflection;
import com.dreameddeath.core.util.processor.AnnotationProcessorUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.*;

/**
 * Created by CEAJ8230 on 29/12/2014.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.core.annotation.dao.DaoEntity"}
)
public class DaoAnnotationProcessor extends AbstractProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(DaoAnnotationProcessor.class);
    public static final  VelocityEngine VELOCITY_ENGINE;
    static {
        Properties props = new Properties();
        URL url = DaoAnnotationProcessor.class.getClassLoader().getResource("core/templates/velocity.properties");
        try {
            props.load(url.openStream());
        }catch (Exception e){
            throw  new RuntimeException(e);
        }

        VELOCITY_ENGINE = new VelocityEngine(props);
        VELOCITY_ENGINE.init();
    }

    public static Template DAO_BUILDER_TEMPLATE=null;

    public DaoAnnotationProcessor(){
        super();
        if(DAO_BUILDER_TEMPLATE==null){
            DAO_BUILDER_TEMPLATE = VELOCITY_ENGINE.getTemplate("core/templates/stdDaoTemplate.vm");
        }
    }
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();

        try {
            Elements elementUtils = processingEnv.getElementUtils();
            for (Element element : roundEnv.getElementsAnnotatedWith(DaoEntity.class)) {
                if (!CouchbaseDocumentReflection.isReflexible(element)) continue;

                CouchbaseDocumentReflection docReflection = CouchbaseDocumentReflection.getReflectionFromTypeElement((TypeElement) element);
                LOG.debug("Starting to process {}", docReflection.getSimpleName());
                LOG.debug("With DaoEntity to process {}", docReflection.getClassInfo().getAnnotationByType(DaoEntity.class).toString());


                VelocityContext context = new VelocityContext();

                context.put("esc", new JavaEscape());


                EntityDef entity = new EntityDef(docReflection);
                context.put("entity", entity);

                DaoDef daoDef = new DaoDef(docReflection);
                context.put("daoDef", daoDef);

                DbPathDef dbPathDef = new DbPathDef(docReflection);
                context.put("dbPath", dbPathDef);

                List<CounterDef> counterDefList = new ArrayList<>();
                List<Counter> countersAnnotation = Arrays.asList(docReflection.getClassInfo().getAnnotationByType(Counter.class));

                for (Counter counterAnnot : countersAnnotation) {
                    CounterDef newCounter = new CounterDef(docReflection, counterAnnot, dbPathDef);
                    counterDefList.add(newCounter);
                    messager.printMessage(Diagnostic.Kind.NOTE, "Generating counter " + newCounter.toString());

                    if (newCounter.isKeyGen()) {
                        context.put("keyCounter", newCounter);
                    }
                }

                context.put("counters", counterDefList);


                List<ViewDef> viewDefList = new ArrayList<>();
                context.put("views", viewDefList);
                List<View> viewsAnnotation = Arrays.asList(docReflection.getClassInfo().getAnnotationByType(View.class));
                for(View viewAnnot : viewsAnnotation){
                    ViewDef newView = new ViewDef(entity,docReflection,viewAnnot);
                    viewDefList.add(newView);
                    messager.printMessage(Diagnostic.Kind.NOTE,"Generating view "+ newView.toString());
                }

                try {
                    JavaFileObject jfo = processingEnv.getFiler().createSourceFile(daoDef.getName());
                    Writer writer = jfo.openWriter();
                    DAO_BUILDER_TEMPLATE.merge(context, writer);
                    writer.close();

                    messager.printMessage(Diagnostic.Kind.NOTE, "Generating file " + jfo.getName());

                    /*if (LOG.isDebugEnabled()) {
                        StringWriter logWriter = new StringWriter();
                        DAO_BUILDER_TEMPLATE.merge(context, logWriter);

                        LOG.debug("Generated file content :\n" + logWriter.toString());
                    }*/
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
        catch(Throwable e){
            LOG.error("Error during processing",e);
            //e.printStackTrace();
            StringBuffer buf = new StringBuffer();
            for(StackTraceElement elt:e.getStackTrace()){
                buf.append(elt.toString());
                buf.append("\n");
            }
            messager.printMessage(Diagnostic.Kind.ERROR,"Error during processing "+e.getMessage()+"\n"+buf.toString());
            throw e;
        }
        return true;
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public static class DbPathDef {
        private String _basePath;
        private String _idFormat;
        private String _idPattern;
        private String _formatPrefix = "";
        private String _patternPrefix = "";

        public DbPathDef(CouchbaseDocumentReflection docReflection) {
            DaoEntity annot = docReflection.getClassInfo().getAnnotation(DaoEntity.class);
            _basePath = annot.dbPath();
            _idFormat = annot.idFormat();
            _idPattern = annot.idPattern();
            DbPathDef parentDbPath = null;
            ParentEntity parentAnnot = docReflection.getClassInfo().getAnnotation(ParentEntity.class);
            try{
                if(parentAnnot!=null){
                    parentDbPath = new DbPathDef(CouchbaseDocumentReflection.getReflectionFromClass(parentAnnot.c()));
                }
            }
            catch (MirroredTypeException e){
                parentDbPath = new DbPathDef(CouchbaseDocumentReflection.getReflectionFromTypeElement((TypeElement)((DeclaredType) e.getTypeMirror()).asElement()));
            }
            if(parentDbPath!=null){
                _formatPrefix="%s"+parentAnnot.separator();
                _patternPrefix = parentDbPath.getFullPattern()+parentAnnot.separator();
            }
        }


        public String getFullPattern(){
            return _patternPrefix+_basePath+_idPattern;
        }
        public String getFullFormat(){
            return _formatPrefix+_basePath+_idFormat;
        }

        public String getFormatPrefix() {
            return _formatPrefix;
        }

        public String getPatternPrefix() {
            return _patternPrefix;
        }

        public String getIdFormat() {
            return _idFormat;
        }

        public String getIdPattern() {
            return _idPattern;
        }

        public String getBasePath() {
            return _basePath;
        }
    }

    public static class DaoDef{
        private AnnotationProcessorUtils.ClassInfo _baseDaoClassInfo;
        private String _simpleName;
        private String _packageName;
        private Type _type;
        private UidType _uidType;
        private boolean _isUidPureField;
        private String _uidSetterPattern;
        private String _uidGetterPattern;

        public DaoDef(CouchbaseDocumentReflection docReflection){

            _simpleName = docReflection.getSimpleName().replaceAll("\\$", "")+"Dao";
            _packageName = docReflection.getClassInfo().getPackageName().replace(".model",".dao");
            try {
                DaoEntity annot = docReflection.getClassInfo().getAnnotation(DaoEntity.class);
                _baseDaoClassInfo= new AnnotationProcessorUtils.ClassInfo(annot.baseDao());
            }
            catch(MirroredTypeException e){
                _baseDaoClassInfo= new AnnotationProcessorUtils.ClassInfo((DeclaredType) e.getTypeMirror());
            }
            if(_baseDaoClassInfo.isInstanceOf(CouchbaseDocumentWithKeyPatternDao.class)){
                if(_baseDaoClassInfo.isInstanceOf(BusinessCouchbaseDocumentDaoWithUID.class)){
                    _type = Type.WITH_UID;
                    UidDef uidDef = docReflection.getClassInfo().getAnnotation(UidDef.class);
                    if(uidDef!=null){
                        String[] fieldNameParts = uidDef.fieldName().split("\\.");
                        _uidGetterPattern="";
                        _uidSetterPattern="";
                        CouchbaseDocumentStructureReflection currStructure = docReflection.getStructure();
                        for(int partPos=0;partPos<fieldNameParts.length;++partPos){
                            CouchbaseDocumentFieldReflection field = currStructure.getFieldByName(fieldNameParts[partPos]);
                            //Last element
                            if(partPos+1==fieldNameParts.length){
                                _isUidPureField = field.isPureField();
                                if(_isUidPureField){
                                    _uidSetterPattern += _uidGetterPattern + "." + field.getGetterName();
                                }
                                else {
                                    _uidSetterPattern += _uidGetterPattern + "." + field.getSetterName();
                                }

                                if(UUID.class.isAssignableFrom(field.getEffectiveTypeClass())){
                                    _uidType = UidType.UUID;
                                }
                                else if(Long.class.isAssignableFrom(field.getEffectiveTypeClass())){
                                    _uidType = UidType.LONG;
                                }
                                else if(String.class.isAssignableFrom(field.getEffectiveTypeClass())){
                                    _uidType = UidType.STRING;
                                }
                                else{
                                    //TODO throw an error
                                }

                            }
                            _uidGetterPattern+="."+field.buildGetterCode();
                            if(partPos+1<fieldNameParts.length){
                                currStructure = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(field.getEffectiveTypeInfo().getMainClass());
                            }
                        }

                        //CouchbaseDocumentReflection entityReflection=CouchbaseDocumentReflection.getReflectionFromTypeElement()
                    }
                    else{
                        //TODO throw error
                    }
                }
                else{
                    _type = Type.WITH_PATTERN;
                }
            }
            else{
                _type = Type.BASE;
            }


            //new StringBuilder().append(packageName).append(".").append(fullClassName.substring(packageName.length() + 1).replace(".", "$")).toString();
        }

        public String getSimpleName(){ return _simpleName;}

        public String getPackageName(){ return _packageName;}

        public String getName(){ return _packageName+"."+_simpleName;}
        public String getFilename(){ return _packageName.replaceAll("\\.", "/")+ "/"+ _simpleName + ".java";
        }
        public String getBaseSimpleName() {
            return _baseDaoClassInfo.getSimpleName();
        }

        public String getBaseName() {
            return _baseDaoClassInfo.getName();
        }

        public Type getType() {
            return _type;
        }
        public boolean isUidTypeLong() {
            return UidType.LONG.equals(_type);
        }

        public UidType getUidType() {
            return _uidType;
        }

        public String getUidSetterPattern() {
            return _uidSetterPattern;
        }
        public String buildUidSetter(String data) {
            if(_isUidPureField) return getUidSetterPattern()+"="+data;
            else return getUidSetterPattern()+"("+data+")";
        }


        public enum Type {
            BASE(false,false),
            WITH_PATTERN(true,false),
            WITH_UID(true,true);

            private boolean _hasPattern;
            private boolean _hasUid;

            Type(boolean pattern, boolean uid){
                _hasPattern = pattern;
                _hasUid = uid;
            }

            public boolean hasPattern() {
                return _hasPattern;
            }

            public boolean hasUid() {
                return _hasUid;
            }
        }

        public enum UidType{
            LONG,
            UUID,
            STRING
        }
    }

    public static class CounterDef{
        private String _name;
        private String _dbName;
        private String _pattern;
        private DbPathDef _dbPathDef;
        private boolean _isKeyGen;
        private int _defaultValue;
        private long _modulus;

        public CounterDef(CouchbaseDocumentReflection docReflection,Counter annot,DbPathDef pathDef){
            _name = annot.name();
            _dbName = annot.name();
            _isKeyGen = annot.isKeyGen();
            _defaultValue = annot.defaultValue();
            _modulus = annot.modulus();
            _dbPathDef = pathDef;
        }


        public String getName() {
            return _name;
        }

        public String getDbName() {
            return _dbName;
        }

        public boolean isKeyGen() {
            return _isKeyGen;
        }

        public int getDefaultValue() {
            return _defaultValue;
        }

        public long getModulus() {
            return _modulus;
        }

        public String getFullPattern(){ return _dbPathDef.getPatternPrefix()+_dbPathDef.getBasePath()+getDbName();}
        public String getFullFormat(){ return _dbPathDef.getFormatPrefix()+_dbPathDef.getBasePath()+getDbName();}

        @Override
        public String toString(){
            return "{"+
                    "fullName:"+getName()+",\n"+
                    "dbName:"+getDbName()+",\n"+
                    "pattern:"+getFullPattern()+",\n"+
                    "format:"+getFullFormat()+",\n"+
                    "}";
        }
    }

    public static class ViewDef{
        private CouchbaseDocumentReflection _documentReflection;
        private String _className;
        private String _name;
        private String _content;
        private String _contentFilename=null;
        private KeyDef _key;
        private ValueDef _value;
        private ContentSource _contentSource;

        public ContentSource getContentSource() {
            return _contentSource;
        }

        public boolean isContentSourceFile(){
            return _contentSource==ContentSource.FILE;
        }

        public ValueDef getValue() {
            return _value;
        }

        public String getClassName() {
            return _className;
        }

        public KeyDef getKey() {
            return _key;
        }

        public String getContentFilename() {
            return _contentFilename;
        }

        public String getContent() {
            return _content;
        }

        public String getName() {
            return _name;
        }

        public enum ContentSource{
            FILE,
            STRING
        }


        public ViewDef(EntityDef entity,CouchbaseDocumentReflection documentReflection,View viewAnnot){
            _documentReflection = documentReflection;
            _name=viewAnnot.name();
            _className = _name.substring(0,1).toUpperCase()+_name.substring(1);

            if((viewAnnot.content() != null) && !viewAnnot.content().equals("")) {
                _content = viewAnnot.content();
                _contentSource = ContentSource.STRING;
            }
            else if((viewAnnot.contentFilename()!=null) && (!viewAnnot.contentFilename().equals(""))) {
                _contentFilename = viewAnnot.contentFilename();
                _contentSource = ContentSource.FILE;
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(_contentFilename);
                if(inputStream==null){
                    throw new RuntimeException("Cannot find the requested file <"+_contentFilename+">");
                }
                else{
                    try {
                        IOUtils.toString(inputStream, "UTF-8");
                    }
                    catch(IOException e){
                        throw new RuntimeException("Cannot read file content <"+_contentFilename+">",e);
                    }
                }
            }
            else{
                throw new RuntimeException("Cannot find content for view <" + viewAnnot.name() + "> for entity " + entity.getName());
            }

            ViewKeyDef keyDefAnnot = viewAnnot.keyDef();
            _key = new KeyDef(keyDefAnnot);

            ViewValueDef viewValueDef = viewAnnot.valueDef();
            _value = new ValueDef(viewValueDef);
        }


        public static class KeyDef{
            private String _type;
            private String _transcoder;

            public KeyDef(ViewKeyDef def){
                try {
                    Class clazz = def.type();
                    _type = clazz.getName();

                }
                catch(MirroredTypeException e){
                    AnnotationProcessorUtils.ClassInfo typeClassInfo= new AnnotationProcessorUtils.ClassInfo((DeclaredType) e.getTypeMirror());
                    _type = typeClassInfo.getName();
                }

                try {
                    Class<? extends IViewKeyTranscoder> clazz = def.transcoder();
                    _transcoder = clazz.getName();

                }
                catch(MirroredTypeException e){
                    AnnotationProcessorUtils.ClassInfo transcoderClassInfo= new AnnotationProcessorUtils.ClassInfo((DeclaredType) e.getTypeMirror());
                    _transcoder = transcoderClassInfo.getName();
                }
            }

            public String getType() {
                return _type;
            }

            public String getTranscoder() {
                return _transcoder;
            }
        }

        public static class ValueDef{
            private String _type;
            private String _transcoder;

            public ValueDef(ViewValueDef def){
                try {
                    Class clazz = def.type();
                    _type = clazz.getName();

                }
                catch(MirroredTypeException e){
                    AnnotationProcessorUtils.ClassInfo typeClassInfo= new AnnotationProcessorUtils.ClassInfo((DeclaredType) e.getTypeMirror());
                    _type = typeClassInfo.getName();
                }

                try {
                    Class<? extends IViewTranscoder> clazz = def.transcoder();
                    _transcoder = clazz.getName();

                }
                catch(MirroredTypeException e){
                    AnnotationProcessorUtils.ClassInfo transcoderClassInfo= new AnnotationProcessorUtils.ClassInfo((DeclaredType) e.getTypeMirror());
                    _transcoder = transcoderClassInfo.getName();
                }
            }

            public String getType() {
                return _type;
            }

            public String getTranscoder() {
                return _transcoder;
            }
        }
    }

    public static class EntityDef{
        private CouchbaseDocumentReflection _docReflection;

        private String _parentKeyAccessor;
        public EntityDef(CouchbaseDocumentReflection docReflection){
            _docReflection = docReflection;
            ParentEntity parentAnnot = docReflection.getClassInfo().getAnnotation(ParentEntity.class);
            if(parentAnnot!=null){
                String[] fieldNameParts = parentAnnot.keyPath().split("\\.");
                CouchbaseDocumentStructureReflection currStructure = docReflection.getStructure();
                _parentKeyAccessor = "";
                for(int partPos=0;partPos<fieldNameParts.length;++partPos){
                    CouchbaseDocumentFieldReflection field = currStructure.getFieldByName(fieldNameParts[partPos]);
                    _parentKeyAccessor += "."+field.buildGetterCode();
                    if(partPos+1<fieldNameParts.length){
                        currStructure = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(field.getEffectiveTypeInfo().getMainClass());
                    }
                }
            }
        }

        public String getSimpleName(){
            return _docReflection.getSimpleName();
        }

        public String getName(){
            return _docReflection.getName();
        }

        public String getDomain(){
            return _docReflection.getStructure().getStructDomain();
        }

        public String getParentKeyAccessor() {
            return _parentKeyAccessor;
        }

        public String getPackageName(){return _docReflection.getClassInfo().getPackageName();}
    }

    public class JavaEscape{
        public String java(String input){
            return StringEscapeUtils.escapeJava(input);
        }
    }
}
