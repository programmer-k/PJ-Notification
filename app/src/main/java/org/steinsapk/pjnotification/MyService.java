package org.steinsapk.pjnotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateUtils;
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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyService extends JobService {
    public static final int JOB_ID = 1;

    UpdateAsyncTask updateAsyncTask;
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        updateAsyncTask = new UpdateAsyncTask(getApplicationContext(), false, this, jobParameters);
        updateAsyncTask.execute();

        // 네트워크 작업이어서 시간이 걸리므로 true 반환하기.
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        // cancel 메서드를 호출한다고 해서 AsyncTask가 멈추지는 않는다.
        // 루틴을 돌리는 중간 중간에 isCancelled() 메서드를 호출해서 취소가 되었는지 확인하고
        // 만약 그렇다면 내가 직접 종료와 그에 필요한 작업들을 처리해야 한다.
        // 현재는 확인을 하지 않기 때문에 이 코드는 아무런 효과를 가지지 않는다.
        // 따라서 Job을 돌릴 조건이 작업 도중에 만족되지 않을 때(Wi-Fi에서 LTE로 바뀐다던가)에도,
        // 바로 작업을 종료하지 않고, 그대로 동작하므로 데이터를 소진한다.
        updateAsyncTask.cancel(true);

        // Job이 실패하면, true를 반환해서, 재시작을 한다.
       return true;
    }

    static public void registerJobService(Context context) {
        // JobScheduler 객체 가져오기
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        // 설정 값을 가져올 수 있는 SharedPreferences 객체 생성
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        int network = preferences.getBoolean("LTE", false) ? JobInfo.NETWORK_TYPE_ANY : JobInfo.NETWORK_TYPE_UNMETERED;
        // 설정 페이지를 들어가지 않았으면 값을 불러올 수 없다.
        int period = Integer.parseInt(preferences.getString("updatePeriod", "15"));

        Log.e("TAG", Integer.toString(network));
        Log.e("TAG", Integer.toString(period));


        // JobInfo 객체 생성
        JobInfo jobInfo = new JobInfo.Builder(MyService.JOB_ID, new ComponentName(context, MyService.class))
                .setRequiredNetworkType(network)
                .setPeriodic(DateUtils.MINUTE_IN_MILLIS * period, DateUtils.MINUTE_IN_MILLIS * 10)
                .setPersisted(true)
                .build();

        // JobScheduler 객체에 작업(JobInfo) 등록
        jobScheduler.schedule(jobInfo);
    }

    public class DatabaseUpdate {
        String idInput;
        String passwordInput;
        WebClient webClient;
        SharedPreferences userInfo;
        HtmlPage page;
        List<String> courseLinkList;
        List<String> courseNameList;
        SQLiteDatabase db;
        JobParameters jobParameters;
        Thread thread;

        public DatabaseUpdate(JobParameters jobParameters) {

            // JobParameters 저장
            this.jobParameters = jobParameters;

            // DB 열기
            db = Database.openDatabase(getApplicationContext());

            // 테이블 만들기
            db.execSQL("CREATE TABLE IF NOT EXISTS NOTICE(COURSENAME TEXT, NOTICETITLE TEXT, NOTICECONTENTS TEXT);");

            // SharedPreferences 객체 가져오기
            userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);

            // 아이디와 비밀번호 가져오기
            loadUserData();

            // Toast.makeText(getApplicationContext(), "test.", Toast.LENGTH_LONG);

            // WebClient 생성
            webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER);
            Log.e("TAG", "WebClient Created!");

            // WebClient 옵션 설정 - 에러 메시지 안 나오게
            // webClient.getOptions().setRedirectEnabled(false);
            // webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setPopupBlockerEnabled(true);
            Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        }

        // 작업 취소 함수
        public void stopUpdate() {
            Log.e("TAG", "STOPPED UPDATE");
            thread.interrupt();
        }

        // DB Update를 하는 함수
        public void DBUpdate() {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // 데이터를 넣었으므로 알림을 띄워준다.
                    createNotificationChannel();

                    Log.e("TAG", "new Thread start!");
                    if (login(idInput, passwordInput)) {
                        // 로그인 성공
                        DebugPrint("로그인 성공");

                        // 모든 데이터 가져오기
                        getAllData();
                        db.close();

                        // UI 설정하기
                        DebugPrint("DB 작업 완료");
                    } else {
                        DebugPrint("로그인 실패");
                    }

                    jobFinished(jobParameters, false);
                }
            });
            thread.start();
        }

        private void loadUserData() {
            // 아이디 가져오기
            idInput = userInfo.getString("ID", "");

            // 비밀번호 가져오기
            passwordInput = (userInfo.getString("PW", ""));
        }

        private boolean login(String id, String pw) {

            // 로그인 페이지 연결
            try {
                page = webClient.getPage("https://yscec.yonsei.ac.kr/login/index.php");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            Log.e("TAG", "Get Login Page!");

            // Log.e("page", page.asXml());


            // 로그인 Form 객체 가져오기
            HtmlForm form = page.getFormByName("ssoLoginForm");

            // 아이디, 비밀번호, 제출 버튼 input 객체 가져오기
            HtmlTextInput userName = form.getInputByName("username");
            HtmlPasswordInput password = form.getInputByName("password");
            HtmlSubmitInput loginButton = (HtmlSubmitInput) page.getElementById("loginbtn");

            // 아이디, 비밀번호 채워 넣기
            try {
                userName.type(id);
                password.type(pw);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            // 제출해서 결과 페이지 얻어내기
            try {
                page = loginButton.click();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            Log.e("TAG", "Get myPage!");

            // 로그인이 성공했는지 확인
            String title = page.getTitleText();

            if (title.equals("YSCEC")) {
                return true;
            } else if (title.equals("YSCEC: 사이트에 로그인")) {
                return false;
            } else {
                return false;
            }
        }

        // 강의 목록 및 강의 공지를 가져오고 업데이트하는 함수.
        private void getAllData() {
            // 강의 목록 가져오기
            getCourseList();

            saveCourseNotice();
        }

        // 강의 목록과 링크를 변수에 저장
        private void getCourseList() {
            courseNameList = new ArrayList<>();
            courseLinkList = new ArrayList<>();

            // 디버깅용 코드 - 학기 바꾸기.


            // 모든 강의에 대해서 링크 페이지와 강의 이름 찾기
            try {
                page = webClient.getPage("https://yscec.yonsei.ac.kr/my/?year=2017&term=1");
            } catch (IOException e) {
                e.printStackTrace();
            }

            DomNodeList<DomNode> courseList = page.querySelectorAll("h3.coursename");

            for (DomNode node : courseList) {
                HtmlAnchor htmlAnchor = (HtmlAnchor) node.getFirstChild();
                String courseLinkStr = htmlAnchor.getAttribute("href");
                String courseName = htmlAnchor.asText();

                // 리스트에 추가
                courseNameList.add(courseName);
                courseLinkList.add(courseLinkStr);
            }
        }

        private void saveCourseNotice() {
            // 각 강의마다 공지 목록을 가져오고 저장하기
            for (int i = 0; i < courseLinkList.size(); i++) {
                String courseLinkStr = courseLinkList.get(i);
                String courseNameStr = courseNameList.get(i);

                // 디버깅용 출력
                DebugPrint(courseNameStr + " DB 저장 시작");

                // 강의 페이지 접속
                HtmlPage coursePage = null;
                try {
                    coursePage = webClient.getPage(courseLinkStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 강의 공지 링크 찾아서 들어가기
                HtmlPage noticePage = null;
                HtmlSpan spanElement = coursePage.querySelector(".instancename");
                try {
                    noticePage = spanElement.click();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.e("TAG", "접근할 수 없는 강의입니다.");
                    continue;
                }

                // 링크 출력
                // writer.write(noticePage.getUrl().toString());
                // writer.newLine();

                // class="thread-post-title"인 h1 태그를 찾는다.
                DomNode domNode = noticePage.querySelector("li:not([class]) h1.thread-post-title");

                // 맨 처음에만 클릭으로 들어가고, 그 다음은 페이지를 열어서 들어간다.
                boolean first = true;
                HtmlPage notice = null;
                // 순회하면서 기록하기
                while (true) {
                    HtmlAnchor anchor;
                    if (first) {
                        // 공지 페이지 들어가기
                        try {
                            anchor = (HtmlAnchor) domNode.getFirstChild();
                        } catch (NullPointerException e) {
                            Log.e("TAG", "공지가 하나도 없습니다!");
                            break;
                        }
                        try {
                            notice = anchor.click();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        // 그 다음 페이지 들어가기
                        // 마지막 tr을 선택
                        // DomNode tr = (DomNode) notice.getElementsByTagName("tr").item(notice.getElementsByTagName("tr").getLength() - 1);

                        // 위와 같은 코드
                        DomNode tr = notice.querySelector("div.table-footer-area + table > thead > tr:last-child");

                        // <a> 선택 후 페이지 이동
                        anchor = tr.querySelector("a");
                        try {
                            notice = webClient.getPage(anchor.getAttribute("href"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // 공지 제목과 글 얻기
                    String noticeTitle = notice.querySelector("span.detail-title").asText();
                    String noticeContent = notice.querySelector("div.detail-contents").asText();

                    // 날짜도 얻기
                    String noticeDate = notice.querySelector("span.detail-date").asText();
                    System.out.println(noticeDate);

                    String target = null;
                    DateFormat df = null;
                    Date result = null;
                    try {
                        target = noticeDate.substring(0, noticeDate.indexOf(" 에"));
                        df = new SimpleDateFormat("yyyy년 MM월 dd일, EEE, a KK:mm", Locale.KOREA);
                        result =  df.parse(target);
                    } catch (Exception e) {
                        // 영어 수업이어서 발생하는 Exception
                        target = noticeDate.substring(noticeDate.indexOf("- ") + 2);
                        df = new SimpleDateFormat("yyyy년 MM월 dd일, EEE, a KK:mm", Locale.KOREA);
                        try {
                            result = df.parse(target);
                        } catch (Exception ex) {
                            e.printStackTrace();
                        }
                    }

                    // DB에 넣기
                    if (!insertDB(courseNameStr, noticeTitle, noticeContent, result.getTime()))
                        break;

                    // 탈출 조건
                    HtmlElement htmlElement = notice.querySelector("div.table-footer-area + table");

                    // 첫 글하고 마지막 글은 <tr> 태그가 3개이다.
                    if (!first && htmlElement.getElementsByTagName("tr").getLength() == 3)
                        break;
                        // 첫 글이자 마지막 글(글이 하나 일 때)은 <tr> 태그가 2개이다.
                    else if (first && htmlElement.getElementsByTagName("tr").getLength() == 2)
                        break;

                    first = false;
                }
            }
        }

        // DB에 넣으려는 데이터가 있는지 확인해서 없으면 넣고, 있으면 통과하는 함수
        private boolean insertDB(String courseName, String noticeTitle, String noticeContents, long time) {
            // 쿼리를 할 때, '가 있으면, 에러가 생겨서 에러가 난다. escape sequence로 '를 하나 더 넣어줘야 한다.
            courseName = courseName.replaceAll("'", "''");
            noticeTitle = noticeTitle.replaceAll("'", "''");
            noticeContents = noticeContents.replaceAll("'", "''");


            boolean ret = true;

            // insert 등 다양한 함수들이 존재하지만, 디버깅의 어려움의 구현의 편의성을 위해서 데이터가 있는지 확인 후 넣는 식으로 일단 구현한다.
            // Query
            Cursor cursor = db.rawQuery("SELECT * FROM NOTICE WHERE NOTICETITLE='" + noticeTitle + "' AND NOTICECONTENTS='" + noticeContents + "' AND COURSENAME='" + courseName +"';", null);

            // 혹시나 같은 noticeTitle을 지닌 공지가 있는지 확인
            if (cursor.getCount() != 1 && cursor.getCount() != 0)
                Log.e("TAG", "같은 noticeTitle을 지닌 공지가 두 개 이상 있습니다!!");

            // 데이터가 있다면
            if (cursor.moveToNext()) {
                // 있으므로 INSERT 하지 않는다.
                ret = false;
            } else {
                // 기존의 데이터가 없으므로
                db.execSQL("INSERT INTO NOTICE VALUES('" + courseName + "', '" + noticeTitle + "', '" + noticeContents + "', " + time + ");");
                Log.e("TAG", "Course Name: " + courseName + ", Notice Title: " + noticeTitle);
                ret = true;





                // Create an explicit intent for an Activity in your app
                Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                // 추가 데이터 삽입 - 어떤 수업을 클릭했는가
                intent.putExtra("courseName", courseName);
                Log.e("TAG", courseName);

                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), noticeTitle.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);



                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "TestChannel")
                        .setSmallIcon(R.drawable.baseline_announcement_black_24dp)
                        .setContentTitle(courseName)
                        .setContentText(noticeTitle)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        // Set the intent that will fire when the user taps the notification
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(noticeTitle.hashCode(), builder.build());

            }


            cursor.close();
            return ret;
        }

        // 디버깅용 Toast 출력 함수
        private void DebugPrint(String str) {
            Log.e("TAG", str);
        }



        private void createNotificationChannel() {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_MAX;
                NotificationChannel channel = new NotificationChannel("TestChannel", "Notification", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Notification Test");
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
