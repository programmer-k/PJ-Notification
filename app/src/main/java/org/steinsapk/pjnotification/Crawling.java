package org.steinsapk.pjnotification;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.Cookie;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Crawling {
    private Map<String, String> cookies;
    private Database db;
    private Context context;

    public Crawling(Context context, SQLiteDatabase db) {
        cookies = new HashMap<String, String>();
        this.context = context;
        this.db = new Database(db);
    }

    public boolean login(String id, String pw) throws Exception {
        WebClient webClient = initializeWebClient();

        // 로그인 페이지 연결
        HtmlPage page = webClient.getPage("https://yscec.yonsei.ac.kr/login/index.php");
        debugLog("Get Login Page!");

        // 로그인 Form 객체 가져오기
        HtmlForm form = page.getFormByName("ssoLoginForm");

        // 아이디, 비밀번호, 제출 버튼 input 객체 가져오기
        HtmlTextInput userName = form.getInputByName("username");
        HtmlPasswordInput password = form.getInputByName("password");
        HtmlSubmitInput loginButton = (HtmlSubmitInput) page.getElementById("loginbtn");

        // 아이디, 비밀번호 채워 넣기
        userName.type(id);
        password.type(pw);

        // 제출해서 결과 페이지 얻어내기
        page = loginButton.click();
        debugLog("Get myPage!");

        // 쿠키 저장
        for (Cookie cookie : webClient.getCookieManager().getCookies()) {
            cookies.put(cookie.getName(), cookie.getValue());
        }

        // 로그인이 성공했는지 확인
        return page.getTitleText().equals("YSCEC");
    }

    // 강의 목록과 링크를 DB에 저장한다.
    public void saveCourseList() throws IOException {
        // My page 접속
        Document document = Jsoup.connect("https://yscec.yonsei.ac.kr/my/").cookies(cookies).get();

        // 디버깅용 코드 - 학기 바꾸기
        //document = Jsoup.connect("https://yscec.yonsei.ac.kr/my/?year=2018&term=1").cookies(cookies).get();

        // 수강 변경, 철회 등을 대비해 기존 데이터 지우기
        db.clearCourse();

        // 모든 강의에 대해서 링크 페이지와 강의 이름 찾기
        Elements elements = document.select("h3.coursename");

        for (Element element : elements) {
            Element courseElement = element.child(0);
            String courseName = courseElement.text();
            String courseLink = courseElement.attr("href");

            // DB에 추가
            db.insertCourse(courseName, courseLink);
        }
    }

    public void saveCourseNotice() throws Exception {
        // 각 강의마다 공지 목록을 가져오고 저장하기
        Cursor cursor = db.getCourseNameAndLink();
        while (cursor.moveToNext()) {
            String courseName = cursor.getString(0);
            String courseLink = cursor.getString(1);

            // 디버깅용 출력
            debugLog(courseName + " DB 저장 시작");

            // 강의 페이지 접속
            Document coursePage = Jsoup.connect(courseLink).cookies(cookies).get();

            // 게시판 링크 찾아서 들어가기
            Elements spanElements = coursePage.select(".instancename");

            for (Element spanElement : spanElements) {
                String boardName = spanElement.text();
                Element accessHide = spanElement.selectFirst(".accesshide");

                if (accessHide == null) {
                    continue;
                }

                String itemAttribute = accessHide.text();


                // 게시판만 루프 돌기
                if (itemAttribute.equals("게시판(일반)") || itemAttribute.equals("Forum(General)")) {
                    String noticeListLink = spanElement.parent().attr("href");
                    Document noticeListPage = Jsoup.connect(noticeListLink).cookies(cookies).get();

                    // 각 공지사항에 대해서 루프 돌면서 DB에 저장하기
                    loopNotice(noticeListPage, courseName, boardName);
                }
            }
        }

        cursor.close();
    }

    public void saveCourseItem() throws Exception {
        Cursor cursor = db.getCourseNameAndLink();
        while (cursor.moveToNext()) {
            String courseName = cursor.getString(0);
            String courseLink = cursor.getString(1);

            // 디버깅용 출력
            debugLog(courseName + " ITEM UPDATE 시작");

            // 강의 페이지 접속
            Document coursePage = Jsoup.connect(courseLink).cookies(cookies).get();

            // Anchor 요소를 찾는다.
            Elements anchors = coursePage.select("ul.section.img-text a");

            for (Element anchor : anchors.subList(1, anchors.size())) {
                String itemLink = anchor.attr("href");

                Element accessHide = anchor.selectFirst(".accesshide");
                if (accessHide == null) {
                    // accesshide가 없는 참고문헌은 무시하기.
                    continue;
                }
                String itemAttribute = accessHide.text();

                //String itemName = anchor.querySelector(".instancename").asText();
                String itemName = anchor.selectFirst(".instancename").text();
                itemName = itemName.substring(0, itemName.indexOf(itemAttribute));
                String itemContents = "";

                debugLog(itemName);
                //debugLog(itemLink);
                //debugLog(itemAttribute);

                // 게시판은 패스
                if (itemAttribute.equals("게시판(일반)") || itemAttribute.equals("Forum(General)"))
                    continue;

                // itemAttribute가 파일이라면 접속하지 말기.
                if (!(itemAttribute.equals("파일") || itemAttribute.equals("File"))) {
                    Document page = Jsoup.connect(itemLink).cookies(cookies).get();
                    itemContents = page.toString();
                }

                // DB에 넣기
                if (db.insertItem(courseName, itemName, itemContents, itemLink, itemAttribute))
                    // 알림 생성하기
                    Notification.makeNotification(courseName, itemName, context, false, itemName, "");
            }
        }

        cursor.close();
    }

    private long parseDate(String noticeDate) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일, EEE, a KK:mm", Locale.KOREA);
        String target;
        Date date;

        try {
            target = noticeDate.substring(0, noticeDate.indexOf(" 에"));
            date =  dateFormat.parse(target);
        } catch (IndexOutOfBoundsException e) {
            // 영어 수업이어서 발생하는 Exception
            target = noticeDate.substring(noticeDate.indexOf("- ") + 2);
            date = dateFormat.parse(target);
        }

        return date.getTime();
    }

    private void loopNotice(Document noticeListPage, String courseName, String boardName) throws Exception {
        boolean first = true;
        Document noticePage = null;
        Element anchor;

        // 순회하면서 기록하기
        while (true) {
            if (first) {
                // class="thread-post-title"인 h1 태그를 찾는다.
                Elements noticeTitles = noticeListPage.select("li:not([class]) h1.thread-post-title");

                // 링크 얻기
                anchor = noticeTitles.first();
                if (anchor == null) {
                    debugLog("공지가 하나도 없습니다!");
                    break;
                }

                anchor = anchor.selectFirst("a[onclick]");
                if (anchor == null) {
                    debugLog("비공개 글입니다!");
                    break;
                }

                // 공지 페이지 들어가기
                String contentId = anchor.attr("onclick").replaceAll("\\D+","");
                String noticePageUrl = "https://yscec.yonsei.ac.kr/mod/jinotechboard/content.php?contentId=" + contentId + "&b=&boardform=1";
                noticePage = Jsoup.connect(noticePageUrl).cookies(cookies).get();
            } else {
                // 그 다음 페이지 들어가기 - 마지막 tr을 선택
                Element tr = noticePage.selectFirst("div.table-footer-area + table > thead > tr:last-child");

                // 댓글 있는 게시글
                if (tr == null)
                    tr = noticePage.selectFirst("div.table-reply-area + table > thead > tr:last-child");

                // <a> 선택 후 페이지 이동
                anchor = tr.selectFirst("a");
                String nextLink = anchor.attr("href");

                if (nextLink.equals("#")) {
                    // 비공개 글
                    break;
                }

                noticePage = Jsoup.connect(nextLink).cookies(cookies).get();
            }

            // 공지 제목, 글, 날짜, 링크 주소, 첨부 파일 얻기
            String noticeTitle = noticePage.selectFirst("span.detail-title").text();
            String noticeContents = noticePage.selectFirst("div.detail-contents").wholeText();
            String noticeDate = noticePage.selectFirst("span.detail-date").text();
            String noticeLink = noticePage.baseUri();
            String attachmentFiles = "";

            Element attachment = noticePage.selectFirst("ul.detail-attachment");
            if (attachment != null) {
                attachmentFiles = attachment.text();
            }

            if (!insertNotice(courseName, noticeTitle, noticeContents, noticeDate, noticeLink, attachmentFiles, boardName))
                break;

            // 현재 루프를 도는 board가 Disabled Board List에 있는지 확인하기
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String[] notificationDisabledList = preferences.getString("notificationDisabledList", "").split(",");
            boolean createNotification = true;
            for (int i = 0; i < notificationDisabledList.length; i++)
                if (boardName.contains(notificationDisabledList[i]))
                    createNotification = false;

            // Disabled Board List에 없을 때만 알림 생성하기
            if (createNotification)
                Notification.makeNotification(courseName, noticeTitle, context, true, "", boardName);

            // 탈출 조건
            Element htmlElement = noticePage.selectFirst("div.table-footer-area + table");

            // 댓글 있는 게시글
            if (htmlElement == null) {
                htmlElement = noticePage.selectFirst("div.table-reply-area + table");
            }

            // 첫 글하고 마지막 글은 <tr> 태그가 3개이다.
            // 첫 글이자 마지막 글(글이 하나 일 때)은 <tr> 태그가 2개이다.
            if ((!first && htmlElement.getElementsByTag("tr").size() == 3) || (first && htmlElement.getElementsByTag("tr").size() == 2))
                break;

            first = false;
        }
    }

    private boolean insertNotice(String courseName, String noticeTitle, String noticeContents, String noticeDate, String noticeLink, String attachmentFiles, String boardName) throws ParseException {
        // DB에 넣기
        return db.insertNotice(courseName, noticeTitle, noticeContents, parseDate(noticeDate), noticeLink, attachmentFiles, boardName);
    }

    public void closeDB() {
        db.closeDB();
    }

    // 디버깅용 로그 출력 함수
    private static void debugLog(String log) {
        Log.e("TAG", log);
    }

    private WebClient initializeWebClient() {
        // WebClient 초기화
        WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER);
        debugLog("WebClient Created!");

        // WebClient 옵션 설정 - 에러 메시지 안 나오게
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        // 비밀번호 변경 창 무시
        webClient.getOptions().setPopupBlockerEnabled(true);

        return webClient;
    }
}
