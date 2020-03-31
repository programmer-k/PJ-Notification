package org.steinsapk.pjnotification;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
        String query = ("SELECT ITEMCONTENTS FROM ITEM WHERE COURSENAME='"+ courseName + "' AND ITEMNAME='" + itemName + "';").replaceAll("'", "''");
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToNext();
        String data = cursor.getString(0);

        cursor.close();

        WebView webView = findViewById(R.id.webView);

        if (data.equals("")) {
            webView.loadData("<center><h2>파일입니다.</h2><p>YSCEC에서 확인해주세요.</p></center>", "text/html", "UTF-8");
        } else
            webView.loadData(data, "text/html", "UTF-8");
    }
}
