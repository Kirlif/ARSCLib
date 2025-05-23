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

import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.NullValueKey;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.value.DexValueType;

import java.io.IOException;

public class SmaliValueNull extends SmaliValue {

    public SmaliValueNull(){
        super();
    }

    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.NULL;
    }

    @Override
    public NullValueKey getKey() {
        return NullValueKey.INSTANCE;
    }
    @Override
    public void setKey(Key key) {
        NullValueKey nullValueKey = (NullValueKey) key;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append("null");
    }
    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        byte[] bytes = new byte[]{'n', 'u', 'l', 'l'};
        if(reader.startsWith(bytes)){
            reader.skip(bytes.length);
        }else {
            throw new SmaliParseException("Not 'null' value", reader);
        }
    }
}
