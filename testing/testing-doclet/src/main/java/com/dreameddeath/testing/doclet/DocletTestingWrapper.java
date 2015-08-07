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

package com.dreameddeath.testing.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javadoc.JavadocTool;
import com.sun.tools.javadoc.ModifierFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
/**
 * Created by Christophe Jeunesse on 27/07/2015.
 */
public class DocletTestingWrapper {


    final private Logger log = LoggerFactory.getLogger(DocletTestingWrapper.class);

    final private File sourceDirectory;
    final private String[] packageNames;
    final private File[] fileNames;
    final private RootDoc rootDoc;

    public DocletTestingWrapper(File sourceDirectory, String... packageNames) {
        this(sourceDirectory, packageNames, new File[0]);
    }

    public DocletTestingWrapper(File sourceDirectory, File... fileNames) {
        this(sourceDirectory, new String[0], fileNames);
    }

    protected DocletTestingWrapper(File sourceDirectory, String[] packageNames, File[] fileNames) {
        this.sourceDirectory = sourceDirectory;
        this.packageNames = packageNames;
        this.fileNames = fileNames;

        Context context = new Context();
        Options compOpts = Options.instance(context);

        if (getSourceDirectory().exists()) {
            log.trace("Using source path: " + getSourceDirectory().getAbsolutePath());
            compOpts.put("-sourcepath", getSourceDirectory().getAbsolutePath());
        } else {
            log.info("Ignoring non-existant source path, check your source directory argument");
        }

        ListBuffer<String> javaNames = new ListBuffer<String>();
        for (File fileName : fileNames) {
            log.trace("Adding file to documentation path: " + fileName.getAbsolutePath());
            javaNames.append(fileName.getPath());
        }

        ListBuffer<String> subPackages = new ListBuffer<String>();
        for (String packageName : packageNames) {
            log.trace("Adding sub-packages to documentation path: " + packageName);
            subPackages.append(packageName);
        }

        /*new PublicMessager(
                context,
                getApplicationName(),
                new PrintWriter(new LogWriter(Level.SEVERE), true),
                new PrintWriter(new LogWriter(Level.WARNING), true),
                new PrintWriter(new LogWriter(Level.FINE), true)
        );*/

        JavadocTool javadocTool = JavadocTool.make0(context);
        try {

            rootDoc = javadocTool.getRootDocImpl(
                    "",
                    null,
                    new ModifierFilter(ModifierFilter.ALL_ACCESS),
                    javaNames.toList(),
                    new ListBuffer<String[]>().toList(),
                    null,//JavaFileObject
                    false,//Break
                    subPackages.toList(),
                    new ListBuffer<String>().toList(),
                    false,
                    false,
                    false);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (log.isTraceEnabled()) {
            for (ClassDoc classDoc : getRootDoc().classes()) {
                log.trace("Parsed Javadoc class source: " + classDoc.position() + " with inline tags: " + classDoc.inlineTags().length );
            }
        }
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public String[] getPackageNames() {
        return packageNames;
    }

    public File[] getFileNames() {
        return fileNames;
    }

    public RootDoc getRootDoc() {
        return rootDoc;
    }


    protected String getApplicationName() {
        return getClass().getSimpleName() + " Application";
    }

}
