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
package com.gargoylesoftware.htmlunit.javascript.host.html;

import com.gargoylesoftware.htmlunit.html.HtmlMenu;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxGetter;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxSetter;

import net.sourceforge.htmlunit.corejs.javascript.Context;

import org.apache.commons.lang3.StringUtils;

import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_MENU_TYPE_EMPTY;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.IE;

/**
 * The JavaScript object {@code HTMLMenuElement}.
 *
 * @author Ahmed Ashour
 * @author Frank Danek
 * @author Ronald Brill
 */
@JsxClass(domClass = HtmlMenu.class)
public class HTMLMenuElement extends HTMLListElement {

    private String label_;

    /**
     * Creates an instance.
     */
    @JsxConstructor({CHROME, FF, EDGE})
    public HTMLMenuElement() {
        label_ = "";
    }

    /**
     * Returns the value of the {@code type} property.
     * @return the value of the {@code type} property
     */
    @Override
    @JsxGetter({FF, IE})
    public String getType() {
        if (getBrowserVersion().hasFeature(JS_MENU_TYPE_EMPTY)) {
            return "";
        }

        final String type = getDomNodeOrDie().getAttributeDirect("type");
        if ("context".equalsIgnoreCase(type)) {
            return "context";
        }
        if ("toolbar".equalsIgnoreCase(type)) {
            return "toolbar";
        }
        return "list";
    }

    /**
     * Sets the value of the {@code type} property.
     * @param type the value of the {@code type} property
     */
    @Override
    @JsxSetter({FF, IE})
    public void setType(final String type) {
        if (getBrowserVersion().hasFeature(JS_MENU_TYPE_EMPTY)) {
            if (StringUtils.isEmpty(type)) {
                return;
            }
            throw Context.reportRuntimeError("Cannot set the type property to invalid value: '" + type + "'");
        }

        if ("context".equalsIgnoreCase(type)) {
            getDomNodeOrDie().setAttribute("type", "context");
            return;
        }
        if ("toolbar".equalsIgnoreCase(type)) {
            getDomNodeOrDie().setAttribute("type", "toolbar");
            return;
        }

        getDomNodeOrDie().setAttribute("type", "list");
    }

    /**
     * Returns the value of the {@code label} property.
     * @return the value of the {@code label} property
     */
    @JsxGetter(FF)
    public String getLabel() {
        return label_;
    }

    /**
     * Sets the value of the {@code label} property.
     * @param label the value of the {@code label} property
     */
    @JsxSetter(FF)
    public void setLabel(final String label) {
        label_ = label;
    }
}
