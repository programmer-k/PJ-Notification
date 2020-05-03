package org.steinsapk.pjnotification;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.webkit.WebView;

public class ItemPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_page);

        Intent intent = getIntent();
        String courseName = intent.getStringExtra("courseName");
        String itemName = intent.getStringExtra("itemName");

        // DB 열기
        SQLiteDatabase db = Database.openDatabase(getApplicationContext());

        // 쿼리하기
        String query = "SELECT ITEMCONTENTS, ITEMLINK FROM ITEM WHERE COURSENAME=? AND ITEMNAME=?;";
        Cursor cursor = db.rawQuery(query, new String[] { courseName, itemName });

        cursor.moveToNext();
        String data = cursor.getString(0);
        String link = cursor.getString(1);

        cursor.close();

        WebView webView = findViewById(R.id.webView);

        if (data.equals("")) {
            webView.loadData("<center><h2>파일입니다.</h2><p>뒤로 돌아가셔서 제목을 길게 누르시거나 아래의 링크를 클릭하시면 웹 브라우저로 파일을 다운로드 받으실 수 있습니다.</p><p><a href=\"" + link + "\">다운로드</a></p></center>", "text/html", "UTF-8");
        } else
            webView.loadData(data, "text/html", "UTF-8");
    }
}
