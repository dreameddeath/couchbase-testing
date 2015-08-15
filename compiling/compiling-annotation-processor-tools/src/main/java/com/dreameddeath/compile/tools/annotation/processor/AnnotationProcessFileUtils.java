/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        //((VelocityLogger)context.get("message")).note("Generating resource file " + jfo.getName());
    }

    public static class ResourceFile{
        private FileObject _fileObject;
        private Writer _writer;
        private Messager _messager;

        public ResourceFile(FileObject fileObject,Writer writer,Messager messager){
            _fileObject = fileObject;
            _writer = writer;
            _messager = messager;
        }

        public FileObject getFileObject() {
            return _fileObject;
        }

        public Writer getWriter() {
            return _writer;
        }

        public void close(){
            try {
                _writer.close();
                _messager.printMessage(Diagnostic.Kind.NOTE, "Generating resource file " + _fileObject.getName());
            }
            catch(IOException e){
                _messager.printMessage(Diagnostic.Kind.ERROR, "Error during generating " + _fileObject.getName()+" "+e.getMessage());
            }
        }
    }

}