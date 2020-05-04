package org.steinsapk.pjnotification.activity;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import org.steinsapk.pjnotification.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        TextView logTextView = findViewById(R.id.logTextView);

        try {
            String log = readLog(getApplicationContext());
            logTextView.setText(log);
        } catch (FileNotFoundException e) {
            logTextView.setText("로그 파일이 없습니다.");
        }
    }

    private static String readLog(Context context) throws FileNotFoundException {
        FileInputStream fileInputStream = context.openFileInput("log_file_crawling.txt");
        Scanner scanner = new Scanner(fileInputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
}
