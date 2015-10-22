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

package com.dreameddeath.core.helper.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.core.helper.annotation.dao.View;
import com.dreameddeath.core.helper.annotation.dao.ViewKeyDef;
import com.dreameddeath.core.helper.annotation.dao.ViewValueDef;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Christophe Jeunesse on 10/04/2015.
 */
public class ViewDef {
    private CouchbaseDocumentReflection documentReflection;
    private String className;
    private String name;
    private String content;
    private String contentFilename = null;
    private KeyDef key;
    private ValueDef value;
    private ContentSource contentSource;

    public ContentSource getContentSource() {
        return contentSource;
    }

    public boolean isContentSourceFile() {
        return contentSource == ContentSource.FILE;
    }

    public ValueDef getValue() {
        return value;
    }

    public String getClassName() {
        return className;
    }

    public KeyDef getKey() {
        return key;
    }

    public String getContentFilename() {
        return contentFilename;
    }

    public String getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

    public enum ContentSource {
        FILE,
        STRING
    }


    public ViewDef(EntityDef entity, CouchbaseDocumentReflection documentReflection, View viewAnnot) {
        this.documentReflection = documentReflection;
        name = viewAnnot.name();
        className = name.substring(0, 1).toUpperCase() + name.substring(1);

        if ((viewAnnot.content() != null) && !viewAnnot.content().equals("")) {
            content = viewAnnot.content();
            contentSource = ContentSource.STRING;
        } else if ((viewAnnot.contentFilename() != null) && (!viewAnnot.contentFilename().equals(""))) {
            contentFilename = viewAnnot.contentFilename();
            contentSource = ContentSource.FILE;
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(contentFilename);
            if (inputStream == null) {
                throw new RuntimeException("Cannot find the requested file <" + contentFilename + ">");
            } else {
                try {
                    IOUtils.toString(inputStream, "UTF-8");
                } catch (IOException e) {
                    throw new RuntimeException("Cannot read file content <" + contentFilename + ">", e);
                }
            }
        } else {
            throw new RuntimeException("Cannot find content for view <" + viewAnnot.name() + "> for entity " + entity.getName());
        }

        ViewKeyDef keyDefAnnot = viewAnnot.keyDef();
        key = new KeyDef(keyDefAnnot);

        ViewValueDef viewValueDef = viewAnnot.valueDef();
        value = new ValueDef(viewValueDef);
    }


    public static class KeyDef {
        private String type;
        private String transcoder;

        public KeyDef(ViewKeyDef def) {
            type = AbstractClassInfo.getClassInfoFromAnnot(def, annot1 -> def.type()).getName();
            transcoder = AbstractClassInfo.getClassInfoFromAnnot(def, annot1 -> def.transcoder()).getName();
        }

        public String getType() {
            return type;
        }

        public String getTranscoder() {
            return transcoder;
        }
    }

    public static class ValueDef {
        private String type;
        private String transcoder;

        public ValueDef(ViewValueDef def) {
            type = AbstractClassInfo.getClassInfoFromAnnot(def, ViewValueDef::type).getName();
            transcoder = AbstractClassInfo.getClassInfoFromAnnot(def, ViewValueDef::transcoder).getName();
        }

        public String getType() {
            return type;
        }

        public String getTranscoder() {
            return transcoder;
        }
    }
}
