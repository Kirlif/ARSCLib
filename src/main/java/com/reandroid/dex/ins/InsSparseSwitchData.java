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
package com.reandroid.dex.ins;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.CountedBlockList;
import com.reandroid.arsc.item.*;
import com.reandroid.common.ArraySupplier;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.dex.smali.model.SmaliPayloadSparseSwitch;
import com.reandroid.dex.smali.model.SmaliSparseSwitchEntry;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ArraySupplierIterator;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

public class InsSparseSwitchData extends InsSwitchPayload implements
        ArraySupplier<InsSparseSwitchData.SparseSwitchEntry> {

    private final ShortItem elementCount;
    final CountedBlockList<IntegerItem> elements;
    final CountedBlockList<EntryKey> keys;

    boolean mSortRequired;

    public InsSparseSwitchData() {
        super(3, Opcode.SPARSE_SWITCH_PAYLOAD);
        this.elementCount = new ShortItem();

        Creator<EntryKey> entryKeyCreator = EntryKey::new;

        this.elements = new CountedBlockList<>(IntegerItem.CREATOR, elementCount);
        this.keys = new CountedBlockList<>(entryKeyCreator, elementCount);

        addChild(1, elementCount);
        addChild(2, elements);
        addChild(3, keys);
    }

    @Override
    public Iterator<SwitchEntry> iterator() {
        return ObjectsUtil.cast(getLabels());
    }
    public SparseSwitchEntry newEntry() {
        int index = getCount();
        setCount(index + 1);
        return get(index);
    }
    @Override
    public SparseSwitchEntry get(int i){
        if(i < 0 || i >= getCount()){
            return null;
        }
        return new SparseSwitchEntry(this, elements.get(i), keys.get(i));
    }
    public boolean remove(SwitchEntry entry) {
        if (!(entry instanceof SparseSwitchEntry)) {
            return false;
        }
        SparseSwitchEntry switchEntry = (SparseSwitchEntry) entry;
        InsBlockList insBlockList = getInsBlockList();
        insBlockList.link();
        boolean removed = this.elements.remove((IntegerItem) switchEntry.element);
        if (removed) {
            this.keys.remove(switchEntry.key);
        }
        insBlockList.unlink();
        return removed;
    }
    @Override
    public int getCount(){
        return elements.size();
    }
    public void setCount(int count){
        int previous = getCount();
        boolean linked = false;
        InsBlockList insBlockList = getInsBlockList();
        if(count != previous) {
            if(insBlockList != null) {
                linked = insBlockList.isLinked();
                insBlockList.link();
            }
        }
        elements.setSize(count);
        keys.setSize(count);
        elementCount.set(count);
        if(insBlockList != null && !linked) {
            insBlockList.unlink();
        }
    }
    public void sort() {
        if(!mSortRequired) {
            return;
        }
        this.mSortRequired = false;
        Comparator<IntegerItem> comparator = (item1, item2) -> CompareUtil.compare(item1.get(), item2.get());
        if(!elements.needsSort(comparator)) {
            return;
        }
        this.elements.sort(comparator, keys);
    }
    @Override
    public Iterator<SparseSwitchEntry> getLabels() {
        return new ArraySupplierIterator<>(this);
    }

    public int getBaseAddress(){
        InsSparseSwitch sparseSwitch = getSwitch();
        if(sparseSwitch == null){
            return 0;
        }
        return sparseSwitch.getAddress();
    }

    @Override
    public Opcode<InsSparseSwitch> getSwitchOpcode() {
        return Opcode.SPARSE_SWITCH;
    }
    @Override
    public InsSparseSwitch getSwitch() {
        return (InsSparseSwitch) super.getSwitch();
    }
    @Override
    protected void onPreRefresh() {
        sort();
        super.onPreRefresh();
    }
    void fromPackedSwitchData(PackedSwitchDataList packedSwitchDataList) {
        int length = packedSwitchDataList.size();
        setCount(length);
        for(int i = 0; i < length; i++) {
            get(i).fromPackedSwitch(packedSwitchDataList.get(i));
        }
    }
    @Override
    public void merge(Ins ins){
        InsSparseSwitchData switchData = (InsSparseSwitchData) ins;
        int size = switchData.getCount();
        this.setCount(size);
        for(int i = 0; i < size; i++){
            get(i).merge(switchData.get(i));
        }
    }

    @Override
    public void fromSmali(SmaliInstruction smaliInstruction) {
        validateOpcode(smaliInstruction);
        SmaliPayloadSparseSwitch smaliPayload = (SmaliPayloadSparseSwitch) smaliInstruction;
        int count = smaliPayload.getCount();
        this.setCount(count);
        for(int i = 0; i < count; i++) {
            get(i).fromSmali(smaliPayload.getEntry(i));
        }
        mSortRequired = true;
    }
    @Override
    void toSmaliEntries(SmaliInstruction instruction) {
        super.toSmaliEntries(instruction);
        SmaliPayloadSparseSwitch smaliPayload = (SmaliPayloadSparseSwitch) instruction;
        int count = getCount();
        for (int i = 0; i < count; i++) {
            smaliPayload.addEntry(get(i).toSmali());
        }
    }

    @Override
    public void appendCode(SmaliWriter writer) throws IOException {
        writer.append('.');
        writer.append(getSmaliDirective().getName());
        int size = getCount();
        writer.indentPlus();
        for(int i = 0; i < size; i++){
            get(i).append(writer);
        }
        writer.indentMinus();
        writer.newLine();
        getSmaliDirective().appendEnd(writer);
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.SPARSE_SWITCH;
    }

    static class EntryKey extends IntegerItem {
        private Ins targetIns;

        EntryKey() {
            super();
        }

        public Ins getTargetIns() {
            return targetIns;
        }
        public void setTargetIns(Ins targetIns) {
            this.targetIns = targetIns;
        }
    }
    public static class SparseSwitchEntry implements InsSwitchPayload.SwitchEntry {

        private final InsSparseSwitchData payload;
        private final IntegerReference element;
        private final EntryKey key;

        public SparseSwitchEntry(InsSparseSwitchData payload, IntegerReference element, EntryKey key){
            this.payload = payload;
            this.element = element;
            this.key = key;
        }

        @Override
        public InsSparseSwitchData getPayload() {
            return payload;
        }
        @Override
        public int get(){
            return element.get();
        }
        @Override
        public void set(int value){
            if(value != element.get()) {
                element.set(value);
                this.payload.mSortRequired = true;
            }
        }
        @Override
        public Ins getTargetIns() {
            Ins targetIns = this.key.getTargetIns();
            if(targetIns == null) {
                setTargetIns(findTargetIns());
                targetIns = this.key.getTargetIns();
            }
            return targetIns;
        }
        @Override
        public void setTargetIns(Ins targetIns) {
            Ins ins = key.getTargetIns();
            if(targetIns != ins) {
                key.setTargetIns(targetIns);
                if(targetIns != null) {
                    targetIns.addExtraLine(this);
                }
            }
        }

        public int getKey(){
            return key.get();
        }
        public void setKey(int value){
            key.set(value);
        }

        @Override
        public int getSortOrder() {
            return ExtraLine.ORDER_INSTRUCTION_LABEL;
        }
        @Override
        public int getAddress() {
            return payload.getAddress();
        }
        @Override
        public int getTargetAddress() {
            return getKey() + payload.getBaseAddress();
        }
        @Override
        public void setTargetAddress(int targetAddress){
            setKey(targetAddress - payload.getBaseAddress());
        }
        @Override
        public String getLabelName() {
            return HexUtil.toHex(":sswitch_", getTargetAddress(), 1);
        }
        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.newLine();
            int value = get();
            writer.appendHex(value);
            writer.append(" -> ");
            writer.appendLabelName(getLabelName());
            writer.appendResourceIdComment(value);
        }

        @Override
        public void appendExtra(SmaliWriter writer) throws IOException {
            writer.appendLabelName(getLabelName());
            writer.appendComment(HexUtil.toSignedHex(get()));
        }

        public void removeSelf() {
            payload.remove(this);
        }
        public void fromPackedSwitch(PackedSwitchDataList.PackedSwitchEntry packedSwitchEntry) {
            this.set(packedSwitchEntry.get());
            Ins ins = packedSwitchEntry.getTargetIns();
            this.setTargetAddress(ins.getAddress());
            this.setTargetIns(ins);
            ins.addExtraLine(this);
        }
        public void merge(SparseSwitchEntry data) {
            set(data.get());
            setKey(data.getKey());
        }
        public void fromSmali(SmaliSparseSwitchEntry smaliEntry) {
            set(smaliEntry.getValue());
            setKey(smaliEntry.getRelativeOffset());
        }
        public SmaliSparseSwitchEntry toSmali() {
            SmaliSparseSwitchEntry entry = new SmaliSparseSwitchEntry();
            entry.getLabel().setLabelName(getLabelName());
            entry.setValue(get());
            return entry;
        }
        @Override
        public int hashCode() {
            return ObjectsUtil.hash(payload, element);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SparseSwitchEntry data = (SparseSwitchEntry) obj;
            return element == data.element && payload == data.payload;
        }
        @Override
        public String toString() {
            return HexUtil.toHex8(get()) + " -> " + getKey();
        }
    }
}