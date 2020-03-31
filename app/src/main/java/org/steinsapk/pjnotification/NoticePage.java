package org.steinsapk.pjnotification;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NoticePage extends AppCompatActivity {
    String courseName;
    String boardName;
    SQLiteDatabase db;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_page);

        // adView = findViewById(R.id.adView);

        // 생성해서 넣을 View 객체의 부모 LinearLayout
        linearLayout = findViewById(R.id.noticeList);

        onNewIntent(getIntent());

        /*// 인텐트 객체 얻기
        Intent intent = getIntent();

        // My Page에서 전달한 데이터를 가져온다.
        courseName = intent.getStringExtra("courseName");

        // 디버깅
        Log.e("TAG", courseName + " 읽어들임");

        // DB 열기
        db = openOrCreateDatabase("database.db", MODE_ENABLE_WRITE_AHEAD_LOGGING,null);

        // 쿼리하기
        Cursor cursor = db.rawQuery("SELECT NOTICETITLE, NOTICECONTENTS FROM NOTICE WHERE COURSENAME='"+ courseName + "';", null);

        // 강의명 텍스트 설정하기
        TextView textView = findViewById(R.id.courseName);
        textView.setText(courseName);

        while (cursor.moveToNext()) {
            String noticeTitle = cursor.getString(0);
            String noticeContents = cursor.getString(1);

            // 레이아웃 그리기
            drawLayout(noticeTitle, noticeContents);
        }*/
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // adView = findViewById(R.id.adView);

        // 생성해서 넣을 View 객체의 부모 LinearLayout
        linearLayout = findViewById(R.id.noticeList);

        // 예전에 그린 레이아웃을 지워야한다.
        linearLayout.removeAllViews();

        // My Page에서 전달한 데이터를 가져온다.
        courseName = intent.getStringExtra("courseName");
        boardName = intent.getStringExtra("boardName");


        // 디버깅
        Log.e("TAG", courseName + " 읽어들임");

        // DB 열기
        db = Database.openDatabase(getApplicationContext());

        // 쿼리하기
        Cursor cursor = db.rawQuery("SELECT NOTICETITLE, NOTICECONTENTS, ATTACHMENTFILES, NOTICELINK FROM NOTICE WHERE COURSENAME='" + courseName + "' AND BOARDNAME='" + boardName + "' ORDER BY TIME DESC" + ";", null);

        // 제목 텍스트 설정하기
        TextView textView1 = findViewById(R.id.courseName);
        textView1.setText(courseName);

        TextView textView2 = findViewById(R.id.boardName);
        textView2.setText(boardName);

        while (cursor.moveToNext()) {
            String noticeTitle = cursor.getString(0);
            String noticeContents = cursor.getString(1);
            String attachmentFiles = cursor.getString(2);
            String noticeLink = cursor.getString(3);

            // 레이아웃 그리기
            drawLayout(noticeTitle, noticeContents, attachmentFiles, noticeLink);
        }

        cursor.close();
    }

    private void drawLayout(String noticeTitle, String noticeContents, String attchmentFiles, String noticeLink) {
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

        // TextView를 생성해서 가져옴.
        TextView textView = (TextView) LayoutInflater.from(this).inflate(R.layout.textview_layout, null);
        linearLayout.addView(textView);

        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams2.setMargins(0, 0,0, 20);
        textView.setLayoutParams(layoutParams2);

        // 텍스트 값 설정
        if (attchmentFiles.equals("")) {
            textView.setText(noticeContents);
        } else {
            textView.setText(noticeContents + "\n\n첨부파일 : " + attchmentFiles);
        }

        // 공지 내용 숨기기
        textView.setVisibility(View.GONE);

        // 버튼에 클릭 이벤트 핸들러 등록
        button.setOnClickListener(new View.OnClickListener() {
            View v = textView;
            @Override
            public void onClick(View view) {
                if (v.getVisibility() == View.GONE) {
                    v.setVisibility(View.VISIBLE);
                } else {
                    v.setVisibility(View.GONE);
                }
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
