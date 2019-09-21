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

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.Undefined;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Contains some missing features of Rhino NativeArray.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
public final class ArrayCustom {

    private ArrayCustom() { }

    /**
     * Creates a new Array instance from an array-like or iterable object.
     * @param context the JavaScript context
     * @param thisObj the scriptable
     * @param args the arguments passed into the method
     * @param function the function
     * @return converted string
     */
    public static Scriptable from(
            final Context context, final Scriptable thisObj, final Object[] args, final Function function) {
        final Object arrayLike = args[0];
        Object[] array = null;
        if (arrayLike instanceof Scriptable) {
            final Scriptable scriptable = (Scriptable) arrayLike;
            final Object length = scriptable.get("length", scriptable);
            if (length != Scriptable.NOT_FOUND) {
                final int size = (int) Context.toNumber(length);
                array = new Object[size];
                for (int i = 0; i < size; i++) {
                    array[i] = scriptable.get(i, scriptable);
                }
            }
            else {
                final List<Object> list = new ArrayList<>();
                if (Iterator.iterate(context, thisObj, scriptable,
                        new Consumer<Object>() {
                            @Override
                            public void accept(Object value) {
                                if (value != Undefined.instance) {
                                    list.add(value);
                                }
                            }
                        })) {
                    array = list.toArray();
                }
            }
        }
        else if (arrayLike instanceof String) {
            final String string = (String) arrayLike;
            array = new Object[string.length()];
            for (int i = 0; i < array.length; i++) {
                array[i] = string.charAt(i);
            }
        }
        if (array == null) {
            array = new Object[0];
        }
        return context.newArray(thisObj, array);
    }
}
