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
package com.gargoylesoftware.htmlunit;

import com.gargoylesoftware.htmlunit.javascript.configuration.AbstractJavaScriptConfiguration;
import com.gargoylesoftware.htmlunit.javascript.configuration.BrowserFeature;
import com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Objects of this class represent one specific version of a given browser. Predefined
 * constants are provided for common browser versions.
 *
 * <p>You can create a different browser setup by using the BrowserVersionFactory.
 * <pre id='htmlUnitCode'>
 *      String applicationName = "APPNAME";
 *      String applicationVersion = "APPVERSION";
 *      String userAgent = "USERAGENT";
 *      int browserVersionNumeric = NUMERIC;
 *
 *      BrowserVersion browser = new BrowserVersion.BrowserVersionFactory(FF52)
 *          .setApplicationName(applicationName)
 *          .setApplicationVersion(applicationVersion)
 *          .setUserAgent(userAgent)
 *          .build();
 * </pre>
 * <p>But keep in mind this now one still behaves like a FF52, only the stuff reported to the
 * outside is changed. This is more or less the same you can do with real browsers installing
 * plugins like UserAgentSwitcher.
 *
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author Daniel Gredler
 * @author Marc Guillemot
 * @author Chris Erskine
 * @author Ahmed Ashour
 * @author Frank Danek
 * @author Ronald Brill
 */
public final class BrowserVersion implements Serializable {

    /**
     * Application name the Netscape navigator series of browsers.
     */
    private static final String NETSCAPE = "Netscape";

    /**
     * United States English language identifier.
     */
    private static final String LANGUAGE_ENGLISH_US = "en-US";

    /**
     * United States.
     */
    private static final String TIMEZONE_NEW_YORK = "America/New_York";

    /**
     * The X86 CPU class.
     */
    private static final String CPU_CLASS_X86 = "x86";

    /**
     * The WIN32 platform.
     */
    private static final String PLATFORM_WIN32 = "Win32";

    /**
     * The WIN64 platform.
     */
    private static final String PLATFORM_WIN64 = "Win64";

    /**
     * Firefox 45 ESR.
     * @since 2.21
     */
    public static final BrowserVersion FIREFOX_45 = new BrowserVersion(45, "FF45");

    /**
     * Firefox 52 ESR.
     * @since 2.26
     */
    public static final BrowserVersion FIREFOX_52 = new BrowserVersion(52, "FF52");

    /** Internet Explorer 11. */
    public static final BrowserVersion INTERNET_EXPLORER = new BrowserVersion(11, "IE");

    /** Latest Chrome. */
    public static final BrowserVersion CHROME = new BrowserVersion(60, "Chrome");

    /** Microsoft Edge. Work In Progress!!! */
    public static final BrowserVersion EDGE = new BrowserVersion(14, "Edge");

    /**
     * The best supported browser version at the moment.
     */
    public static final BrowserVersion BEST_SUPPORTED = CHROME;

    /** The default browser version. */
    private static BrowserVersion DefaultBrowserVersion_ = BEST_SUPPORTED;

    /** Register plugins for the browser versions. */
    static {
        // FF45
        FIREFOX_45.applicationVersion_ = "5.0 (Windows)";
        FIREFOX_45.userAgent_ = "Mozilla/5.0 (Windows NT 6.1; rv:45.0) Gecko/20100101 Firefox/45.0";
        FIREFOX_45.platform_ = PLATFORM_WIN32;
        FIREFOX_45.buildId_ = "20170411115307";
        FIREFOX_45.productSub_ = "20100101";
        FIREFOX_45.headerNamesOrdered_ = new String[] {
            HttpHeader.HOST,
            HttpHeader.USER_AGENT,
            HttpHeader.ACCEPT,
            HttpHeader.ACCEPT_LANGUAGE,
            HttpHeader.ACCEPT_ENCODING,
            HttpHeader.REFERER,
            HttpHeader.COOKIE,
            HttpHeader.CONNECTION};
        FIREFOX_45.htmlAcceptHeader_ = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        FIREFOX_45.xmlHttpRequestAcceptHeader_ = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        FIREFOX_45.imgAcceptHeader_ = "image/png,image/*;q=0.8,*/*;q=0.5";
        FIREFOX_45.cssAcceptHeader_ = "text/css,*/*;q=0.1";
        FIREFOX_45.fontHeights_ = new int[] {
            0, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
            30, 31, 32, 33, 34, 35, 36, 37, 38, 40, 41, 42, 43, 44, 45, 46, 47, 48, 50, 51, 52, 53, 53, 55, 57, 58,
            59, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 72, 73, 74, 75, 76, 77, 78, 79, 80, 82, 84, 85, 86, 87, 88,
            89, 90, 91, 93, 94, 95, 96, 96, 98, 99, 100, 101, 103, 104, 105, 106, 106, 108, 109, 111, 112, 113, 115,
            116, 117, 118, 119, 120, 121, 122, 123, 125, 126, 127, 128, 129, 130, 131, 132, 133, 135, 136, 138, 139,
            139, 141, 142, 143, 144, 146, 147, 148, 149};

        // FF52
        FIREFOX_52.applicationVersion_ = "5.0 (Windows)";
        FIREFOX_52.userAgent_ = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0";
        FIREFOX_52.buildId_ = "20180315163333";
        FIREFOX_52.productSub_ = "20100101";
        FIREFOX_52.headerNamesOrdered_ = new String[] {
            HttpHeader.HOST,
            HttpHeader.USER_AGENT,
            HttpHeader.ACCEPT,
            HttpHeader.ACCEPT_LANGUAGE,
            HttpHeader.ACCEPT_ENCODING,
            HttpHeader.REFERER,
            HttpHeader.COOKIE,
            HttpHeader.CONNECTION,
            "Upgrade-Insecure-Requests"};
        FIREFOX_52.htmlAcceptHeader_ = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        FIREFOX_52.cssAcceptHeader_ = "text/css,*/*;q=0.1";
        FIREFOX_52.fontHeights_ = new int[] {
            0, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
            30, 31, 32, 33, 34, 35, 36, 37, 38, 40, 41, 42, 43, 44, 45, 46, 47, 48, 50, 51, 52, 53, 53, 55, 57, 58,
            59, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 72, 73, 74, 75, 76, 77, 78, 79, 80, 82, 84, 85, 86, 87, 88,
            89, 90, 91, 93, 94, 95, 96, 96, 98, 99, 100, 101, 103, 104, 105, 106, 106, 108, 109, 111, 112, 113, 115,
            116, 117, 118, 119, 120, 121, 122, 123, 125, 126, 127, 128, 129, 130, 131, 132, 133, 135, 136, 138, 139,
            139, 141, 142, 143, 144, 146, 147, 148, 149};

        // IE
        INTERNET_EXPLORER.applicationVersion_ = "5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko";
        INTERNET_EXPLORER.userAgent_ = "Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko";
        INTERNET_EXPLORER.platform_ = PLATFORM_WIN32;
        INTERNET_EXPLORER.headerNamesOrdered_ = new String[] {
            HttpHeader.ACCEPT,
            HttpHeader.REFERER,
            HttpHeader.ACCEPT_LANGUAGE,
            HttpHeader.USER_AGENT,
            HttpHeader.ACCEPT_ENCODING,
            HttpHeader.HOST,
            HttpHeader.DNT,
            HttpHeader.CONNECTION,
            HttpHeader.COOKIE};
        INTERNET_EXPLORER.htmlAcceptHeader_ = "text/html, application/xhtml+xml, */*";
        INTERNET_EXPLORER.imgAcceptHeader_ = "image/png, image/svg+xml, image/*;q=0.8, */*;q=0.5";
        INTERNET_EXPLORER.cssAcceptHeader_ = "text/css, */*";
        INTERNET_EXPLORER.scriptAcceptHeader_ = "application/javascript, */*;q=0.8";
        INTERNET_EXPLORER.fontHeights_ = new int[] {
            0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22, 23, 24, 25, 26, 28,
            29, 30, 31, 32, 33, 35, 36, 37, 38, 39, 40, 41, 43, 44, 45, 46, 47, 48, 49, 51, 52, 53, 54, 55, 56, 58,
            59, 60, 61, 62, 63, 64, 66, 67, 68, 69, 70, 71, 72, 74, 75, 76, 77, 78, 79, 80, 82, 83, 84, 85, 86, 87,
            89, 90, 91, 92, 93, 94, 95, 97, 98, 99, 100, 101, 102, 103, 105, 106, 107, 108, 109, 110, 112, 113, 114,
            115, 116, 117, 118, 120, 121, 122, 123, 124, 125, 126, 128, 129, 130, 131, 132, 133, 135, 136, 137, 138,
            139, 140, 141, 143, 144, 145, 146, 147};

        // EDGE
        EDGE.applicationVersion_ = "5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393";
        EDGE.userAgent_ = "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393";

        EDGE.htmlAcceptHeader_ = "text/html, application/xhtml+xml, image/jxr, */*";
        EDGE.cssAcceptHeader_ = "text/css, */*";
        EDGE.imgAcceptHeader_ = "image/png, image/svg+xml, image/jxr, image/*;q=0.8, */*;q=0.5";
        EDGE.scriptAcceptHeader_ = "application/javascript, */*;q=0.8";

        // CHROME
        CHROME.applicationVersion_ = "5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36";
        CHROME.userAgent_ = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36";

        CHROME.applicationCodeName_ = "Mozilla";
        CHROME.vendor_ = "Google Inc.";
        CHROME.platform_ = PLATFORM_WIN32;
        CHROME.cpuClass_ = null;
        CHROME.productSub_ = "20030107";
        CHROME.headerNamesOrdered_ = new String[] {
            HttpHeader.HOST,
            HttpHeader.CONNECTION,
            "Upgrade-Insecure-Requests",
            HttpHeader.USER_AGENT,
            HttpHeader.ACCEPT,
            HttpHeader.REFERER,
            HttpHeader.ACCEPT_ENCODING,
            HttpHeader.ACCEPT_LANGUAGE,
            HttpHeader.COOKIE};
        CHROME.htmlAcceptHeader_ = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8";
        CHROME.imgAcceptHeader_ = "image/webp,image/apng,image/*,*/*;q=0.8";
        CHROME.cssAcceptHeader_ = "text/css,*/*;q=0.1";
        CHROME.scriptAcceptHeader_ = "*/*";
        // there are other issues with Chrome; a different productSub, etc.
        CHROME.fontHeights_ = new int[] {
            0, 1, 2, 4, 5, 5, 6, 8, 9, 10, 11, 12, 15, 16, 16, 17, 18, 20, 21, 22, 23, 25, 26, 26,
            27, 28, 30, 31, 32, 33, 34, 36, 37, 37, 38, 40, 42, 43, 44, 45, 47, 48, 48, 49, 51, 52, 53, 54, 55, 57,
            58, 58, 59, 60, 62, 63, 64, 65, 67, 69, 69, 70, 71, 73, 74, 75, 76, 77, 79, 79, 80, 81, 83, 84, 85, 86,
            87, 89, 90, 90, 91, 93, 94, 96, 97, 98, 100, 101, 101, 102, 103, 105, 106, 107, 108, 110, 111, 111, 112,
            113, 115, 116, 117, 118, 119, 121, 122, 123, 124, 126, 127, 128, 129, 130, 132, 132, 133, 134, 136, 137,
            138, 139, 140, 142, 142, 143, 144, 145, 147};

        // default file upload mime types
        CHROME.registerUploadMimeType("html", "text/html");
        CHROME.registerUploadMimeType("htm", "text/html");
        CHROME.registerUploadMimeType("css", "text/css");
        CHROME.registerUploadMimeType("xml", "text/xml");
        CHROME.registerUploadMimeType("gif", "image/gif");
        CHROME.registerUploadMimeType("jpeg", "image/jpeg");
        CHROME.registerUploadMimeType("jpg", "image/jpeg");
        CHROME.registerUploadMimeType("webp", "image/webp");
        CHROME.registerUploadMimeType("mp4", "video/mp4");
        CHROME.registerUploadMimeType("m4v", "video/mp4");
        CHROME.registerUploadMimeType("m4a", "audio/x-m4a");
        CHROME.registerUploadMimeType("mp3", "audio/mp3");
        CHROME.registerUploadMimeType("ogv", "video/ogg");
        CHROME.registerUploadMimeType("ogm", "video/ogg");
        CHROME.registerUploadMimeType("ogg", "audio/ogg");
        CHROME.registerUploadMimeType("oga", "audio/ogg");
        CHROME.registerUploadMimeType("opus", "audio/ogg");
        CHROME.registerUploadMimeType("webm", "video/webm");
        CHROME.registerUploadMimeType("wav", "audio/wav");
        CHROME.registerUploadMimeType("flac", "audio/flac");
        CHROME.registerUploadMimeType("xhtml", "application/xhtml+xml");
        CHROME.registerUploadMimeType("xht", "application/xhtml+xml");
        CHROME.registerUploadMimeType("xhtm", "application/xhtml+xml");
        CHROME.registerUploadMimeType("txt", "text/plain");
        CHROME.registerUploadMimeType("text", "text/plain");

        FIREFOX_45.registerUploadMimeType("html", "text/html");
        FIREFOX_45.registerUploadMimeType("htm", "text/html");
        FIREFOX_45.registerUploadMimeType("css", "text/css");
        FIREFOX_45.registerUploadMimeType("xml", "text/xml");
        FIREFOX_45.registerUploadMimeType("gif", "image/gif");
        FIREFOX_45.registerUploadMimeType("jpeg", "image/jpeg");
        FIREFOX_45.registerUploadMimeType("jpg", "image/jpeg");
        FIREFOX_45.registerUploadMimeType("mp4", "video/mp4");
        FIREFOX_45.registerUploadMimeType("m4v", "video/mp4");
        FIREFOX_45.registerUploadMimeType("m4a", "audio/mp4");
        FIREFOX_45.registerUploadMimeType("mp3", "audio/mpeg");
        FIREFOX_45.registerUploadMimeType("ogv", "video/ogg");
        FIREFOX_45.registerUploadMimeType("ogm", "video/x-ogm");
        FIREFOX_45.registerUploadMimeType("ogg", "video/ogg");
        FIREFOX_45.registerUploadMimeType("oga", "audio/ogg");
        FIREFOX_45.registerUploadMimeType("opus", "audio/ogg");
        FIREFOX_45.registerUploadMimeType("webm", "video/webm");
        FIREFOX_45.registerUploadMimeType("wav", "audio/wav");
        FIREFOX_45.registerUploadMimeType("flac", "audio/x-flac");
        FIREFOX_45.registerUploadMimeType("xhtml", "application/xhtml+xml");
        FIREFOX_45.registerUploadMimeType("xht", "application/xhtml+xml");
        FIREFOX_45.registerUploadMimeType("txt", "text/plain");
        FIREFOX_45.registerUploadMimeType("text", "text/plain");

        FIREFOX_52.registerUploadMimeType("html", "text/html");
        FIREFOX_52.registerUploadMimeType("htm", "text/html");
        FIREFOX_52.registerUploadMimeType("css", "text/css");
        FIREFOX_52.registerUploadMimeType("xml", "text/xml");
        FIREFOX_52.registerUploadMimeType("gif", "image/gif");
        FIREFOX_52.registerUploadMimeType("jpeg", "image/jpeg");
        FIREFOX_52.registerUploadMimeType("jpg", "image/jpeg");
        FIREFOX_52.registerUploadMimeType("mp4", "video/mp4");
        FIREFOX_52.registerUploadMimeType("m4v", "video/mp4");
        FIREFOX_52.registerUploadMimeType("m4a", "audio/mp4");
        FIREFOX_52.registerUploadMimeType("mp3", "audio/mpeg");
        FIREFOX_52.registerUploadMimeType("ogv", "video/ogg");
        FIREFOX_52.registerUploadMimeType("ogm", "video/x-ogm");
        FIREFOX_52.registerUploadMimeType("ogg", "video/ogg");
        FIREFOX_52.registerUploadMimeType("oga", "audio/ogg");
        FIREFOX_52.registerUploadMimeType("opus", "audio/ogg");
        FIREFOX_52.registerUploadMimeType("webm", "video/webm");
        FIREFOX_52.registerUploadMimeType("wav", "audio/wav");
        FIREFOX_52.registerUploadMimeType("xhtml", "application/xhtml+xml");
        FIREFOX_52.registerUploadMimeType("xht", "application/xhtml+xml");
        FIREFOX_52.registerUploadMimeType("txt", "text/plain");
        FIREFOX_52.registerUploadMimeType("text", "text/plain");

        INTERNET_EXPLORER.registerUploadMimeType("html", "text/html");
        INTERNET_EXPLORER.registerUploadMimeType("htm", "text/html");
        INTERNET_EXPLORER.registerUploadMimeType("css", "text/css");
        INTERNET_EXPLORER.registerUploadMimeType("xml", "text/xml");
        INTERNET_EXPLORER.registerUploadMimeType("gif", "image/gif");
        INTERNET_EXPLORER.registerUploadMimeType("jpeg", "image/jpeg");
        INTERNET_EXPLORER.registerUploadMimeType("jpg", "image/jpeg");
        INTERNET_EXPLORER.registerUploadMimeType("mp4", "video/mp4");
        INTERNET_EXPLORER.registerUploadMimeType("m4v", "video/mp4");
        INTERNET_EXPLORER.registerUploadMimeType("m4a", "audio/mp4");
        INTERNET_EXPLORER.registerUploadMimeType("mp3", "audio/mpeg");
        INTERNET_EXPLORER.registerUploadMimeType("ogm", "video/x-ogm");
        INTERNET_EXPLORER.registerUploadMimeType("ogg", "application/ogg");
        INTERNET_EXPLORER.registerUploadMimeType("wav", "audio/wav");
        INTERNET_EXPLORER.registerUploadMimeType("xhtml", "application/xhtml+xml");
        INTERNET_EXPLORER.registerUploadMimeType("xht", "application/xhtml+xml");
        INTERNET_EXPLORER.registerUploadMimeType("txt", "text/plain");

        EDGE.registerUploadMimeType("html", "text/html");
        EDGE.registerUploadMimeType("htm", "text/html");
        EDGE.registerUploadMimeType("css", "text/css");
        EDGE.registerUploadMimeType("xml", "text/xml");
        EDGE.registerUploadMimeType("gif", "image/gif");
        EDGE.registerUploadMimeType("jpeg", "image/jpeg");
        EDGE.registerUploadMimeType("jpg", "image/jpeg");
        EDGE.registerUploadMimeType("mp4", "video/mp4");
        EDGE.registerUploadMimeType("m4v", "video/mp4");
        EDGE.registerUploadMimeType("m4a", "audio/mp4");
        EDGE.registerUploadMimeType("mp3", "audio/mpeg");
        EDGE.registerUploadMimeType("ogm", "video/x-ogm");
        EDGE.registerUploadMimeType("ogg", "application/ogg");
        EDGE.registerUploadMimeType("wav", "audio/wav");
        EDGE.registerUploadMimeType("xhtml", "application/xhtml+xml");
        EDGE.registerUploadMimeType("xht", "application/xhtml+xml");
        EDGE.registerUploadMimeType("txt", "text/plain");

        // flush plugin (windows version)
        PluginConfiguration flash = new PluginConfiguration("Shockwave Flash",
                "Shockwave Flash 29.0 r0", "29.0.0.113", "NPSWF32_29_0_0_113.dll");
        flash.getMimeTypes().add(new PluginConfiguration.MimeType("application/x-shockwave-flash",
                "Shockwave Flash", "swf"));
        FIREFOX_45.plugins_.add(flash);

        flash = new PluginConfiguration("Shockwave Flash",
                "Shockwave Flash 29.0 r0", "29.0.0.113", "NPSWF64_29_0_0_113.dll");
        flash.getMimeTypes().add(new PluginConfiguration.MimeType("application/x-shockwave-flash",
                "Shockwave Flash", "swf"));
        FIREFOX_52.plugins_.add(flash);

        flash = new PluginConfiguration("Shockwave Flash",
                "Shockwave Flash 29.0 r0", "29.0.0.113", "Flash32_29_0_0_113.ocx");
        flash.getMimeTypes().add(new PluginConfiguration.MimeType("application/x-shockwave-flash",
                "Shockwave Flash", "swf"));
        INTERNET_EXPLORER.plugins_.add(flash);

        flash = new PluginConfiguration("Shockwave Flash",
                "Shockwave Flash 18.0 r0", "18.0.0.232", "Flash.ocx");
        flash.getMimeTypes().add(new PluginConfiguration.MimeType("application/x-shockwave-flash",
                "Shockwave Flash", "swf"));
        EDGE.plugins_.add(flash);
    }

    private final int browserVersionNumeric_;
    private final String nickname_;

    private String applicationCodeName_ = "Mozilla";
    private String applicationMinorVersion_ = "0";
    private String applicationName_;
    private String applicationVersion_;
    private String buildId_;
    private String productSub_;
    private String vendor_ = "";
    private String browserLanguage_ = LANGUAGE_ENGLISH_US;
    private String cpuClass_ = CPU_CLASS_X86;
    private boolean onLine_ = true;
    private String platform_ = PLATFORM_WIN64;
    private String systemLanguage_ = LANGUAGE_ENGLISH_US;
    private TimeZone systemTimezone_ = TimeZone.getTimeZone(TIMEZONE_NEW_YORK);
    private String userAgent_;
    private String userLanguage_ = LANGUAGE_ENGLISH_US;
    private final Set<PluginConfiguration> plugins_;
    private final Set<BrowserVersionFeatures> features_;
    private String htmlAcceptHeader_;
    private String imgAcceptHeader_;
    private String cssAcceptHeader_;
    private String scriptAcceptHeader_;
    private String xmlHttpRequestAcceptHeader_;
    private String[] headerNamesOrdered_;
    private int[] fontHeights_;
    private final Map<String, String> uploadMimeTypes_;

    /**
     * Creates a new browser version instance.
     *
     * @param browserVersionNumeric the floating number version of the browser
     * @param nickname the short name of the browser (like "FF52", "IE", ...) - has to be unique
     */
    private BrowserVersion(final int browserVersionNumeric, final String nickname) {
        browserVersionNumeric_ = browserVersionNumeric;
        nickname_ = nickname;

        applicationName_ = NETSCAPE;
        htmlAcceptHeader_ = "*/*";
        imgAcceptHeader_ = "*/*";
        cssAcceptHeader_ = "*/*";
        scriptAcceptHeader_ = "*/*";
        xmlHttpRequestAcceptHeader_ = "*/*";

        plugins_ = new HashSet<>();
        features_ = EnumSet.noneOf(BrowserVersionFeatures.class);
        uploadMimeTypes_ = new HashMap<>();

        initFeatures();
    }

    private void initFeatures() {
        final SupportedBrowser expectedBrowser;
        if (isChrome()) {
            expectedBrowser = SupportedBrowser.CHROME;
        }
        else if (isFirefox52()) {
            expectedBrowser = SupportedBrowser.FF52;
        }
        else if (isFirefox()) {
            expectedBrowser = SupportedBrowser.FF45;
        }
        else if (isIE()) {
            expectedBrowser = SupportedBrowser.IE;
        }
        else {
            expectedBrowser = SupportedBrowser.EDGE;
        }

        for (final BrowserVersionFeatures features : BrowserVersionFeatures.values()) {
            try {
                final Field field = BrowserVersionFeatures.class.getField(features.name());
                final BrowserFeature browserFeature = field.getAnnotation(BrowserFeature.class);
                if (browserFeature != null) {
                    for (final SupportedBrowser browser : browserFeature.value()) {
                        if (AbstractJavaScriptConfiguration.isCompatible(expectedBrowser, browser)) {
                            features_.add(features);
                        }
                    }
                }
            }
            catch (final NoSuchFieldException e) {
                // should never happen
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Returns the default browser version that is used whenever a specific version isn't specified.
     * Defaults to {@link #BEST_SUPPORTED}.
     * @return the default browser version
     */
    public static BrowserVersion getDefault() {
        return DefaultBrowserVersion_;
    }

    /**
     * Sets the default browser version that is used whenever a specific version isn't specified.
     * @param newBrowserVersion the new default browser version
     */
    public static void setDefault(final BrowserVersion newBrowserVersion) {
        WebAssert.notNull("newBrowserVersion", newBrowserVersion);
        DefaultBrowserVersion_ = newBrowserVersion;
    }

    /**
     * Returns {@code true} if this <tt>BrowserVersion</tt> instance represents some
     * version of Internet Explorer.
     * @return whether or not this version is a version of IE
     */
    public boolean isIE() {
        return getNickname().startsWith("IE");
    }

    /**
     * Returns {@code true} if this <tt>BrowserVersion</tt> instance represents some
     * version of Google Chrome. Note that Google Chrome does not return 'Chrome'
     * in the application name, we have to look in the nickname.
     * @return whether or not this version is a version of a Chrome browser
     */
    public boolean isChrome() {
        return getNickname().startsWith("Chrome");
    }

    /**
     * Returns {@code true} if this <tt>BrowserVersion</tt> instance represents some
     * version of Microsoft Edge.
     * @return whether or not this version is a version of an Edge browser
     */
    public boolean isEdge() {
        return getNickname().startsWith("Edge");
    }

    /**
     * Returns {@code true} if this <tt>BrowserVersion</tt> instance represents some
     * version of Firefox.
     * @return whether or not this version is a version of a Firefox browser
     */
    public boolean isFirefox() {
        return getNickname().startsWith("FF");
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br>
     * @return whether or not this version version 52 of a Firefox browser
     */
    public boolean isFirefox52() {
        return isFirefox() && getBrowserVersionNumeric() == 52;
    }

    /**
     * Returns the short name of the browser like {@code FF3}, {@code IE}, etc.
     *
     * @return the short name (if any)
     */
    public String getNickname() {
        return nickname_;
    }

    /**
     * @return the browserVersionNumeric
     */
    public int getBrowserVersionNumeric() {
        return browserVersionNumeric_;
    }

    /**
     * Returns the application code name, for example "Mozilla".
     * Default value is "Mozilla" if not explicitly configured.
     * @return the application code name
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms533077.aspx">MSDN documentation</a>
     */
    public String getApplicationCodeName() {
        return applicationCodeName_;
    }

    /**
     * Returns the application minor version, for example "0".
     * Default value is "0" if not explicitly configured.
     * @return the application minor version
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms533078.aspx">MSDN documentation</a>
     */
    public String getApplicationMinorVersion() {
        return applicationMinorVersion_;
    }

    /**
     * Returns the application name, for example "Microsoft Internet Explorer".
     * @return the application name
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms533079.aspx">MSDN documentation</a>
     */
    public String getApplicationName() {
        return applicationName_;
    }

    /**
     * Returns the application version, for example "4.0 (compatible; MSIE 6.0b; Windows 98)".
     * @return the application version
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms533080.aspx">MSDN documentation</a>
     */
    public String getApplicationVersion() {
        return applicationVersion_;
    }

    /**
     * @return the vendor
     */
    public String getVendor() {
        return vendor_;
    }

    /**
     * Returns the browser application language, for example "en-us".
     * Default value is {@link #LANGUAGE_ENGLISH_US} if not explicitly configured.
     * @return the browser application language
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms533542.aspx">MSDN documentation</a>
     */
    public String getBrowserLanguage() {
        return browserLanguage_;
    }

    /**
     * Returns the type of CPU in the machine, for example "x86".
     * Default value is {@link #CPU_CLASS_X86} if not explicitly configured.
     * @return the type of CPU in the machine
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms533697.aspx">MSDN documentation</a>
     */
    public String getCpuClass() {
        return cpuClass_;
    }

    /**
     * Returns {@code true} if the browser is currently online.
     * Default value is {@code true} if not explicitly configured.
     * @return {@code true} if the browser is currently online
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms534307.aspx">MSDN documentation</a>
     */
    public boolean isOnLine() {
        return onLine_;
    }

    /**
     * Returns the platform on which the application is running, for example "Win32".
     * Default value is {@link #PLATFORM_WIN32} if not explicitly configured.
     * @return the platform on which the application is running
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms534340.aspx">MSDN documentation</a>
     */
    public String getPlatform() {
        return platform_;
    }

    /**
     * Returns the system language, for example "en-us".
     * Default value is {@link #LANGUAGE_ENGLISH_US} if not explicitly configured.
     * @return the system language
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms534653.aspx">MSDN documentation</a>
     */
    public String getSystemLanguage() {
        return systemLanguage_;
    }

    /**
     * Returns the system {@link TimeZone}.
     * Default value is {@code America/New_York} if not explicitly configured.
     * @return the system {@link TimeZone}
     */
    public TimeZone getSystemTimezone() {
        return systemTimezone_;
    }

    /**
     * Returns the user agent string, for example "Mozilla/4.0 (compatible; MSIE 6.0b; Windows 98)".
     * @return the user agent string
     */
    public String getUserAgent() {
        return userAgent_;
    }

    /**
     * Returns the user language, for example "en-us".
     * Default value is {@link #LANGUAGE_ENGLISH_US} if not explicitly configured.
     * @return the user language
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms534713.aspx">MSDN documentation</a>
     */
    public String getUserLanguage() {
        return userLanguage_;
    }

    /**
     * Returns the value used by the browser for the {@code Accept} header if requesting a page.
     * @return the accept header string
     */
    public String getHtmlAcceptHeader() {
        return htmlAcceptHeader_;
    }

    /**
     * Returns the value used by the browser for the {@code Accept} header
     * if requesting a script.
     * @return the accept header string
     */
    public String getScriptAcceptHeader() {
        return scriptAcceptHeader_;
    }

    /**
     * Returns the value used by the browser for the {@code Accept} header
     * if performing an XMLHttpRequest.
     * @return the accept header string
     */
    public String getXmlHttpRequestAcceptHeader() {
        return xmlHttpRequestAcceptHeader_;
    }

    /**
     * Returns the value used by the browser for the {@code Accept} header
     * if requesting an image.
     * @return the accept header string
     */
    public String getImgAcceptHeader() {
        return imgAcceptHeader_;
    }

    /**
     * Returns the value used by the browser for the {@code Accept} header
     * if requesting a CSS declaration.
     * @return the accept header string
     */
    public String getCssAcceptHeader() {
        return cssAcceptHeader_;
    }

    /**
     * Returns the available plugins. This makes only sense for Firefox as only this
     * browser makes this kind of information available via JavaScript.
     * @return the available plugins
     */
    public Set<PluginConfiguration> getPlugins() {
        return plugins_;
    }

    /**
     * Indicates if this instance has the given feature. Used for HtmlUnit internal processing.
     * @param property the property name
     * @return {@code false} if this browser doesn't have this feature
     */
    public boolean hasFeature(final BrowserVersionFeatures property) {
        return features_.contains(property);
    }

    /**
     * Returns the buildId.
     * @return the buildId
     */
    public String getBuildId() {
        return buildId_;
    }

    /**
     * Returns the productSub.
     * @return the buildId
     */
    public String getProductSub() {
        return productSub_;
    }

    /**
     * Gets the headers names, so they are sent in the given order (if included in the request).
     * @return headerNames the header names in ordered manner
     */
    public String[] getHeaderNamesOrdered() {
        return headerNamesOrdered_;
    }

    /**
     * Registers a new mime type for the provided file extension.
     * @param fileExtension the file extension used to determine the mime type
     * @param mimeType the mime type to be used when uploading files with this extension
     */
    public void registerUploadMimeType(final String fileExtension, final String mimeType) {
        if (fileExtension != null) {
            uploadMimeTypes_.put(fileExtension.toLowerCase(Locale.ROOT), mimeType);
        }
    }

    /**
     * Determines the content type for the given file.
     * @param file the file
     * @return a content type or an empty string if unknown
     */
    public String getUploadMimeType(final File file) {
        if (file == null) {
            return "";
        }

        final String fileExtension = FilenameUtils.getExtension(file.getName());

        final String mimeType = uploadMimeTypes_.get(fileExtension.toLowerCase(Locale.ROOT));
        if (mimeType != null) {
            return mimeType;
        }
        return "";
    }

    /**
     * Returns the corresponding height of the specified {@code fontSize}
     * @param fontSize the font size
     * @return the corresponding height
     */
    public int getFontHeight(final String fontSize) {
        if (fontHeights_ == null) {
            return 18;
        }
        final int fontSizeInt = Integer.parseInt(fontSize.substring(0, fontSize.length() - 2));
        if (fontSizeInt < fontHeights_.length) {
            return fontHeights_[fontSizeInt];
        }
        return (int) (fontSizeInt * 1.2);
    }

    /**
     * @return the pixesPerChar based on the specified {@code fontSize}
     */
    public int getPixesPerChar() {
        return 10;
    }

    @Override
    public String toString() {
        return nickname_;
    }

    /**
     * Because BrowserVersion is immutable we need a builder
     * for this complex object setup.
     */
    public static class BrowserVersionBuilder {
        private final BrowserVersion workPiece_;

        /**
         * Creates a new BrowserVersionBuilder using the given browser version
         * as template for the browser to be constructed.
         * @param version the blueprint
         */
        public BrowserVersionBuilder(final BrowserVersion version) {
            workPiece_ = new BrowserVersion(version.getBrowserVersionNumeric(), version.getNickname());

            setApplicationVersion(version.getApplicationVersion())
                .setUserAgent(version.getUserAgent())
                .setApplicationName(version.getApplicationName())
                .setApplicationCodeName(version.getApplicationCodeName())
                .setApplicationMinorVersion(version.getApplicationMinorVersion())
                .setVendor(version.getVendor())
                .setBrowserLanguage(version.getBrowserLanguage())
                .setCpuClass(version.getCpuClass())
                .setOnLine(version.isOnLine())
                .setPlatform(version.getPlatform())
                .setSystemLanguage(version.getSystemLanguage())
                .setSystemTimezone(version.getSystemTimezone())
                .setUserLanguage(version.getUserLanguage());

            workPiece_.buildId_ = version.getBuildId();
            workPiece_.productSub_ = version.getProductSub();
            workPiece_.htmlAcceptHeader_ = version.getHtmlAcceptHeader();
            workPiece_.imgAcceptHeader_ = version.getImgAcceptHeader();
            workPiece_.cssAcceptHeader_ = version.getCssAcceptHeader();
            workPiece_.scriptAcceptHeader_ = version.getScriptAcceptHeader();
            workPiece_.xmlHttpRequestAcceptHeader_ = version.getXmlHttpRequestAcceptHeader();
            workPiece_.headerNamesOrdered_ = version.getHeaderNamesOrdered();
            workPiece_.fontHeights_ = version.fontHeights_;

            for (final PluginConfiguration pluginConf : version.getPlugins()) {
                workPiece_.plugins_.add(pluginConf.clone());
            }

            workPiece_.features_.addAll(version.features_);
            workPiece_.uploadMimeTypes_.putAll(version.uploadMimeTypes_);
        }

        /**
         * @return the new immutable browser version
         */
        public BrowserVersion build() {
            return workPiece_;
        }

        /**
         * @param applicationMinorVersion the applicationMinorVersion to set
         * @return this for fluent use
         */
        public BrowserVersionBuilder setApplicationMinorVersion(final String applicationMinorVersion) {
            workPiece_.applicationMinorVersion_ = applicationMinorVersion;
            return this;
        }

        /**
         * @param applicationName the applicationName to set
         * @return this for fluent use
         */
        public BrowserVersionBuilder setApplicationName(final String applicationName) {
            workPiece_.applicationName_ = applicationName;
            return this;
        }

        /**
         * @param applicationVersion the applicationVersion to set
         * @return this for fluent use
         */
        public BrowserVersionBuilder setApplicationVersion(final String applicationVersion) {
            workPiece_.applicationVersion_ = applicationVersion;
            return this;
        }

        /**
         * @param vendor the vendor to set
         * @return this for fluent use
         */
        public BrowserVersionBuilder setVendor(final String vendor) {
            workPiece_.vendor_ = vendor;
            return this;
        }

        /**
         * @param applicationCodeName the applicationCodeName to set
         * @return this for fluent use
         */
        public BrowserVersionBuilder setApplicationCodeName(final String applicationCodeName) {
            workPiece_.applicationCodeName_ = applicationCodeName;
            return this;
        }

        /**
         * @param browserLanguage the browserLanguage to set
         * @return this for fluent use
         */
        public BrowserVersionBuilder setBrowserLanguage(final String browserLanguage) {
            workPiece_.browserLanguage_ = browserLanguage;
            return this;
        }

        /**
         * @param cpuClass the cpuClass to set
         * @return this for fluent use
         */
        public BrowserVersionBuilder setCpuClass(final String cpuClass) {
            workPiece_.cpuClass_ = cpuClass;
            return this;
        }

        /**
         * @param onLine the onLine to set
         * @return this for fluent use
         */
        public BrowserVersionBuilder setOnLine(final boolean onLine) {
            workPiece_.onLine_ = onLine;
            return this;
        }

        /**
         * @param platform the platform to set
         * @return this for fluent use
         */
        public BrowserVersionBuilder setPlatform(final String platform) {
            workPiece_.platform_ = platform;
            return this;
        }

        /**
         * @param systemLanguage the systemLanguage to set
         * @return this for fluent use
         */
        public BrowserVersionBuilder setSystemLanguage(final String systemLanguage) {
            workPiece_.systemLanguage_ = systemLanguage;
            return this;
        }

        /**
         * @param systemTimezone the systemTimezone to set
         * @return this for fluent use
         */
        public BrowserVersionBuilder setSystemTimezone(final TimeZone systemTimezone) {
            workPiece_.systemTimezone_ = systemTimezone;
            return this;
        }

        /**
         * @param userAgent the userAgent to set
         * @return this for fluent use
         */
        public BrowserVersionBuilder setUserAgent(final String userAgent) {
            workPiece_.userAgent_ = userAgent;
            return this;
        }

        /**
         * @param userLanguage the userLanguage to set
         * @return this for fluent use
         */
        public BrowserVersionBuilder setUserLanguage(final String userLanguage) {
            workPiece_.userLanguage_ = userLanguage;
            return this;
        }

        /**
         * @param htmlAcceptHeader the {@code Accept} header to be used when retrieving pages
         * @return this for fluent use
         */
        public BrowserVersionBuilder setHtmlAcceptHeader(final String htmlAcceptHeader) {
            workPiece_.htmlAcceptHeader_ = htmlAcceptHeader;
            return this;
        }

        /**
         * @param imgAcceptHeader the {@code Accept} header to be used when retrieving images
         * @return this for fluent use
         */
        public BrowserVersionBuilder setImgAcceptHeader(final String imgAcceptHeader) {
            workPiece_.imgAcceptHeader_ = imgAcceptHeader;
            return this;
        }

        /**
         * @param cssAcceptHeader the {@code Accept} header to be used when retrieving pages
         * @return this for fluent use
         */
        public BrowserVersionBuilder setCssAcceptHeader(final String cssAcceptHeader) {
            workPiece_.cssAcceptHeader_ = cssAcceptHeader;
            return this;
        }

        /**
         * @param scriptAcceptHeader the {@code Accept} header to be used when retrieving scripts
         * @return this for fluent use
         */
        public BrowserVersionBuilder setScriptAcceptHeader(final String scriptAcceptHeader) {
            workPiece_.scriptAcceptHeader_ = scriptAcceptHeader;
            return this;
        }

        /**
         * @param xmlHttpRequestAcceptHeader the {@code Accept} header to be used when
         * performing XMLHttpRequests
         * @return this for fluent use
         */
        public BrowserVersionBuilder setXmlHttpRequestAcceptHeader(final String xmlHttpRequestAcceptHeader) {
            workPiece_.xmlHttpRequestAcceptHeader_ = xmlHttpRequestAcceptHeader;
            return this;
        }
    }
}
