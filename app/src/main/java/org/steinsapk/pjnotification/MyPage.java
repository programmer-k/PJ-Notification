package org.steinsapk.pjnotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.delight.apprater.AppRater;

public class MyPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        // 버전 정보 가져오기
        int version = 1;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            debugLog(e.toString());
        }

        // 1. JobScheduler를 이용해 주기적으로 새로운 공지 확인하는 작업 등록하기 (딱 한 번만)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getInt("versionCode", 10) != version) {
            // <---- run your one time code here
            MyService.registerJobService(this);

            // mark first time has ran.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("versionCode", version);
            editor.apply();
        }

        // 2. 강의 목록 불러오기
        loadCourseList();

        // 3. NotificationChannel 생성
        createNotificationChannel();

        // 4. 평점 팝업 창 생성하기
        AppRater appRater = new AppRater(this);
        appRater.setDaysBeforePrompt(5);
        appRater.setLaunchesBeforePrompt(10);
        appRater.setPhrases("평점 남기기","평점과 리뷰를 적어주시면 큰 도움이 될 것 같은데, 혹시 부탁드려도 될까요..?", "평점 남기기", "나중에 하기", "No, thanks");

        appRater.show();

        // 광고 초기화 및 호출
        // Advertisement.initializeAd(this, findViewById(R.id.adView));
    }

    // PJ 버튼 클릭 처리
    public void updateCourseData(View v) {
        UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask(this, true);
        updateAsyncTask.execute();

        // 강의 데이터 업데이트, Thread 버전
        /*
        new Thread(() -> {
            // 크롤링 객체 생성
            Crawling crawling = new Crawling(this, openOrCreateDatabase("database.db", MODE_ENABLE_WRITE_AHEAD_LOGGING, null));

            // 아이디와 비밀번호 가져오기
            UserInfo userInfo = new UserInfo(this);
            String id = userInfo.getSavedInfo("ID");
            String password = userInfo.getSavedInfo("PW");

            try {
                // 로그인
                crawling.login(id, password);

                // 새로운 강의가 있으면 추가한다. (없어진 강의를 없애지는 못함)
                crawling.saveCourseList();

                // 강의 공지 업데이트
                crawling.saveCourseNotice();
            } catch (Exception e) {
                debugLog(e.toString());
            }

            // 로딩화면 없애기
            progressDialog.dismiss();
        }).start();
        */
    }

    public void inflateSettingsActivity(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void loadCourseList() {
        // Database 객체 생성
        Database database = new Database(openOrCreateDatabase("database.db", MODE_ENABLE_WRITE_AHEAD_LOGGING,null));

        // 강의 목록 불러오기
        Cursor cursor = database.getCourseList();

        // 강의명 하나씩 추출하고 설정하기
        int i;
        for (i = 1; cursor.moveToNext(); i++) {
            // 강의명 가져오기
            String courseName = cursor.getString(0);

            // View 객체 가져오기
            // Dynamic String을 이용해서 View 객체를 가져오는 건 findViewByID 메소드로는 불가능하므로 getIdentifier 메소드를 사용함.
            TextView textView = findViewById(getResources().getIdentifier("course" + i, "id", getPackageName()));
            LinearLayout linearLayout = findViewById(getResources().getIdentifier("courseLayout" + i, "id", getPackageName()));

            // 강의명 텍스트 설정
            textView.setText(courseName);

            // 강의 클릭 콜백 메소드 설정하기
            linearLayout.setOnClickListener((View v) -> {
                // 인텐트 객체 생성
                Intent intent = new Intent(getApplicationContext(), NoticePage.class);

                // 추가 데이터 삽입 - 어떤 수업을 클릭했는지
                intent.putExtra("courseName", courseName);

                // 로그 출력
                debugLog(courseName + " 클릭");

                // 공지 목록 화면 띄우기
                startActivity(intent);
            });
        }

        cursor.close();
        database.closeDB();

        for (; i <= 15; i++) {
            // 나머지 칸들 안 보이게 하기
            findViewById(getResources().getIdentifier("courseLayout" + i, "id", getPackageName())).setVisibility(View.GONE);
        }
    }

    private static void debugLog(String log) {
        Log.e("MyPage", log);
    }

    // Notification 클래스로 코드를 옮겨야 함.
    // getSystemService 메서드가 문제를 일으켜서 임시로 여기에 놓음.
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_MAX;
            NotificationChannel channel = new NotificationChannel("Notice", "Notification", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notification Test");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
