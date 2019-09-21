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
package com.gargoylesoftware.htmlunit.javascript.host.crypto;

import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxFunction;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxGetter;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.gargoylesoftware.htmlunit.javascript.host.arrays.ArrayBufferViewBase;

import net.sourceforge.htmlunit.corejs.javascript.Context;

import java.util.Random;

import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF;

/**
 * A JavaScript object for {@code Crypto}.
 *
 * @author Ahmed Ashour
 * @author Marc Guillemot
 * @author Ronald Brill
 */
@JsxClass
public class Crypto extends SimpleScriptable {

    /**
     * Creates an instance.
     */
    @JsxConstructor({CHROME, FF, EDGE})
    public Crypto() {
    }

    /**
     * Facility constructor.
     * @param window the owning window
     */
    public Crypto(final Window window) {
        setParentScope(window);
        setPrototype(window.getPrototype(Crypto.class));
    }

    /**
     * Fills array with random values.
     * @param array the array to fill
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/RandomSource/getRandomValues">MDN Doc</a>
     */
    @JsxFunction
    public void getRandomValues(final ArrayBufferViewBase array) {
        if (array == null) {
            throw Context.reportRuntimeError("TypeError: Argument 1 of Crypto.getRandomValues is not an object.");
        }

        final Random random = new Random();
        for (int i = 0; i < array.getLength(); i++) {
            array.put(i, array, random.nextInt());
        }
    }

    /**
     * Returns the {@code subtle} property.
     * @return the {@code stuble} property
     */
    @JsxGetter({CHROME, FF})
    public SubtleCrypto getSubtle() {
        final SubtleCrypto stuble = new SubtleCrypto();
        final Window window = getWindow();
        stuble.setParentScope(window);
        stuble.setPrototype(window.getPrototype(SubtleCrypto.class));
        return stuble;
    }
}
