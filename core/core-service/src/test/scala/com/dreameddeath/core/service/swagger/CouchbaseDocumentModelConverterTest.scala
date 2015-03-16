package com.dreameddeath.core.service.swagger

import com.dreameddeath.core.service.swagger.TestingDocument.TestingInnerElement
import com.wordnik.swagger.converter.ModelConverters
import org.junit.{Assert, Test}

/**
 * Created by CEAJ8230 on 27/02/2015.
 */
class CouchbaseDocumentModelConverterTest extends Assert {

    @Test
    def testGeneration(): Unit ={
        /*val obj=new TestingDocument()
        val obj_sub = new TestingInnerElement()
        val res_subclass=new CouchbaseDocumentModel().read(obj_sub.getClassFrom,Map.empty[String,String])
        val res=new CouchbaseDocumentModel().read(obj.getClassFrom,Map.empty[String,String])*/

        //val res_std = ModelConverters.readAll(obj.getClassFrom)

        ModelConverters.addConverter(new CouchbaseDocumentModelConverter(),true)
        val res_integrated = ModelConverters.readAll(classOf[TestingDocument])

        Assert.assertEquals(3L,res_integrated.size)
        for(model<-res_integrated){
            model.id match {
                case "TestingDocument" => {
                    Assert.assertEquals(classOf[TestingDocument].getName, model.qualifiedType)
                    Assert.assertEquals(6L,model.properties.size)
                    //TODO better check of properties
                }
                case "TestingInnerElement"=>{
                    Assert.assertEquals(classOf[TestingInnerElement].getName, model.qualifiedType)
                    Assert.assertEquals(1L,model.properties.size)
                    //TODO better check of properties
                }
                case "TestingExternalElement"=>{
                    Assert.assertEquals(classOf[TestingExternalElement].getName, model.qualifiedType)
                    Assert.assertEquals(1L,model.properties.size)
                    //TODO better check of properties
                }
            }
        }

    }
}
