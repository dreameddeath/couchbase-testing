/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.compile.tools.annotation.processor;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by Christophe Jeunesse on 09/08/2015.
 */
public class AnnotationProcessFileUtils {

    public static ResourceFile createResourceFile(ProcessingEnvironment env,String filename,Element... originatingElements)throws IOException {
        FileObject jfo = env.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                "",//Empty Package
                filename,
                originatingElements);
        Writer writer = new BufferedWriter(jfo.openWriter());
        return new ResourceFile(jfo,writer,env.getMessager());
        //((VelocityLogger)context.get("message")).printNote("Generating resource file " + jfo.getMethodParamName());
    }

    public static class ResourceFile{
        private FileObject fileObject;
        private Writer writer;
        private Messager messager;

        public ResourceFile(FileObject fileObject,Writer writer,Messager messager){
            this.fileObject = fileObject;
            this.writer = writer;
            this.messager = messager;
        }

        public FileObject getFileObject() {
            return fileObject;
        }

        public Writer getWriter() {
            return writer;
        }

        public void close(){
            try {
                writer.close();
                messager.printMessage(Diagnostic.Kind.NOTE, "Generating resource file " + fileObject.getName());
            }
            catch(IOException e){
                messager.printMessage(Diagnostic.Kind.ERROR, "Error during generating " + fileObject.getName()+" "+e.getMessage());
            }
        }
    }

}
