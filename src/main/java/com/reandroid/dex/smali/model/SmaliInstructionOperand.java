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

import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.key.*;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public abstract class SmaliInstructionOperand extends Smali{

    public SmaliInstructionOperand(){
        super();
    }

    public long getDataLong(){
        return getData() & 0x00000000ffffffffL;
    }
    public abstract int getData();
    @Override
    public abstract void append(SmaliWriter writer) throws IOException;

    @Override
    public final void parse(SmaliReader reader) throws IOException {
        throw new RuntimeException("Must call parse(Opcode, SmaliReader)");
    }
    public abstract void parse(Opcode<?> opcode, SmaliReader reader) throws IOException;

    public static class LabelOperand extends SmaliInstructionOperand {

        private final SmaliLabel label;

        public LabelOperand(){
            super();
            this.label = new SmaliLabel();
            this.label.setParent(this);
        }

        public SmaliLabel getLabel() {
            return label;
        }

        @Override
        public int getData() {
            return getLabel().getAddress();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            getLabel().append(writer);
        }

        @Override
        public void parse(Opcode<?> opcode, SmaliReader reader) throws IOException {
            getLabel().parse(reader);
        }
    }
    public static class HexOperand extends SmaliInstructionOperand {

        private SmaliValueNumber<?> valueNumber;

        public HexOperand(){
            super();
        }

        public Number getNumber() {
            return valueNumber.getNumber();
        }
        public void setNumber(Number number) {
            setNumberValue(SmaliValueNumber.createFor(number));
        }

        public SmaliValueNumber<?> getValueNumber() {
            return valueNumber;
        }
        public void setNumberValue(SmaliValueNumber<?> valueNumber) {
            this.valueNumber = valueNumber;
            if(valueNumber != null){
                valueNumber.setParent(this);
            }
        }

        @Override
        public int getData() {
            Number number = getNumber();
            int i = number.intValue();
            if(number instanceof Byte){
                return i & 0xff;
            }
            if(number instanceof Short){
                return i & 0xffff;
            }
            return i;
        }
        @Override
        public long getDataLong() {
            Number number = getNumber();
            if(number instanceof Long){
                return number.longValue();
            }
            return 0x00000000ffffffffL & getData();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.appendOptional(getValueNumber());
        }

        @Override
        public void parse(Opcode<?> opcode, SmaliReader reader) throws IOException {
            reader.skipSpaces();
            SmaliValueNumber<?> value = SmaliValueNumber.createNumber(reader);
            setNumberValue(value);
            value.parse(reader);
        }
    }
    public static class IntegerOperand extends SmaliInstructionOperand {
        private int number;

        public IntegerOperand(){
            super();
        }

        public int getNumber() {
            return number;
        }
        public void setNumber(int number) {
            this.number = number;
        }

        @Override
        public int getData() {
            return getNumber();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.appendInteger(getNumber());
        }

        @Override
        public void parse(Opcode<?> opcode, SmaliReader reader) throws IOException {
            reader.skipSpaces();
            setNumber(reader.readInteger());
        }
    }
    public static class KeyOperand extends SmaliInstructionOperand {
        private Key key;

        public KeyOperand(){
            super();
        }
        public Key getKey() {
            return key;
        }
        public void setKey(Key key) {
            this.key = key;
        }

        @Override
        public int getData() {
            return -1;
        }
        @Override
        public long getDataLong() {
            return -1;
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            Key key = getKey();
            if(key != null){
                key.append(writer);
            }
        }
        @Override
        public void parse(Opcode<?> opcode, SmaliReader reader) throws IOException {
            SectionType<?> sectionType = opcode.getSectionType();
            Key key;
            if(sectionType == SectionType.STRING_ID){
                key = StringKey.read(reader);
            }else if(sectionType == SectionType.TYPE_ID){
                key = TypeKey.read(reader);
            }else if(sectionType == SectionType.FIELD_ID){
                key = FieldKey.read(reader);
            }else if(sectionType == SectionType.METHOD_ID){
                key = MethodKey.read(reader);
            }else {
                throw new SmaliParseException("Invalid key", reader);
            }
            setKey(key);
        }
    }
    public static final SmaliInstructionOperand NO_OPERAND = new SmaliInstructionOperand() {
        @Override
        public int getData() {
            return -1;
        }
        @Override
        public long getDataLong() {
            return -1;
        }

        @Override
        public void append(SmaliWriter writer) {
        }
        @Override
        public void parse(Opcode<?> opcode, SmaliReader reader) {
            reader.skipSpaces();
        }
        @Override
        void setParent(Smali parent) {
        }
    };
}