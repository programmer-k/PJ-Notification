package org.steinsapk.pjnotification;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Crawling {
    private WebClient webClient;
    private HtmlPage page;
    private Database db;
    private Context context;

    public Crawling(Context context, SQLiteDatabase db) {
        this.context = context;

        // WebClient 초기화
        webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER);
        debugLog("WebClient Created!");

        // WebClient 옵션 설정 - 에러 메시지 안 나오게
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        // 비밀번호 변경 창 무시
        webClient.getOptions().setPopupBlockerEnabled(true);

        // 필드 초기화
        this.db = new Database(db);
    }

    public boolean login(String id, String pw) throws Exception {
        // 로그인 페이지 연결
        page = webClient.getPage("https://yscec.yonsei.ac.kr/login/index.php");
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

        // 로그인이 성공했는지 확인
        return page.getTitleText().equals("YSCEC");
    }

    // 강의 목록과 링크를 DB에 저장한다.
    public void saveCourseList() {
        // 디버깅용 코드 - 학기 바꾸기.
        /*
        try {
            page = webClient.getPage("https://yscec.yonsei.ac.kr/my/?year=2018&term=2");
        } catch (IOException e) {
            debugLog(e.toString());
        }
        */
        // 수강 변경, 철회 등을 대비해 기존 데이터 지우기
        db.clearCourse();

        // 모든 강의에 대해서 링크 페이지와 강의 이름 찾기
        DomNodeList<DomNode> courseList = page.querySelectorAll("h3.coursename");

        for (DomNode node : courseList) {
            HtmlAnchor htmlAnchor = (HtmlAnchor) node.getFirstChild();
            String courseLink = htmlAnchor.getAttribute("href");
            String courseName = htmlAnchor.asText();

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
            HtmlPage coursePage = webClient.getPage(courseLink);

            // 게시판 링크 찾아서 들어가기
            DomNodeList<DomNode> spanElements = coursePage.querySelectorAll(".instancename");

            for (int i = 0; i < spanElements.size(); i++) {
                HtmlSpan spanElement = (HtmlSpan) spanElements.get(i);

                String boardName = spanElement.asText();
                String itemAttribute = null;
                try {
                    itemAttribute = spanElement.querySelector(".accesshide").asText();
                } catch (NullPointerException e) {
                    // accesshide가 없는 참고문헌은 무시하기.
                    continue;
                }

                // 게시판만 루프 돌기
                if (itemAttribute.equals("게시판(일반)") || itemAttribute.equals("Forum(General)")) {
                    HtmlPage noticeListPage = spanElement.click();

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
            HtmlPage coursePage = webClient.getPage(courseLink);

            // Anchor 요소를 찾는다.
            DomNodeList<DomNode> list = coursePage.querySelectorAll("ul.section.img-text a");

            for (int i = 1; i < list.size(); i++) {
                // anchor 가져오기
                HtmlAnchor anchor = (HtmlAnchor) list.get(i);

                // itemName, itemLink, itemAttribute 가져오기
                String itemLink = anchor.getHrefAttribute();
                String itemAttribute = null;
                try {
                    itemAttribute = anchor.querySelector(".accesshide").asText();
                } catch (NullPointerException e) {
                    // accesshide가 없는 참고문헌은 무시하기.
                    continue;
                }
                String itemName = anchor.querySelector(".instancename").asText();
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
                    HtmlPage page = webClient.getPage(itemLink);
                    itemContents = page.asXml();
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

    private void loopNotice(HtmlPage noticeListPage, String courseName, String boardName) throws Exception {
        boolean first = true;
        HtmlPage noticePage = null;
        HtmlAnchor anchor;

        // 순회하면서 기록하기
        while (true) {
            if (first) {
                // class="thread-post-title"인 h1 태그를 찾는다.
                DomNode domNode = noticeListPage.querySelector("li:not([class]) h1.thread-post-title");

                // 링크 얻기
                try {
                    anchor = (HtmlAnchor) domNode.getFirstChild();
                } catch (NullPointerException e) {
                    debugLog("공지가 하나도 없습니다!");
                    break;
                } catch (ClassCastException e) {
                    debugLog("비공개 글입니다.");
                    break;
                }

                // 공지 페이지 들어가기
                noticePage = anchor.click();

                // 다시 들어가기
                anchor = noticePage.querySelector(".title.col-2").querySelector("a");
                noticePage = webClient.getPage(anchor.getAttribute("href"));
            } else {
                // 그 다음 페이지 들어가기 - 마지막 tr을 선택
                DomNode tr = noticePage.querySelector("div.table-footer-area + table > thead > tr:last-child");

                // 댓글 있는 게시글
                if (tr == null)
                    tr = noticePage.querySelector("div.table-reply-area + table > thead > tr:last-child");

                // <a> 선택 후 페이지 이동
                anchor = tr.querySelector("a");
                try {
                    noticePage = webClient.getPage(anchor.getAttribute("href"));
                } catch (MalformedURLException malformedURLException) {
                    // 비공개 글
                    break;
                }
            }

            // 공지 제목, 글, 날짜, 링크 주소, 첨부 파일 얻기
            String noticeTitle = noticePage.querySelector("span.detail-title").asText();
            String noticeContents = noticePage.querySelector("div.detail-contents").asText();
            String noticeDate = noticePage.querySelector("span.detail-date").asText();
            String noticeLink = noticePage.getUrl().toString();
            String attachmentFiles = "";


            try {
                attachmentFiles = noticePage.querySelector("ul.detail-attachment").asText();
            } catch (NullPointerException e) {
                // 첨부 파일이 없을 경우
            }


            if (!insertNotice(courseName, noticeTitle, noticeContents, noticeDate, noticeLink, attachmentFiles, boardName))
                break;

            // 알림 생성하기
            Notification.makeNotification(courseName, noticeTitle, context, true, "", boardName);

            // 탈출 조건
            HtmlElement htmlElement = noticePage.querySelector("div.table-footer-area + table");

            // 댓글 있는 게시글
            if (htmlElement == null) {
                htmlElement = noticePage.querySelector("div.table-reply-area + table");
            }

            // 첫 글하고 마지막 글은 <tr> 태그가 3개이다.
            // 첫 글이자 마지막 글(글이 하나 일 때)은 <tr> 태그가 2개이다.
            if ((!first && htmlElement.getElementsByTagName("tr").getLength() == 3) || (first && htmlElement.getElementsByTagName("tr").getLength() == 2))
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
}
