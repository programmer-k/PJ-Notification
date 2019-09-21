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

import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.BaseFrameElement;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlAttributeChangeEvent;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlInlineFrame;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlScript;
import com.gargoylesoftware.htmlunit.httpclient.HtmlUnitBrowserCompatCookieSpec;
import com.gargoylesoftware.htmlunit.javascript.PostponedAction;
import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;
import com.gargoylesoftware.htmlunit.javascript.configuration.CanSetReadOnly;
import com.gargoylesoftware.htmlunit.javascript.configuration.CanSetReadOnlyStatus;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxFunction;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxGetter;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxSetter;
import com.gargoylesoftware.htmlunit.javascript.host.Element;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.gargoylesoftware.htmlunit.javascript.host.dom.Attr;
import com.gargoylesoftware.htmlunit.javascript.host.dom.Document;
import com.gargoylesoftware.htmlunit.javascript.host.dom.Selection;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;
import com.gargoylesoftware.htmlunit.util.Cookie;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.HTMLDOCUMENT_COLOR;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.HTMLDOCUMENT_FUNCTION_DETACHED;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.HTMLDOCUMENT_GET_ALSO_FRAMES;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.HTMLDOCUMENT_GET_FOR_ID_AND_OR_NAME;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.HTMLDOCUMENT_GET_PREFERS_STANDARD_FUNCTIONS;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.HTML_COLOR_EXPAND_ZERO;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_DOCUMENT_CREATE_ATTRUBUTE_LOWER_CASE;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.IE;

/**
 * A JavaScript object for {@code HTMLDocument}.
 *
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author David K. Taylor
 * @author <a href="mailto:chen_jun@users.sourceforge.net">Chen Jun</a>
 * @author <a href="mailto:cse@dynabean.de">Christian Sell</a>
 * @author Chris Erskine
 * @author Marc Guillemot
 * @author Daniel Gredler
 * @author Michael Ottati
 * @author <a href="mailto:george@murnock.com">George Murnock</a>
 * @author Ahmed Ashour
 * @author Rob Di Marco
 * @author Sudhan Moghe
 * @author <a href="mailto:mike@10gen.com">Mike Dirolf</a>
 * @author Ronald Brill
 * @author Frank Danek
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms535862.aspx">MSDN documentation</a>
 * @see <a href="http://www.w3.org/TR/2000/WD-DOM-Level-1-20000929/level-one-html.html#ID-7068919">
 * W3C DOM Level 1</a>
 */
@JsxClass
public class HTMLDocument extends Document {

    private static final Log LOG = LogFactory.getLog(HTMLDocument.class);

    private enum ParsingStatus { OUTSIDE, START, IN_NAME, INSIDE, IN_STRING }

    private HTMLElement activeElement_;

    /** The buffer that will be used for calls to document.write(). */
    private final StringBuilder writeBuilder_ = new StringBuilder();
    private boolean writeInCurrentDocument_ = true;

    private boolean closePostponedAction_;

    /**
     * The constructor.
     */
    @JsxConstructor({CHROME, FF, EDGE})
    public HTMLDocument() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DomNode getDomNodeOrDie() {
        try {
            return super.getDomNodeOrDie();
        }
        catch (final IllegalStateException e) {
            throw Context.reportRuntimeError("No node attached to this object");
        }
    }

    /**
     * Returns the HTML page that this document is modeling.
     * @return the HTML page that this document is modeling
     */
    @Override
    public HtmlPage getPage() {
        return (HtmlPage) getDomNodeOrDie();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter(FF)
    public Object getForms() {
        return super.getForms();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter(FF)
    public Object getEmbeds() {
        return super.getEmbeds();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter(FF)
    public Object getPlugins() {
        return super.getPlugins();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter(FF)
    public Object getLinks() {
        return super.getLinks();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter(FF)
    public Object getAnchors() {
        return super.getAnchors();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter(FF)
    public Object getApplets() {
        return super.getApplets();
    }

    /**
     * JavaScript function "write" may accept a variable number of arguments.
     * @param context the JavaScript context
     * @param thisObj the scriptable
     * @param args the arguments passed into the method
     * @param function the function
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms536782.aspx">MSDN documentation</a>
     */
    @JsxFunction
    public static void write(final Context context, final Scriptable thisObj, final Object[] args,
        final Function function) {
        final HTMLDocument thisAsDocument = getDocument(thisObj);
        thisAsDocument.write(concatArgsAsString(args));
    }

    /**
     * Converts the arguments to strings and concatenate them.
     * @param args the JavaScript arguments
     * @return the string concatenation
     */
    private static String concatArgsAsString(final Object[] args) {
        final StringBuilder builder = new StringBuilder();
        for (final Object arg : args) {
            builder.append(Context.toString(arg));
        }
        return builder.toString();
    }

    /**
     * JavaScript function "writeln" may accept a variable number of arguments.
     * @param context the JavaScript context
     * @param thisObj the scriptable
     * @param args the arguments passed into the method
     * @param function the function
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms536783.aspx">MSDN documentation</a>
     */
    @JsxFunction
    public static void writeln(
        final Context context, final Scriptable thisObj, final Object[] args, final Function function) {
        final HTMLDocument thisAsDocument = getDocument(thisObj);
        thisAsDocument.write(concatArgsAsString(args) + "\n");
    }

    /**
     * Returns the current document instance, using <tt>thisObj</tt> as a hint.
     * @param thisObj a hint as to the current document (may be the prototype when function is used without "this")
     * @return the current document instance
     */
    private static HTMLDocument getDocument(final Scriptable thisObj) {
        // if function is used "detached", then thisObj is the top scope (ie Window), not the real object
        // cf unit test DocumentTest#testDocumentWrite_AssignedToVar
        // may be the prototype too
        // cf DocumentTest#testDocumentWrite_AssignedToVar2
        if (thisObj instanceof HTMLDocument && thisObj.getPrototype() instanceof HTMLDocument) {
            return (HTMLDocument) thisObj;
        }
        if (thisObj instanceof DocumentProxy && thisObj.getPrototype() instanceof HTMLDocument) {
            return (HTMLDocument) ((DocumentProxy) thisObj).getDelegee();
        }

        final Window window = getWindow(thisObj);
        if (window.getBrowserVersion().hasFeature(HTMLDOCUMENT_FUNCTION_DETACHED)) {
            return (HTMLDocument) window.getDocument();
        }
        throw Context.reportRuntimeError("Function can't be used detached from document");
    }

    private boolean executionExternalPostponed_;

    /**
     * This a hack!!! A cleaner way is welcome.
     * Handle a case where document.write is simply ignored.
     * See HTMLDocumentWrite2Test.write_fromScriptAddedWithAppendChild_external.
     * @param executing indicates if executing or not
     */
    public void setExecutingDynamicExternalPosponed(final boolean executing) {
        executionExternalPostponed_ = executing;
    }

    /**
     * JavaScript function "write".
     *
     * See http://www.whatwg.org/specs/web-apps/current-work/multipage/section-dynamic.html for
     * a good description of the semantics of open(), write(), writeln() and close().
     *
     * @param content the content to write
     */
    protected void write(final String content) {
        // really strange: if called from an external script loaded as postponed action, write is ignored!!!
        if (executionExternalPostponed_) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("skipping write for external posponed: " + content);
            }
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("write: " + content);
        }

        final HtmlPage page = (HtmlPage) getDomNodeOrDie();
        if (!page.isBeingParsed()) {
            writeInCurrentDocument_ = false;
        }

        // Add content to the content buffer.
        writeBuilder_.append(content);

        // If open() was called; don't write to doc yet -- wait for call to close().
        if (!writeInCurrentDocument_) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("wrote content to buffer");
            }
            scheduleImplicitClose();
            return;
        }
        final String bufferedContent = writeBuilder_.toString();
        if (!canAlreadyBeParsed(bufferedContent)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("write: not enough content to parse it now");
            }
            return;
        }

        writeBuilder_.setLength(0);
        page.writeInParsedStream(bufferedContent);
    }

    private void scheduleImplicitClose() {
        if (!closePostponedAction_) {
            closePostponedAction_ = true;
            final HtmlPage page = (HtmlPage) getDomNodeOrDie();
            final WebWindow enclosingWindow = page.getEnclosingWindow();
            page.getWebClient().getJavaScriptEngine().addPostponedAction(new PostponedAction(page) {
                @Override
                public void execute() throws Exception {
                    if (writeBuilder_.length() != 0) {
                        close();
                    }
                    closePostponedAction_ = false;
                }

                @Override
                public boolean isStillAlive() {
                    return !enclosingWindow.isClosed();
                }
            });
        }
    }

    /**
     * Indicates if the content is a well formed HTML snippet that can already be parsed to be added to the DOM.
     *
     * @param content the HTML snippet
     * @return {@code false} if it not well formed
     */
    static boolean canAlreadyBeParsed(final String content) {
        // all <script> must have their </script> because the parser doesn't close automatically this tag
        // All tags must be complete, that is from '<' to '>'.
        ParsingStatus tagState = ParsingStatus.OUTSIDE;
        int tagNameBeginIndex = 0;
        int scriptTagCount = 0;
        boolean tagIsOpen = true;
        char stringBoundary = 0;
        boolean stringSkipNextChar = false;
        int index = 0;
        char openingQuote = 0;
        for (final char currentChar : content.toCharArray()) {
            switch (tagState) {
                case OUTSIDE:
                    if (currentChar == '<') {
                        tagState = ParsingStatus.START;
                        tagIsOpen = true;
                    }
                    else if (scriptTagCount > 0 && (currentChar == '\'' || currentChar == '"')) {
                        tagState = ParsingStatus.IN_STRING;
                        stringBoundary = currentChar;
                        stringSkipNextChar = false;
                    }
                    break;
                case START:
                    if (currentChar == '/') {
                        tagIsOpen = false;
                        tagNameBeginIndex = index + 1;
                    }
                    else {
                        tagNameBeginIndex = index;
                    }
                    tagState = ParsingStatus.IN_NAME;
                    break;
                case IN_NAME:
                    if (Character.isWhitespace(currentChar) || currentChar == '>') {
                        final String tagName = content.substring(tagNameBeginIndex, index);
                        if ("script".equalsIgnoreCase(tagName)) {
                            if (tagIsOpen) {
                                scriptTagCount++;
                            }
                            else if (scriptTagCount > 0) {
                                // Ignore extra close tags for now. Let the parser deal with them.
                                scriptTagCount--;
                            }
                        }
                        if (currentChar == '>') {
                            tagState = ParsingStatus.OUTSIDE;
                        }
                        else {
                            tagState = ParsingStatus.INSIDE;
                        }
                    }
                    else if (!Character.isLetter(currentChar)) {
                        tagState = ParsingStatus.OUTSIDE;
                    }
                    break;
                case INSIDE:
                    if (currentChar == openingQuote) {
                        openingQuote = 0;
                    }
                    else if (openingQuote == 0) {
                        if (currentChar == '\'' || currentChar == '"') {
                            openingQuote = currentChar;
                        }
                        else if (currentChar == '>' && openingQuote == 0) {
                            tagState = ParsingStatus.OUTSIDE;
                        }
                    }
                    break;
                case IN_STRING:
                    if (stringSkipNextChar) {
                        stringSkipNextChar = false;
                    }
                    else {
                        if (currentChar == stringBoundary) {
                            tagState = ParsingStatus.OUTSIDE;
                        }
                        else if (currentChar == '\\') {
                            stringSkipNextChar = true;
                        }
                    }
                    break;
                default:
                    // nothing
            }
            index++;
        }
        if (scriptTagCount > 0 || tagState != ParsingStatus.OUTSIDE) {
            if (LOG.isDebugEnabled()) {
                final StringBuilder message = new StringBuilder();
                message.append("canAlreadyBeParsed() retruns false for content: '");
                message.append(StringUtils.abbreviateMiddle(content, ".", 100));
                message.append("' (scriptTagCount: " + scriptTagCount);
                message.append(" tagState: " + tagState);
                message.append(")");
                LOG.debug(message.toString());
            }
            return false;
        }

        return true;
    }

    /**
     * Gets the node that is the last one when exploring following nodes, depth-first.
     * @param node the node to search
     * @return the searched node
     */
    HtmlElement getLastHtmlElement(final HtmlElement node) {
        final DomNode lastChild = node.getLastChild();
        if (lastChild == null
                || !(lastChild instanceof HtmlElement)
                || lastChild instanceof HtmlScript) {
            return node;
        }

        return getLastHtmlElement((HtmlElement) lastChild);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter
    public String getCookie() {
        final HtmlPage page = getPage();

        final URL url = page.getUrl();

        final StringBuilder builder = new StringBuilder();
        final Set<Cookie> cookies = page.getWebClient().getCookies(url);
        for (final Cookie cookie : cookies) {
            if (cookie.isHttpOnly()) {
                continue;
            }
            if (builder.length() != 0) {
                builder.append("; ");
            }
            if (!HtmlUnitBrowserCompatCookieSpec.EMPTY_COOKIE_NAME.equals(cookie.getName())) {
                builder.append(cookie.getName());
                builder.append("=");
            }
            builder.append(cookie.getValue());
        }

        return builder.toString();
    }

    /**
     * Adds a cookie, as long as cookies are enabled.
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms533693.aspx">MSDN documentation</a>
     * @param newCookie in the format "name=value[;expires=date][;domain=domainname][;path=path][;secure]
     */
    @JsxSetter
    public void setCookie(final String newCookie) {
        final HtmlPage page = getPage();
        final WebClient client = page.getWebClient();

        client.addCookie(newCookie, getPage().getUrl(), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter(FF)
    public Object getImages() {
        return super.getImages();
    }

    /**
     * Returns the value of the {@code all} property.
     * @return the value of the {@code all} property
     */
    @JsxGetter
    public HTMLCollection getAll() {
        return new HTMLAllCollection(getDomNodeOrDie()) {
            @Override
            protected boolean isMatching(final DomNode node) {
                return true;
            }

            @Override
            public boolean avoidObjectDetection() {
                return true;
            }
        };
    }

    /**
     * JavaScript function "open".
     *
     * See http://www.whatwg.org/specs/web-apps/current-work/multipage/section-dynamic.html for
     * a good description of the semantics of open(), write(), writeln() and close().
     *
     * @param url when a new document is opened, <i>url</i> is a String that specifies a MIME type for the document.
     *        When a new window is opened, <i>url</i> is a String that specifies the URL to render in the new window
     * @param name the name
     * @param features the features
     * @param replace whether to replace in the history list or no
     * @return a reference to the new document object.
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms536652.aspx">MSDN documentation</a>
     */
    @JsxFunction
    public Object open(final Object url, final Object name, final Object features,
            final Object replace) {
        // Any open() invocations are ignored during the parsing stage, because write() and
        // writeln() invocations will directly append content to the current insertion point.
        final HtmlPage page = getPage();
        if (page.isBeingParsed()) {
            LOG.warn("Ignoring call to open() during the parsing stage.");
            return null;
        }

        // We're not in the parsing stage; OK to continue.
        if (!writeInCurrentDocument_) {
            LOG.warn("Function open() called when document is already open.");
        }
        writeInCurrentDocument_ = false;
        final WebWindow ww = getWindow().getWebWindow();
        if (ww instanceof FrameWindow
                && WebClient.ABOUT_BLANK.equals(getPage().getUrl().toExternalForm())) {
            final URL enclosingUrl = ((FrameWindow) ww).getEnclosingPage().getUrl();
            getPage().getWebResponse().getWebRequest().setUrl(enclosingUrl);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxFunction(FF)
    public void close() throws IOException {
        if (writeInCurrentDocument_) {
            LOG.warn("close() called when document is not open.");
        }
        else {
            final HtmlPage page = getPage();
            final URL url = page.getUrl();
            final StringWebResponse webResponse = new StringWebResponse(writeBuilder_.toString(), url);
            webResponse.setFromJavascript(true);
            writeInCurrentDocument_ = true;
            writeBuilder_.setLength(0);

            final WebClient webClient = page.getWebClient();
            final WebWindow window = page.getEnclosingWindow();
            // reset isAttachedToPageDuringOnload_ to trigger the onload event for chrome also
            if (window instanceof FrameWindow) {
                final BaseFrameElement frame = ((FrameWindow) window).getFrameElement();
                final ScriptableObject scriptable = frame.getScriptableObject();
                if (scriptable instanceof HTMLIFrameElement) {
                    ((HTMLIFrameElement) scriptable).onRefresh();
                }
            }
            webClient.loadWebResponseInto(webResponse, window);
        }
    }

    /**
     * {@inheritDoc}
     */
    @JsxGetter
    public Element getDocumentElement() {
        implicitCloseIfNecessary();
        return super.getDocumentElement();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxFunction(FF)
    public boolean execCommand(final String cmd, final boolean userInterface, final Object value) {
        return super.execCommand(cmd, userInterface, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxFunction(FF)
    public boolean queryCommandEnabled(final String cmd) {
        return super.queryCommandEnabled(cmd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxFunction(FF)
    public boolean queryCommandSupported(final String cmd) {
        return super.queryCommandSupported(cmd);
    }

    /**
     * Closes the document implicitly, i.e. flushes the <tt>document.write</tt> buffer (IE only).
     */
    private void implicitCloseIfNecessary() {
        if (!writeInCurrentDocument_) {
            try {
                close();
            }
            catch (final IOException e) {
                throw Context.throwAsScriptRuntimeEx(e);
            }
        }
    }

    /**
     * Gets the window in which this document is contained.
     * @return the window
     */
    @JsxGetter(IE)
    public Object getParentWindow() {
        return getWindow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object appendChild(final Object childObject) {
        throw Context.reportRuntimeError("Node cannot be inserted at the specified point in the hierarchy.");
    }

    /**
     * Returns the element with the specified ID, or {@code null} if that element could not be found.
     * @param id the ID to search for
     * @return the element, or {@code null} if it could not be found
     */
    @JsxFunction
    public Object getElementById(final String id) {
        implicitCloseIfNecessary();
        Object result = null;
        final DomElement domElement = getPage().getElementById(id);
        if (null == domElement) {
            // Just fall through - result is already set to null
            if (LOG.isDebugEnabled()) {
                LOG.debug("getElementById(" + id + "): no DOM node found with this id");
            }
        }
        else {
            final Object jsElement = getScriptableFor(domElement);
            if (jsElement == NOT_FOUND) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getElementById(" + id
                            + ") cannot return a result as there isn't a JavaScript object for the HTML element "
                            + domElement.getClass().getName());
                }
            }
            else {
                result = jsElement;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HTMLCollection getElementsByClassName(final String className) {
        return ((HTMLElement) getDocumentElement()).getElementsByClassName(className);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxFunction(FF)
    public HTMLCollection getElementsByName(final String elementName) {
        implicitCloseIfNecessary();
        if ("null".equals(elementName)) {
            return HTMLCollection.emptyCollection(getWindow().getDomNodeOrDie());
        }
        // Null must me changed to '' for proper collection initialization.
        final String expElementName = "null".equals(elementName) ? "" : elementName;

        final HtmlPage page = getPage();
        final HTMLCollection collection = new HTMLCollection(page, true) {
            @Override
            protected List<DomNode> computeElements() {
                return new ArrayList<DomNode>(page.getElementsByName(expElementName));
            }

            @Override
            protected EffectOnCache getEffectOnCache(final HtmlAttributeChangeEvent event) {
                if ("name".equals(event.getName())) {
                    return EffectOnCache.RESET;
                }
                return EffectOnCache.NONE;
            }
        };

        return collection;
    }

    /**
     * Calls to <tt>document.XYZ</tt> should first look at elements named <tt>XYZ</tt> before
     * using standard functions.
     *
     * {@inheritDoc}
     */
    @Override
    protected Object getWithPreemption(final String name) {
        final HtmlPage page = (HtmlPage) getDomNodeOrNull();
        if (page == null || getBrowserVersion().hasFeature(HTMLDOCUMENT_GET_PREFERS_STANDARD_FUNCTIONS)) {
            final Object response = getPrototype().get(name, this);
            if (response != NOT_FOUND) {
                return response;
            }
        }
        return getIt(name);
    }

    private Object getIt(final String name) {
        final HtmlPage page = (HtmlPage) getDomNodeOrNull();
        if (page == null) {
            return NOT_FOUND;
        }

        final boolean forIDAndOrName = getBrowserVersion().hasFeature(HTMLDOCUMENT_GET_FOR_ID_AND_OR_NAME);
        final boolean alsoFrames = getBrowserVersion().hasFeature(HTMLDOCUMENT_GET_ALSO_FRAMES);

        // for performance
        // we will calculate the elements to decide if we really have
        // to really create a HTMLCollection or not
        final List<DomNode> matchingElements = getItComputeElements(page, name, forIDAndOrName, alsoFrames);
        final int size = matchingElements.size();
        if (size == 0) {
            return NOT_FOUND;
        }
        if (size == 1) {
            final DomNode object = matchingElements.get(0);
            if (alsoFrames && object instanceof BaseFrameElement) {
                return (SimpleScriptable) ((BaseFrameElement) object).getEnclosedWindow().getScriptableObject();
            }
            return super.getScriptableFor(object);
        }

        final HTMLCollection collection = new HTMLCollection(page, matchingElements) {
            @Override
            protected List<DomNode> computeElements() {
                return getItComputeElements(page, name, forIDAndOrName, alsoFrames);
            }

            @Override
            protected EffectOnCache getEffectOnCache(final HtmlAttributeChangeEvent event) {
                final String attributeName = event.getName();
                if ("name".equals(attributeName)) {
                    return EffectOnCache.RESET;
                }
                else if (forIDAndOrName && "id".equals(attributeName)) {
                    return EffectOnCache.RESET;
                }

                return EffectOnCache.NONE;
            }

            @Override
            protected SimpleScriptable getScriptableFor(final Object object) {
                if (alsoFrames && object instanceof BaseFrameElement) {
                    return (SimpleScriptable) ((BaseFrameElement) object).getEnclosedWindow().getScriptableObject();
                }
                return super.getScriptableFor(object);
            }
        };

        return collection;
    }

    private List<DomNode> getItComputeElements(final HtmlPage page, final String name,
            final boolean forIDAndOrName, final boolean alsoFrames) {
        final List<DomElement> elements;
        if (forIDAndOrName) {
            elements = page.getElementsByIdAndOrName(name);
        }
        else {
            elements = page.getElementsByName(name);
        }
        final List<DomNode> matchingElements = new ArrayList<>();
        for (final DomElement elt : elements) {
            if (elt instanceof HtmlForm || elt instanceof HtmlImage
                    || (alsoFrames && elt instanceof BaseFrameElement)) {
                matchingElements.add(elt);
            }
        }
        return matchingElements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter
    public HTMLElement getHead() {
        final HtmlElement head = getPage().getHead();
        if (head != null) {
            return (HTMLElement) head.getScriptableObject();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter(FF)
    @CanSetReadOnly(CanSetReadOnlyStatus.EXCEPTION)
    public HTMLElement getBody() {
        return super.getBody();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return getPage().getTitleText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTitle(final String title) {
        getPage().setTitleText(title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter({CHROME, FF})
    public String getBgColor() {
        String color = getPage().getBody().getAttribute("bgColor");
        if (color == DomElement.ATTRIBUTE_NOT_DEFINED && getBrowserVersion().hasFeature(HTMLDOCUMENT_COLOR)) {
            color = "#ffffff";
        }
        if (getBrowserVersion().hasFeature(HTML_COLOR_EXPAND_ZERO) && "#0".equals(color)) {
            color = "#000000";
        }
        return color;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxSetter({CHROME, FF})
    public void setBgColor(final String color) {
        final HTMLBodyElement body = (HTMLBodyElement) getPage().getBody().getScriptableObject();
        body.setBgColor(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter({CHROME, FF})
    public String getAlinkColor() {
        String color = getPage().getBody().getAttribute("aLink");
        if (color == DomElement.ATTRIBUTE_NOT_DEFINED && getBrowserVersion().hasFeature(HTMLDOCUMENT_COLOR)) {
            color = "#0000ff";
        }
        if (getBrowserVersion().hasFeature(HTML_COLOR_EXPAND_ZERO) && "#0".equals(color)) {
            color = "#000000";
        }
        return color;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxSetter({CHROME, FF})
    public void setAlinkColor(final String color) {
        final HTMLBodyElement body = (HTMLBodyElement) getPage().getBody().getScriptableObject();
        body.setALink(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter({CHROME, FF})
    public String getLinkColor() {
        String color = getPage().getBody().getAttributeDirect("link");
        if (color == DomElement.ATTRIBUTE_NOT_DEFINED && getBrowserVersion().hasFeature(HTMLDOCUMENT_COLOR)) {
            color = "#0000ff";
        }
        if (getBrowserVersion().hasFeature(HTML_COLOR_EXPAND_ZERO) && "#0".equals(color)) {
            color = "#000000";
        }
        return color;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxSetter({CHROME, FF})
    public void setLinkColor(final String color) {
        final HTMLBodyElement body = (HTMLBodyElement) getPage().getBody().getScriptableObject();
        body.setLink(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter({CHROME, FF})
    public String getVlinkColor() {
        String color = getPage().getBody().getAttribute("vLink");
        if (color == DomElement.ATTRIBUTE_NOT_DEFINED && getBrowserVersion().hasFeature(HTMLDOCUMENT_COLOR)) {
            color = "#800080";
        }
        if (getBrowserVersion().hasFeature(HTML_COLOR_EXPAND_ZERO) && "#0".equals(color)) {
            color = "#000000";
        }
        return color;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxSetter({CHROME, FF})
    public void setVlinkColor(final String color) {
        final HTMLBodyElement body = (HTMLBodyElement) getPage().getBody().getScriptableObject();
        body.setVLink(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter({CHROME, FF})
    public String getFgColor() {
        String color = getPage().getBody().getAttributeDirect("text");
        if (color == DomElement.ATTRIBUTE_NOT_DEFINED && getBrowserVersion().hasFeature(HTMLDOCUMENT_COLOR)) {
            color = "#000000";
        }
        if (getBrowserVersion().hasFeature(HTML_COLOR_EXPAND_ZERO) && "#0".equals(color)) {
            color = "#000000";
        }
        return color;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxSetter({CHROME, FF})
    public void setFgColor(final String color) {
        final HTMLBodyElement body = (HTMLBodyElement) getPage().getBody().getScriptableObject();
        body.setText(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter(FF)
    public String getDomain() {
        return super.getDomain();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxSetter(FF)
    public void setDomain(final String newDomain) {
        super.setDomain(newDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter(FF)
    public Object getScripts() {
        return super.getScripts();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HTMLElement getActiveElement() {
        if (activeElement_ == null) {
            final HtmlElement body = getPage().getBody();
            if (body != null) {
                activeElement_ = (HTMLElement) getScriptableFor(body);
            }
        }

        return activeElement_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasFocus() {
        return activeElement_ != null && getPage().getFocusedElement() == activeElement_.getDomNodeOrDie();
    }

    /**
     * Sets the specified element as the document's active element.
     * @see HTMLElement#setActive()
     * @param element the new active element for this document
     */
    public void setActiveElement(final HTMLElement element) {
        // TODO update page focus element also

        activeElement_ = element;

        if (element != null) {
            // if this is part of an iFrame, make the iFrame tag the
            // active element of his doc
            final WebWindow window = element.getDomNodeOrDie().getPage().getEnclosingWindow();
            if (window instanceof FrameWindow) {
                final BaseFrameElement frame = ((FrameWindow) window).getFrameElement();
                if (frame instanceof HtmlInlineFrame) {
                    final Window winWithFrame = (Window) frame.getPage().getEnclosingWindow().getScriptableObject();
                    ((HTMLDocument) winWithFrame.getDocument()).setActiveElement(
                                (HTMLElement) frame.getScriptableObject());
                }
            }
        }
    }

    /**
     * Dispatches an event into the event system (standards-conformant browsers only). See
     * <a href="https://developer.mozilla.org/en-US/docs/DOM/element.dispatchEvent">the Gecko
     * DOM reference</a> for more information.
     *
     * @param event the event to be dispatched
     * @return {@code false} if at least one of the event handlers which handled the event
     *         called <tt>preventDefault</tt>; {@code true} otherwise
     */
    @Override
    @JsxFunction
    public boolean dispatchEvent(final Event event) {
        event.setTarget(this);
        final ScriptResult result = fireEvent(event);
        return !event.isAborted(result);
    }

    /**
     * Does... nothing.
     * @see <a href="https://developer.mozilla.org/en/DOM/document.clear">Mozilla doc</a>
     */
    @JsxFunction
    public void clear() {
        // nothing
    }

    /**
     * Sets the head.
     * @param head the head
     */
    @JsxSetter({FF, IE})
    public void setHead(final ScriptableObject head) {
        //ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxFunction
    public Selection getSelection() {
        return getWindow().getSelectionImpl();
    }

    /**
     * Creates a new HTML attribute with the specified name.
     *
     * @param attributeName the name of the attribute to create
     * @return an attribute with the specified name
     */
    @Override
    public Attr createAttribute(final String attributeName) {
        String name = attributeName;
        if (StringUtils.isNotEmpty(name)
                && getBrowserVersion().hasFeature(JS_DOCUMENT_CREATE_ATTRUBUTE_LOWER_CASE)) {
            name = name.toLowerCase(Locale.ROOT);
        }

        return super.createAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBaseURI() {
        return getPage().getBaseURL().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxFunction({CHROME, FF})
    public void captureEvents(final String type) {
        // Empty.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxFunction({CHROME, FF})
    public void releaseEvents(final String type) {
        // Empty.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter(FF)
    public String getDesignMode() {
        return super.getDesignMode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxSetter(FF)
    public void setDesignMode(final String mode) {
        super.setDesignMode(mode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object elementFromPoint(final int x, final int y) {
        final HtmlElement element = getPage().getElementFromPoint(x, y);
        return element == null ? null : element.getScriptableObject();
    }
}
