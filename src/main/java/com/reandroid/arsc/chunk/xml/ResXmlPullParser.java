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
package com.reandroid.arsc.chunk.xml;

import android.content.res.XmlResourceParser;
import com.reandroid.arsc.decoder.Decoder;
import com.reandroid.arsc.value.ValueType;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ResXmlPullParser implements XmlResourceParser {
    private Decoder mDecoder;
    private final ParserEventList mEventList = new ParserEventList();
    private ResXmlDocument mDocument;
    private boolean mDocumentCreatedHere;


    public ResXmlPullParser(){
    }

    public void setResXmlDocument(ResXmlDocument xmlDocument){
        closeDocument();
        this.mDocument = xmlDocument;
        initializeDecoder(xmlDocument);
        xmlDocument.addEvents(mEventList);
    }
    public ResXmlDocument getResXmlDocument() {
        return mDocument;
    }

    public void setDecoder(Decoder decoder) {
        this.mDecoder = decoder;
    }
    public Decoder getDecoder(){
        return mDecoder;
    }
    private void initializeDecoder(ResXmlDocument xmlDocument){
        if(mDecoder!=null){
            return;
        }
        mDecoder = Decoder.create(xmlDocument);
    }

    public void closeDocument(){
        mEventList.clear();
        destroyDocument();
    }
    private void destroyDocument(){
        if(!mDocumentCreatedHere){
            return;
        }
        mDocumentCreatedHere = false;
        if(this.mDocument == null){
            return;
        }
        this.mDocument.destroy();
        this.mDocument = null;
    }

    @Override
    public void close(){
        closeDocument();
    }
    @Override
    public int getAttributeCount() {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.getAttributeCount();
        }
        return 0;
    }
    @Override
    public String getAttributeName(int index) {
        return decodeAttributeName(getResXmlAttributeAt(index));
    }
    @Override
    public String getAttributeValue(int index) {
        return decodeAttributeValue(getResXmlAttributeAt(index));
    }
    @Override
    public String getAttributeValue(String namespace, String name) {
        return decodeAttributeValue(getAttribute(namespace, name));
    }
    @Override
    public String getPositionDescription() {
        return null;
    }
    @Override
    public int getAttributeNameResource(int index) {
        ResXmlAttribute attribute = getResXmlAttributeAt(index);
        if(attribute!=null){
            return attribute.getNameResourceID();
        }
        return 0;
    }
    @Override
    public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null){
            return 0;
        }
        List<String> list = Arrays.asList(options);
        int index = list.indexOf(decodeAttributeValue(xmlAttribute));
        if(index==-1){
            return defaultValue;
        }
        return index;
    }
    @Override
    public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null || xmlAttribute.getValueType() != ValueType.INT_BOOLEAN){
            return defaultValue;
        }
        return xmlAttribute.getValueAsBoolean();
    }
    @Override
    public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.ATTRIBUTE
                ||valueType==ValueType.REFERENCE
                ||valueType==ValueType.DYNAMIC_ATTRIBUTE
                ||valueType==ValueType.DYNAMIC_REFERENCE){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.INT_DEC
                ||valueType==ValueType.INT_HEX){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.INT_DEC){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.FLOAT){
            return Float.intBitsToFloat(xmlAttribute.getData());
        }
        return defaultValue;
    }

    @Override
    public int getAttributeListValue(int index, String[] options, int defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute == null){
            return 0;
        }
        List<String> list = Arrays.asList(options);
        int i = list.indexOf(decodeAttributeValue(xmlAttribute));
        if(i==-1){
            return defaultValue;
        }
        return index;
    }
    @Override
    public boolean getAttributeBooleanValue(int index, boolean defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute == null || xmlAttribute.getValueType() != ValueType.INT_BOOLEAN){
            return defaultValue;
        }
        return xmlAttribute.getValueAsBoolean();
    }
    @Override
    public int getAttributeResourceValue(int index, int defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.ATTRIBUTE
                ||valueType==ValueType.REFERENCE
                ||valueType==ValueType.DYNAMIC_ATTRIBUTE
                ||valueType==ValueType.DYNAMIC_REFERENCE){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public int getAttributeIntValue(int index, int defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute == null){
            return defaultValue;
        }
        return xmlAttribute.getData();
    }
    @Override
    public int getAttributeUnsignedIntValue(int index, int defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute == null){
            return 0;
        }
        return xmlAttribute.getData();
    }
    @Override
    public float getAttributeFloatValue(int index, float defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute == null){
            return 0;
        }
        ValueType valueType=xmlAttribute.getValueType();
        if(valueType==ValueType.FLOAT){
            return Float.intBitsToFloat(xmlAttribute.getData());
        }
        return defaultValue;
    }

    @Override
    public String getIdAttribute() {
        ResXmlStartElement startElement = getResXmlStartElement();
        if(startElement!=null){
            ResXmlAttribute attribute = startElement.getIdAttribute();
            if(attribute!=null){
                return attribute.getName();
            }
        }
        return null;
    }
    @Override
    public String getClassAttribute() {
        ResXmlStartElement startElement = getResXmlStartElement();
        if(startElement!=null){
            ResXmlAttribute attribute = startElement.getClassAttribute();
            if(attribute!=null){
                return attribute.getName();
            }
        }
        return null;
    }
    @Override
    public int getIdAttributeResourceValue(int defaultValue) {
        ResXmlStartElement startElement = getResXmlStartElement();
        if(startElement!=null){
            ResXmlAttribute attribute = startElement.getIdAttribute();
            if(attribute!=null){
                return attribute.getNameResourceID();
            }
        }
        return 0;
    }
    @Override
    public int getStyleAttribute() {
        ResXmlStartElement startElement = getResXmlStartElement();
        if(startElement!=null){
            ResXmlAttribute attribute = startElement.getStyleAttribute();
            if(attribute!=null){
                return attribute.getNameResourceID();
            }
        }
        return 0;
    }

    @Override
    public void setFeature(String name, boolean state) throws XmlPullParserException {
    }
    @Override
    public boolean getFeature(String name) {
        return false;
    }
    @Override
    public void setProperty(String name, Object value) throws XmlPullParserException {
    }
    @Override
    public Object getProperty(String name) {
        return null;
    }
    @Override
    public void setInput(Reader in) throws XmlPullParserException {
        InputStream inputStream = getFromLock(in);
        if(inputStream == null){
            throw new XmlPullParserException("Can't parse binary xml from reader");
        }
        setInput(inputStream, null);
    }
    @Override
    public void setInput(InputStream inputStream, String inputEncoding) throws XmlPullParserException {
        ResXmlDocument xmlDocument = new ResXmlDocument();
        try {
            xmlDocument.readBytes(inputStream);
        } catch (IOException exception) {
            XmlPullParserException pullParserException = new XmlPullParserException(exception.getMessage());
            pullParserException.initCause(exception);
            throw pullParserException;
        }
        setResXmlDocument(xmlDocument);
        this.mDocumentCreatedHere = true;
    }
    @Override
    public String getInputEncoding() {
        // Not applicable but let not return null
        return "UTF-8";
    }
    @Override
    public void defineEntityReplacementText(String entityName, String replacementText) throws XmlPullParserException {
    }
    @Override
    public int getNamespaceCount(int depth) throws XmlPullParserException {
        ResXmlElement element = getCurrentElement();
        while(element!=null && element.getDepth()>depth){
            element=element.getParentResXmlElement();
        }
        if(element!=null){
            return element.getStartNamespaceList().size();
        }
        return 0;
    }
    @Override
    public String getNamespacePrefix(int pos) throws XmlPullParserException {
        ResXmlAttribute attribute = getResXmlAttributeAt(pos);
        if(attribute!=null){
            return attribute.getNamePrefix();
        }
        return null;
    }
    @Override
    public String getNamespaceUri(int pos) throws XmlPullParserException {
        ResXmlAttribute attribute = getResXmlAttributeAt(pos);
        if(attribute!=null){
            return attribute.getUri();
        }
        return null;
    }
    @Override
    public String getNamespace(String prefix) {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            ResXmlStartNamespace startNamespace = element.getStartNamespaceByPrefix(prefix);
            if(startNamespace!=null){
                return startNamespace.getUri();
            }
        }
        return null;
    }
    @Override
    public int getDepth() {
        int event = mEventList.getType();
        if(event == START_TAG || event == END_TAG || event == TEXT){
            return mEventList.getXmlNode().getDepth();
        }
        return 0;
    }
    @Override
    public int getLineNumber() {
        return mEventList.getLineNumber();
    }
    @Override
    public int getColumnNumber() {
        return 0;
    }
    @Override
    public boolean isWhitespace() throws XmlPullParserException {
        String text = getText();
        if(text == null){
            return true;
        }
        text = text.trim();
        return text.length() == 0;
    }
    @Override
    public String getText() {
        return mEventList.getText();
    }
    @Override
    public char[] getTextCharacters(int[] holderForStartAndLength) {
        String text = getText();
        if (text == null) {
            holderForStartAndLength[0] = -1;
            holderForStartAndLength[1] = -1;
            return null;
        }
        char[] result = text.toCharArray();
        holderForStartAndLength[0] = 0;
        holderForStartAndLength[1] = result.length;
        return result;
    }
    @Override
    public String getNamespace() {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.getTagUri();
        }
        return null;
    }
    @Override
    public String getName() {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.getTag();
        }
        return null;
    }
    @Override
    public String getPrefix() {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.getTagPrefix();
        }
        return null;
    }
    @Override
    public boolean isEmptyElementTag() throws XmlPullParserException {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.countResXmlNodes() == 0 && element.getAttributeCount()==0;
        }
        return false;
    }
    @Override
    public String getAttributeNamespace(int index) {
        ResXmlAttribute attribute = getResXmlAttributeAt(index);
        if(attribute != null){
            return attribute.getUri();
        }
        return null;
    }
    @Override
    public String getAttributePrefix(int index) {
        ResXmlAttribute attribute = getResXmlAttributeAt(index);
        if(attribute != null){
            return attribute.getNamePrefix();
        }
        return null;
    }
    @Override
    public String getAttributeType(int index) {
        return "CDATA";
    }
    @Override
    public boolean isAttributeDefault(int index) {
        return false;
    }
    private String decodeAttributeName(ResXmlAttribute attribute){
        if(attribute==null){
            return null;
        }
        String name;
        int resourceId = attribute.getNameResourceID();
        if(resourceId == 0 || mDecoder==null){
            name = attribute.getName();
        }else {
            name = mDecoder.decodeResourceName(attribute.getNameResourceID(), true);
        }
        return name;
    }
    private String decodeAttributeValue(ResXmlAttribute attribute){
        if(attribute==null){
            return null;
        }
        return mDecoder.decodeAttributeValue(attribute);
    }
    public ResXmlAttribute getResXmlAttributeAt(int index){
        ResXmlElement element = getCurrentElement();
        if(element == null){
            return null;
        }
        return element.getAttributeAt(index);
    }
    public ResXmlAttribute getAttribute(String namespace, String name) {
        ResXmlElement element = getCurrentElement();
        if(element == null){
            return null;
        }
        for(ResXmlAttribute attribute:element.listAttributes()){
            if(Objects.equals(namespace, attribute.getUri())
                    && Objects.equals(name, attribute.getName())){
                return attribute;
            }
        }
        return null;
    }
    private ResXmlStartElement getResXmlStartElement(){
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.getStartElement();
        }
        return null;
    }
    public ResXmlElement getCurrentElement() {
        int type = mEventList.getType();
        if(type==START_TAG||type==END_TAG){
            return mEventList.getElement();
        }
        return null;
    }
    @Override
    public int getEventType() throws XmlPullParserException {
        return mEventList.getType();
    }
    @Override
    public int next() throws XmlPullParserException, IOException {
        mEventList.next();
        return mEventList.getType();
    }
    @Override
    public int nextToken() throws XmlPullParserException, IOException {
        return next();
    }
    @Override
    public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {
        if (type != this.getEventType()
                || (namespace != null && !namespace.equals(getNamespace()))
                || (name != null && !name.equals(getName()))) {
            throw new XmlPullParserException(
                    "expected: " + TYPES[type] + " {" + namespace + "}" + name, this, null);
        }
    }
    @Override
    public String nextText() throws XmlPullParserException, IOException {
        int event = getEventType();
        if (event != START_TAG) {
            throw new XmlPullParserException("precondition: START_TAG", this, null);
        }
        while (event!=TEXT && event!=END_TAG && event!=END_DOCUMENT){
            event=next();
        }
        if(event==TEXT){
            return getText();
        }
        return "";
    }
    @Override
    public int nextTag() throws XmlPullParserException, IOException {
        int event = getEventType();
        if (event != START_TAG) {
            throw new XmlPullParserException("precondition: START_TAG", this, null);
        }
        event = next();
        while (event!=START_TAG && event!=END_DOCUMENT){
            event=next();
        }
        return event;
    }

    private static InputStream getFromLock(Reader reader){
        try{
            Field field = Reader.class.getDeclaredField("lock");
            field.setAccessible(true);
            Object obj = field.get(reader);
            if(obj instanceof InputStream){
                return (InputStream) obj;
            }
        }catch (Throwable ignored){
        }
        return null;
    }

}