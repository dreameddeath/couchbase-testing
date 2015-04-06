/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.service.swagger

import com.dreameddeath.core.model.document.{CouchbaseDocument, CouchbaseDocumentElement}
import com.dreameddeath.core.tools.annotation.processor.reflection.{AbstractClassInfo, ClassInfo}
import com.dreameddeath.core.util.{CouchbaseDocumentFieldReflection, CouchbaseDocumentStructureReflection}
import com.wordnik.swagger.converter.{BaseConverter, ModelConverter}
import com.wordnik.swagger.core.SwaggerSpec
import com.wordnik.swagger.model._

import scala.Predef._
import scala.collection.JavaConverters._
import scala.collection.immutable.Map
import scala.collection.mutable
import scala.collection.mutable.{HashMap, LinkedHashMap}

class CouchbaseDocumentModelConverter extends ModelConverter with BaseConverter {
    val m = new HashMap[String, String]

    def isComplex(typeName: String): Boolean = {
        !SwaggerSpec.baseTypes.contains(typeName.toLowerCase)
    }

    override def read(cls: Class[_], typeMap: Map[String, String]): Option[Model]={
        var model: Option[Model] = None
        if(classOf[CouchbaseDocument].isAssignableFrom(cls) || classOf[CouchbaseDocumentElement].isAssignableFrom(cls)){
            implicit val docStructReflection:CouchbaseDocumentStructureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(AbstractClassInfo.getClassInfo(cls).asInstanceOf[ClassInfo])
            //implicit val docReflection:CouchbaseDocumentReflection  = CouchbaseDocumentReflection.getReflectionFromClass(cls.asInstanceOf[Class[CouchbaseDocument]])
            val sortedProperties = new LinkedHashMap[String, ModelProperty]()
            for(prop<-docStructReflection.getFields.asScala){
                sortedProperties+=prop.getName -> buildModel(prop,typeMap)
            }
            model = Some(Model(
                getId(docStructReflection),
                docStructReflection.getSimpleName(),
                docStructReflection.getName(),
                sortedProperties,
                Some("")
            ))
        }
        model
    }

    def getId(docReflection:CouchbaseDocumentStructureReflection): String ={
        if(docReflection.getId!=null){
            docReflection.getId
        }
        else{
            docReflection.getSimpleName
        }
    }

    def buildModel(field:CouchbaseDocumentFieldReflection, typeMap: Map[String, String]): ModelProperty={
        val fieldType:String=typeMap.getOrElse(field.getEffectiveTypeClass.getSimpleName.toLowerCase, field.getEffectiveTypeClass.getSimpleName)
        var allowableValues:AllowableValues = AnyAllowableValues
        val isRequired:Boolean = true
        if(fieldType.getClass.isEnum){
            var values:mutable.MutableList[String]=mutable.MutableList.empty[String]
            for(e<-field.getClass.getEnumConstants){
                values+=e.toString //TODO manage more complex json mapping
            }
            allowableValues = AllowableListValues(values.toList)
        }
        var items:Option[ModelRef]=None
        if(field.isCollection || field.isMap){
            val complexType = field.getEffectiveTypeClass.getClass
            val itemType = if(field.isCollection)
                    field.getCollectionElementClass.getSimpleName
            else
                field.getMapValueClass.getSimpleName;

            val itemFullType = if(field.isCollection)
                field.getCollectionElementClass.getName
            else
                field.getMapValueClass.getName;


            if(isComplex(itemType)){
                items = Some(ModelRef(
                    null,Some(itemType),Some(itemFullType)
                ))
            }
            else{
                items = Some(ModelRef(
                    itemType,None,Some(itemFullType)
                ))
            }

        }
        ModelProperty(
            fieldType,
            field.getEffectiveTypeClass.getName,//TODO build better with generics
            0, //TODO manage annotations for position
            isRequired,//TODO manage Required
            Some(""),//Empty Description for now
            allowableValues,
            items
        )
    }


}