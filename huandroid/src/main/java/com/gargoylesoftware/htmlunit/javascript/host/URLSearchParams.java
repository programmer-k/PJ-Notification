/*
 * Copyright (c) 2002-2018 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit.javascript.host;

import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxFunction;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.NativeArray;
import net.sourceforge.htmlunit.corejs.javascript.ScriptRuntime;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.TopLevel;
import net.sourceforge.htmlunit.corejs.javascript.Undefined;

import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF;

/**
 * A JavaScript object for {@code URLSearchParams}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Ween Jiann
 */
@JsxClass({CHROME, FF})
public class URLSearchParams extends SimpleScriptable {

    private static final String ITERATOR_NAME = "URLSearchParamsIterator";
    private static com.gargoylesoftware.htmlunit.javascript.host.Iterator ITERATOR_PROTOTYPE_;

    private final List<Entry<String, String>> params_ = new LinkedList<>();

    /**
     * Constructs a new instance.
     */
    public URLSearchParams() {
    }

    /**
     * Constructs a new instance.
     * @param params the params string
     */
    @JsxConstructor
    public URLSearchParams(final Object params) {
        // TODO: Pass in a sequence
        // new URLSearchParams([["foo", 1],["bar", 2]]);

        // TODO: Pass in a record
        // new URLSearchParams({"foo" : 1 , "bar" : 2});

        if (Undefined.instance == params || null == params) {
            return;
        }

        splitQuery(Context.toString(params));
    }

    private void splitQuery(String params) {
        params = StringUtils.stripStart(params, "?");
        if (StringUtils.isEmpty(params)) {
            return;
        }

        // TODO: encoding
        final String[] parts = StringUtils.split(params, '&');
        for (int i = 0; i < parts.length; i++) {
            params_.add(splitQueryParameter(parts[i]));
        }
    }

    private Entry<String, String> splitQueryParameter(final String singleParam) {
        final int idx = singleParam.indexOf('=');
        if (idx > -1) {
            final String key = singleParam.substring(0, idx);
            String value = null;
            if (idx < singleParam.length()) {
                value = singleParam.substring(idx + 1);
            }
            return new AbstractMap.SimpleEntry<>(key, value);
        }
        final String key = singleParam;
        final String value = null;
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    /**
     * The append() method of the URLSearchParams interface appends a specified
     * key/value pair as a new search parameter.
     *
     * @param name  The name of the parameter to append.
     * @param value The value of the parameter to append.
     */
    @JsxFunction
    public void append(final String name, final String value) {
        params_.add(new AbstractMap.SimpleEntry<>(name, value));
    }

    /**
     * The delete() method of the URLSearchParams interface deletes the given search
     * parameter and its associated value, from the list of all search parameters.
     *
     * @param name The name of the parameter to be deleted.
     */
    @JsxFunction
    public void delete(final String name) {
        final Iterator<Entry<String, String>> iter = params_.iterator();
        while (iter.hasNext()) {
            final Entry<String, String> entry = iter.next();
            if (entry.getKey().equals(name)) {
                iter.remove();
            }
        }
    }

    /**
     * The get() method of the URLSearchParams interface returns the
     * first value associated to the given search parameter.
     *
     * @param name The name of the parameter to return.
     * @return An array of USVStrings.
     */
    @JsxFunction
    public String get(final String name) {
        for (Entry<String, String> param : params_) {
            if (param.getKey().equals(name)) {
                return param.getValue();
            }
        }
        return null;
    }

    /**
     * The getAll() method of the URLSearchParams interface returns all the values
     * associated with a given search parameter as an array.
     *
     * @param name The name of the parameter to return.
     * @return An array of USVStrings.
     */
    @JsxFunction
    public NativeArray getAll(final String name) {
        final List<String> result = new LinkedList<>();
        for (Entry<String, String> param : params_) {
            if (param.getKey().equals(name)) {
                result.add(param.getValue());
            }
        }

        final NativeArray jsValues = new NativeArray(result.toArray());
        ScriptRuntime.setBuiltinProtoAndParent(jsValues, getWindow(this), TopLevel.Builtins.Array);
        return jsValues;
    }

    /**
     * The set() method of the URLSearchParams interface sets the value associated with a
     * given search parameter to the given value. If there were several matching values,
     * this method deletes the others. If the search parameter doesn't exist, this method
     * creates it.
     *
     * @param name  The name of the parameter to set.
     * @param value The value of the parameter to set.
     */
    @JsxFunction
    public void set(final String name, final String value) {
        final Iterator<Entry<String, String>> iter = params_.iterator();
        boolean change = true;
        while (iter.hasNext()) {
            final Entry<String, String> entry = iter.next();
            if (entry.getKey().equals(name)) {
                if (change) {
                    entry.setValue(value);
                    change = false;
                }
                else {
                    iter.remove();
                }
            }
        }

        if (change) {
            append(name, value);
        }
    }

    /**
     * The has() method of the URLSearchParams interface returns a Boolean that
     * indicates whether a parameter with the specified name exists.
     *
     * @param name The name of the parameter to find.
     * @return A Boolean.
     */
    @JsxFunction
    public boolean has(final String name) {
        for (Entry<String, String> param : params_) {
            if (param.getKey().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The URLSearchParams.entries() method returns an iterator allowing to go through
     * all key/value pairs contained in this object. The key and value of each pair
     * are USVString objects.
     *
     * @return an iterator.
     */
    @JsxFunction
    public Object entries() {
        final SimpleScriptable object =
                new com.gargoylesoftware.htmlunit.javascript.host.Iterator(ITERATOR_NAME, params_.iterator());
        object.setParentScope(getParentScope());
        setIteratorPrototype(object);
        return object;
    }

    /**
     * The URLSearchParams.keys() method returns an iterator allowing to go through
     * all keys contained in this object. The keys are USVString objects.
     *
     * @return an iterator.
     */
    @JsxFunction
    public Object keys() {
        final List<String> keys = new ArrayList<>(params_.size());
        for (Entry<String, String> entry : params_) {
            keys.add(entry.getKey());
        }

        final SimpleScriptable object =
                new com.gargoylesoftware.htmlunit.javascript.host.Iterator(ITERATOR_NAME, keys.iterator());
        object.setParentScope(getParentScope());
        setIteratorPrototype(object);
        return object;
    }

    /**
     * The URLSearchParams.values() method returns an iterator allowing to go through
     * all values contained in this object. The values are USVString objects.
     *
     * @return an iterator.
     */
    @JsxFunction
    public Object values() {
        final List<String> values = new ArrayList<>(params_.size());
        for (Entry<String, String> entry : params_) {
            values.add(entry.getValue());
        }

        final SimpleScriptable object =
                new com.gargoylesoftware.htmlunit.javascript.host.Iterator(ITERATOR_NAME, values.iterator());
        object.setParentScope(getParentScope());
        setIteratorPrototype(object);
        return object;
    }

    private static void setIteratorPrototype(final Scriptable scriptable) {
        if (ITERATOR_PROTOTYPE_ == null) {
            ITERATOR_PROTOTYPE_ = new com.gargoylesoftware.htmlunit.javascript.host.Iterator(ITERATOR_NAME, null);
        }
        scriptable.setPrototype(ITERATOR_PROTOTYPE_);
    }

    /**
     * Calls for instance for implicit conversion to string.
     * @see com.gargoylesoftware.htmlunit.javascript.SimpleScriptable#getDefaultValue(Class)
     * @param hint the type hint
     * @return the default value
     */
    @Override
    public Object getDefaultValue(final Class<?> hint) {
        final StringBuilder paramStr = new StringBuilder();
        String delim = "";
        for (Entry<String, String> param : params_) {
            paramStr.append(delim);
            delim = "&";
            paramStr.append(param.getKey());
            paramStr.append("=");
            // TODO: need to encode value
            final String value = param.getValue();
            if (value != null) {
                paramStr.append(param.getValue());
            }
        }
        return paramStr.toString();
    }
}
