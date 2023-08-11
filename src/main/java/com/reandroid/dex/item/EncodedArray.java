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
package com.reandroid.dex.item;

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.value.DexValue;
import com.reandroid.dex.value.DexValueType;

import java.io.IOException;
import java.util.Iterator;

public class EncodedArray extends BaseItem{
    private final Ule128Item elementCount;
    private final BlockList<DexValue<?>> elements;
    public EncodedArray() {
        super(2);
        this.elementCount = new Ule128Item();
        this.elements = new BlockList<>();
        addChild(0, elementCount);
        addChild(1, elements);
    }
    public int getCountValue() {
        return elementCount.get();
    }
    public BlockList<DexValue<?>> getElements() {
        return elements;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        super.onReadBytes(reader);
        int count = elementCount.get();
        for(int i = 0; i < count; i++){
            int type = reader.read();
            reader.offset(-1);
            DexValueType valueType = DexValueType.fromFlag(type);
            DexValue<?> dexValue = DexValue.createFor(valueType);
            elements.add(dexValue);
            dexValue.readBytes(reader);
        }
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        Iterator<DexValue<?>> iterator = getElements().iterator();
        boolean appendOnce = false;
        while (iterator.hasNext()){
            if(appendOnce){
                builder.append(',');
            }
            builder.append('\n');
            builder.append("    ");
            builder.append(iterator.next());
            appendOnce = true;
        }
        if(appendOnce){
            builder.append('\n');
        }
        builder.append('}');
        return builder.toString();
    }
}