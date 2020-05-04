package org.steinsapk.pjnotification.object;

import android.app.ProgressDialog;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class UpdateAsyncTask extends AsyncTask<Void, Void, Void> {
    private JobService jobService;
    private JobParameters jobParameters;
    private Crawling crawling;
    private ProgressDialog progressDialog;
    private String error = "";
    private Context context;
    private String id;
    private String password;

    public UpdateAsyncTask(Context context, boolean setDialog) {
        if (setDialog) {
            // 로딩 화면 객체 설정
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("강의 공지사항 업데이트 중..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        crawling = new Crawling(context, Database.openDatabase(context.getApplicationContext()));
        this.context = context;

        UserInfo userInfo = new UserInfo(context);
        id = userInfo.getSavedInfo("ID");
        password = userInfo.getSavedInfo("PW");
    }

    public UpdateAsyncTask(Context context, boolean setDialog, JobService jobService, JobParameters jobParameters) {
        this(context, setDialog);
        this.jobService = jobService;
        this.jobParameters = jobParameters;
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
    protected Void doInBackground(Void... values) {
        writeLog(context, "Crawling Start!");

        try {
            // 로그인
            crawling.login(id, password);

            // 강의 리스트 업데이트
            crawling.saveCourseList();

            // 강의 아이템 업데이트
            crawling.saveCourseItem();

            // 강의 공지 업데이트
            crawling.saveCourseNotice();
        } catch (Exception e) {
            debugLog(e.toString());
            error = e.getMessage();
            debugLog(error);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString(); // stack trace as a string
            debugLog(sStackTrace);

            writeLog(context, sStackTrace);
            return null;
        }

        writeLog(context, "Crawling Success!");
        return null;
    }

    @Override
    protected void onPostExecute(Void value) {
        super.onPostExecute(value);

        try {
            if (!error.equals(""))
                Toast.makeText(context, error, Toast.LENGTH_LONG).show();

            progressDialog.dismiss();
        } catch (Exception e) {
            // MyService에서 실행될 때, 화면이 없기 때문에 Exception 발생
            debugLog(e.toString());
        }

        // DB 닫기
        crawling.closeDB();

        // Inform that the job is finished.
        if (jobService != null)
            jobService.jobFinished(jobParameters, false);
    }

    private static void debugLog(String log) {
        Log.e("UpdateAsyncTask", log);
    }

    private static void writeLog(Context context, String str) {
        FileOutputStream outputStream;
        Date date = new Date();

        try {
            outputStream = context.openFileOutput("log_file_crawling.txt", Context.MODE_APPEND);
            outputStream.write((date.toString() + "\n").getBytes());
            outputStream.write((str + "\n\n").getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {
            debugLog("log file write fail: " + ex.getMessage());
        }
    }
}
