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

package com.dreameddeath.testing;

import javax.annotation.processing.Processor;
import javax.tools.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Christophe Jeunesse on 10/01/2015.
 */
public class AnnotationProcessorTestingWrapper {
    private JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private String tempDirectoryPrefix;
    private List<Processor> annotationProcessors=new ArrayList<>();

    public Result run(Iterable<JavaFileObject> files) throws Exception{
        Result result = new Result();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        result.setDiagnostics(diagnostics);

        result.setSourceFiles(files);

        Path tmpDir = Files.createTempDirectory(tempDirectoryPrefix);
        result.setOutputDir(tmpDir);

        StandardJavaFileManager fileManager=compiler.getStandardFileManager(null,null,null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(tmpDir.toFile()));
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(tmpDir.toFile()));
        result.setOutputManager(fileManager);

        JavaCompiler.CompilationTask task = compiler.getTask(new PrintWriter(System.out), fileManager, diagnostics, null, null, files);
        task.setProcessors(annotationProcessors);

        result.setResult(task.call());
        return result;
    }

    public Result run(String path) throws Exception {
        Iterable<JavaFileObject> files = getSourceFiles(path);
        return run(files);
    }

    public Result run(Map<String,String> sources) throws Exception {
        List<JavaFileObject> files=new ArrayList<>();
        for(Map.Entry<String,String> srcDef :sources.entrySet()){
            files.add(new JavaSourceFromString(srcDef.getKey(),srcDef.getValue()));
        }
        return run(files);
    }


    private Iterable<JavaFileObject> getSourceFiles(String p_path) throws Exception {
        StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null);

        files.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(new File(p_path)));

        Set<JavaFileObject.Kind> fileKinds = Collections.singleton(JavaFileObject.Kind.SOURCE);
        return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
    }

    public String getTempDirectoryPrefix() {
        return tempDirectoryPrefix;
    }

    public void setTempDirectoryPrefix(String tempDirectoryPrefix) {
        this.tempDirectoryPrefix = tempDirectoryPrefix;
    }

    public List<Processor> getAnnotationProcessors() {
        return Collections.unmodifiableList(annotationProcessors);
    }

    public void addAnnotationProcessor(Processor processor){
        annotationProcessors.add(processor);
    }

    public AnnotationProcessorTestingWrapper withAnnotationProcessor(Processor processor){
        addAnnotationProcessor(processor);
        return this;
    }


    public AnnotationProcessorTestingWrapper  withTempDirectoryPrefix(String tempDirectoryPrefix) {
        setTempDirectoryPrefix(tempDirectoryPrefix);
        return this;
    }


    public static class Result{
        private Boolean result;
        private DiagnosticCollector<JavaFileObject> diagnostics;
        private Iterable<JavaFileObject> sourceFiles;
        private Path outputDir;
        private JavaFileManager outputManager;
        private ClassLoader outputClassLoader;
        private ClassLoader oldThreadClassLoader=null;

        public boolean hasClass(String name){
            try{
                if(getClass(name)!=null){
                    return true;
                }
            }
            catch (ClassNotFoundException e){
                //ignore
            }
            return false;
        }

        public Class getClass(String name) throws ClassNotFoundException{
            return outputClassLoader.loadClass(name);
        }

        public Constructor getConstructor(String className,Class<?>... parameterTypes)  throws ClassNotFoundException,NoSuchMethodException{
            Class<?>[] params = new Class<?>[parameterTypes.length];
            for(int i=0;i<parameterTypes.length;++i){
                params[i] = getClass(parameterTypes[i].getName());
            }

            return outputClassLoader.loadClass(className).getConstructor(params);
        }


        public Method getMethod(String className,String methodName,Class<?>... parameterTypes) throws ClassNotFoundException,NoSuchMethodException{
            Class<?>[] params = new Class<?>[parameterTypes.length];
            for(int i=0;i<parameterTypes.length;++i){
                params[i] = getClass(parameterTypes[i].getName());
            }
            return outputClassLoader.loadClass(className).getDeclaredMethod(methodName,params);
        }


        public boolean hasFile(String name){
            return getFile(name)!=null;
        }

        public File getFile(String name){
            URL result = outputClassLoader.getResource(name);
            if(result!=null) {
                return new File(result.getFile());
            }

            return null;
        }


        public void cleanUp(){
            try {
                if(oldThreadClassLoader!=null){
                    Thread.currentThread().setContextClassLoader(oldThreadClassLoader);
                }
                deleteRecursive(outputDir.toFile());
            }
            catch(FileNotFoundException e){
                //Ignore error
            }
        }

        public Boolean getResult() {
            return result;
        }

        public void setResult(Boolean result) {
            this.result = result;
        }

        public DiagnosticCollector<JavaFileObject> getDiagnostics() {
            return diagnostics;
        }

        public void setDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
            this.diagnostics = diagnostics;
        }

        public Iterable<JavaFileObject> getSourceFiles() {
            return sourceFiles;
        }

        public void setSourceFiles(Iterable<JavaFileObject> sourceFiles) {
            this.sourceFiles = sourceFiles;
        }

        public Path getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(Path outputDir) {
            this.outputDir = outputDir;
        }

        private  boolean deleteRecursive(File path) throws FileNotFoundException {
            if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
            boolean ret = true;
            if (path.isDirectory()){
                for (File f : path.listFiles()){
                    ret = ret && deleteRecursive(f);
                }
            }
            return ret && path.delete();
        }

        public JavaFileManager getOutputManager() {
            return outputManager;
        }

        public void setOutputManager(JavaFileManager outputManager) {
            this.outputManager = outputManager;
            outputClassLoader = outputManager.getClassLoader(StandardLocation.CLASS_OUTPUT);
        }

        public void updateSystemClassLoader() throws IOException{
            oldThreadClassLoader=Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(outputClassLoader);
        }
    }

    class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;

        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}
