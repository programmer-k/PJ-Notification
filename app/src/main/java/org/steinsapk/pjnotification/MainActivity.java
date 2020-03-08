package org.steinsapk.pjnotification;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    EditText idInput;
    EditText passwordInput;
    UserInfo userInfo;
    boolean loginRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 뷰 객체 불러오기
        idInput = findViewById(R.id.idInput);
        passwordInput = findViewById(R.id.passwordInput);

        // 유저 정보 가져오기
        userInfo = new UserInfo(this);
        loginRecord = userInfo.loadUserInfo();

        // 데이터베이스 버전 확인
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getInt("databaseVersion", 1) == 1) {
            // <---- run your one time code here
            // NOTICELINK, ATTACHMENTFILES column 추가
            SQLiteDatabase db = Database.openDatabase(getApplicationContext());

            try {
                db.execSQL("ALTER TABLE NOTICE ADD NOTICELINK TEXT;");
                db.execSQL("ALTER TABLE NOTICE ADD ATTACHMENTFILES TEXT;");

                // 새로 추가한 column을 빈 문자열로 만들기
                db.execSQL("UPDATE NOTICE SET NOTICELINK = '' WHERE NOTICELINK IS NULL;");
                db.execSQL("UPDATE NOTICE SET ATTACHMENTFILES = '' WHERE ATTACHMENTFILES IS NULL;");
            } catch (Exception e) {
                // 신규 설치
            }

            db.close();

            // mark first time has ran.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("databaseVersion", 2);
            editor.apply();
        }
    }

    private boolean loginWithPreviousInfo(String id, String pw) {
        String savedID = userInfo.getSavedInfo("ID");
        String savedPassword = userInfo.getSavedInfo("PW");

        // 기록된 데이터와 이번에 입력한 값들이 서로 일치하는지 확인
        if (savedID.equals(id) && savedPassword.equals(pw)) {
            debugLog("Login Without Accessing Server");
            return true;
        }

        return false;
    }

    // 로그인 버튼을 클릭하면 호출되는 콜백 함수
    public void loginButtonClicked(View view) {
        // 아이디와 비밀번호가 입력되었는지 확인
        if (!checkForm())
            return;

        // 아이디 비밀번호 가져오기
        String id = idInput.getText().toString();
        String pw = passwordInput.getText().toString();

        // 전에 로그인 했던 기록이 있다면, 이전 데이터와 비교해서 로그인을 진행한다.
        if (loginRecord) {
            if (loginWithPreviousInfo(id, pw)) {
                // 토스트 메시지 출력
                Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_LONG).show();

                // 아이디, 비밀번호 저장하기
                userInfo.saveUserInfo();

                // 새로운 액티비티 띄우기
                startActivity(new Intent(getApplicationContext(), MyPage.class));
            } else {
                // 토스트 메시지 출력
                Toast.makeText(getApplicationContext(), "아이디 또는 비밀번호를 다시 확인하세요.", Toast.LENGTH_LONG).show();
            }
        } else {
            // 실제로 서버에 접속해서 로그인
            LoginAsyncTask loginAsyncTask = new LoginAsyncTask(this);
            loginAsyncTask.execute(id, pw);
        }

    }

    // 아이디와 비밀번호가 입력되었는 확인하는 함수
    private boolean checkForm() {
        if (idInput.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (passwordInput.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

            return true;
    }

    private static void debugLog(String log) {
        Log.e("MainPage", log);
    }

    static class LoginAsyncTask extends AsyncTask<String, Void, Boolean> {
        private Crawling crawling;
        private ProgressDialog progressDialog;
        private String error = "";
        private Activity activity;
        private UserInfo userInfo;

        public LoginAsyncTask(Activity activity) {
            crawling = new Crawling(null, Database.openDatabase(activity.getApplicationContext()));
            this.activity = activity;
            userInfo = new UserInfo(activity);

            // 로딩 화면 객체 설정
            progressDialog = new ProgressDialog(activity);
            progressDialog.setTitle("로그인 중...");
            // progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("잠시만 기다려 주세요..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String id = strings[0];
            String password = strings[1];

            try {
                // 실제로 서버에 접속해서 로그인
                if (crawling.login(id, password)) {
                    crawling.saveCourseList();
                    return true;
                }
            } catch (Exception e) {
                debugLog(e.toString());
                error = e.getMessage();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {
                // 토스트 메시지 출력
                Toast.makeText(activity, "로그인 성공", Toast.LENGTH_LONG).show();

                // 아이디, 비밀번호 저장하기
                userInfo.saveUserInfo();

                progressDialog.dismiss();

                // 새로운 액티비티 띄우기
                activity.startActivity(new Intent(activity, MyPage.class));
            } else {
                // 토스트 메시지 출력
                if (error.equals("")) {
                    Toast.makeText(activity, "아이디 또는 비밀번호를 다시 확인하세요.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                }

                progressDialog.dismiss();
            }

            // DB 닫기
            crawling.closeDB();
        }
    }
}
