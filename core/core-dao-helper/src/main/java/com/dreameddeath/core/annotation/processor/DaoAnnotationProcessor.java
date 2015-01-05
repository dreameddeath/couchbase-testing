package com.dreameddeath.core.annotation.processor;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.dao.DaoEntity;
import com.dreameddeath.core.annotation.dao.UidDef;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import org.apache.velocity.VelocityContext;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Set;

/**
 * Created by CEAJ8230 on 29/12/2014.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.core.annotation.dao.DaoEntity"}
)
public class DaoAnnotationProcessor extends AbstractProcessor {
    /*public static final  VelocityEngine VELOCITY_ENGINE;
    static {
        VELOCITY_ENGINE = new VelocityEngine("core/templates/velocity.properties");
        VELOCITY_ENGINE.init();
    }

    public static final Template DAO_BUILDER_TEMPLATE = VELOCITY_ENGINE.getTemplate("core/templates/stdDaoTemplate.vm");
*/
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        for (Element classElem : roundEnv.getElementsAnnotatedWith(DaoEntity.class)) {
            Elements elementUtils = processingEnv.getElementUtils();

            EntityDef entity = new EntityDef(classElem);

            VelocityContext context = new VelocityContext();



            /*JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                    fqClassName + "BeanInfo");
            /*try {
                String fileName = Utils.getFilename(annot, classElem);
                FileObject jfo = processingEnv.getFiler().createResource(
                        StandardLocation.CLASS_OUTPUT,
                        "",
                        fileName,
                        classElem);
                String packageName = elementUtils.getPackageOf(classElem).getQualifiedName().toString();
                String fullClassName = ((TypeElement) classElem).getQualifiedName().toString();
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

    public static class DbPathDef{
        private String _baseName;
        private String _idFormat;
        private String _idPattern;
        private String _formatPrefix="";
        private String _patternPrefix="";

    }

    public static class DaoDef{
        private String _simpleName;
        private String _name;
        private Type _type;
        private UidType _uidType;
        private String _uidSetterPattern;
        private String _uidGetterPattern;

        public DaoDef(Element element){
            DaoEntity annot = element.getAnnotation(DaoEntity.class);
            DocumentDef docDef=element.getAnnotation(DocumentDef.class);

            TypeElement tElem =((TypeElement)element);
            _simpleName =annot.baseDao().getSimpleName();
            _name=annot.baseDao().getName();
            if(CouchbaseDocumentWithKeyPatternDao.class.isAssignableFrom(annot.baseDao())){
                if(BusinessCouchbaseDocumentDaoWithUID.class.isAssignableFrom(annot.baseDao())){
                    _type = Type.WITH_UID;
                    UidDef uidDef = element.getAnnotation(UidDef.class);
                    if(uidDef!=null){
                        String[] fieldNameParts = uidDef.fieldName().split("\\.");
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
        private String _simpleName;
        private String _name;
        private String _domain;
        private String _parentKeyAccessor;
        public EntityDef(Element element){
            DaoEntity annot = element.getAnnotation(DaoEntity.class);
            DocumentDef docDef=element.getAnnotation(DocumentDef.class);

            TypeElement tElem =((TypeElement)element);
            _simpleName =tElem.getSimpleName().toString();
            _name=tElem.getQualifiedName().toString();
            _domain = (docDef!=null)?docDef.domain():"";

            //new StringBuilder().append(packageName).append(".").append(fullClassName.substring(packageName.length() + 1).replace(".", "$")).toString();
        }

        public String getSimpleName(){
            return _simpleName;
        }

    }
}
