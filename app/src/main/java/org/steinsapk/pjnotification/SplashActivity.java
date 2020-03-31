package org.steinsapk.pjnotification;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent;

        // 어느 액티비티를 띄울 건지 알아보기 위해 인텐트의 추가 정보 courseName 값을 확인
        String courseName = getIntent().getStringExtra("courseName");
        boolean isNotice = getIntent().getBooleanExtra("isNotice", false);
        String itemName = getIntent().getStringExtra("itemName");
        String boardName = getIntent().getStringExtra("boardName");

        // courseName 값이 없으면 일반적인 앱 실행
        if (courseName == null) {
            // 로그인 기록이 없으면 로그인 창, 있으면 마이 페이지 띄우기
            UserInfo userInfo = new UserInfo(this);
            if (userInfo.getLoginRecord())
                intent = new Intent(this, MyPage.class);
            else
                intent = new Intent(this, MainActivity.class);

        } else {
            if (isNotice)
                intent = new Intent(this, NoticePage.class);
            else
                intent = new Intent(this, ItemPage.class);

            // 추가 데이터 삽입 - 어떤 수업을 클릭했는가
            intent.putExtra("courseName", courseName);
            intent.putExtra("itemName", itemName);
            intent.putExtra("boardName", boardName);
        }

        startActivity(intent);
        finish();
    }
}