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
package com.gargoylesoftware.htmlunit.javascript.host.dom;

import com.gargoylesoftware.css.parser.CSSException;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.HttpHeader;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.BaseFrameElement;
import com.gargoylesoftware.htmlunit.html.DomComment;
import com.gargoylesoftware.htmlunit.html.DomDocumentFragment;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlArea;
import com.gargoylesoftware.htmlunit.html.HtmlAttributeChangeEvent;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlEmbed;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRp;
import com.gargoylesoftware.htmlunit.html.HtmlRt;
import com.gargoylesoftware.htmlunit.html.HtmlScript;
import com.gargoylesoftware.htmlunit.html.HtmlSvg;
import com.gargoylesoftware.htmlunit.html.HtmlUnknownElement;
import com.gargoylesoftware.htmlunit.html.UnknownElementFactory;
import com.gargoylesoftware.htmlunit.html.impl.SimpleRange;
import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;
import com.gargoylesoftware.htmlunit.javascript.configuration.CanSetReadOnly;
import com.gargoylesoftware.htmlunit.javascript.configuration.CanSetReadOnlyStatus;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxFunction;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxGetter;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxSetter;
import com.gargoylesoftware.htmlunit.javascript.host.Element;
import com.gargoylesoftware.htmlunit.javascript.host.Location;
import com.gargoylesoftware.htmlunit.javascript.host.NativeFunctionPrefixResolver;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.gargoylesoftware.htmlunit.javascript.host.css.StyleSheetList;
import com.gargoylesoftware.htmlunit.javascript.host.event.BeforeUnloadEvent;
import com.gargoylesoftware.htmlunit.javascript.host.event.CloseEvent;
import com.gargoylesoftware.htmlunit.javascript.host.event.CustomEvent;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;
import com.gargoylesoftware.htmlunit.javascript.host.event.HashChangeEvent;
import com.gargoylesoftware.htmlunit.javascript.host.event.KeyboardEvent;
import com.gargoylesoftware.htmlunit.javascript.host.event.MessageEvent;
import com.gargoylesoftware.htmlunit.javascript.host.event.MouseEvent;
import com.gargoylesoftware.htmlunit.javascript.host.event.MutationEvent;
import com.gargoylesoftware.htmlunit.javascript.host.event.PointerEvent;
import com.gargoylesoftware.htmlunit.javascript.host.event.PopStateEvent;
import com.gargoylesoftware.htmlunit.javascript.host.event.ProgressEvent;
import com.gargoylesoftware.htmlunit.javascript.host.event.UIEvent;
import com.gargoylesoftware.htmlunit.javascript.host.html.DocumentProxy;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLAnchorElement;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLCollection;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLDocument;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;
import com.gargoylesoftware.htmlunit.util.EncodingSniffer;

import net.sourceforge.htmlunit.corejs.javascript.Callable;
import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.FunctionObject;
import net.sourceforge.htmlunit.corejs.javascript.NativeFunction;
import net.sourceforge.htmlunit.corejs.javascript.ScriptRuntime;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.utils.PrefixResolver;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.EVENT_ONCLOSE_DOCUMENT_CREATE_NOT_SUPPORTED;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.EVENT_TYPE_BEFOREUNLOADEVENT;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.EVENT_TYPE_HASHCHANGEEVENT;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.EVENT_TYPE_KEY_EVENTS;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.EVENT_TYPE_POINTEREVENT;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.EVENT_TYPE_PROGRESSEVENT;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.HTMLDOCUMENT_CHARSET_LOWERCASE;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_ANCHORS_REQUIRES_NAME_OR_ID;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_DOCUMENT_CREATE_ELEMENT_STRICT;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_DOCUMENT_DESIGN_MODE_INHERIT;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_DOCUMENT_FORMS_FUNCTION_SUPPORTED;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_DOCUMENT_SELECTION_RANGE_COUNT;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_DOCUMENT_SETTING_DOMAIN_THROWS_FOR_ABOUT_BLANK;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_DOCUMENT_SET_LOCATION_EXECUTED_IN_ANCHOR;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_TREEWALKER_EXPAND_ENTITY_REFERENCES_FALSE;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_TREEWALKER_FILTER_FUNCTION_ONLY;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.QUERYSELECTORALL_NOT_IN_QUIRKS;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF45;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF52;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.IE;
import static com.gargoylesoftware.htmlunit.util.StringUtils.parseHttpDate;

/**
 * A JavaScript object for {@code Document}.
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
 * @author Ronald Brill
 * @author Chuck Dumont
 * @author Frank Danek
 * @author Madis Pärn
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms531073.aspx">MSDN documentation</a>
 * @see <a href="http://www.w3.org/TR/2000/WD-DOM-Level-1-20000929/level-one-html.html#ID-7068919">W3C Dom Level 1</a>
 */
@JsxClass
public class Document extends Node {

    private static final Log LOG = LogFactory.getLog(Document.class);
    private static final Pattern TAG_NAME_PATTERN = Pattern.compile("\\w+");
    // all as lowercase for performance
    private static final Set<String> EXECUTE_CMDS_IE = new HashSet<>();
    /** https://developer.mozilla.org/en/Rich-Text_Editing_in_Mozilla#Executing_Commands */
    private static final Set<String> EXECUTE_CMDS_FF = new HashSet<>();
    private static final Set<String> EXECUTE_CMDS_CHROME = new HashSet<>();
    /** The format to use for the <tt>lastModified</tt> attribute. */
    private static final String LAST_MODIFIED_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    /**
     * Map<String, Class> which maps strings a caller may use when calling into
     * {@link #createEvent(String)} to the associated event class. To support a new
     * event creation type, the event type and associated class need to be added into this map in
     * the static initializer. The map is unmodifiable. Any class that is a value in this map MUST
     * have a no-arg constructor.
     */
    /** Contains all supported DOM level 2 events. */
    private static final Map<String, Class<? extends Event>> SUPPORTED_DOM2_EVENT_TYPE_MAP;
    /** Contains all supported DOM level 3 events. DOM level 2 events are not included. */
    private static final Map<String, Class<? extends Event>> SUPPORTED_DOM3_EVENT_TYPE_MAP;
    /** Contains all supported vendor specific events. */
    private static final Map<String, Class<? extends Event>> SUPPORTED_VENDOR_EVENT_TYPE_MAP;

    /** Initializes the supported event type map. */
    static {
        final Map<String, Class<? extends Event>> dom2EventMap = new HashMap<>();
        dom2EventMap.put("HTMLEvents", Event.class);
        dom2EventMap.put("MouseEvents", MouseEvent.class);
        dom2EventMap.put("MutationEvents", MutationEvent.class);
        dom2EventMap.put("UIEvents", UIEvent.class);
        SUPPORTED_DOM2_EVENT_TYPE_MAP = Collections.unmodifiableMap(dom2EventMap);

        final Map<String, Class<? extends Event>> dom3EventMap = new HashMap<>();
        dom3EventMap.put("Event", Event.class);
        dom3EventMap.put("KeyboardEvent", KeyboardEvent.class);
        dom3EventMap.put("MouseEvent", MouseEvent.class);
        dom3EventMap.put("MessageEvent", MessageEvent.class);
        dom3EventMap.put("MutationEvent", MutationEvent.class);
        dom3EventMap.put("UIEvent", UIEvent.class);
        dom3EventMap.put("CustomEvent", CustomEvent.class);
        dom3EventMap.put("CloseEvent", CloseEvent.class);
        SUPPORTED_DOM3_EVENT_TYPE_MAP = Collections.unmodifiableMap(dom3EventMap);

        final Map<String, Class<? extends Event>> additionalEventMap = new HashMap<>();
        additionalEventMap.put("BeforeUnloadEvent", BeforeUnloadEvent.class);
        additionalEventMap.put("Events", Event.class);
        additionalEventMap.put("HashChangeEvent", HashChangeEvent.class);
        additionalEventMap.put("KeyEvents", KeyboardEvent.class);
        additionalEventMap.put("PointerEvent", PointerEvent.class);
        additionalEventMap.put("PopStateEvent", PopStateEvent.class);
        additionalEventMap.put("ProgressEvent", ProgressEvent.class);
        SUPPORTED_VENDOR_EVENT_TYPE_MAP = Collections.unmodifiableMap(additionalEventMap);
    }

    /**
     * Static counter for {@link #uniqueID_}.
     */
    private static int UniqueID_Counter_ = 1;

    private Window window_;
    private DOMImplementation implementation_;
    private String designMode_;
    private String compatMode_;
    private int documentMode_ = -1;
    private String uniqueID_;
    private String domain_;
    private String lastModified_;
    private ScriptableObject currentScript_;
    private transient StyleSheetList styleSheetList_;

    static {
        // commands
        List<String> cmds = Arrays.asList(
            "2D-Position", "AbsolutePosition",
            "BlockDirLTR", "BlockDirRTL", "BrowseMode",
            "ClearAuthenticationCache", "CreateBookmark", "Copy", "Cut",
            "DirLTR", "DirRTL",
            "EditMode",
            "InlineDirLTR", "InlineDirRTL", "InsertButton", "InsertFieldset",
            "InsertIFrame", "InsertInputButton", "InsertInputCheckbox",
            "InsertInputFileUpload", "InsertInputHidden", "InsertInputImage", "InsertInputPassword", "InsertInputRadio",
            "InsertInputReset", "InsertInputSubmit", "InsertInputText", "InsertMarquee",
            "InsertSelectDropdown", "InsertSelectListbox", "InsertTextArea",
            "LiveResize", "MultipleSelection", "Open",
            "OverWrite", "PlayImage",
            "Refresh", "RemoveParaFormat", "SaveAs",
            "SizeToControl", "SizeToControlHeight", "SizeToControlWidth", "Stop", "StopImage",
            "UnBookmark",
            "Paste"
        );
        for (final String cmd : cmds) {
            EXECUTE_CMDS_IE.add(cmd.toLowerCase(Locale.ROOT));
        }

        cmds = Arrays.asList(
            "BackColor", "BackgroundImageCache" /* Undocumented */,
            "Bold",
            "CreateLink", "Delete",
            "FontName", "FontSize", "ForeColor", "FormatBlock",
            "Indent", "InsertHorizontalRule", "InsertImage",
            "InsertOrderedList", "InsertParagraph", "InsertUnorderedList",
            "Italic", "JustifyCenter", "JustifyFull", "JustifyLeft", "JustifyNone",
            "JustifyRight",
            "Outdent",
            "Print",
            "Redo", "RemoveFormat",
            "SelectAll", "StrikeThrough", "Subscript", "Superscript",
            "Underline", "Undo", "Unlink", "Unselect"
        );
        for (final String cmd : cmds) {
            EXECUTE_CMDS_IE.add(cmd.toLowerCase(Locale.ROOT));
            if (!"Bold".equals(cmd)) {
                EXECUTE_CMDS_CHROME.add(cmd.toLowerCase(Locale.ROOT));
            }
        }

        cmds = Arrays.asList(
            "backColor", "bold", "contentReadOnly", "copy", "createLink", "cut", "decreaseFontSize", "delete",
            "fontName", "fontSize", "foreColor", "formatBlock", "heading", "hiliteColor", "increaseFontSize",
            "indent", "insertHorizontalRule", "insertHTML", "insertImage", "insertOrderedList", "insertUnorderedList",
            "insertParagraph", "italic",
            "justifyCenter", "JustifyFull", "justifyLeft", "justifyRight", "outdent", "paste", "redo",
            "removeFormat", "selectAll", "strikeThrough", "subscript", "superscript", "underline", "undo", "unlink",
            "useCSS", "styleWithCSS"
        );
        for (final String cmd : cmds) {
            EXECUTE_CMDS_FF.add(cmd.toLowerCase(Locale.ROOT));
            if (!"bold".equals(cmd)) {
                EXECUTE_CMDS_CHROME.add(cmd.toLowerCase(Locale.ROOT));
            }
        }
    }

    /**
     * Creates an instance.
     */
    @JsxConstructor({CHROME, FF, EDGE})
    public Document() {
    }

    /**
     * Sets the Window JavaScript object that encloses this document.
     * @param window the Window JavaScript object that encloses this document
     */
    public void setWindow(final Window window) {
        window_ = window;
    }

    /**
     * Returns the value of the {@code location} property.
     * @return the value of the {@code location} property
     */
    @JsxGetter
    public Location getLocation() {
        if (window_ == null) {
            return null;
        }
        return window_.getLocation();
    }

    /**
     * Sets the value of the {@code location} property. The location's default property is "href",
     * so setting "document.location='http://www.sf.net'" is equivalent to setting
     * "document.location.href='http://www.sf.net'".
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms535866.aspx">MSDN documentation</a>
     * @param location the location to navigate to
     * @throws IOException when location loading fails
     */
    @JsxSetter
    public void setLocation(final String location) throws IOException {
        final Object event = getWindow().getEvent();
        boolean setLocation = true;
        if (event instanceof UIEvent) {
            final Object target = ((UIEvent) event).getTarget();
            if (target instanceof HTMLAnchorElement) {
                final String href = ((HTMLAnchorElement) target).getHref();
                if (!href.isEmpty()
                        && !getBrowserVersion().hasFeature(JS_DOCUMENT_SET_LOCATION_EXECUTED_IN_ANCHOR)) {
                    setLocation = false;
                }
            }
        }
        if (setLocation) {
            window_.setLocation(location);
        }
    }

    /**
     * Returns the value of the {@code referrer} property.
     * @return the value of the {@code referrer} property
     */
    @JsxGetter
    public String getReferrer() {
        String referrer = "";
        final WebResponse webResponse = getPage().getWebResponse();
        if (webResponse != null) {
            referrer = webResponse.getWebRequest().getAdditionalHeaders().get(HttpHeader.REFERER);
            if (referrer == null) {
                referrer = "";
            }
        }
        return referrer;
    }

    /**
     * Gets the JavaScript property {@code documentElement} for the document.
     * @return the root node for the document
     */
    @JsxGetter
    public Element getDocumentElement() {
        final Object documentElement = getPage().getDocumentElement();
        if (documentElement == null) {
            // for instance with an XML document with parsing error
            return null;
        }
        return (Element) getScriptableFor(documentElement);
    }

    /**
     * Gets the JavaScript property {@code rootElement}.
     * @return the root node for the document
     */
    @JsxGetter({CHROME, FF52, IE})
    public Element getRootElement() {
        return null;
    }

    /**
     * Gets the JavaScript property {@code doctype} for the document.
     * @return the DocumentType of the document
     */
    @JsxGetter
    public SimpleScriptable getDoctype() {
        final Object documentType = getPage().getDoctype();
        if (documentType == null) {
            return null;
        }
        return getScriptableFor(documentType);
    }

    /**
     * Returns a value which indicates whether or not the document can be edited.
     * @return a value which indicates whether or not the document can be edited
     */
    @JsxGetter({CHROME, IE})
    public String getDesignMode() {
        if (designMode_ == null) {
            if (getBrowserVersion().hasFeature(JS_DOCUMENT_DESIGN_MODE_INHERIT)) {
                designMode_ = "inherit";
            }
            else {
                designMode_ = "off";
            }
        }
        return designMode_;
    }

    /**
     * Sets a value which indicates whether or not the document can be edited.
     * @param mode a value which indicates whether or not the document can be edited
     */
    @JsxSetter({CHROME, IE})
    public void setDesignMode(final String mode) {
        final BrowserVersion browserVersion = getBrowserVersion();
        final boolean inherit = browserVersion.hasFeature(JS_DOCUMENT_DESIGN_MODE_INHERIT);
        if (inherit) {
            if (!"on".equalsIgnoreCase(mode) && !"off".equalsIgnoreCase(mode) && !"inherit".equalsIgnoreCase(mode)) {
                throw Context.reportRuntimeError("Invalid document.designMode value '" + mode + "'.");
            }

            if ("on".equalsIgnoreCase(mode)) {
                designMode_ = "on";
            }
            else if ("off".equalsIgnoreCase(mode)) {
                designMode_ = "off";
            }
            else if ("inherit".equalsIgnoreCase(mode)) {
                designMode_ = "inherit";
            }
        }
        else {
            if ("on".equalsIgnoreCase(mode)) {
                designMode_ = "on";
                final SgmlPage page = getPage();
                if (page != null && page.isHtmlPage()
                        && getBrowserVersion().hasFeature(JS_DOCUMENT_SELECTION_RANGE_COUNT)) {
                    final HtmlPage htmlPage = (HtmlPage) page;
                    final DomNode child = htmlPage.getBody().getFirstChild();
                    final DomNode rangeNode = child == null ? htmlPage.getBody() : child;
                    htmlPage.setSelectionRange(new SimpleRange(rangeNode, 0));
                }
            }
            else if ("off".equalsIgnoreCase(mode)) {
                designMode_ = "off";
            }
        }
    }

    /**
     * Returns the page that this document is modeling.
     * @return the page that this document is modeling
     */
    public SgmlPage getPage() {
        return (SgmlPage) getDomNodeOrDie();
    }

    /**
     * Gets the window in which this document is contained.
     * @return the window
     */
    @JsxGetter
    public Object getDefaultView() {
        return getWindow();
    }

    /**
     * Creates a new document fragment.
     * @return a newly created document fragment
     */
    @JsxFunction
    public Object createDocumentFragment() {
        final DomDocumentFragment fragment = getDomNodeOrDie().getPage().createDocumentFragment();
        final DocumentFragment node = new DocumentFragment();
        node.setParentScope(getParentScope());
        node.setPrototype(getPrototype(node.getClass()));
        node.setDomNode(fragment);
        return getScriptableFor(fragment);
    }

    /**
     * Creates a new HTML attribute with the specified name.
     *
     * @param attributeName the name of the attribute to create
     * @return an attribute with the specified name
     */
    @JsxFunction
    public Attr createAttribute(final String attributeName) {
        return (Attr) getPage().createAttribute(attributeName).getScriptableObject();
    }

    /**
     * Imports a node from another document to this document.
     * The source node is not altered or removed from the original document;
     * this method creates a new copy of the source node.
     *
     * @param importedNode the node to import
     * @param deep Whether to recursively import the subtree under the specified node; or not
     * @return the imported node that belongs to this Document
     */
    @JsxFunction
    public Object importNode(final Node importedNode, final boolean deep) {
        DomNode domNode = importedNode.getDomNodeOrDie();
        domNode = domNode.cloneNode(deep);
        domNode.processImportNode(this);
        for (final DomNode childNode : domNode.getDescendants()) {
            childNode.processImportNode(this);
        }
        return domNode.getScriptableObject();
    }

    /**
     * Adopts a node from an external document.
     * The node and its subtree is removed from the document it's in (if any),
     * and its ownerDocument is changed to the current document.
     * The node can then be inserted into the current document.
     *
     * @param externalNode the node from another document to be adopted
     * @return the adopted node that can be used in the current document
     */
    @JsxFunction
    public Object adoptNode(final Node externalNode) {
        externalNode.remove();
        return importNode(externalNode, true);
    }

    /**
     * Returns the implementation object of the current document.
     * @return implementation-specific object
     */
    @JsxGetter
    public DOMImplementation getImplementation() {
        if (implementation_ == null) {
            implementation_ = new DOMImplementation();
            implementation_.setParentScope(getWindow());
            implementation_.setPrototype(getPrototype(implementation_.getClass()));
        }
        return implementation_;
    }

    /**
     * Adapts any DOM node to resolve namespaces so that an XPath expression can be easily
     * evaluated relative to the context of the node where it appeared within the document.
     * @param nodeResolver the node to be used as a context for namespace resolution
     * @return an XPathNSResolver which resolves namespaces with respect to the definitions
     *         in scope for a specified node
     */
    @JsxFunction({CHROME, FF})
    public XPathNSResolver createNSResolver(final Node nodeResolver) {
        final XPathNSResolver resolver = new XPathNSResolver();
        resolver.setElement(nodeResolver);
        resolver.setParentScope(getWindow());
        resolver.setPrototype(getPrototype(resolver.getClass()));
        return resolver;
    }

    /**
     * Create a new DOM text node with the given data.
     *
     * @param newData the string value for the text node
     * @return the new text node or NOT_FOUND if there is an error
     */
    @JsxFunction
    public Object createTextNode(final String newData) {
        Object result = NOT_FOUND;
        try {
            final DomNode domNode = new DomText(getDomNodeOrDie().getPage(), newData);
            final Object jsElement = getScriptableFor(domNode);

            if (jsElement == NOT_FOUND) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("createTextNode(" + newData
                            + ") cannot return a result as there isn't a JavaScript object for the DOM node "
                            + domNode.getClass().getName());
                }
            }
            else {
                result = jsElement;
            }
        }
        catch (final ElementNotFoundException e) {
            // Just fall through - result is already set to NOT_FOUND
        }
        return result;
    }

    /**
     * Creates a new Comment.
     * @param comment the comment text
     * @return the new Comment
     */
    @JsxFunction
    public Object createComment(final String comment) {
        final DomNode domNode = new DomComment(getDomNodeOrDie().getPage(), comment);
        return getScriptableFor(domNode);
    }

    /**
     * Evaluates an XPath expression string and returns a result of the specified type if possible.
     * @param expression the XPath expression string to be parsed and evaluated
     * @param contextNode the context node for the evaluation of this XPath expression
     * @param resolver the resolver permits translation of all prefixes, including the XML namespace prefix,
     *        within the XPath expression into appropriate namespace URIs.
     * @param type If a specific type is specified, then the result will be returned as the corresponding type
     * @param result the result object which may be reused and returned by this method
     * @return the result of the evaluation of the XPath expression
     */
    @JsxFunction({CHROME, FF})
    public XPathResult evaluate(final String expression, final Node contextNode,
            final Object resolver, final int type, final Object result) {
        XPathResult xPathResult = (XPathResult) result;
        if (xPathResult == null) {
            xPathResult = new XPathResult();
            xPathResult.setParentScope(getParentScope());
            xPathResult.setPrototype(getPrototype(xPathResult.getClass()));
        }

        PrefixResolver prefixResolver = null;
        if (resolver instanceof NativeFunction) {
            prefixResolver = new NativeFunctionPrefixResolver((NativeFunction) resolver, contextNode.getParentScope());
        }
        else if (resolver instanceof PrefixResolver) {
            prefixResolver = (PrefixResolver) resolver;
        }
        xPathResult.init(contextNode.getDomNodeOrDie().getByXPath(expression, prefixResolver), type);
        return xPathResult;
    }

    /**
     * Creates a new element with the given tag name.
     *
     * @param tagName the tag name
     * @return the new HTML element, or NOT_FOUND if the tag is not supported
     */
    @JsxFunction
    public Object createElement(String tagName) {
        Object result = NOT_FOUND;
        try {
            final BrowserVersion browserVersion = getBrowserVersion();

            if (browserVersion.hasFeature(JS_DOCUMENT_CREATE_ELEMENT_STRICT)
                  && (tagName.contains("<") || tagName.contains(">"))) {
                LOG.info("createElement: Provided string '"
                            + tagName + "' contains an invalid character; '<' and '>' are not allowed");
                throw Context.reportRuntimeError("String contains an invalid character");
            }
            else if (tagName.startsWith("<") && tagName.endsWith(">")) {
                tagName = tagName.substring(1, tagName.length() - 1);

                final Matcher matcher = TAG_NAME_PATTERN.matcher(tagName);
                if (!matcher.matches()) {
                    LOG.info("createElement: Provided string '" + tagName + "' contains an invalid character");
                    throw Context.reportRuntimeError("String contains an invalid character");
                }
            }

            final SgmlPage page = getPage();
            org.w3c.dom.Node element = page.createElement(tagName);

            if (element instanceof BaseFrameElement) {
                ((BaseFrameElement) element).markAsCreatedByJavascript();
            }
            else if (element instanceof HtmlInput) {
                ((HtmlInput) element).markAsCreatedByJavascript();
            }
            else if (element instanceof HtmlImage) {
                ((HtmlImage) element).markAsCreatedByJavascript();
            }
            else if (element instanceof HtmlRp) {
                ((HtmlRp) element).markAsCreatedByJavascript();
            }
            else if (element instanceof HtmlRt) {
                ((HtmlRt) element).markAsCreatedByJavascript();
            }
            else if (element instanceof HtmlScript) {
                ((HtmlScript) element).markAsCreatedByJavascript();
            }
            else if (element instanceof HtmlUnknownElement) {
                ((HtmlUnknownElement) element).markAsCreatedByJavascript();
            }
            else if (element instanceof HtmlSvg) {
                element = UnknownElementFactory.instance.createElementNS(page, "", "svg", null);
                ((HtmlUnknownElement) element).markAsCreatedByJavascript();
            }
            final Object jsElement = getScriptableFor(element);

            if (jsElement == NOT_FOUND) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("createElement(" + tagName
                        + ") cannot return a result as there isn't a JavaScript object for the element "
                        + element.getClass().getName());
                }
            }
            else {
                result = jsElement;
            }
        }
        catch (final ElementNotFoundException e) {
            // Just fall through - result is already set to NOT_FOUND
        }
        return result;
    }

    /**
     * Creates a new HTML element with the given tag name, and name.
     *
     * @param namespaceURI the URI that identifies an XML namespace
     * @param qualifiedName the qualified name of the element type to instantiate
     * @return the new HTML element, or NOT_FOUND if the tag is not supported
     */
    @JsxFunction
    public Object createElementNS(final String namespaceURI, final String qualifiedName) {
        final org.w3c.dom.Element element;
        if ("http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul".equals(namespaceURI)) {
            throw Context.reportRuntimeError("XUL not available");
        }

        if (HTMLParser.XHTML_NAMESPACE.equals(namespaceURI)
                || HTMLParser.SVG_NAMESPACE.equals(namespaceURI)) {
            element = getPage().createElementNS(namespaceURI, qualifiedName);
        }
        else {
            element = new DomElement(namespaceURI, qualifiedName, getPage(), null);
        }
        return getScriptableFor(element);
    }

    /**
     * Returns all the descendant elements with the specified tag name.
     * @param tagName the name to search for
     * @return all the descendant elements with the specified tag name
     */
    @JsxFunction
    public HTMLCollection getElementsByTagName(final String tagName) {
        final HTMLCollection collection;
        if ("*".equals(tagName)) {
            collection = new HTMLCollection(getDomNodeOrDie(), false) {
                @Override
                protected boolean isMatching(final DomNode node) {
                    return true;
                }
            };
        }
        else {
            collection = new HTMLCollection(getDomNodeOrDie(), false) {
                @Override
                protected boolean isMatching(final DomNode node) {
                    return tagName.equalsIgnoreCase(node.getNodeName());
                }
            };
        }

        return collection;
    }

    /**
     * Returns a list of elements with the given tag name belonging to the given namespace.
     * @param namespaceURI the namespace URI of elements to look for
     * @param localName is either the local name of elements to look for or the special value "*",
     *                  which matches all elements.
     * @return a live NodeList of found elements in the order they appear in the tree
     */
    @JsxFunction
    public Object getElementsByTagNameNS(final Object namespaceURI, final String localName) {
        final HTMLCollection collection = new HTMLCollection(getDomNodeOrDie(), false) {
            @Override
            protected boolean isMatching(final DomNode node) {
                return localName.equals(node.getLocalName());
            }
        };

        return collection;
    }

    /**
     * Returns the value of the {@code activeElement} property.
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms533065.aspx">MSDN documentation</a>
     * @return the value of the {@code activeElement} property
     */
    @JsxGetter
    public Object getActiveElement() {
        return null;
    }

    /**
     * Returns the character encoding of the current document.
     * @return the character encoding of the current document
     */
    @JsxGetter
    public String getCharacterSet() {
        if (!(getPage() instanceof HtmlPage)) {
            // TODO: implement XmlPage.getCharset
            return "";
        }
        final Charset charset = getPage().getCharset();
        if (charset != null && getBrowserVersion().hasFeature(HTMLDOCUMENT_CHARSET_LOWERCASE)) {
            return charset.name().toLowerCase(Locale.ROOT);
        }
        return EncodingSniffer.translateEncodingLabel(charset);
    }

    /**
     * Retrieves the character set used to encode the document.
     * @return the character set used to encode the document
     */
    @JsxGetter
    public String getCharset() {
        if (!(getPage() instanceof HtmlPage)) {
            // TODO: implement XmlPage.getCharset
            return "";
        }
        final Charset charset = getPage().getCharset();
        if (getBrowserVersion().hasFeature(HTMLDOCUMENT_CHARSET_LOWERCASE)) {
            return charset.name().toLowerCase(Locale.ROOT);
        }
        return EncodingSniffer.translateEncodingLabel(charset);
    }

    /**
     * Gets the default character set from the current regional language settings.
     * @return the default character set from the current regional language settings
     */
    @JsxGetter(IE)
    public String getDefaultCharset() {
        return "windows-1252";
    }

    /**
     * Returns the value of the JavaScript property {@code anchors}.
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms537435.aspx">MSDN documentation</a>
     * @see <a href="http://www.mozilla.org/docs/dom/domref/dom_doc_ref4.html#1024543">
     * Gecko DOM reference</a>
     * @return the value of this property
     */
    @JsxGetter({CHROME, IE})
    public Object getAnchors() {
        return new HTMLCollection(getDomNodeOrDie(), true) {
            @Override
            protected boolean isMatching(final DomNode node) {
                if (!(node instanceof HtmlAnchor)) {
                    return false;
                }
                final HtmlAnchor anchor = (HtmlAnchor) node;
                if (getBrowserVersion().hasFeature(JS_ANCHORS_REQUIRES_NAME_OR_ID)) {
                    return anchor.hasAttribute("name") || anchor.hasAttribute("id");
                }
                return anchor.hasAttribute("name");
            }

            @Override
            protected EffectOnCache getEffectOnCache(final HtmlAttributeChangeEvent event) {
                if ("name".equals(event.getName()) || "id".equals(event.getName())) {
                    return EffectOnCache.RESET;
                }
                return EffectOnCache.NONE;
            }
        };
    }

    /**
     * Returns the value of the JavaScript property {@code applets}.
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms537436.aspx">
     * MSDN documentation</a>
     * @see <a href="https://developer.mozilla.org/En/DOM:document.applets">
     * Gecko DOM reference</a>
     * @return the value of this property
     */
    @JsxGetter({CHROME, IE})
    public Object getApplets() {
        return new HTMLCollection(getDomNodeOrDie(), false) {
            @Override
            protected boolean isMatching(final DomNode node) {
                return false;
            }
        };
    }

    /**
     * Returns this document's {@code body} element.
     * @return this document's {@code body} element
     */
    @JsxGetter({CHROME, IE, EDGE})
    @CanSetReadOnly(CanSetReadOnlyStatus.EXCEPTION)
    public HTMLElement getBody() {
        final Page page = getPage();
        if (page instanceof HtmlPage) {
            final HtmlElement body = ((HtmlPage) page).getBody();
            if (body != null) {
                return (HTMLElement) body.getScriptableObject();
            }
        }
        return null;
    }

    /**
     * JavaScript function {@code close}.
     *
     * See http://www.whatwg.org/specs/web-apps/current-work/multipage/section-dynamic.html for
     * a good description of the semantics of open(), write(), writeln() and close().
     *
     * @throws IOException if an IO problem occurs
     */
    @JsxFunction({CHROME, IE})
    public void close() throws IOException {
    }

    /**
     * Returns the {@code compatMode} property.
     * @return the {@code compatMode} property
     */
    @JsxGetter
    public String getCompatMode() {
        // initialize the modes
        getDocumentMode();
        return compatMode_;
    }

    /**
     * Returns the {@code documentMode} property.
     * @return the {@code documentMode} property
     */
    @JsxGetter(IE)
    public int getDocumentMode() {
        if (documentMode_ != -1) {
            return documentMode_;
        }

        compatMode_ = "CSS1Compat";

        if (isQuirksDocType()) {
            compatMode_ = "BackCompat";
        }

        final float version = getBrowserVersion().getBrowserVersionNumeric();
        documentMode_ = (int) Math.floor(version);
        return documentMode_;
    }

    private boolean isQuirksDocType() {
        final DocumentType docType = getPage().getDoctype();
        if (docType != null) {
            final String systemId = docType.getSystemId();
            if (systemId != null) {
                if ("http://www.w3.org/TR/html4/strict.dtd".equals(systemId)) {
                    return false;
                }

                if ("http://www.w3.org/TR/html4/loose.dtd".equals(systemId)) {
                    final String publicId = docType.getPublicId();
                    if ("-//W3C//DTD HTML 4.01 Transitional//EN".equals(publicId)) {
                        return false;
                    }
                }

                if ("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd".equals(systemId)
                    || "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd".equals(systemId)) {
                    return false;
                }
            }
            else if (docType.getPublicId() == null) {
                if (docType.getName() != null) {
                    return false;
                }
                return true;
            }
        }
        return true;
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br>
     *
     * Called from the HTMLParser if a 'X-UA-Compatible' meta tag found.
     * @param documentMode the mode forced by the meta tag
     */
    public void forceDocumentMode(final int documentMode) {
        documentMode_ = documentMode;
        compatMode_ = documentMode == 5 ? "BackCompat" : "CSS1Compat";
    }

    /**
     * Returns the first element within the document that matches the specified group of selectors.
     * @param selectors the selectors
     * @return null if no matches are found; otherwise, it returns the first matching element
     */
    @JsxFunction
    public Node querySelector(final String selectors) {
        try {
            final DomNode node = getDomNodeOrDie().querySelector(selectors);
            if (node != null) {
                return (Node) node.getScriptableObject();
            }
            return null;
        }
        catch (final CSSException e) {
            throw Context.reportRuntimeError("An invalid or illegal selector was specified (selector: '"
                    + selectors + "' error: " + e.getMessage() + ").");
        }
    }

    /**
     * Retrieves all element nodes from descendants of the starting element node that match any selector
     * within the supplied selector strings.
     * The NodeList object returned by the querySelectorAll() method must be static, not live.
     * @param selectors the selectors
     * @return the static node list
     */
    @JsxFunction
    public NodeList querySelectorAll(final String selectors) {
        try {
            return NodeList.staticNodeList(this, getDomNodeOrDie().querySelectorAll(selectors));
        }
        catch (final CSSException e) {
            throw Context.reportRuntimeError("An invalid or illegal selector was specified (selector: '"
                    + selectors + "' error: " + e.getMessage() + ").");
        }
    }

    /**
     * Indicates if the command is supported.
     * @see <a href="http://msdn2.microsoft.com/en-us/library/ms536681.aspx">MSDN documentation</a>
     * @param cmd the command identifier
     * @return {@code true} if the command is supported
     */
    @JsxFunction({CHROME, IE})
    public boolean queryCommandSupported(final String cmd) {
        return hasCommand(cmd, true);
    }

    private boolean hasCommand(final String cmd, final boolean includeBold) {
        if (null == cmd) {
            return false;
        }

        final String cmdLC = cmd.toLowerCase(Locale.ROOT);
        if (getBrowserVersion().isIE()) {
            return EXECUTE_CMDS_IE.contains(cmdLC);
        }
        if (getBrowserVersion().isChrome()) {
            return EXECUTE_CMDS_CHROME.contains(cmdLC) || (includeBold && "bold".equalsIgnoreCase(cmd));
        }
        return EXECUTE_CMDS_FF.contains(cmdLC);
    }

    /**
     * Indicates if the command can be successfully executed using <tt>execCommand</tt>, given
     * the current state of the document.
     * @param cmd the command identifier
     * @return {@code true} if the command can be successfully executed
     */
    @JsxFunction({CHROME, IE})
    public boolean queryCommandEnabled(final String cmd) {
        return hasCommand(cmd, true);
    }

    /**
     * Executes a command.
     * @see <a href="http://msdn2.microsoft.com/en-us/library/ms536419.aspx">MSDN documentation</a>
     * @param cmd the command identifier
     * @param userInterface display a user interface if the command supports one
     * @param value the string, number, or other value to assign (possible values depend on the command)
     * @return {@code true} if the command was successful, {@code false} otherwise
     */
    @JsxFunction({CHROME, IE})
    public boolean execCommand(final String cmd, final boolean userInterface, final Object value) {
        if (!hasCommand(cmd, false)) {
            return false;
        }
        LOG.warn("Nothing done for execCommand(" + cmd + ", ...) (feature not implemented)");
        return true;
    }

    /**
     * Retrieves an auto-generated, unique identifier for the object.
     * <b>Note</b> The unique ID generated is not guaranteed to be the same every time the page is loaded.
     * @return an auto-generated, unique identifier for the object
     */
    @JsxGetter(IE)
    public String getUniqueID() {
        if (uniqueID_ == null) {
            uniqueID_ = "ms__id" + UniqueID_Counter_++;
        }
        return uniqueID_;
    }

    /**
     * Returns the value of the {@code URL} property.
     * @return the value of the {@code URL} property
     */
    @JsxGetter(propertyName = "URL")
    public String getURL() {
        if (!(getPage() instanceof HtmlPage)) {
            // TODO: implement XmlPage.getUrl
            return "";
        }
        return getPage().getUrl().toExternalForm();
    }

    /**
     * Returns the value of the {@code documentURI} property.
     * @return the value of the {@code documentURI} property
     */
    @JsxGetter({CHROME, FF})
    public String getDocumentURI() {
        return getURL();
    }

    /**
     * Returns the value of the {@code URLUnencoded} property.
     * @return the value of the {@code URLUnencoded} property
     */
    @JsxGetter(value = IE, propertyName = "URLUnencoded")
    public String getURLUnencoded() {
        return getURL();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(final String name, final Scriptable start) {
        final Object response = super.get(name, start);

        // IE support .querySelector(All) but not in quirks mode
        // => TODO: find a better way to handle this!
        if (response instanceof FunctionObject
            && ("querySelectorAll".equals(name) || "querySelector".equals(name))
            && getBrowserVersion().hasFeature(QUERYSELECTORALL_NOT_IN_QUIRKS)) {
            Document document = null;
            if (start instanceof DocumentProxy) {
                // if in prototype no domNode is set -> use start
                document = ((DocumentProxy) start).getDelegee();
            }
            else if (start instanceof HTMLDocument) {
                final DomNode page = ((HTMLDocument) start).getDomNodeOrNull();
                if (page != null) {
                    document = (Document) page.getScriptableObject();
                }
            }
            if (document instanceof HTMLDocument && ((HTMLDocument) document).getDocumentMode() < 8) {
                return NOT_FOUND;
            }
        }

        return response;
    }

    /**
     * Returns the {@code cookie} property.
     * @return the {@code cookie} property
     */
    @JsxGetter({CHROME, IE})
    public String getCookie() {
        return "";
    }

    /**
     * Implementation of the {@link org.w3c.dom.events.DocumentEvent} interface's
     * {@link org.w3c.dom.events.DocumentEvent#createEvent(String)} method. The method creates an
     * uninitialized event of the specified type.
     *
     * @see <a href="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-DocumentEvent">DocumentEvent</a>
     * @param eventType the event type to create
     * @return an event object for the specified type
     * @throws DOMException if the event type is not supported (will have a type of
     *         DOMException.NOT_SUPPORTED_ERR)
     */
    @JsxFunction
    public Event createEvent(final String eventType) throws DOMException {
        Class<? extends Event> clazz = null;
        clazz = SUPPORTED_DOM2_EVENT_TYPE_MAP.get(eventType);
        if (clazz == null) {
            clazz = SUPPORTED_DOM3_EVENT_TYPE_MAP.get(eventType);
            if (CloseEvent.class == clazz
                    && getBrowserVersion().hasFeature(EVENT_ONCLOSE_DOCUMENT_CREATE_NOT_SUPPORTED)) {
                clazz = null;
            }
        }
        if (clazz == null
                && ("Events".equals(eventType)
                || "KeyEvents".equals(eventType) && getBrowserVersion().hasFeature(EVENT_TYPE_KEY_EVENTS)
                || "HashChangeEvent".equals(eventType)
                && getBrowserVersion().hasFeature(EVENT_TYPE_HASHCHANGEEVENT)
                || "BeforeUnloadEvent".equals(eventType)
                && getBrowserVersion().hasFeature(EVENT_TYPE_BEFOREUNLOADEVENT)
                || "PointerEvent".equals(eventType)
                && getBrowserVersion().hasFeature(EVENT_TYPE_POINTEREVENT)
                || "PopStateEvent".equals(eventType)
                || "ProgressEvent".equals(eventType)
                && getBrowserVersion().hasFeature(EVENT_TYPE_PROGRESSEVENT))) {
            clazz = SUPPORTED_VENDOR_EVENT_TYPE_MAP.get(eventType);
        }
        if (clazz == null) {
            Context.throwAsScriptRuntimeEx(new DOMException(DOMException.NOT_SUPPORTED_ERR,
                "Event Type is not supported: " + eventType));
            return null; // to stop eclipse warning
        }
        try {
            final Event event = clazz.newInstance();
            event.setParentScope(getWindow());
            event.setPrototype(getPrototype(clazz));
            event.eventCreated();
            return event;
        }
        catch (final InstantiationException e) {
            throw Context.reportRuntimeError("Failed to instantiate event: class ='" + clazz.getName()
                            + "' for event type of '" + eventType + "': " + e.getMessage());
        }
        catch (final IllegalAccessException e) {
            throw Context.reportRuntimeError("Failed to instantiate event: class ='" + clazz.getName()
                            + "' for event type of '" + eventType + "': " + e.getMessage());
        }
    }

    /**
     * Returns a new NodeIterator object.
     *
     * @param root The root node at which to begin the NodeIterator's traversal.
     * @param whatToShow an optional long representing a bitmask created by combining
     * the constant properties of {@link NodeFilter}
     * @param filter an object implementing the {@link NodeFilter} interface
     * @return a new NodeIterator object
     */
    @JsxFunction
    public NodeIterator createNodeIterator(final Node root, final int whatToShow, final Scriptable filter) {
        final org.w3c.dom.traversal.NodeFilter filterWrapper = createFilterWrapper(filter, false);
        final NodeIterator iterator = new NodeIterator(getPage(), root, whatToShow, filterWrapper);
        iterator.setParentScope(getParentScope());
        iterator.setPrototype(getPrototype(iterator.getClass()));
        return iterator;
    }

    private static org.w3c.dom.traversal.NodeFilter createFilterWrapper(final Scriptable filter,
            final boolean filterFunctionOnly) {
        org.w3c.dom.traversal.NodeFilter filterWrapper = null;
        if (filter != null) {
            filterWrapper = new org.w3c.dom.traversal.NodeFilter() {
                @Override
                public short acceptNode(final org.w3c.dom.Node n) {
                    final Object[] args = new Object[] {((DomNode) n).getScriptableObject()};
                    final Object response;
                    if (filter instanceof Callable) {
                        response = ((Callable) filter).call(Context.getCurrentContext(), filter, filter, args);
                    }
                    else {
                        if (filterFunctionOnly) {
                            throw Context.reportRuntimeError("only a function is allowed as filter");
                        }
                        response = ScriptableObject.callMethod(filter, "acceptNode", args);
                    }
                    return (short) Context.toNumber(response);
                }
            };
        }
        return filterWrapper;
    }

    /**
     * Creates and returns a new TreeWalker. The following JavaScript parameters are passed into this method:
     * <ul>
     *   <li>JavaScript param 1: The root node of the TreeWalker. Must not be {@code null}.</li>
     *   <li>JavaScript param 2: Flag specifying which types of nodes appear in the logical view of the TreeWalker.
     *       See {@link NodeFilter} for the set of possible Show_ values.</li>
     *   <li>JavaScript param 3: The {@link NodeFilter} to be used with this TreeWalker, or {@code null}
     *       to indicate no filter.</li>
     *   <li>JavaScript param 4: If {@code false}, the contents of EntityReference nodes are not present
     *       in the logical view.</li>
     * </ul>
     *
     * @see <a href="http://www.w3.org/TR/DOM-Level-2-Traversal-Range/traversal.html">DOM-Level-2-Traversal-Range</a>
     * @param root the node which will serve as the root for the TreeWalker
     * @param whatToShow specifies which node types may appear in the logical view of the tree presented
     * @param filter the NodeFilter to be used with this TreeWalker, or null to indicate no filter
     * @param expandEntityReferences If false,
     *        the contents of EntityReference nodes are not presented in the logical view
     * @throws DOMException on attempt to create a TreeWalker with a root that is {@code null}
     * @return a new TreeWalker
     */
    @JsxFunction
    public Object createTreeWalker(final Node root, final double whatToShow, final Scriptable filter,
            boolean expandEntityReferences) throws DOMException {

        // seems that Rhino doesn't like long as parameter type
        final int whatToShowI = (int) Double.valueOf(whatToShow).longValue();

        if (getBrowserVersion().hasFeature(JS_TREEWALKER_EXPAND_ENTITY_REFERENCES_FALSE)) {
            expandEntityReferences = false;
        }

        final boolean filterFunctionOnly = getBrowserVersion().hasFeature(JS_TREEWALKER_FILTER_FUNCTION_ONLY);
        final org.w3c.dom.traversal.NodeFilter filterWrapper = createFilterWrapper(filter, filterFunctionOnly);
        final TreeWalker t = new TreeWalker(getPage(), root, whatToShowI, filterWrapper, expandEntityReferences);
        t.setParentScope(getWindow(this));
        t.setPrototype(staticGetPrototype(getWindow(this), TreeWalker.class));
        return t;
    }

    @SuppressWarnings("unchecked")
    private static Scriptable staticGetPrototype(final Window window,
            final Class<? extends SimpleScriptable> javaScriptClass) {
        final Scriptable prototype = window.getPrototype(javaScriptClass);
        if (prototype == null && javaScriptClass != SimpleScriptable.class) {
            return staticGetPrototype(window, (Class<? extends SimpleScriptable>) javaScriptClass.getSuperclass());
        }
        return prototype;
    }

    /**
     * Creates and returns a new range.
     * @return a new range
     * @see <a href="http://www.xulplanet.com/references/objref/HTMLDocument.html#method_createRange">XUL Planet</a>
     */
    @JsxFunction
    public Range createRange() {
        final Range r = new Range(this);
        r.setParentScope(getWindow());
        r.setPrototype(getPrototype(Range.class));
        return r;
    }

    /**
     * Returns the domain name of the server that served the document, or {@code null} if the server
     * cannot be identified by a domain name.
     * @return the domain name of the server that served the document
     * @see <a href="http://www.w3.org/TR/2000/WD-DOM-Level-1-20000929/level-one-html.html#ID-2250147">
     * W3C documentation</a>
     */
    @JsxGetter({CHROME, IE})
    public String getDomain() {
        if (domain_ == null && getPage().getWebResponse() != null) {
            URL url = getPage().getUrl();
            if (url == WebClient.URL_ABOUT_BLANK) {
                final WebWindow w = getWindow().getWebWindow();
                if (w instanceof FrameWindow) {
                    url = ((FrameWindow) w).getEnclosingPage().getUrl();
                }
                else {
                    return null;
                }
            }
            domain_ = url.getHost().toLowerCase(Locale.ROOT);
        }

        return domain_;
    }

    /**
     * Sets the domain of this document.
     *
     * Domains can only be set to suffixes of the existing domain
     * with the exception of setting the domain to itself.
     * <p>
     * The domain will be set according to the following rules:
     * <ol>
     * <li>If the newDomain.equalsIgnoreCase(currentDomain) the method returns with no error.</li>
     * <li>If the browser version is netscape, the newDomain is downshifted.</li>
     * <li>The change will take place if and only if the suffixes of the
     *       current domain and the new domain match AND there are at least
     *       two domain qualifiers e.g. the following transformations are legal
     *       d1.d2.d3.gargoylesoftware.com may be transformed to itself or:
     *          d2.d3.gargoylesoftware.com
     *             d3.gargoylesoftware.com
     *                gargoylesoftware.com
     *
     *        transformation to:        com
     *        will fail
     * </li>
     * </ol>
     * <p>
     * TODO This code could be modified to understand country domain suffixes.
     * The domain www.bbc.co.uk should be trimmable only down to bbc.co.uk
     * trimming to co.uk should not be possible.
     * @param newDomain the new domain to set
     */
    @JsxSetter({CHROME, IE})
    public void setDomain(String newDomain) {
        final BrowserVersion browserVersion = getBrowserVersion();

        // IE (at least 6) doesn't allow to set domain of about:blank
        if (WebClient.URL_ABOUT_BLANK == getPage().getUrl()
            && browserVersion.hasFeature(JS_DOCUMENT_SETTING_DOMAIN_THROWS_FOR_ABOUT_BLANK)) {
            throw Context.reportRuntimeError("Illegal domain value, cannot set domain from \""
                    + WebClient.URL_ABOUT_BLANK + "\" to: \""
                    + newDomain + "\".");
        }

        newDomain = newDomain.toLowerCase(Locale.ROOT);

        final String currentDomain = getDomain();
        if (currentDomain.equalsIgnoreCase(newDomain)) {
            return;
        }

        if (newDomain.indexOf('.') == -1) {
            throw Context.reportRuntimeError("Illegal domain value, cannot set domain from: \""
                    + currentDomain + "\" to: \"" + newDomain + "\" (new domain has to contain a dot).");
        }

        if (currentDomain.indexOf('.') > -1
                && !currentDomain.toLowerCase(Locale.ROOT).endsWith("." + newDomain.toLowerCase(Locale.ROOT))) {
            throw Context.reportRuntimeError("Illegal domain value, cannot set domain from: \""
                    + currentDomain + "\" to: \"" + newDomain + "\"");
        }

        domain_ = newDomain;
    }

    /**
     * Sets the {@code onclick} event handler for this element.
     * @param handler the {@code onclick} event handler for this element
     */
    @JsxSetter
    public void setOnclick(final Object handler) {
        setEventHandler(MouseEvent.TYPE_CLICK, handler);
    }

    /**
     * Returns the {@code onclick} event handler for this element.
     * @return the {@code onclick} event handler for this element
     */
    @JsxGetter
    public Object getOnclick() {
        return getEventHandler(MouseEvent.TYPE_CLICK);
    }

    /**
     * Sets the {@code ondblclick} event handler for this element.
     * @param handler the {@code ondblclick} event handler for this element
     */
    @JsxSetter
    public void setOndblclick(final Object handler) {
        setEventHandler(MouseEvent.TYPE_DBL_CLICK, handler);
    }

    /**
     * Returns the {@code ondblclick} event handler for this element.
     * @return the {@code ondblclick} event handler for this element
     */
    @JsxGetter
    public Object getOndblclick() {
        return getEventHandler(MouseEvent.TYPE_DBL_CLICK);
    }

    /**
     * Sets the {@code onblur} event handler for this element.
     * @param handler the {@code onblur} event handler for this element
     */
    @JsxSetter
    public void setOnblur(final Object handler) {
        setEventHandler(Event.TYPE_BLUR, handler);
    }

    /**
     * Returns the {@code onblur} event handler for this element.
     * @return the {@code onblur} event handler for this element
     */
    @JsxGetter
    public Object getOnblur() {
        return getEventHandler(Event.TYPE_BLUR);
    }

    /**
     * Sets the {@code onfocus} event handler for this element.
     * @param handler the {@code onfocus} event handler for this element
     */
    @JsxSetter
    public void setOnfocus(final Object handler) {
        setEventHandler(Event.TYPE_FOCUS, handler);
    }

    /**
     * Returns the {@code onfocus} event handler for this element.
     * @return the {@code onfocus} event handler for this element
     */
    @JsxGetter
    public Object getOnfocus() {
        return getEventHandler(Event.TYPE_FOCUS);
    }

    /**
     * Sets the {@code onfocusin} event handler for this element.
     * @param handler the {@code onfocusin} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnfocusin(final Object handler) {
        setEventHandler(Event.TYPE_FOCUS_IN, handler);
    }

    /**
     * Returns the {@code onfocusin} event handler for this element.
     * @return the {@code onfocusin} event handler for this element
     */
    @JsxGetter(IE)
    public Object getOnfocusin() {
        return getEventHandler(Event.TYPE_FOCUS_IN);
    }

    /**
     * Sets the {@code onfocusout} event handler for this element.
     * @param handler the {@code onfocusout} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnfocusout(final Object handler) {
        setEventHandler(Event.TYPE_FOCUS_OUT, handler);
    }

    /**
     * Returns the {@code onfocusout} event handler for this element.
     * @return the {@code onfocusout} event handler for this element
     */
    @JsxGetter(IE)
    public Object getOnfocusout() {
        return getEventHandler(Event.TYPE_FOCUS_OUT);
    }

    /**
     * Sets the {@code onkeydown} event handler for this element.
     * @param handler the {@code onkeydown} event handler for this element
     */
    @JsxSetter
    public void setOnkeydown(final Object handler) {
        setEventHandler(Event.TYPE_KEY_DOWN, handler);
    }

    /**
     * Returns the {@code onkeydown} event handler for this element.
     * @return the {@code onkeydown} event handler for this element
     */
    @JsxGetter
    public Object getOnkeydown() {
        return getEventHandler(Event.TYPE_KEY_DOWN);
    }

    /**
     * Sets the {@code onkeypress} event handler for this element.
     * @param handler the {@code onkeypress} event handler for this element
     */
    @JsxSetter
    public void setOnkeypress(final Object handler) {
        setEventHandler(Event.TYPE_KEY_PRESS, handler);
    }

    /**
     * Returns the {@code onkeypress} event handler for this element.
     * @return the {@code onkeypress} event handler for this element
     */
    @JsxGetter
    public Object getOnkeypress() {
        return getEventHandler(Event.TYPE_KEY_PRESS);
    }

    /**
     * Sets the {@code onkeyup} event handler for this element.
     * @param handler the {@code onkeyup} event handler for this element
     */
    @JsxSetter
    public void setOnkeyup(final Object handler) {
        setEventHandler(Event.TYPE_KEY_UP, handler);
    }

    /**
     * Returns the {@code onkeyup} event handler for this element.
     * @return the {@code onkeyup} event handler for this element
     */
    @JsxGetter
    public Object getOnkeyup() {
        return getEventHandler(Event.TYPE_KEY_UP);
    }

    /**
     * Sets the {@code onmousedown} event handler for this element.
     * @param handler the {@code onmousedown} event handler for this element
     */
    @JsxSetter
    public void setOnmousedown(final Object handler) {
        setEventHandler(MouseEvent.TYPE_MOUSE_DOWN, handler);
    }

    /**
     * Returns the {@code onmousedown} event handler for this element.
     * @return the {@code onmousedown} event handler for this element
     */
    @JsxGetter
    public Object getOnmousedown() {
        return getEventHandler(MouseEvent.TYPE_MOUSE_DOWN);
    }

    /**
     * Sets the {@code onmousemove} event handler for this element.
     * @param handler the {@code onmousemove} event handler for this element
     */
    @JsxSetter
    public void setOnmousemove(final Object handler) {
        setEventHandler(MouseEvent.TYPE_MOUSE_MOVE, handler);
    }

    /**
     * Returns the {@code onmousemove} event handler for this element.
     * @return the {@code onmousemove} event handler for this element
     */
    @JsxGetter
    public Object getOnmousemove() {
        return getEventHandler(MouseEvent.TYPE_MOUSE_MOVE);
    }

    /**
     * Sets the {@code onmouseout} event handler for this element.
     * @param handler the {@code onmouseout} event handler for this element
     */
    @JsxSetter
    public void setOnmouseout(final Object handler) {
        setEventHandler(MouseEvent.TYPE_MOUSE_OUT, handler);
    }

    /**
     * Returns the {@code onmouseout} event handler for this element.
     * @return the {@code onmouseout} event handler for this element
     */
    @JsxGetter
    public Object getOnmouseout() {
        return getEventHandler(MouseEvent.TYPE_MOUSE_OUT);
    }

    /**
     * Sets the {@code onmouseover} event handler for this element.
     * @param handler the {@code onmouseover} event handler for this element
     */
    @JsxSetter
    public void setOnmouseover(final Object handler) {
        setEventHandler(MouseEvent.TYPE_MOUSE_OVER, handler);
    }

    /**
     * Returns the {@code onmouseover} event handler for this element.
     * @return the {@code onmouseover} event handler for this element
     */
    @JsxGetter
    public Object getOnmouseover() {
        return getEventHandler(MouseEvent.TYPE_MOUSE_OVER);
    }

    /**
     * Sets the {@code onmouseup} event handler for this element.
     * @param handler the {@code onmouseup} event handler for this element
     */
    @JsxSetter
    public void setOnmouseup(final Object handler) {
        setEventHandler(MouseEvent.TYPE_MOUSE_UP, handler);
    }

    /**
     * Returns the {@code onmouseup} event handler for this element.
     * @return the {@code onmouseup} event handler for this element
     */
    @JsxGetter
    public Object getOnmouseup() {
        return getEventHandler(MouseEvent.TYPE_MOUSE_UP);
    }

    /**
     * Sets the {@code oncontextmenu} event handler for this element.
     * @param handler the {@code oncontextmenu} event handler for this element
     */
    @JsxSetter
    public void setOncontextmenu(final Object handler) {
        setEventHandler(MouseEvent.TYPE_CONTEXT_MENU, handler);
    }

    /**
     * Returns the {@code oncontextmenu} event handler for this element.
     * @return the {@code oncontextmenu} event handler for this element
     */
    @JsxGetter
    public Object getOncontextmenu() {
        return getEventHandler(MouseEvent.TYPE_CONTEXT_MENU);
    }

    /**
     * Sets the {@code onresize} event handler for this element.
     * @param handler the {@code onresize} event handler for this element
     */
    @JsxSetter({CHROME, FF})
    public void setOnresize(final Object handler) {
        setEventHandler("resize", handler);
    }

    /**
     * Returns the {@code onresize} event handler for this element.
     * @return the {@code onresize} event handler for this element
     */
    @JsxGetter({CHROME, FF})
    public Object getOnresize() {
        return getEventHandler("resize");
    }

    /**
     * Sets the {@code onerror} event handler for this element.
     * @param handler the {@code onerror} event handler for this element
     */
    @JsxSetter
    public void setOnerror(final Object handler) {
        setEventHandler(Event.TYPE_ERROR, handler);
    }

    /**
     * Returns the {@code onerror} event handler for this element.
     * @return the {@code onerror} event handler for this element
     */
    @JsxGetter
    public Object getOnerror() {
        return getEventHandler(Event.TYPE_ERROR);
    }

    /**
     * Returns the {@code oninput} event handler for this element.
     * @return the {@code oninput} event handler for this element
     */
    @JsxGetter
    public Function getOninput() {
        return getEventHandler(Event.TYPE_INPUT);
    }

    /**
     * Sets the {@code oninput} event handler for this element.
     * @param oninput the {@code oninput} event handler for this element
     */
    @JsxSetter
    public void setOninput(final Object oninput) {
        setEventHandler(Event.TYPE_INPUT, oninput);
    }

    /**
     * Returns the {@code hidden} property.
     * @return the {@code hidden} property
     */
    @JsxGetter
    public boolean getHidden() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter({CHROME, FF})
    public int getChildElementCount() {
        int counter = 0;
        if (getPage().getDocumentElement() != null) {
            counter++;
        }
        return counter;
    }

    /**
     * Returns the element for the specified x coordinate and the specified y coordinate.
     * The current implementation always returns the &lt;body&gt; element.
     *
     * @param x the x offset, in pixels
     * @param y the y offset, in pixels
     * @return the element for the specified x coordinate and the specified y coordinate
     */
    @JsxFunction
    public Object elementFromPoint(final int x, final int y) {
        return null;
    }

    /**
     * Returns the value of the {@code forms} property.
     * @return the value of the {@code forms} property
     */
    @JsxGetter({CHROME, IE})
    public Object getForms() {
        return new HTMLCollection(getDomNodeOrDie(), false) {
            @Override
            protected boolean isMatching(final DomNode node) {
                return node instanceof HtmlForm && node.getPrefix() == null;
            }

            @Override
            public Object call(final Context cx, final Scriptable scope,
                    final Scriptable thisObj, final Object[] args) {
                if (getBrowserVersion().hasFeature(JS_DOCUMENT_FORMS_FUNCTION_SUPPORTED)) {
                    return super.call(cx, scope, thisObj, args);
                }
                throw Context.reportRuntimeError("TypeError: document.forms is not a function");
            }
        };
    }

    /**
     * Returns the value of the {@code embeds} property.
     * @return the value of the {@code embeds} property
     */
    @JsxGetter({CHROME, IE})
    public Object getEmbeds() {
        return new HTMLCollection(getDomNodeOrDie(), false) {
            @Override
            protected boolean isMatching(final DomNode node) {
                return node instanceof HtmlEmbed;
            }

            @Override
            public Object call(final Context cx, final Scriptable scope,
                    final Scriptable thisObj, final Object[] args) {
                if (getBrowserVersion().hasFeature(JS_DOCUMENT_FORMS_FUNCTION_SUPPORTED)) {
                    return super.call(cx, scope, thisObj, args);
                }
                throw Context.reportRuntimeError("TypeError: document.embeds is not a function");
            }
        };
    }

    /**
     * Returns the value of the {@code embeds} property.
     * @return the value of the {@code embeds} property
     */
    @JsxGetter({CHROME, IE})
    public Object getImages() {
        return new HTMLCollection(getDomNodeOrDie(), false) {
            @Override
            protected boolean isMatching(final DomNode node) {
                return node instanceof HtmlImage;
            }

            @Override
            public Object call(final Context cx, final Scriptable scope,
                    final Scriptable thisObj, final Object[] args) {
                if (getBrowserVersion().hasFeature(JS_DOCUMENT_FORMS_FUNCTION_SUPPORTED)) {
                    return super.call(cx, scope, thisObj, args);
                }
                throw Context.reportRuntimeError("TypeError: document.images is not a function");
            }
        };
    }

    /**
     * Returns the value of the {@code scripts} property.
     * @return the value of the {@code scripts} property
     */
    @JsxGetter({CHROME, IE})
    public Object getScripts() {
        return new HTMLCollection(getDomNodeOrDie(), false) {
            @Override
            protected boolean isMatching(final DomNode node) {
                return node instanceof HtmlScript;
            }

            @Override
            public Object call(final Context cx, final Scriptable scope,
                    final Scriptable thisObj, final Object[] args) {
                if (getBrowserVersion().hasFeature(JS_DOCUMENT_FORMS_FUNCTION_SUPPORTED)) {
                    return super.call(cx, scope, thisObj, args);
                }
                throw Context.reportRuntimeError("TypeError: document.scripts is not a function");
            }
        };
    }

    /**
     * Retrieves a collection of stylesheet objects representing the style sheets that correspond
     * to each instance of a Link or
     * {@link com.gargoylesoftware.htmlunit.javascript.host.css.CSSStyleDeclaration} object in the document.
     *
     * @return styleSheet collection
     */
    @JsxGetter
    public StyleSheetList getStyleSheets() {
        if (styleSheetList_ == null) {
            styleSheetList_ = new StyleSheetList(this);
        }
        return styleSheetList_;
    }

    /**
     * Returns the value of the {@code plugins} property.
     * @return the value of the {@code plugins} property
     */
    @JsxGetter({CHROME, IE})
    public Object getPlugins() {
        return getEmbeds();
    }

    /**
     * Returns the value of the JavaScript property {@code links}. Refer also to the
     * <a href="http://msdn.microsoft.com/en-us/library/ms537465.aspx">MSDN documentation</a>.
     * @return the value of this property
     */
    @JsxGetter({CHROME, IE})
    public Object getLinks() {
        return new HTMLCollection(getDomNodeOrDie(), true) {
            @Override
            protected boolean isMatching(final DomNode node) {
                return (node instanceof HtmlAnchor || node instanceof HtmlArea)
                        && ((HtmlElement) node).hasAttribute("href");
            }

            @Override
            protected EffectOnCache getEffectOnCache(final HtmlAttributeChangeEvent event) {
                final HtmlElement node = event.getHtmlElement();
                if ((node instanceof HtmlAnchor || node instanceof HtmlArea) && "href".equals(event.getName())) {
                    return EffectOnCache.RESET;
                }
                return EffectOnCache.NONE;
            }
        };
    }

    /**
     * Returns all the descendant elements with the specified class name.
     * @param className the name to search for
     * @return all the descendant elements with the specified class name
     * @see <a href="https://developer.mozilla.org/en/DOM/document.getElementsByClassName">Mozilla doc</a>
     */
    @JsxFunction
    public HTMLCollection getElementsByClassName(final String className) {
        return null;
    }

    /**
     * Returns all HTML elements that have a {@code name} attribute with the specified value.
     *
     * Refer to <a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-71555259">
     * The DOM spec</a> for details.
     *
     * @param elementName - value of the {@code name} attribute to look for
     * @return all HTML elements that have a {@code name} attribute with the specified value
     */
    @JsxFunction({CHROME, IE})
    public HTMLCollection getElementsByName(final String elementName) {
        return null;
    }

    /**
     * Returns {@code false} if the active element in the document has no focus;
     * {@code true} if the active element in the document has focus.
     * @return whether the active element in the document has focus or not
     */
    @JsxFunction
    public boolean hasFocus() {
        return false;
    }

    /**
     * Returns this document's title.
     * @return this document's title
     */
    @JsxGetter
    public String getTitle() {
        return "";
    }

    /**
     * Sets this document's title.
     * @param title the new title
     */
    @JsxSetter
    public void setTitle(final String title) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter({CHROME, FF, EDGE})
    public HTMLCollection getChildren() {
        return super.getChildren();
    }

    /**
     * Returns the {@code contentType} property.
     * @return the {@code contentType} property
     */
    @JsxGetter({CHROME, FF})
    public String getContentType() {
        return getPage().getContentType();
    }

    /**
     * Returns the current selection.
     * @return the current selection
     */
    @JsxFunction(CHROME)
    public Selection getSelection() {
        return null;
    }

    /**
     * Returns this document's {@code head} element.
     * @return this document's {@code head} element
     */
    @JsxGetter({CHROME, IE})
    public Object getHead() {
        return null;
    }

    /**
     * Returns a string representing the encoding under which the document was parsed.
     * @return a string representing the encoding under which the document was parsed
     */
    @JsxGetter
    public String getInputEncoding() {
        final Charset encoding = getPage().getCharset();
        if (getBrowserVersion().hasFeature(HTMLDOCUMENT_CHARSET_LOWERCASE)) {
            return encoding.name();
        }
        return EncodingSniffer.translateEncodingLabel(encoding);
    }

    /**
     * Returns the last modification date of the document.
     * @see <a href="https://developer.mozilla.org/en/DOM/document.lastModified">Mozilla documentation</a>
     * @return the date as string
     */
    @JsxGetter
    public String getLastModified() {
        if (lastModified_ == null) {
            final WebResponse webResponse = getPage().getWebResponse();
            final Date lastModified;
            if (webResponse != null) {
                String stringDate = webResponse.getResponseHeaderValue("Last-Modified");
                if (stringDate == null) {
                    stringDate = webResponse.getResponseHeaderValue("Date");
                }
                lastModified = parseDateOrNow(stringDate);
            }
            else {
                lastModified = new Date();
            }
            lastModified_ = new SimpleDateFormat(LAST_MODIFIED_DATE_FORMAT, Locale.ROOT).format(lastModified);
        }
        return lastModified_;
    }

    private static Date parseDateOrNow(final String stringDate) {
        final Date date = parseHttpDate(stringDate);
        if (date == null) {
            return new Date();
        }
        return date;
    }

    /**
     * Mock for the moment.
     * @return true for success
     */
    @JsxFunction({FF, IE})
    public boolean releaseCapture() {
        return true;
    }

    /**
     * Returns the ready state of the document.
     * @return the ready state of the document
     *
     * @see DomNode#READY_STATE_UNINITIALIZED
     * @see DomNode#READY_STATE_LOADING
     * @see DomNode#READY_STATE_LOADED
     * @see DomNode#READY_STATE_INTERACTIVE
     * @see DomNode#READY_STATE_COMPLETE
     */
    @JsxGetter
    public String getReadyState() {
        return getDomNodeOrDie().getReadyState();
    }

    /**
     * Does nothing special anymore.
     *
     * @param type the type of events to capture
     * @see Window#captureEvents(String)
     */
    @JsxFunction(IE)
    public void captureEvents(final String type) {
        // Empty.
    }

    /**
     * Does nothing special anymore.
     *
     * @param type the type of events to capture
     * @see Window#releaseEvents(String)
     */
    @JsxFunction(IE)
    public void releaseEvents(final String type) {
        // Empty.
    }

    /**
     * Returns the value of the {@code alinkColor} property.
     * @return the value of the {@code alinkColor} property
     */
    @JsxGetter(IE)
    public String getAlinkColor() {
        return "#0000ff";
    }

    /**
     * Sets the value of the {@code alinkColor} property.
     * @param color the value of the {@code alinkColor} property
     */
    @JsxSetter(IE)
    public void setAlinkColor(final String color) {
    }

    /**
     * Returns the value of the {@code bgColor} property.
     * @return the value of the {@code bgColor} property
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms533505.aspx">MSDN Documentation</a>
     */
    @JsxGetter(IE)
    public String getBgColor() {
        return "#ffffff";
    }

    /**
     * Sets the value of the {@code bgColor} property.
     * @param color the value of the {@code bgColor} property
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms533505.aspx">MSDN Documentation</a>
     */
    @JsxSetter(IE)
    public void setBgColor(final String color) {
    }

    /**
     * Returns the value of the {@code fgColor} property.
     * @return the value of the {@code fgColor} property
     */
    @JsxGetter(IE)
    public String getFgColor() {
        return "#000000";
    }

    /**
     * Sets the value of the {@code fgColor} property.
     * @param color the value of the {@code fgColor} property
     */
    @JsxSetter(IE)
    public void setFgColor(final String color) {
    }

    /**
     * Returns the value of the {@code linkColor} property.
     * @return the value of the {@code linkColor} property
     */
    @JsxGetter(IE)
    public String getLinkColor() {
        return "#0000ff";
    }

    /**
     * Sets the value of the {@code linkColor} property.
     * @param color the value of the {@code linkColor} property
     */
    @JsxSetter(IE)
    public void setLinkColor(final String color) {
    }

    /**
     * Returns the value of the {@code vlinkColor} property.
     * @return the value of the {@code vlinkColor} property
     */
    @JsxGetter(IE)
    public String getVlinkColor() {
        return "#800080";
    }

    /**
     * Sets the value of the {@code vlinkColor} property.
     * @param color the value of the {@code vlinkColor} property
     */
    @JsxSetter(IE)
    public void setVlinkColor(final String color) {
    }

    /**
     * Returns the value of the {@code frames} property.
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms537459.aspx">MSDN documentation</a>
     * @return the live collection of frames contained by this document
     */
    @JsxGetter(IE)
    public Object getFrames() {
        if (ScriptableObject.getTopLevelScope(this) == null) {
            throw ScriptRuntime.constructError("Error", "Not implemented");
        }
        return getWindow().getFrames_js();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter({CHROME, FF})
    public Element getLastElementChild() {
        return super.getLastElementChild();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxGetter({CHROME, FF})
    public Element getFirstElementChild() {
        return super.getFirstElementChild();
    }

    /**
     * Returns the {@code xmlEncoding} property.
     * @return the {@code xmlEncoding} property
     */
    @JsxGetter({CHROME, IE})
    public String getXmlEncoding() {
        String encoding = getPage().getXmlEncoding();
        if (encoding == null && getBrowserVersion().hasFeature(HTMLDOCUMENT_CHARSET_LOWERCASE)) {
            encoding = "";
        }
        return encoding;
    }

    /**
     * Returns the {@code xmlStandalone} property.
     * @return the {@code xmlStandalone} property
     */
    @JsxGetter({CHROME, IE})
    public boolean isXmlStandalone() {
        return getPage().getXmlStandalone();
    }

    /**
     * Returns the {@code xmlVersion} property.
     * @return the {@code xmlVersion} property
     */
    @JsxGetter({CHROME, IE})
    public String getXmlVersion() {
        String version = getPage().getXmlVersion();
        if (version == null && getBrowserVersion().hasFeature(HTMLDOCUMENT_CHARSET_LOWERCASE)) {
            version = "";
        }
        return version;
    }

    /**
     * Returns the {@code onabort} event handler for this element.
     * @return the {@code onabort} event handler for this element
     */
    @JsxGetter
    public Function getOnabort() {
        return getEventHandler("abort");
    }

    /**
     * Sets the {@code onabort} event handler for this element.
     * @param onabort the {@code onabort} event handler for this element
     */
    @JsxSetter
    public void setOnabort(final Object onabort) {
        setEventHandler("abort", onabort);
    }

    /**
     * Returns the {@code onauxclick} event handler for this element.
     * @return the {@code onauxclick} event handler for this element
     */
    @JsxGetter(CHROME)
    public Function getOnauxclick() {
        return getEventHandler("auxclick");
    }

    /**
     * Sets the {@code onauxclick} event handler for this element.
     * @param onauxclick the {@code onauxclick} event handler for this element
     */
    @JsxSetter(CHROME)
    public void setOnauxclick(final Object onauxclick) {
        setEventHandler("auxclick", onauxclick);
    }

    /**
     * Returns the {@code onbeforecopy} event handler for this element.
     * @return the {@code onbeforecopy} event handler for this element
     */
    @JsxGetter(CHROME)
    public Function getOnbeforecopy() {
        return getEventHandler("beforecopy");
    }

    /**
     * Sets the {@code onbeforecopy} event handler for this element.
     * @param onbeforecopy the {@code onbeforecopy} event handler for this element
     */
    @JsxSetter(CHROME)
    public void setOnbeforecopy(final Object onbeforecopy) {
        setEventHandler("beforecopy", onbeforecopy);
    }

    /**
     * Returns the {@code onbeforecut} event handler for this element.
     * @return the {@code onbeforecut} event handler for this element
     */
    @JsxGetter(CHROME)
    public Function getOnbeforecut() {
        return getEventHandler("beforecut");
    }

    /**
     * Sets the {@code onbeforecut} event handler for this element.
     * @param onbeforecut the {@code onbeforecut} event handler for this element
     */
    @JsxSetter(CHROME)
    public void setOnbeforecut(final Object onbeforecut) {
        setEventHandler("beforecut", onbeforecut);
    }

    /**
     * Returns the {@code onbeforepaste} event handler for this element.
     * @return the {@code onbeforepaste} event handler for this element
     */
    @JsxGetter(CHROME)
    public Function getOnbeforepaste() {
        return getEventHandler("beforepaste");
    }

    /**
     * Sets the {@code onbeforepaste} event handler for this element.
     * @param onbeforepaste the {@code onbeforepaste} event handler for this element
     */
    @JsxSetter(CHROME)
    public void setOnbeforepaste(final Object onbeforepaste) {
        setEventHandler("beforepaste", onbeforepaste);
    }

    /**
     * Returns the {@code oncancel} event handler for this element.
     * @return the {@code oncancel} event handler for this element
     */
    @JsxGetter(CHROME)
    public Function getOncancel() {
        return getEventHandler("cancel");
    }

    /**
     * Sets the {@code oncancel} event handler for this element.
     * @param oncancel the {@code oncancel} event handler for this element
     */
    @JsxSetter(CHROME)
    public void setOncancel(final Object oncancel) {
        setEventHandler("cancel", oncancel);
    }

    /**
     * Returns the {@code oncanplay} event handler for this element.
     * @return the {@code oncanplay} event handler for this element
     */
    @JsxGetter
    public Function getOncanplay() {
        return getEventHandler("canplay");
    }

    /**
     * Sets the {@code oncanplay} event handler for this element.
     * @param oncanplay the {@code oncanplay} event handler for this element
     */
    @JsxSetter
    public void setOncanplay(final Object oncanplay) {
        setEventHandler("canplay", oncanplay);
    }

    /**
     * Returns the {@code oncanplaythrough} event handler for this element.
     * @return the {@code oncanplaythrough} event handler for this element
     */
    @JsxGetter
    public Function getOncanplaythrough() {
        return getEventHandler("canplaythrough");
    }

    /**
     * Sets the {@code oncanplaythrough} event handler for this element.
     * @param oncanplaythrough the {@code oncanplaythrough} event handler for this element
     */
    @JsxSetter
    public void setOncanplaythrough(final Object oncanplaythrough) {
        setEventHandler("canplaythrough", oncanplaythrough);
    }

    /**
     * Returns the {@code onchange} event handler for this element.
     * @return the {@code onchange} event handler for this element
     */
    @JsxGetter
    public Function getOnchange() {
        return getEventHandler(Event.TYPE_CHANGE);
    }

    /**
     * Sets the {@code onchange} event handler for this element.
     * @param onchange the {@code onchange} event handler for this element
     */
    @JsxSetter
    public void setOnchange(final Object onchange) {
        setEventHandler(Event.TYPE_CHANGE, onchange);
    }

    /**
     * Returns the {@code onclose} event handler for this element.
     * @return the {@code onclose} event handler for this element
     */
    @JsxGetter(CHROME)
    public Function getOnclose() {
        return getEventHandler(Event.TYPE_CLOSE);
    }

    /**
     * Sets the {@code onclose} event handler for this element.
     * @param onclose the {@code onclose} event handler for this element
     */
    @JsxSetter(CHROME)
    public void setOnclose(final Object onclose) {
        setEventHandler(Event.TYPE_CLOSE, onclose);
    }

    /**
     * Returns the {@code oncopy} event handler for this element.
     * @return the {@code oncopy} event handler for this element
     */
    @JsxGetter({CHROME, FF})
    public Function getOncopy() {
        return getEventHandler("copy");
    }

    /**
     * Sets the {@code oncopy} event handler for this element.
     * @param oncopy the {@code oncopy} event handler for this element
     */
    @JsxSetter({CHROME, FF})
    public void setOncopy(final Object oncopy) {
        setEventHandler("copy", oncopy);
    }

    /**
     * Returns the {@code oncuechange} event handler for this element.
     * @return the {@code oncuechange} event handler for this element
     */
    @JsxGetter(CHROME)
    public Function getOncuechange() {
        return getEventHandler("cuechange");
    }

    /**
     * Sets the {@code oncuechange} event handler for this element.
     * @param oncuechange the {@code oncuechange} event handler for this element
     */
    @JsxSetter(CHROME)
    public void setOncuechange(final Object oncuechange) {
        setEventHandler("cuechange", oncuechange);
    }

    /**
     * Returns the {@code oncut} event handler for this element.
     * @return the {@code oncut} event handler for this element
     */
    @JsxGetter({CHROME, FF})
    public Function getOncut() {
        return getEventHandler("cut");
    }

    /**
     * Sets the {@code oncut} event handler for this element.
     * @param oncut the {@code oncut} event handler for this element
     */
    @JsxSetter({CHROME, FF})
    public void setOncut(final Object oncut) {
        setEventHandler("cut", oncut);
    }

    /**
     * Returns the {@code ondrag} event handler for this element.
     * @return the {@code ondrag} event handler for this element
     */
    @JsxGetter
    public Function getOndrag() {
        return getEventHandler("drag");
    }

    /**
     * Sets the {@code ondrag} event handler for this element.
     * @param ondrag the {@code ondrag} event handler for this element
     */
    @JsxSetter
    public void setOndrag(final Object ondrag) {
        setEventHandler("drag", ondrag);
    }

    /**
     * Returns the {@code ondragend} event handler for this element.
     * @return the {@code ondragend} event handler for this element
     */
    @JsxGetter
    public Function getOndragend() {
        return getEventHandler("dragend");
    }

    /**
     * Sets the {@code ondragend} event handler for this element.
     * @param ondragend the {@code ondragend} event handler for this element
     */
    @JsxSetter
    public void setOndragend(final Object ondragend) {
        setEventHandler("dragend", ondragend);
    }

    /**
     * Returns the {@code ondragenter} event handler for this element.
     * @return the {@code ondragenter} event handler for this element
     */
    @JsxGetter
    public Function getOndragenter() {
        return getEventHandler("dragenter");
    }

    /**
     * Sets the {@code ondragenter} event handler for this element.
     * @param ondragenter the {@code ondragenter} event handler for this element
     */
    @JsxSetter
    public void setOndragenter(final Object ondragenter) {
        setEventHandler("dragenter", ondragenter);
    }

    /**
     * Returns the {@code ondragleave} event handler for this element.
     * @return the {@code ondragleave} event handler for this element
     */
    @JsxGetter
    public Function getOndragleave() {
        return getEventHandler("dragleave");
    }

    /**
     * Sets the {@code ondragleave} event handler for this element.
     * @param ondragleave the {@code ondragleave} event handler for this element
     */
    @JsxSetter
    public void setOndragleave(final Object ondragleave) {
        setEventHandler("dragleave", ondragleave);
    }

    /**
     * Returns the {@code ondragover} event handler for this element.
     * @return the {@code ondragover} event handler for this element
     */
    @JsxGetter
    public Function getOndragover() {
        return getEventHandler("dragover");
    }

    /**
     * Sets the {@code ondragover} event handler for this element.
     * @param ondragover the {@code ondragover} event handler for this element
     */
    @JsxSetter
    public void setOndragover(final Object ondragover) {
        setEventHandler("dragover", ondragover);
    }

    /**
     * Returns the {@code ondragstart} event handler for this element.
     * @return the {@code ondragstart} event handler for this element
     */
    @JsxGetter
    public Function getOndragstart() {
        return getEventHandler("dragstart");
    }

    /**
     * Sets the {@code ondragstart} event handler for this element.
     * @param ondragstart the {@code ondragstart} event handler for this element
     */
    @JsxSetter
    public void setOndragstart(final Object ondragstart) {
        setEventHandler("dragstart", ondragstart);
    }

    /**
     * Returns the {@code ondrop} event handler for this element.
     * @return the {@code ondrop} event handler for this element
     */
    @JsxGetter
    public Function getOndrop() {
        return getEventHandler("drop");
    }

    /**
     * Sets the {@code ondrop} event handler for this element.
     * @param ondrop the {@code ondrop} event handler for this element
     */
    @JsxSetter
    public void setOndrop(final Object ondrop) {
        setEventHandler("drop", ondrop);
    }

    /**
     * Returns the {@code ondurationchange} event handler for this element.
     * @return the {@code ondurationchange} event handler for this element
     */
    @JsxGetter
    public Function getOndurationchange() {
        return getEventHandler("durationchange");
    }

    /**
     * Sets the {@code ondurationchange} event handler for this element.
     * @param ondurationchange the {@code ondurationchange} event handler for this element
     */
    @JsxSetter
    public void setOndurationchange(final Object ondurationchange) {
        setEventHandler("durationchange", ondurationchange);
    }

    /**
     * Returns the {@code onemptied} event handler for this element.
     * @return the {@code onemptied} event handler for this element
     */
    @JsxGetter
    public Function getOnemptied() {
        return getEventHandler("emptied");
    }

    /**
     * Sets the {@code onemptied} event handler for this element.
     * @param onemptied the {@code onemptied} event handler for this element
     */
    @JsxSetter
    public void setOnemptied(final Object onemptied) {
        setEventHandler("emptied", onemptied);
    }

    /**
     * Returns the {@code onended} event handler for this element.
     * @return the {@code onended} event handler for this element
     */
    @JsxGetter
    public Function getOnended() {
        return getEventHandler("ended");
    }

    /**
     * Sets the {@code onended} event handler for this element.
     * @param onended the {@code onended} event handler for this element
     */
    @JsxSetter
    public void setOnended(final Object onended) {
        setEventHandler("ended", onended);
    }

    /**
     * Returns the {@code ongotpointercapture} event handler for this element.
     * @return the {@code ongotpointercapture} event handler for this element
     */
    @JsxGetter(CHROME)
    public Function getOngotpointercapture() {
        return getEventHandler("gotpointercapture");
    }

    /**
     * Sets the {@code ongotpointercapture} event handler for this element.
     * @param ongotpointercapture the {@code ongotpointercapture} event handler for this element
     */
    @JsxSetter(CHROME)
    public void setOngotpointercapture(final Object ongotpointercapture) {
        setEventHandler("gotpointercapture", ongotpointercapture);
    }

    /**
     * Returns the {@code oninvalid} event handler for this element.
     * @return the {@code oninvalid} event handler for this element
     */
    @JsxGetter({CHROME, FF})
    public Function getOninvalid() {
        return getEventHandler("invalid");
    }

    /**
     * Sets the {@code oninvalid} event handler for this element.
     * @param oninvalid the {@code oninvalid} event handler for this element
     */
    @JsxSetter({CHROME, FF})
    public void setOninvalid(final Object oninvalid) {
        setEventHandler("invalid", oninvalid);
    }

    /**
     * Returns the {@code onload} event handler for this element.
     * @return the {@code onload} event handler for this element
     */
    @JsxGetter
    public Function getOnload() {
        return getEventHandler(Event.TYPE_LOAD);
    }

    /**
     * Sets the {@code onload} event handler for this element.
     * @param onload the {@code onload} event handler for this element
     */
    @JsxSetter
    public void setOnload(final Object onload) {
        setEventHandler(Event.TYPE_LOAD, onload);
    }

    /**
     * Returns the {@code onloadeddata} event handler for this element.
     * @return the {@code onloadeddata} event handler for this element
     */
    @JsxGetter
    public Function getOnloadeddata() {
        return getEventHandler("loadeddata");
    }

    /**
     * Sets the {@code onloadeddata} event handler for this element.
     * @param onloadeddata the {@code onloadeddata} event handler for this element
     */
    @JsxSetter
    public void setOnloadeddata(final Object onloadeddata) {
        setEventHandler("loadeddata", onloadeddata);
    }

    /**
     * Returns the {@code onloadedmetadata} event handler for this element.
     * @return the {@code onloadedmetadata} event handler for this element
     */
    @JsxGetter
    public Function getOnloadedmetadata() {
        return getEventHandler("loadedmetadata");
    }

    /**
     * Sets the {@code onloadedmetadata} event handler for this element.
     * @param onloadedmetadata the {@code onloadedmetadata} event handler for this element
     */
    @JsxSetter
    public void setOnloadedmetadata(final Object onloadedmetadata) {
        setEventHandler("loadedmetadata", onloadedmetadata);
    }

    /**
     * Returns the {@code onloadstart} event handler for this element.
     * @return the {@code onloadstart} event handler for this element
     */
    @JsxGetter
    public Function getOnloadstart() {
        return getEventHandler("loadstart");
    }

    /**
     * Sets the {@code onloadstart} event handler for this element.
     * @param onloadstart the {@code onloadstart} event handler for this element
     */
    @JsxSetter
    public void setOnloadstart(final Object onloadstart) {
        setEventHandler("loadstart", onloadstart);
    }

    /**
     * Returns the {@code onlostpointercapture} event handler for this element.
     * @return the {@code onlostpointercapture} event handler for this element
     */
    @JsxGetter(CHROME)
    public Function getOnlostpointercapture() {
        return getEventHandler("lostpointercapture");
    }

    /**
     * Sets the {@code onlostpointercapture} event handler for this element.
     * @param onlostpointercapture the {@code onlostpointercapture} event handler for this element
     */
    @JsxSetter(CHROME)
    public void setOnlostpointercapture(final Object onlostpointercapture) {
        setEventHandler("lostpointercapture", onlostpointercapture);
    }

    /**
     * Returns the {@code onmouseenter} event handler for this element.
     * @return the {@code onmouseenter} event handler for this element
     */
    @JsxGetter({CHROME, FF})
    public Function getOnmouseenter() {
        return getEventHandler("mouseenter");
    }

    /**
     * Sets the {@code onmouseenter} event handler for this element.
     * @param onmouseenter the {@code onmouseenter} event handler for this element
     */
    @JsxSetter({CHROME, FF})
    public void setOnmouseenter(final Object onmouseenter) {
        setEventHandler("mouseenter", onmouseenter);
    }

    /**
     * Returns the {@code onmouseleave} event handler for this element.
     * @return the {@code onmouseleave} event handler for this element
     */
    @JsxGetter({CHROME, FF})
    public Function getOnmouseleave() {
        return getEventHandler("mouseleave");
    }

    /**
     * Sets the {@code onmouseleave} event handler for this element.
     * @param onmouseleave the {@code onmouseleave} event handler for this element
     */
    @JsxSetter({CHROME, FF})
    public void setOnmouseleave(final Object onmouseleave) {
        setEventHandler("mouseleave", onmouseleave);
    }

    /**
     * Returns the {@code onmousewheel} event handler for this element.
     * @return the {@code onmousewheel} event handler for this element
     */
    @JsxGetter({CHROME, IE})
    public Function getOnmousewheel() {
        return getEventHandler("mousewheel");
    }

    /**
     * Sets the {@code onmousewheel} event handler for this element.
     * @param onmousewheel the {@code onmousewheel} event handler for this element
     */
    @JsxSetter({CHROME, IE})
    public void setOnmousewheel(final Object onmousewheel) {
        setEventHandler("mousewheel", onmousewheel);
    }

    /**
     * Returns the {@code onpaste} event handler for this element.
     * @return the {@code onpaste} event handler for this element
     */
    @JsxGetter({CHROME, FF})
    public Function getOnpaste() {
        return getEventHandler("paste");
    }

    /**
     * Sets the {@code onpaste} event handler for this element.
     * @param onpaste the {@code onpaste} event handler for this element
     */
    @JsxSetter({CHROME, FF})
    public void setOnpaste(final Object onpaste) {
        setEventHandler("paste", onpaste);
    }

    /**
     * Returns the {@code onpause} event handler for this element.
     * @return the {@code onpause} event handler for this element
     */
    @JsxGetter
    public Function getOnpause() {
        return getEventHandler("pause");
    }

    /**
     * Sets the {@code onpause} event handler for this element.
     * @param onpause the {@code onpause} event handler for this element
     */
    @JsxSetter
    public void setOnpause(final Object onpause) {
        setEventHandler("pause", onpause);
    }

    /**
     * Returns the {@code onplay} event handler for this element.
     * @return the {@code onplay} event handler for this element
     */
    @JsxGetter
    public Function getOnplay() {
        return getEventHandler("play");
    }

    /**
     * Sets the {@code onplay} event handler for this element.
     * @param onplay the {@code onplay} event handler for this element
     */
    @JsxSetter
    public void setOnplay(final Object onplay) {
        setEventHandler("play", onplay);
    }

    /**
     * Returns the {@code onplaying} event handler for this element.
     * @return the {@code onplaying} event handler for this element
     */
    @JsxGetter
    public Function getOnplaying() {
        return getEventHandler("playing");
    }

    /**
     * Sets the {@code onplaying} event handler for this element.
     * @param onplaying the {@code onplaying} event handler for this element
     */
    @JsxSetter
    public void setOnplaying(final Object onplaying) {
        setEventHandler("playing", onplaying);
    }

    /**
     * Returns the {@code onpointercancel} event handler for this element.
     * @return the {@code onpointercancel} event handler for this element
     */
    @JsxGetter({CHROME, IE})
    public Function getOnpointercancel() {
        return getEventHandler("pointercancel");
    }

    /**
     * Sets the {@code onpointercancel} event handler for this element.
     * @param onpointercancel the {@code onpointercancel} event handler for this element
     */
    @JsxSetter({CHROME, IE})
    public void setOnpointercancel(final Object onpointercancel) {
        setEventHandler("pointercancel", onpointercancel);
    }

    /**
     * Returns the {@code onpointerdown} event handler for this element.
     * @return the {@code onpointerdown} event handler for this element
     */
    @JsxGetter({CHROME, IE})
    public Function getOnpointerdown() {
        return getEventHandler("pointerdown");
    }

    /**
     * Sets the {@code onpointerdown} event handler for this element.
     * @param onpointerdown the {@code onpointerdown} event handler for this element
     */
    @JsxSetter({CHROME, IE})
    public void setOnpointerdown(final Object onpointerdown) {
        setEventHandler("pointerdown", onpointerdown);
    }

    /**
     * Returns the {@code onpointerenter} event handler for this element.
     * @return the {@code onpointerenter} event handler for this element
     */
    @JsxGetter({CHROME, IE})
    public Function getOnpointerenter() {
        return getEventHandler("pointerenter");
    }

    /**
     * Sets the {@code onpointerenter} event handler for this element.
     * @param onpointerenter the {@code onpointerenter} event handler for this element
     */
    @JsxSetter({CHROME, IE})
    public void setOnpointerenter(final Object onpointerenter) {
        setEventHandler("pointerenter", onpointerenter);
    }

    /**
     * Returns the {@code onpointerleave} event handler for this element.
     * @return the {@code onpointerleave} event handler for this element
     */
    @JsxGetter({CHROME, IE})
    public Function getOnpointerleave() {
        return getEventHandler("pointerleave");
    }

    /**
     * Sets the {@code onpointerleave} event handler for this element.
     * @param onpointerleave the {@code onpointerleave} event handler for this element
     */
    @JsxSetter({CHROME, IE})
    public void setOnpointerleave(final Object onpointerleave) {
        setEventHandler("pointerleave", onpointerleave);
    }

    /**
     * Returns the {@code onpointerlockchange} event handler for this element.
     * @return the {@code onpointerlockchange} event handler for this element
     */
    @JsxGetter({CHROME, FF52})
    public Function getOnpointerlockchange() {
        return getEventHandler("pointerlockchange");
    }

    /**
     * Sets the {@code onpointerlockchange} event handler for this element.
     * @param onpointerlockchange the {@code onpointerlockchange} event handler for this element
     */
    @JsxSetter({CHROME, FF52})
    public void setOnpointerlockchange(final Object onpointerlockchange) {
        setEventHandler("pointerlockchange", onpointerlockchange);
    }

    /**
     * Returns the {@code onpointerlockerror} event handler for this element.
     * @return the {@code onpointerlockerror} event handler for this element
     */
    @JsxGetter({CHROME, FF52})
    public Function getOnpointerlockerror() {
        return getEventHandler("pointerlockerror");
    }

    /**
     * Sets the {@code onpointerlockerror} event handler for this element.
     * @param onpointerlockerror the {@code onpointerlockerror} event handler for this element
     */
    @JsxSetter({CHROME, FF52})
    public void setOnpointerlockerror(final Object onpointerlockerror) {
        setEventHandler("pointerlockerror", onpointerlockerror);
    }

    /**
     * Returns the {@code onpointermove} event handler for this element.
     * @return the {@code onpointermove} event handler for this element
     */
    @JsxGetter({CHROME, IE})
    public Function getOnpointermove() {
        return getEventHandler("pointermove");
    }

    /**
     * Sets the {@code onpointermove} event handler for this element.
     * @param onpointermove the {@code onpointermove} event handler for this element
     */
    @JsxSetter({CHROME, IE})
    public void setOnpointermove(final Object onpointermove) {
        setEventHandler("pointermove", onpointermove);
    }

    /**
     * Returns the {@code onpointerout} event handler for this element.
     * @return the {@code onpointerout} event handler for this element
     */
    @JsxGetter({CHROME, IE})
    public Function getOnpointerout() {
        return getEventHandler("pointerout");
    }

    /**
     * Sets the {@code onpointerout} event handler for this element.
     * @param onpointerout the {@code onpointerout} event handler for this element
     */
    @JsxSetter({CHROME, IE})
    public void setOnpointerout(final Object onpointerout) {
        setEventHandler("pointerout", onpointerout);
    }

    /**
     * Returns the {@code onpointerover} event handler for this element.
     * @return the {@code onpointerover} event handler for this element
     */
    @JsxGetter({CHROME, IE})
    public Function getOnpointerover() {
        return getEventHandler("pointerover");
    }

    /**
     * Sets the {@code onpointerover} event handler for this element.
     * @param onpointerover the {@code onpointerover} event handler for this element
     */
    @JsxSetter({CHROME, IE})
    public void setOnpointerover(final Object onpointerover) {
        setEventHandler("pointerover", onpointerover);
    }

    /**
     * Returns the {@code onpointerup} event handler for this element.
     * @return the {@code onpointerup} event handler for this element
     */
    @JsxGetter({CHROME, IE})
    public Function getOnpointerup() {
        return getEventHandler("pointerup");
    }

    /**
     * Sets the {@code onpointerup} event handler for this element.
     * @param onpointerup the {@code onpointerup} event handler for this element
     */
    @JsxSetter({CHROME, IE})
    public void setOnpointerup(final Object onpointerup) {
        setEventHandler("pointerup", onpointerup);
    }

    /**
     * Returns the {@code onprogress} event handler for this element.
     * @return the {@code onprogress} event handler for this element
     */
    @JsxGetter
    public Function getOnprogress() {
        return getEventHandler("progress");
    }

    /**
     * Sets the {@code onprogress} event handler for this element.
     * @param onprogress the {@code onprogress} event handler for this element
     */
    @JsxSetter
    public void setOnprogress(final Object onprogress) {
        setEventHandler("progress", onprogress);
    }

    /**
     * Returns the {@code onratechange} event handler for this element.
     * @return the {@code onratechange} event handler for this element
     */
    @JsxGetter
    public Function getOnratechange() {
        return getEventHandler("ratechange");
    }

    /**
     * Sets the {@code onratechange} event handler for this element.
     * @param onratechange the {@code onratechange} event handler for this element
     */
    @JsxSetter
    public void setOnratechange(final Object onratechange) {
        setEventHandler("ratechange", onratechange);
    }

    /**
     * Returns the {@code onreadystatechange} event handler for this element.
     * @return the {@code onreadystatechange} event handler for this element
     */
    @JsxGetter
    public Function getOnreadystatechange() {
        return getEventHandler(Event.TYPE_READY_STATE_CHANGE);
    }

    /**
     * Sets the {@code onreadystatechange} event handler for this element.
     * @param onreadystatechange the {@code onreadystatechange} event handler for this element
     */
    @JsxSetter
    public void setOnreadystatechange(final Object onreadystatechange) {
        setEventHandler(Event.TYPE_READY_STATE_CHANGE, onreadystatechange);
    }

    /**
     * Returns the {@code onreset} event handler for this element.
     * @return the {@code onreset} event handler for this element
     */
    @JsxGetter
    public Function getOnreset() {
        return getEventHandler(Event.TYPE_RESET);
    }

    /**
     * Sets the {@code onreset} event handler for this element.
     * @param onreset the {@code onreset} event handler for this element
     */
    @JsxSetter
    public void setOnreset(final Object onreset) {
        setEventHandler(Event.TYPE_RESET, onreset);
    }

    /**
     * Returns the {@code onscroll} event handler for this element.
     * @return the {@code onscroll} event handler for this element
     */
    @JsxGetter
    public Function getOnscroll() {
        return getEventHandler("scroll");
    }

    /**
     * Sets the {@code onscroll} event handler for this element.
     * @param onscroll the {@code onscroll} event handler for this element
     */
    @JsxSetter
    public void setOnscroll(final Object onscroll) {
        setEventHandler("scroll", onscroll);
    }

    /**
     * Returns the {@code onsearch} event handler for this element.
     * @return the {@code onsearch} event handler for this element
     */
    @JsxGetter(CHROME)
    public Function getOnsearch() {
        return getEventHandler("search");
    }

    /**
     * Sets the {@code onsearch} event handler for this element.
     * @param onsearch the {@code onsearch} event handler for this element
     */
    @JsxSetter(CHROME)
    public void setOnsearch(final Object onsearch) {
        setEventHandler("search", onsearch);
    }

    /**
     * Returns the {@code onseeked} event handler for this element.
     * @return the {@code onseeked} event handler for this element
     */
    @JsxGetter
    public Function getOnseeked() {
        return getEventHandler("seeked");
    }

    /**
     * Sets the {@code onseeked} event handler for this element.
     * @param onseeked the {@code onseeked} event handler for this element
     */
    @JsxSetter
    public void setOnseeked(final Object onseeked) {
        setEventHandler("seeked", onseeked);
    }

    /**
     * Returns the {@code onseeking} event handler for this element.
     * @return the {@code onseeking} event handler for this element
     */
    @JsxGetter
    public Function getOnseeking() {
        return getEventHandler("seeking");
    }

    /**
     * Sets the {@code onseeking} event handler for this element.
     * @param onseeking the {@code onseeking} event handler for this element
     */
    @JsxSetter
    public void setOnseeking(final Object onseeking) {
        setEventHandler("seeking", onseeking);
    }

    /**
     * Returns the {@code onselect} event handler for this element.
     * @return the {@code onselect} event handler for this element
     */
    @JsxGetter
    public Function getOnselect() {
        return getEventHandler("select");
    }

    /**
     * Sets the {@code onselect} event handler for this element.
     * @param onselect the {@code onselect} event handler for this element
     */
    @JsxSetter
    public void setOnselect(final Object onselect) {
        setEventHandler("select", onselect);
    }

    /**
     * Returns the {@code onselectionchange} event handler for this element.
     * @return the {@code onselectionchange} event handler for this element
     */
    @JsxGetter({CHROME, IE, FF52})
    public Function getOnselectionchange() {
        return getEventHandler("selectionchange");
    }

    /**
     * Sets the {@code onselectionchange} event handler for this element.
     * @param onselectionchange the {@code onselectionchange} event handler for this element
     */
    @JsxSetter({CHROME, IE, FF52})
    public void setOnselectionchange(final Object onselectionchange) {
        setEventHandler("selectionchange", onselectionchange);
    }

    /**
     * Returns the {@code onselectstart} event handler for this element.
     * @return the {@code onselectstart} event handler for this element
     */
    @JsxGetter({CHROME, IE, FF52})
    public Function getOnselectstart() {
        return getEventHandler("selectstart");
    }

    /**
     * Sets the {@code onselectstart} event handler for this element.
     * @param onselectstart the {@code onselectstart} event handler for this element
     */
    @JsxSetter({CHROME, IE, FF52})
    public void setOnselectstart(final Object onselectstart) {
        setEventHandler("selectstart", onselectstart);
    }

    /**
     * Returns the {@code onshow} event handler for this element.
     * @return the {@code onshow} event handler for this element
     */
    @JsxGetter({CHROME, FF})
    public Function getOnshow() {
        return getEventHandler("show");
    }

    /**
     * Sets the {@code onshow} event handler for this element.
     * @param onshow the {@code onshow} event handler for this element
     */
    @JsxSetter({CHROME, FF})
    public void setOnshow(final Object onshow) {
        setEventHandler("show", onshow);
    }

    /**
     * Returns the {@code onstalled} event handler for this element.
     * @return the {@code onstalled} event handler for this element
     */
    @JsxGetter
    public Function getOnstalled() {
        return getEventHandler("stalled");
    }

    /**
     * Sets the {@code onstalled} event handler for this element.
     * @param onstalled the {@code onstalled} event handler for this element
     */
    @JsxSetter
    public void setOnstalled(final Object onstalled) {
        setEventHandler("stalled", onstalled);
    }

    /**
     * Returns the {@code onsubmit} event handler for this element.
     * @return the {@code onsubmit} event handler for this element
     */
    @JsxGetter
    public Function getOnsubmit() {
        return getEventHandler(Event.TYPE_SUBMIT);
    }

    /**
     * Sets the {@code onsubmit} event handler for this element.
     * @param onsubmit the {@code onsubmit} event handler for this element
     */
    @JsxSetter
    public void setOnsubmit(final Object onsubmit) {
        setEventHandler(Event.TYPE_SUBMIT, onsubmit);
    }

    /**
     * Returns the {@code onsuspend} event handler for this element.
     * @return the {@code onsuspend} event handler for this element
     */
    @JsxGetter
    public Function getOnsuspend() {
        return getEventHandler("suspend");
    }

    /**
     * Sets the {@code onsuspend} event handler for this element.
     * @param onsuspend the {@code onsuspend} event handler for this element
     */
    @JsxSetter
    public void setOnsuspend(final Object onsuspend) {
        setEventHandler("suspend", onsuspend);
    }

    /**
     * Returns the {@code ontimeupdate} event handler for this element.
     * @return the {@code ontimeupdate} event handler for this element
     */
    @JsxGetter
    public Function getOntimeupdate() {
        return getEventHandler("timeupdate");
    }

    /**
     * Sets the {@code ontimeupdate} event handler for this element.
     * @param ontimeupdate the {@code ontimeupdate} event handler for this element
     */
    @JsxSetter
    public void setOntimeupdate(final Object ontimeupdate) {
        setEventHandler("timeupdate", ontimeupdate);
    }

    /**
     * Returns the {@code ontoggle} event handler for this element.
     * @return the {@code ontoggle} event handler for this element
     */
    @JsxGetter({CHROME, FF52})
    public Function getOntoggle() {
        return getEventHandler("toggle");
    }

    /**
     * Sets the {@code ontoggle} event handler for this element.
     * @param ontoggle the {@code ontoggle} event handler for this element
     */
    @JsxSetter({CHROME, FF52})
    public void setOntoggle(final Object ontoggle) {
        setEventHandler("toggle", ontoggle);
    }

    /**
     * Returns the {@code onvolumechange} event handler for this element.
     * @return the {@code onvolumechange} event handler for this element
     */
    @JsxGetter
    public Function getOnvolumechange() {
        return getEventHandler("volumechange");
    }

    /**
     * Sets the {@code onvolumechange} event handler for this element.
     * @param onvolumechange the {@code onvolumechange} event handler for this element
     */
    @JsxSetter
    public void setOnvolumechange(final Object onvolumechange) {
        setEventHandler("volumechange", onvolumechange);
    }

    /**
     * Returns the {@code onwaiting} event handler for this element.
     * @return the {@code onwaiting} event handler for this element
     */
    @JsxGetter
    public Function getOnwaiting() {
        return getEventHandler("waiting");
    }

    /**
     * Sets the {@code onwaiting} event handler for this element.
     * @param onwaiting the {@code onwaiting} event handler for this element
     */
    @JsxSetter
    public void setOnwaiting(final Object onwaiting) {
        setEventHandler("waiting", onwaiting);
    }

    /**
     * Returns the {@code onwebkitfullscreenchange} event handler for this element.
     * @return the {@code onwebkitfullscreenchange} event handler for this element
     */
    @JsxGetter(CHROME)
    public Function getOnwebkitfullscreenchange() {
        return getEventHandler("webkitfullscreenchange");
    }

    /**
     * Sets the {@code onwebkitfullscreenchange} event handler for this element.
     * @param onwebkitfullscreenchange the {@code onwebkitfullscreenchange} event handler for this element
     */
    @JsxSetter(CHROME)
    public void setOnwebkitfullscreenchange(final Object onwebkitfullscreenchange) {
        setEventHandler("webkitfullscreenchange", onwebkitfullscreenchange);
    }

    /**
     * Returns the {@code onwebkitfullscreenerror} event handler for this element.
     * @return the {@code onwebkitfullscreenerror} event handler for this element
     */
    @JsxGetter(CHROME)
    public Function getOnwebkitfullscreenerror() {
        return getEventHandler("webkitfullscreenerror");
    }

    /**
     * Sets the {@code onwebkitfullscreenerror} event handler for this element.
     * @param onwebkitfullscreenerror the {@code onwebkitfullscreenerror} event handler for this element
     */
    @JsxSetter
    public void setOnwebkitfullscreenerror(final Object onwebkitfullscreenerror) {
        setEventHandler("webkitfullscreenerror", onwebkitfullscreenerror);
    }

    /**
     * Returns the {@code onwheel} event handler for this element.
     * @return the {@code onwheel} event handler for this element
     */
    @JsxGetter({CHROME, FF})
    public Function getOnwheel() {
        return getEventHandler("wheel");
    }

    /**
     * Sets the {@code onwheel} event handler for this element.
     * @param onwheel the {@code onwheel} event handler for this element
     */
    @JsxSetter({CHROME, FF})
    public void setOnwheel(final Object onwheel) {
        setEventHandler("wheel", onwheel);
    }

    /**
     * Returns the {@code onafterscriptexecute} event handler for this element.
     * @return the {@code onafterscriptexecute} event handler for this element
     */
    @JsxGetter(FF)
    public Function getOnafterscriptexecute() {
        return getEventHandler("afterscriptexecute");
    }

    /**
     * Sets the {@code onafterscriptexecute} event handler for this element.
     * @param onafterscriptexecute the {@code onafterscriptexecute} event handler for this element
     */
    @JsxSetter(FF)
    public void setOnafterscriptexecute(final Object onafterscriptexecute) {
        setEventHandler("afterscriptexecute", onafterscriptexecute);
    }

    /**
     * Returns the {@code onbeforescriptexecute} event handler for this element.
     * @return the {@code onbeforescriptexecute} event handler for this element
     */
    @JsxGetter(FF)
    public Function getOnbeforescriptexecute() {
        return getEventHandler("beforescriptexecute");
    }

    /**
     * Sets the {@code onbeforescriptexecute} event handler for this element.
     * @param onbeforescriptexecute the {@code onbeforescriptexecute} event handler for this element
     */
    @JsxSetter(FF)
    public void setOnbeforescriptexecute(final Object onbeforescriptexecute) {
        setEventHandler("beforescriptexecute", onbeforescriptexecute);
    }

    /**
     * Returns the {@code onmozfullscreenchange} event handler for this element.
     * @return the {@code onmozfullscreenchange} event handler for this element
     */
    @JsxGetter(FF)
    public Function getOnmozfullscreenchange() {
        return getEventHandler("mozfullscreenchange");
    }

    /**
     * Sets the {@code onmozfullscreenchange} event handler for this element.
     * @param onmozfullscreenchange the {@code onmozfullscreenchange} event handler for this element
     */
    @JsxSetter(FF)
    public void setOnmozfullscreenchange(final Object onmozfullscreenchange) {
        setEventHandler("mozfullscreenchange", onmozfullscreenchange);
    }

    /**
     * Returns the {@code onmozfullscreenerror} event handler for this element.
     * @return the {@code onmozfullscreenerror} event handler for this element
     */
    @JsxGetter(FF)
    public Function getOnmozfullscreenerror() {
        return getEventHandler("mozfullscreenerror");
    }

    /**
     * Sets the {@code onmozfullscreenerror} event handler for this element.
     * @param onmozfullscreenerror the {@code onmozfullscreenerror} event handler for this element
     */
    @JsxSetter(FF)
    public void setOnmozfullscreenerror(final Object onmozfullscreenerror) {
        setEventHandler("mozfullscreenerror", onmozfullscreenerror);
    }

    /**
     * Returns the {@code onmozpointerlockchange} event handler for this element.
     * @return the {@code onmozpointerlockchange} event handler for this element
     */
    @JsxGetter(FF45)
    public Function getOnmozpointerlockchange() {
        return getEventHandler("mozpointerlockchange");
    }

    /**
     * Sets the {@code onmozpointerlockchange} event handler for this element.
     * @param onmozpointerlockchange the {@code onmozpointerlockchange} event handler for this element
     */
    @JsxSetter(FF45)
    public void setOnmozpointerlockchange(final Object onmozpointerlockchange) {
        setEventHandler("mozpointerlockchange", onmozpointerlockchange);
    }

    /**
     * Returns the {@code onmozpointerlockerror} event handler for this element.
     * @return the {@code onmozpointerlockerror} event handler for this element
     */
    @JsxGetter(FF45)
    public Function getOnmozpointerlockerror() {
        return getEventHandler("mozpointerlockerror");
    }

    /**
     * Sets the {@code onmozpointerlockerror} event handler for this element.
     * @param onmozpointerlockerror the {@code onmozpointerlockerror} event handler for this element
     */
    @JsxSetter(FF45)
    public void setOnmozpointerlockerror(final Object onmozpointerlockerror) {
        setEventHandler("mozpointerlockerror", onmozpointerlockerror);
    }

    /**
     * Returns the {@code onhelp} event handler for this element.
     * @return the {@code onhelp} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnhelp() {
        return getEventHandler("help");
    }

    /**
     * Sets the {@code onhelp} event handler for this element.
     * @param onhelp the {@code onhelp} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnhelp(final Object onhelp) {
        setEventHandler("help", onhelp);
    }

    /**
     * Returns the {@code onmscontentzoom} event handler for this element.
     * @return the {@code onmscontentzoom} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmscontentzoom() {
        return getEventHandler("mscontentzoom");
    }

    /**
     * Sets the {@code onmscontentzoom} event handler for this element.
     * @param onmscontentzoom the {@code onmscontentzoom} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmscontentzoom(final Object onmscontentzoom) {
        setEventHandler("mscontentzoom", onmscontentzoom);
    }

    /**
     * Returns the {@code onmsfullscreenchange} event handler for this element.
     * @return the {@code onmsfullscreenchange} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmsfullscreenchange() {
        return getEventHandler("msfullscreenchange");
    }

    /**
     * Sets the {@code onmsfullscreenchange} event handler for this element.
     * @param onmsfullscreenchange the {@code onmsfullscreenchange} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmsfullscreenchange(final Object onmsfullscreenchange) {
        setEventHandler("msfullscreenchange", onmsfullscreenchange);
    }

    /**
     * Returns the {@code onmsfullscreenerror} event handler for this element.
     * @return the {@code onmsfullscreenerror} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmsfullscreenerror() {
        return getEventHandler("msfullscreenerror");
    }

    /**
     * Sets the {@code onmsfullscreenerror} event handler for this element.
     * @param onmsfullscreenerror the {@code onmsfullscreenerror} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmsfullscreenerror(final Object onmsfullscreenerror) {
        setEventHandler("msfullscreenerror", onmsfullscreenerror);
    }

    /**
     * Returns the {@code onmsgesturechange} event handler for this element.
     * @return the {@code onmsgesturechange} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmsgesturechange() {
        return getEventHandler("msgesturechange");
    }

    /**
     * Sets the {@code onmsgesturechange} event handler for this element.
     * @param onmsgesturechange the {@code onmsgesturechange} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmsgesturechange(final Object onmsgesturechange) {
        setEventHandler("msgesturechange", onmsgesturechange);
    }

    /**
     * Returns the {@code onmsgesturedoubletap} event handler for this element.
     * @return the {@code onmsgesturedoubletap} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmsgesturedoubletap() {
        return getEventHandler("msgesturedoubletap");
    }

    /**
     * Sets the {@code onmsgesturedoubletap} event handler for this element.
     * @param onmsgesturedoubletap the {@code onmsgesturedoubletap} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmsgesturedoubletap(final Object onmsgesturedoubletap) {
        setEventHandler("msgesturedoubletap", onmsgesturedoubletap);
    }

    /**
     * Returns the {@code onmsgestureend} event handler for this element.
     * @return the {@code onmsgestureend} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmsgestureend() {
        return getEventHandler("msgestureend");
    }

    /**
     * Sets the {@code onmsgestureend} event handler for this element.
     * @param onmsgestureend the {@code onmsgestureend} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmsgestureend(final Object onmsgestureend) {
        setEventHandler("msgestureend", onmsgestureend);
    }

    /**
     * Returns the {@code onmsgesturehold} event handler for this element.
     * @return the {@code onmsgesturehold} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmsgesturehold() {
        return getEventHandler("msgesturehold");
    }

    /**
     * Sets the {@code onmsgesturehold} event handler for this element.
     * @param onmsgesturehold the {@code onmsgesturehold} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmsgesturehold(final Object onmsgesturehold) {
        setEventHandler("msgesturehold", onmsgesturehold);
    }

    /**
     * Returns the {@code onmsgesturestart} event handler for this element.
     * @return the {@code onmsgesturestart} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmsgesturestart() {
        return getEventHandler("msgesturestart");
    }

    /**
     * Sets the {@code onmsgesturestart} event handler for this element.
     * @param onmsgesturestart the {@code onmsgesturestart} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmsgesturestart(final Object onmsgesturestart) {
        setEventHandler("msgesturestart", onmsgesturestart);
    }

    /**
     * Returns the {@code onmsgesturetap} event handler for this element.
     * @return the {@code onmsgesturetap} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmsgesturetap() {
        return getEventHandler("msgesturetap");
    }

    /**
     * Sets the {@code onmsgesturetap} event handler for this element.
     * @param onmsgesturetap the {@code onmsgesturetap} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmsgesturetap(final Object onmsgesturetap) {
        setEventHandler("msgesturetap", onmsgesturetap);
    }

    /**
     * Returns the {@code onmsinertiastart} event handler for this element.
     * @return the {@code onmsinertiastart} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmsinertiastart() {
        return getEventHandler("msinertiastart");
    }

    /**
     * Sets the {@code onmsinertiastart} event handler for this element.
     * @param onmsinertiastart the {@code onmsinertiastart} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmsinertiastart(final Object onmsinertiastart) {
        setEventHandler("msinertiastart", onmsinertiastart);
    }

    /**
     * Returns the {@code onmsmanipulationstatechanged} event handler for this element.
     * @return the {@code onmsmanipulationstatechanged} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmsmanipulationstatechanged() {
        return getEventHandler("msmanipulationstatechanged");
    }

    /**
     * Sets the {@code onmsmanipulationstatechanged} event handler for this element.
     * @param onmsmanipulationstatechanged the {@code onmsmanipulationstatechanged} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmsmanipulationstatechanged(final Object onmsmanipulationstatechanged) {
        setEventHandler("msmanipulationstatechanged", onmsmanipulationstatechanged);
    }

    /**
     * Returns the {@code onmspointercancel} event handler for this element.
     * @return the {@code onmspointercancel} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmspointercancel() {
        return getEventHandler("mspointercancel");
    }

    /**
     * Sets the {@code onmspointercancel} event handler for this element.
     * @param onmspointercancel the {@code onmspointercancel} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmspointercancel(final Object onmspointercancel) {
        setEventHandler("mspointercancel", onmspointercancel);
    }

    /**
     * Returns the {@code onmspointerdown} event handler for this element.
     * @return the {@code onmspointerdown} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmspointerdown() {
        return getEventHandler("mspointerdown");
    }

    /**
     * Sets the {@code onmspointerdown} event handler for this element.
     * @param onmspointerdown the {@code onmspointerdown} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmspointerdown(final Object onmspointerdown) {
        setEventHandler("mspointerdown", onmspointerdown);
    }

    /**
     * Returns the {@code onmspointerenter} event handler for this element.
     * @return the {@code onmspointerenter} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmspointerenter() {
        return getEventHandler("mspointerenter");
    }

    /**
     * Sets the {@code onmspointerenter} event handler for this element.
     * @param onmspointerenter the {@code onmspointerenter} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmspointerenter(final Object onmspointerenter) {
        setEventHandler("mspointerenter", onmspointerenter);
    }

    /**
     * Returns the {@code onmspointerleave} event handler for this element.
     * @return the {@code onmspointerleave} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmspointerleave() {
        return getEventHandler("mspointerleave");
    }

    /**
     * Sets the {@code onmspointerleave} event handler for this element.
     * @param onmspointerleave the {@code onmspointerleave} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmspointerleave(final Object onmspointerleave) {
        setEventHandler("mspointerleave", onmspointerleave);
    }

    /**
     * Returns the {@code onmspointermove} event handler for this element.
     * @return the {@code onmspointermove} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmspointermove() {
        return getEventHandler("mspointermove");
    }

    /**
     * Sets the {@code onmspointermove} event handler for this element.
     * @param onmspointermove the {@code onmspointermove} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmspointermove(final Object onmspointermove) {
        setEventHandler("mspointermove", onmspointermove);
    }

    /**
     * Returns the {@code onmspointerout} event handler for this element.
     * @return the {@code onmspointerout} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmspointerout() {
        return getEventHandler("mspointerout");
    }

    /**
     * Sets the {@code onmspointerout} event handler for this element.
     * @param onmspointerout the {@code onmspointerout} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmspointerout(final Object onmspointerout) {
        setEventHandler("mspointerout", onmspointerout);
    }

    /**
     * Returns the {@code onmspointerover} event handler for this element.
     * @return the {@code onmspointerover} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmspointerover() {
        return getEventHandler("mspointerover");
    }

    /**
     * Sets the {@code onmspointerover} event handler for this element.
     * @param onmspointerover the {@code onmspointerover} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmspointerover(final Object onmspointerover) {
        setEventHandler("mspointerover", onmspointerover);
    }

    /**
     * Returns the {@code onmspointerup} event handler for this element.
     * @return the {@code onmspointerup} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmspointerup() {
        return getEventHandler("mspointerup");
    }

    /**
     * Sets the {@code onmspointerup} event handler for this element.
     * @param onmspointerup the {@code onmspointerup} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmspointerup(final Object onmspointerup) {
        setEventHandler("mspointerup", onmspointerup);
    }

    /**
     * Returns the {@code onmssitemodejumplistitemremoved} event handler for this element.
     * @return the {@code onmssitemodejumplistitemremoved} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmssitemodejumplistitemremoved() {
        return getEventHandler("mssitemodejumplistitemremoved");
    }

    /**
     * Sets the {@code onmssitemodejumplistitemremoved} event handler for this element.
     * @param onmssitemodejumplistitemremoved the {@code onmssitemodejumplistitemremoved} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmssitemodejumplistitemremoved(final Object onmssitemodejumplistitemremoved) {
        setEventHandler("mssitemodejumplistitemremoved", onmssitemodejumplistitemremoved);
    }

    /**
     * Returns the {@code onmsthumbnailclick} event handler for this element.
     * @return the {@code onmsthumbnailclick} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnmsthumbnailclick() {
        return getEventHandler("msthumbnailclick");
    }

    /**
     * Sets the {@code onmsthumbnailclick} event handler for this element.
     * @param onmsthumbnailclick the {@code onmsthumbnailclick} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnmsthumbnailclick(final Object onmsthumbnailclick) {
        setEventHandler("msthumbnailclick", onmsthumbnailclick);
    }

    /**
     * Returns the {@code onstop} event handler for this element.
     * @return the {@code onstop} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnstop() {
        return getEventHandler("stop");
    }

    /**
     * Sets the {@code onstop} event handler for this element.
     * @param onstop the {@code onstop} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnstop(final Object onstop) {
        setEventHandler("stop", onstop);
    }

    /**
     * Returns the {@code onstoragecommit} event handler for this element.
     * @return the {@code onstoragecommit} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnstoragecommit() {
        return getEventHandler("storagecommit");
    }

    /**
     * Sets the {@code onstoragecommit} event handler for this element.
     * @param onstoragecommit the {@code onstoragecommit} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnstoragecommit(final Object onstoragecommit) {
        setEventHandler("storagecommit", onstoragecommit);
    }

    /**
     * Returns the {@code onactivate} event handler for this element.
     * @return the {@code onactivate} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnactivate() {
        return getEventHandler("activate");
    }

    /**
     * Sets the {@code onactivate} event handler for this element.
     * @param onactivate the {@code onactivate} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnactivate(final Object onactivate) {
        setEventHandler("activate", onactivate);
    }

    /**
     * Returns the {@code onbeforeactivate} event handler for this element.
     * @return the {@code onbeforeactivate} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnbeforeactivate() {
        return getEventHandler("beforeactivate");
    }

    /**
     * Sets the {@code onbeforeactivate} event handler for this element.
     * @param onbeforeactivate the {@code onbeforeactivate} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnbeforeactivate(final Object onbeforeactivate) {
        setEventHandler("beforeactivate", onbeforeactivate);
    }

    /**
     * Returns the {@code onbeforedeactivate} event handler for this element.
     * @return the {@code onbeforedeactivate} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOnbeforedeactivate() {
        return getEventHandler("beforedeactivate");
    }

    /**
     * Sets the {@code onbeforedeactivate} event handler for this element.
     * @param onbeforedeactivate the {@code onbeforedeactivate} event handler for this element
     */
    @JsxSetter(IE)
    public void setOnbeforedeactivate(final Object onbeforedeactivate) {
        setEventHandler("beforedeactivate", onbeforedeactivate);
    }

    /**
     * Returns the {@code ondeactivate} event handler for this element.
     * @return the {@code ondeactivate} event handler for this element
     */
    @JsxGetter(IE)
    public Function getOndeactivate() {
        return getEventHandler("deactivate");
    }

    /**
     * Sets the {@code ondeactivate} event handler for this element.
     * @param ondeactivate the {@code ondeactivate} event handler for this element
     */
    @JsxSetter(IE)
    public void setOndeactivate(final Object ondeactivate) {
        setEventHandler("deactivate", ondeactivate);
    }

    /**
     * Returns the {@code onanimationend} event handler.
     * @return the {@code onanimationend} event handler
     */
    @JsxGetter(FF52)
    public Function getOnanimationend() {
        return getEventHandler("animationend");
    }

    /**
     * Sets the {@code onanimationend} event handler.
     * @param animationend the {@code onanimationend} event handler
     */
    @JsxSetter(FF52)
    public void setOnanimationend(final Object animationend) {
        setEventHandler("animationend", animationend);
    }

    /**
     * Returns the {@code onanimationiteration} event handler.
     * @return the {@code onanimationiteration} event handler
     */
    @JsxGetter(FF52)
    public Function getOnanimationiteration() {
        return getEventHandler("animationiteration");
    }

    /**
     * Sets the {@code onanimationiteration} event handler.
     * @param animationiteration the {@code onanimationiteration} event handler
     */
    @JsxSetter(FF52)
    public void setOnanimationiteration(final Object animationiteration) {
        setEventHandler("animationiteration", animationiteration);
    }

    /**
     * Returns the {@code onanimationstart} event handler.
     * @return the {@code onanimationstart} event handler
     */
    @JsxGetter(FF52)
    public Function getOnanimationstart() {
        return getEventHandler("animationstart");
    }

    /**
     * Sets the {@code onanimationstart} event handler.
     * @param animationstart the {@code onanimationstart} event handler
     */
    @JsxSetter(FF52)
    public void setOnanimationstart(final Object animationstart) {
        setEventHandler("animationstart", animationstart);
    }

    /**
     * Returns the {@code ondragexit} event handler.
     * @return the {@code ondragexit} event handler
     */
    @JsxGetter(FF52)
    public Function getOndragexit() {
        return getEventHandler("dragexit");
    }

    /**
     * Sets the {@code ondragexit} event handler.
     * @param dragexit the {@code ondragexit} event handler
     */
    @JsxSetter(FF52)
    public void setOndragexit(final Object dragexit) {
        setEventHandler("dragexit", dragexit);
    }

    /**
     * Returns the {@code onloadend} event handler.
     * @return the {@code onloadend} event handler
     */
    @JsxGetter(FF52)
    public Function getOnloadend() {
        return getEventHandler("loadend");
    }

    /**
     * Sets the {@code onloadend} event handler.
     * @param loadend the {@code onloadend} event handler
     */
    @JsxSetter(FF52)
    public void setOnloadend(final Object loadend) {
        setEventHandler("loadend", loadend);
    }

    /**
     * Returns the {@code ontransitionend} event handler.
     * @return the {@code ontransitionend} event handler
     */
    @JsxGetter(FF52)
    public Function getOntransitionend() {
        return getEventHandler("transitionend");
    }

    /**
     * Sets the {@code ontransitionend} event handler.
     * @param transitionend the {@code ontransitionend} event handler
     */
    @JsxSetter(FF52)
    public void setOntransitionend(final Object transitionend) {
        setEventHandler("transitionend", transitionend);
    }

    /**
     * Returns the {@code onwebkitanimationend} event handler.
     * @return the {@code onwebkitanimationend} event handler
     */
    @JsxGetter(FF52)
    public Function getOnwebkitanimationend() {
        return getEventHandler("webkitanimationend");
    }

    /**
     * Sets the {@code onwebkitanimationend} event handler.
     * @param webkitanimationend the {@code onwebkitanimationend} event handler
     */
    @JsxSetter(FF52)
    public void setOnwebkitanimationend(final Object webkitanimationend) {
        setEventHandler("webkitanimationend", webkitanimationend);
    }

    /**
     * Returns the {@code onwebkitanimationiteration} event handler.
     * @return the {@code onwebkitanimationiteration} event handler
     */
    @JsxGetter(FF52)
    public Function getOnwebkitanimationiteration() {
        return getEventHandler("webkitanimationiteration");
    }

    /**
     * Sets the {@code onwebkitanimationiteration} event handler.
     * @param webkitanimationiteration the {@code onwebkitanimationiteration} event handler
     */
    @JsxSetter(FF52)
    public void setOnwebkitanimationiteration(final Object webkitanimationiteration) {
        setEventHandler("webkitanimationiteration", webkitanimationiteration);
    }

    /**
     * Returns the {@code onwebkitanimationstart} event handler.
     * @return the {@code onwebkitanimationstart} event handler
     */
    @JsxGetter(FF52)
    public Function getOnwebkitanimationstart() {
        return getEventHandler("webkitanimationstart");
    }

    /**
     * Sets the {@code onwebkitanimationstart} event handler.
     * @param webkitanimationstart the {@code onwebkitanimationstart} event handler
     */
    @JsxSetter(FF52)
    public void setOnwebkitanimationstart(final Object webkitanimationstart) {
        setEventHandler("webkitanimationstart", webkitanimationstart);
    }

    /**
     * Returns the {@code onwebkittransitionend} event handler.
     * @return the {@code onwebkittransitionend} event handler
     */
    @JsxGetter(FF52)
    public Function getOnwebkittransitionend() {
        return getEventHandler("webkittransitionend");
    }

    /**
     * Sets the {@code onwebkittransitionend} event handler.
     * @param webkittransitionend the {@code onwebkittransitionend} event handler
     */
    @JsxSetter(FF52)
    public void setOnwebkittransitionend(final Object webkittransitionend) {
        setEventHandler("webkittransitionend", webkittransitionend);
    }

    /**
     * @return the {@code currentScript}
     */
    @JsxGetter({CHROME, FF})
    public ScriptableObject getCurrentScript() {
        return currentScript_;
    }

    /**
     * @param script the {@code currentScript}
     */
    public void setCurrentScript(final ScriptableObject script) {
        currentScript_ = script;
    }
}
