package org.steinsapk.pjnotification.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.steinsapk.pjnotification.R;
import org.steinsapk.pjnotification.object.Database;

public class NoticePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_page);

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        TextView courseName = findViewById(R.id.courseName);
        TextView boardName = findViewById(R.id.boardName);
        TextView noticeTitle = findViewById(R.id.noticeTitle);
        TextView noticeContents = findViewById(R.id.noticeContents);

        //Log.e("TAG2", intent.getStringExtra("noticeContents"));
        courseName.setText(intent.getStringExtra("courseName"));
        boardName.setText(intent.getStringExtra("boardName"));
        noticeTitle.setText(intent.getStringExtra("noticeTitle"));
        // noticeContents.setText(extras.getString("noticeContents"));
        //noticeContents.setText(String.format("%s%s%s", intent.getStringExtra("noticeContents"), "\n\n첨부파일 : ", intent.getStringExtra("attachmentFiles")));

        // DB 열기
        SQLiteDatabase db = Database.openDatabase(getApplicationContext());

        // 쿼리하기
        Cursor cursor = db.rawQuery("SELECT NOTICECONTENTS, ATTACHMENTFILES, NOTICELINK FROM NOTICE WHERE COURSENAME=? AND NOTICETITLE=? AND BOARDNAME=?;", new String[] { intent.getStringExtra("courseName"), intent.getStringExtra("noticeTitle"), intent.getStringExtra("boardName") });

        cursor.moveToLast();
        StringBuilder stringBuilder = new StringBuilder();
        String noticeLink = cursor.getString(2);

        while (!cursor.isBeforeFirst()) {
            // 첨부 파일이 없으면 첨부 파일 부분은 출력하지 않기
            if (cursor.getString(1).equals(""))
                stringBuilder.append(cursor.getString(0));
            else
                stringBuilder.append(String.format("%s%s%s", cursor.getString(0), "\n\n첨부파일 : ", cursor.getString(1)));

            // 맨 아랫 글 다음에는 Separator 넣지 말기
            if (!cursor.isFirst())
                stringBuilder.append("\n\n----------------------------\n\n");

            cursor.moveToPrevious();
        }

        noticeContents.setText(stringBuilder);

        noticeTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(noticeLink));
                startActivity(browserIntent);
            }
        });

        cursor.close();
    }
}
