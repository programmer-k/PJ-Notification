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

public class CoursePage extends AppCompatActivity {
    String courseName;
    SQLiteDatabase db;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_page);

        // 생성해서 넣을 View 객체의 부모 LinearLayout
        linearLayout = findViewById(R.id.noticeList);

        onNewIntent(getIntent());
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

        // 디버깅
        Log.e("TAG", courseName + " 읽어들임");

        // DB 열기
        db = Database.openDatabase(getApplicationContext());

        // 강의명 텍스트 설정하기
        TextView textView = findViewById(R.id.courseName);
        textView.setText(courseName);

        // 게시판 쿼리하기
        Cursor cursor = db.rawQuery("SELECT DISTINCT BOARDNAME FROM NOTICE WHERE COURSENAME='"+ courseName + "';", null);

        while (cursor.moveToNext()) {
            String boardName = cursor.getString(0);

            // 레이아웃 그리기
            drawLayout(boardName, null, null);
        }

        cursor.close();

        // 쿼리하기
        cursor = db.rawQuery("SELECT ITEMNAME, ITEMCONTENTS, ITEMLINK FROM ITEM WHERE COURSENAME='"+ courseName + "';", null);

        while (cursor.moveToNext()) {
            String itemName = cursor.getString(0);
            String itemContents = cursor.getString(1);
            String itemLink = cursor.getString(2);

            // 레이아웃 그리기
            drawLayout(itemName, itemContents, itemLink);
        }

        cursor.close();
    }

    private void drawLayout(String boardName) {

    }

    private void drawLayout(String itemName, String itemContents, String itemLink) {
        // 버튼 레이아웃을 생성해서 가져옴.
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.button_layout, null);
        linearLayout.addView(viewGroup);

        LinearLayout layout = (LinearLayout) viewGroup;

        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams1.setMargins(0, 0,0, 20);
        layout.setLayoutParams(layoutParams1);

        // 텍스트 값 설정
        Button button = (Button) viewGroup.getChildAt(0);
        button.setText(itemName);

        // 게시판
        if (itemContents == null && itemLink == null) {
            // 버튼에 클릭 이벤트 핸들러 등록
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), NoticePage.class);
                    intent.putExtra("courseName", courseName);
                    intent.putExtra("boardName", itemName);
                    startActivity(intent);
                }
            });
            return;
        }

        // TextView를 생성해서 가져옴.
        /*TextView textView = (TextView) LayoutInflater.from(this).inflate(R.layout.textview_layout, null);
        linearLayout.addView(textView);

        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams2.setMargins(0, 0,0, 20);
        textView.setLayoutParams(layoutParams2);

        // 텍스트 값 설정
        textView.setText(Html.fromHtml(itemContents, Html.FROM_HTML_MODE_COMPACT));

        // 공지 내용 숨기기
        textView.setVisibility(View.GONE);*/

        // 버튼에 클릭 이벤트 핸들러 등록
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ItemPage.class);
                intent.putExtra("courseName", courseName);
                intent.putExtra("itemName", itemName);
                startActivity(intent);
            }
        });

        // 버튼을 길게 누르면 웹 브라우저 띄우기
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemLink));
                startActivity(browserIntent);
                return true;
            }
        });
    }
}
