package com.dreameddeath.core.annotation.processor;


import com.dreameddeath.core.annotation.dao.DaoEntity;
import com.dreameddeath.core.annotation.dao.ParentEntity;
import com.dreameddeath.core.annotation.dao.UidDef;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.util.CouchbaseDocumentStructureReflection;
import com.dreameddeath.core.util.processor.AnnotationProcessorUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

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
import java.net.URL;
import java.util.*;

/**
 * Created by CEAJ8230 on 29/12/2014.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.core.annotation.dao.DaoEntity"}
)
public class DaoAnnotationProcessor extends AbstractProcessor {
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

    public static final Template DAO_BUILDER_TEMPLATE = VELOCITY_ENGINE.getTemplate("core/templates/stdDaoTemplate.vm");

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        Elements elementUtils = processingEnv.getElementUtils();
        for (Element element : roundEnv.getElementsAnnotatedWith(DaoEntity.class)){
            if(!CouchbaseDocumentReflection.isReflexible(element)) continue;
            CouchbaseDocumentReflection docReflection = CouchbaseDocumentReflection.getReflectionFromTypeElement((TypeElement)element);
            EntityDef entity = new EntityDef(docReflection);
            DaoDef daoDef = new DaoDef(docReflection);
            DbPathDef dbPathDef =new DbPathDef(docReflection);
            List<CounterDef>  counterDefList = new ArrayList<>();
            List<ViewDef> viewDefList = new ArrayList<>();
            VelocityContext context = new VelocityContext();



            /*

                VelocityEngine ve = new VelocityEngine(props);
                ve.init();

                VelocityContext vc = new VelocityContext();

                vc.put("classNameassName);
                vc.put("packageNameckageName);
                vc.put("fieldselds);
                vc.put("methodsthods);

                Template vt = ve.getTemplate("beaninfo.vm");

                    JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                    fqClassName + "BeanInfo");

                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "creating source file: " + jfo.toUri());

                Writer writer = jfo.openWriter();

                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "applying velocity template: " + vt.getName());

                vt.merge(vc, writer);

                writer.close();
            /*try {
                String fileName = Utils.getFilename(annot, element);
                FileObject jfo = processingEnv.getFiler().createResource(
                        StandardLocation.CLASS_OUTPUT,
                        "",
                        fileName,
                        element);
                String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
                String fullClassName = ((TypeElement) element).getQualifiedName().toString();
                String realClassName = new StringBuilder().append(packageName).append(".").append(fullClassName.substring(packageName.length() + 1).replace(".", "$")).toString();
                BufferedWriter bw = new BufferedWriter(jfo.openWriter());
                bw.write(realClassName);
                bw.flush();
                bw.close();
                messager.printMessage(Diagnostic.Kind.NOTE, "Creating file " + fileName + " for class " + realClassName);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Cannot write with error" + e.getMessage());
            }*/
        }
        return true;
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public static class DbPathDef {
        private String _baseName;
        private String _idFormat;
        private String _idPattern;
        private String _formatPrefix = "";
        private String _patternPrefix = "";

        public DbPathDef(CouchbaseDocumentReflection docReflection) {
            DaoEntity annot = docReflection.getClassInfo().getAnnotation(DaoEntity.class);
            _baseName = annot.dbPath();
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
            return _patternPrefix+_idPattern;
        }
        public String getFullFormat(){
            return _formatPrefix+_idFormat;
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

        public String getBaseName() {
            return _baseName;
        }
    }

    public static class DaoDef{
        private AnnotationProcessorUtils.ClassInfo _baseDaoClassInfo;
        private Type _type;
        private UidType _uidType;
        private String _uidSetterPattern;
        private String _uidGetterPattern;

        public DaoDef(CouchbaseDocumentReflection docReflection){
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
                            CouchbaseDocumentFieldReflection field = currStructure.getDeclaredFieldByName(fieldNameParts[partPos]);
                            //Last element
                            if(partPos+1==fieldNameParts.length){
                                _uidSetterPattern+=_uidGetterPattern+"."+field.getSetterName();
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

        public String getSimpleName() {
            return _baseDaoClassInfo.getSimpleName();
        }

        public String getName() {
            return _baseDaoClassInfo.getName();
        }

        public Type getType() {
            return _type;
        }

        public UidType getUidType() {
            return _uidType;
        }

        public String getUidSetterPattern() {
            return _uidSetterPattern;
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
        private boolean _isKeyGen;
        private int _defaultValue;
        private int _modulus;
    }

    public static class ViewDef{
        private String _name;
        private String _content;
        private KeyDef _key;
        private ValueDef _value;

        public static class KeyDef{
            private String _type;
            private String _transcoder;
        }

        public static class ValueDef{
            private String _type;
            private String _transcoder;
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
    }
}
