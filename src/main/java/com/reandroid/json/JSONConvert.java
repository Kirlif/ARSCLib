/*
 * Copyright (c) 2002 JSON.org (now "Public Domain")
 * This is NOT property of REAndroid
 * This package is renamed from org.json.* to avoid class conflict when used on android platforms
*/
package com.reandroid.json;

public interface JSONConvert<T extends JSONItem> {
    public T toJson();
    public void fromJson(T json);
}
