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
package com.reandroid.dex.data;

import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.arsc.item.VisitableInteger;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

public abstract class DefArray<T extends Def<?>> extends BlockArray<T>  implements Iterable<T>, SmaliFormat, VisitableInteger {

    private final IntegerReference itemCount;

    private ClassId mClassId;

    public DefArray(IntegerReference itemCount){
        super();
        this.itemCount = itemCount;
    }

    @Override
    public boolean remove(T def){
        AnnotationsDirectory directory = getUniqueAnnotationsDirectory();
        if(directory != null){
            directory.remove(def);
        }
        boolean removed = super.remove(def);
        if(removed && directory != null){
            sortAnnotations();
        }
        if(removed){
            resetIndex();
        }
        updateCount();
        return removed;
    }
    @Override
    public final boolean sort(Comparator<? super T> comparator) {
        if(!needsSort(comparator)){
            return false;
        }
        onPreSort();
        boolean changed = super.sort(comparator);
        onPostSort();
        return changed;
    }
    void onPreSort(){
        ClassId classId = getClassId();
        if(classId != null){
            classId.getUniqueAnnotationsDirectory();
        }
        linkAnnotation();
    }
    void onPostSort(){
        resetIndex();
        sortAnnotations();
    }
    void sortAnnotations(){
    }

    public T getOrCreate(Key key) {
        T item = get(key);
        if(item != null){
            return item;
        }
        item = createNext();
        item.setKey(key);
        return item;
    }
    public T get(Key key) {
        for(T def : this){
            if(key.equals(def.getKey())){
                return def;
            }
        }
        return null;
    }
    @Override
    public void visitIntegers(IntegerVisitor visitor) {
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()) {
            iterator.next().visitIntegers(visitor);
        }
    }

    @Override
    public T createNext() {
        T item = super.createNext();
        updateCount();
        return item;
    }

    private ClassId searchClassId() {
        ClassId classId = mClassId;
        if(classId != null){
            return classId;
        }
        Iterator<T> iterator = iterator();
        if(iterator.hasNext()){
            classId = iterator.next().getClassId();
        }
        return classId;
    }
    public ClassId getClassId() {
        return mClassId;
    }
    public void setClassId(ClassId classId) {
        if(mClassId == classId){
            return;
        }
        this.mClassId = classId;
        linkAnnotation();
    }
    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        linkAnnotation();
        boolean sorted = sort(CompareUtil.getComparatorUnchecked());
        if(!sorted){
            resetIndex();
        }
    }

    @Override
    protected void onRefreshed() {
        updateCount();
    }
    public void updateCount(){
        itemCount.set(getCount());
    }
    public ClassData getClassData(){
        return (ClassData) getParent();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setChildesCount(itemCount.get());
        super.onReadBytes(reader);
    }
    private void linkAnnotation(){
        if(getCount() == 0){
            return;
        }
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory == null){
            return;
        }
        for(Def<?> def : this){
            directory.link(def);
        }
    }
    AnnotationsDirectory getAnnotationsDirectory(){
        ClassId classId = searchClassId();
        if(classId == null){
            return null;
        }
        return classId.getAnnotationsDirectory();
    }
    AnnotationsDirectory getUniqueAnnotationsDirectory(){
        ClassId classId = searchClassId();
        if(classId == null){
            return null;
        }
        return classId.getUniqueAnnotationsDirectory();
    }
    private void resetIndex(){
        for(T def : this){
            def.resetIndex();
        }
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        for(Def<?> def : getChildes()){
            def.append(writer);
            writer.newLine();
        }
    }
}