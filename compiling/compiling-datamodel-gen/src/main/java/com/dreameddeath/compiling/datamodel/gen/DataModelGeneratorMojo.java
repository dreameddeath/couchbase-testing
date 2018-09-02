package com.dreameddeath.compiling.datamodel.gen;

import com.dreameddeath.compiling.datamodel.gen.generators.ImplementationGenerator;
import com.dreameddeath.compiling.datamodel.gen.generators.InterfaceGenerator;
import com.dreameddeath.compiling.datamodel.gen.generators.TypeHelper;
import com.dreameddeath.compiling.datamodel.gen.model.ModelDef;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Preconditions;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Christophe Jeunesse on 09/02/2018.
 */
@Mojo(name = "compiling-datamodel-gen", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class DataModelGeneratorMojo extends AbstractMojo {
    @Parameter(defaultValue = "${basedir}/src/main/resources/model-gen",readonly = true)
    public File modelPath;
    @Parameter(defaultValue="${project.build.directory}/generated-sources/datamodel-gen",readonly = true)
    public File outputPath;

    public final ObjectMapper mapper=new ObjectMapper(new YAMLFactory());

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Preconditions.checkArgument(outputPath.exists() || outputPath.mkdirs(),"Cannot find or create output directory");
        List<ModelDef> models = new ArrayList<>();
        for(File file: FileUtils.listFiles(modelPath, null, true)){
            try {
                if(!file.isDirectory()) {
                    ModelDef modelDef = mapper.readValue(file, ModelDef.class);
                    modelDef.relativeFilename = file.getAbsolutePath().substring(modelPath.getAbsolutePath().length() + 1);
                    modelDef.fullFilename = file.getAbsolutePath();
                    models.add(modelDef);
                }
            }
            catch (IOException e){
                throw new MojoExecutionException("Cannot manage file "+file.getAbsolutePath(),e);
            }
        }


        TypeHelper typeHelper = new TypeHelper();
        InterfaceGenerator interfaceGenerator=new InterfaceGenerator();
        ImplementationGenerator implementationGenerator = new ImplementationGenerator();
        for(ModelDef model:models){
            {
                TypeSpec generatedInterface = interfaceGenerator.generate(model);
                JavaFile javaFile = JavaFile.builder(typeHelper.getPackage(model, TypeHelper.SubType.INTERFACE), generatedInterface)
                        .indent("    ")
                        .build();
                try {

                    javaFile.writeTo(outputPath);
                } catch (IOException e) {
                    throw new MojoExecutionException("Cannot write file " + javaFile.packageName + "/" + javaFile.typeSpec.name, e);
                }
            }
            {
                TypeSpec generatedImpl = implementationGenerator.generate(model);
                JavaFile javaFile = JavaFile.builder(typeHelper.getPackage(model, TypeHelper.SubType.IMPL), generatedImpl)
                        .indent("    ")
                        .build();
                try {
                    javaFile.writeTo(outputPath);
                } catch (IOException e) {
                    throw new MojoExecutionException("Cannot write file " + javaFile.packageName + "/" + javaFile.typeSpec.name, e);
                }
            }

        }
    }


}
