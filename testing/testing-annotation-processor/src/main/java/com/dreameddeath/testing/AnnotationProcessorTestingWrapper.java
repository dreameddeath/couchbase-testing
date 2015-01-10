package com.dreameddeath.testing;

import javax.annotation.processing.Processor;
import javax.tools.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by CEAJ8230 on 10/01/2015.
 */
public class AnnotationProcessorTestingWrapper {
    private JavaCompiler _compiler = ToolProvider.getSystemJavaCompiler();
    private String _tempDirectoryPrefix;
    private List<Processor> _annotationProcessors=new ArrayList<>();

    public Result run(String path) throws Exception {
        Result result = new Result();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        result.setDiagnostics(diagnostics);

        //String source = this.getClass().getClassLoader().getResource(path).getPath();
        Iterable<JavaFileObject> files = getSourceFiles(path);
        result.setSourceFiles(files);

        Path tmpDir = Files.createTempDirectory(_tempDirectoryPrefix);
        result.setOutputDir(tmpDir);

        StandardJavaFileManager fileManager=_compiler.getStandardFileManager(null,null,null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(tmpDir.toFile()));
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(tmpDir.toFile()));
        result.setOutputManager(fileManager);

        JavaCompiler.CompilationTask task = _compiler.getTask(new PrintWriter(System.out), fileManager, diagnostics, null, null, files);
        task.setProcessors(_annotationProcessors);

        result.setResult(task.call());
        return result;
    }

    private Iterable<JavaFileObject> getSourceFiles(String p_path) throws Exception {
        StandardJavaFileManager files = _compiler.getStandardFileManager(null, null, null);

        files.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(new File(p_path)));

        Set<JavaFileObject.Kind> fileKinds = Collections.singleton(JavaFileObject.Kind.SOURCE);
        return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
    }

    public String getTempDirectoryPrefix() {
        return _tempDirectoryPrefix;
    }

    public void setTempDirectoryPrefix(String tempDirectoryPrefix) {
        _tempDirectoryPrefix = tempDirectoryPrefix;
    }

    public List<Processor> getAnnotationProcessors() {
        return Collections.unmodifiableList(_annotationProcessors);
    }

    public void addAnnotationProcessor(Processor processor){
        _annotationProcessors.add(processor);
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
        private Boolean _result;
        private DiagnosticCollector<JavaFileObject> _diagnostics;
        private Iterable<JavaFileObject> _sourceFiles;
        private Path _outputDir;
        private JavaFileManager _outputManager;
        private ClassLoader _outputClassLoader;

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
            return _outputClassLoader.loadClass(name);
        }

        public boolean hasFile(String name){
            return getFile(name)!=null;
        }

        public File getFile(String name){
            URL result = _outputClassLoader.getResource(name);
            if(result!=null) {
                return new File(result.getFile());
            }

            return null;
        }


        public void cleanUp(){
            try {
                deleteRecursive(_outputDir.toFile());
            }
            catch(FileNotFoundException e){
                //Ignore error
            }
        }

        public Boolean getResult() {
            return _result;
        }

        public void setResult(Boolean result) {
            _result = result;
        }

        public DiagnosticCollector<JavaFileObject> getDiagnostics() {
            return _diagnostics;
        }

        public void setDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
            _diagnostics = diagnostics;
        }

        public Iterable<JavaFileObject> getSourceFiles() {
            return _sourceFiles;
        }

        public void setSourceFiles(Iterable<JavaFileObject> sourceFiles) {
            _sourceFiles = sourceFiles;
        }

        public Path getOutputDir() {
            return _outputDir;
        }

        public void setOutputDir(Path outputDir) {
            _outputDir = outputDir;
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
            return _outputManager;
        }

        public void setOutputManager(JavaFileManager outputManager) {
            _outputManager = outputManager;
            _outputClassLoader = _outputManager.getClassLoader(StandardLocation.CLASS_OUTPUT);
        }
    }

}
