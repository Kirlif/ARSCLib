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
package com.reandroid.dex.debug;

import com.reandroid.dex.smali.model.Smali;
import com.reandroid.dex.smali.model.SmaliDebugEpilogue;

public class DebugEpilogue extends DebugElement {

    public DebugEpilogue() {
        super(DebugElementType.EPILOGUE);
    }

    @Override
    public void fromSmali(Smali smali) {
        super.fromSmali(smali);
        if(!(smali instanceof SmaliDebugEpilogue)){
            throw new ClassCastException("Mismatch class: " + smali.getClass() +
                    ", expecting: " + SmaliDebugEpilogue.class);
        }
    }
    @Override
    public DebugElementType<DebugEpilogue> getElementType() {
        return DebugElementType.EPILOGUE;
    }
}
