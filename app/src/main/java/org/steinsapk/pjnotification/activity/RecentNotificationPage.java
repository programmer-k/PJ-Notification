package org.steinsapk.pjnotification.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import org.steinsapk.pjnotification.R;
import org.steinsapk.pjnotification.object.Database;

public class RecentNotificationPage extends AppCompatActivity {
    LinearLayout linearLayout;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_notification_page);

        // 생성해서 넣을 View 객체의 부모 LinearLayout
        linearLayout = findViewById(R.id.noticeList);
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // adView = findViewById(R.id.adView);

        // 생성해서 넣을 View 객체의 부모 LinearLayout
        linearLayout = findViewById(R.id.noticeList);

        // 예전에 그린 레이아웃을 지워야한다.
        linearLayout.removeAllViews();

        // DB 열기
        db = Database.openDatabase(getApplicationContext());

        // 쿼리하기
        Cursor cursor = db.rawQuery("SELECT DISTINCT COURSENAME, BOARDNAME, NOTICETITLE, NOTICELINK FROM NOTICE ORDER BY TIME DESC;", null);

        for (int i = 0; cursor.moveToNext() && i < 20; i++) {
            String courseName = cursor.getString(0);
            String boardName = cursor.getString(1);
            String noticeTitle = cursor.getString(2);
            String noticeLink = cursor.getString(3);

            // 레이아웃 그리기
            drawLayout(courseName, boardName, noticeTitle, noticeLink);
        }

        cursor.close();
    }

    private void drawLayout(String courseName, String boardName, String noticeTitle, String noticeLink) {
        // 버튼 레이아웃을 생성해서 가져옴.
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.button_layout, null);
        linearLayout.addView(viewGroup);

        LinearLayout layout = (LinearLayout) viewGroup;

        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams1.setMargins(0, 0,0, 20);
        layout.setLayoutParams(layoutParams1);

        // 텍스트 값 설정
        Button button = (Button) viewGroup.getChildAt(0);
        button.setText(noticeTitle);

        // 버튼에 클릭 이벤트 핸들러 등록
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NoticePage.class);

                intent.putExtra("courseName", courseName);
                intent.putExtra("boardName", boardName);
                intent.putExtra("noticeTitle", noticeTitle);
                startActivity(intent);
            }
        });

        // 버튼을 길게 누르면 웹 브라우저 띄우기
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(noticeLink));
                startActivity(browserIntent);
                return true;
            }
        });
    }
}
