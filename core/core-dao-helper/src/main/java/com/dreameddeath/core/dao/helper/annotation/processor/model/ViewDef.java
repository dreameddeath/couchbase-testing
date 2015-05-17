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

package com.dreameddeath.core.dao.helper.annotation.processor.model;

import com.dreameddeath.core.dao.helper.annotation.View;
import com.dreameddeath.core.dao.helper.annotation.ViewKeyDef;
import com.dreameddeath.core.dao.helper.annotation.ViewValueDef;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.tools.annotation.processor.reflection.AbstractClassInfo;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by CEAJ8230 on 10/04/2015.
 */
public class ViewDef {
    private CouchbaseDocumentReflection _documentReflection;
    private String _className;
    private String _name;
    private String _content;
    private String _contentFilename = null;
    private KeyDef _key;
    private ValueDef _value;
    private ContentSource _contentSource;

    public ContentSource getContentSource() {
        return _contentSource;
    }

    public boolean isContentSourceFile() {
        return _contentSource == ContentSource.FILE;
    }

    public ValueDef getValue() {
        return _value;
    }

    public String getClassName() {
        return _className;
    }

    public KeyDef getKey() {
        return _key;
    }

    public String getContentFilename() {
        return _contentFilename;
    }

    public String getContent() {
        return _content;
    }

    public String getName() {
        return _name;
    }

    public enum ContentSource {
        FILE,
        STRING
    }


    public ViewDef(EntityDef entity, CouchbaseDocumentReflection documentReflection, View viewAnnot) {
        _documentReflection = documentReflection;
        _name = viewAnnot.name();
        _className = _name.substring(0, 1).toUpperCase() + _name.substring(1);

        if ((viewAnnot.content() != null) && !viewAnnot.content().equals("")) {
            _content = viewAnnot.content();
            _contentSource = ContentSource.STRING;
        } else if ((viewAnnot.contentFilename() != null) && (!viewAnnot.contentFilename().equals(""))) {
            _contentFilename = viewAnnot.contentFilename();
            _contentSource = ContentSource.FILE;
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(_contentFilename);
            if (inputStream == null) {
                throw new RuntimeException("Cannot find the requested file <" + _contentFilename + ">");
            } else {
                try {
                    IOUtils.toString(inputStream, "UTF-8");
                } catch (IOException e) {
                    throw new RuntimeException("Cannot read file content <" + _contentFilename + ">", e);
                }
            }
        } else {
            throw new RuntimeException("Cannot find content for view <" + viewAnnot.name() + "> for entity " + entity.getName());
        }

        ViewKeyDef keyDefAnnot = viewAnnot.keyDef();
        _key = new KeyDef(keyDefAnnot);

        ViewValueDef viewValueDef = viewAnnot.valueDef();
        _value = new ValueDef(viewValueDef);
    }


    public static class KeyDef {
        private String _type;
        private String _transcoder;

        public KeyDef(ViewKeyDef def) {
            _type = AbstractClassInfo.getClassInfoFromAnnot(def, annot1 -> def.type()).getName();
            _transcoder = AbstractClassInfo.getClassInfoFromAnnot(def, annot1 -> def.transcoder()).getName();
        }

        public String getType() {
            return _type;
        }

        public String getTranscoder() {
            return _transcoder;
        }
    }

    public static class ValueDef {
        private String _type;
        private String _transcoder;

        public ValueDef(ViewValueDef def) {
            _type = AbstractClassInfo.getClassInfoFromAnnot(def, ViewValueDef::type).getName();
            _transcoder = AbstractClassInfo.getClassInfoFromAnnot(def, ViewValueDef::transcoder).getName();
        }

        public String getType() {
            return _type;
        }

        public String getTranscoder() {
            return _transcoder;
        }
    }
}
