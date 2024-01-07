/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.dex.smali.model;

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class SmaliClass extends SmaliDef{

    private TypeKey superClass;
    private StringKey sourceFile;

    private final SmaliInterfaceSet interfaces;
    private final SmaliFieldSet  fields;
    private final SmaliMethodSet methods;

    public SmaliClass(){
        super();

        this.interfaces = new SmaliInterfaceSet();
        this.fields = new SmaliFieldSet();
        this.methods = new SmaliMethodSet();

        interfaces.setParent(this);
        fields.setParent(this);
        methods.setParent(this);
    }

    @Override
    public TypeKey getKey() {
        return TypeKey.create(getName());
    }
    public void setKey(TypeKey key) {
        String name;
        if(key != null){
            name = key.getTypeName();
        }else {
            name = null;
        }
        setName(name);
    }

    public TypeKey getSuperClass() {
        return superClass;
    }
    public void setSuperClass(TypeKey typeKey) {
        this.superClass = typeKey;
    }
    public StringKey getSourceFile() {
        return sourceFile;
    }
    public void setSourceFile(StringKey sourceFile) {
        this.sourceFile = sourceFile;
    }

    public SmaliInterfaceSet getInterfaces() {
        return interfaces;
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.CLASS;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        Modifier.append(writer, getAccessFlags());
        writer.appendOptional(getKey());
        writer.newLine();
        SmaliDirective.SUPER.append(writer);
        writer.appendOptional(getSuperClass());
        StringKey source = getSourceFile();
        if(source != null){
            writer.newLine();
            SmaliDirective.SOURCE.append(writer);
            source.append(writer);
        }
        getInterfaces().append(writer);
        if(hasAnnotation()){
            writer.newLine(2);
            writer.appendComment("annotations");
            writer.appendAllWithDoubleNewLine(getAnnotation().iterator());
        }
        fields.append(writer);
        methods.append(writer);


    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        parseClass(reader);
    }
    private void parseClass(SmaliReader reader) throws IOException {
        SmaliDirective directive = SmaliDirective.parse(reader);
        if(directive != SmaliDirective.CLASS){
            throw new SmaliParseException("Expecting '" + SmaliDirective.CLASS + "'" , reader);
        }
        // TODO: validate directive == SmaliDirective.CLASS
        setAccessFlags(AccessFlag.parse(reader));
        setKey(TypeKey.read(reader));
        while (parseNext(reader)){
            reader.skipWhitespacesOrComment();
        }
    }
    private boolean parseNext(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if(directive == SmaliDirective.CLASS){
            return false;
        }
        if(directive == SmaliDirective.SUPER){
            parseSuper(reader);
            return true;
        }
        if(directive == SmaliDirective.SOURCE){
            parseSource(reader);
            return true;
        }
        if(directive == SmaliDirective.ANNOTATION){
            getOrCreateAnnotation().parse(reader);
            return true;
        }
        if(directive == SmaliDirective.FIELD){
            fields.parse(reader);
            return true;
        }
        if(directive == SmaliDirective.METHOD){
            methods.parse(reader);
            return true;
        }
        if(directive == SmaliDirective.IMPLEMENTS){
            interfaces.parse(reader);
            return true;
        }
        return false;
    }
    private void parseSuper(SmaliReader reader) throws IOException {
        SmaliParseException.expect(reader, SmaliDirective.SUPER);
        setSuperClass(TypeKey.read(reader));
    }
    private void parseSource(SmaliReader reader) throws IOException{
        SmaliParseException.expect(reader, SmaliDirective.SOURCE);
        reader.skipSpaces();
        setSourceFile(StringKey.read(reader));
    }
}