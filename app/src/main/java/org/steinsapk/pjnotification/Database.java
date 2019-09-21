package org.steinsapk.pjnotification;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Database {
    private SQLiteDatabase db;

    public Database(SQLiteDatabase db) {
        this.db = db;

        db.execSQL("CREATE TABLE IF NOT EXISTS NOTICE(COURSENAME TEXT, NOTICETITLE TEXT, NOTICECONTENTS TEXT, TIME INTEGER, NOTICELINK TEXT, ATTACHMENTFILES TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS COURSE(COURSENAME TEXT, COURSELINK TEXT);");
    }

    public boolean insertCourse(String courseName, String courseLink) {
        boolean success = false;

        /* 데이터가 있는지 확인 후 없으면 넣는다. */

        // Query
        Cursor cursor = db.rawQuery("SELECT * FROM COURSE WHERE COURSENAME='" + courseName + "' AND COURSELINK='" + courseLink + "';", null);

        // Query 한 데이터가 없다면, INSERT
        if (!cursor.moveToNext()) {
            db.execSQL("INSERT INTO COURSE VALUES('" + courseName + "', '" + courseLink + "');");
            debugLog(courseName);
            success = true;
        }

        cursor.close();
        return success;
    }

    public Cursor getCourseList() {
        return db.rawQuery("SELECT DISTINCT COURSENAME FROM COURSE;", null);
    }

    public Cursor getCourseNameAndLink() {
        return db.rawQuery("SELECT * FROM COURSE;", null);
    }

    public boolean insertNotice(String courseName, String noticeTitle, String noticeContents, long time, String noticeLink, String attachmentFiles) {
        boolean success = false;

        // 쿼리를 할 때, '가 있으면, 에러가 생겨서 에러가 난다. escape sequence로 '를 하나 더 넣어줘야 한다.
        courseName = courseName.replaceAll("'", "''");
        noticeTitle = noticeTitle.replaceAll("'", "''");
        noticeContents = noticeContents.replaceAll("'", "''");
        attachmentFiles = attachmentFiles.replaceAll("'", "''");
        noticeLink = noticeLink.replaceAll("'", "''");

        /* 데이터가 있는지 확인 후 없으면 넣는다. */

        // Query
        Cursor cursor = db.rawQuery("SELECT * FROM NOTICE WHERE NOTICETITLE='" + noticeTitle + "' AND NOTICECONTENTS='" + noticeContents + "' AND COURSENAME='" + courseName +"';", null);

        // Query 한 데이터가 없다면, INSERT
        if (!cursor.moveToNext()) {
            db.execSQL("INSERT INTO NOTICE VALUES('" + courseName + "', '" + noticeTitle + "', '" + noticeContents + "', '" + time + "', '" + noticeLink + "', '" + attachmentFiles +"');");
            debugLog(noticeTitle);
            success = true;



            // 인텐트 생성과 알림 띄우는 코드, Database 클래스의 역할에는 맞지 않는 것 같으므로 잠시 주석 처리해 둠.
            // 추후 Crawling으로 옮겨야 함.

            /*

            // Create an explicit intent for an Activity in your app
            Intent intent = new Intent(getApplicationContext(), NoticePage.class);
            // 추가 데이터 삽입 - 어떤 수업을 클릭했는가
            intent.putExtra("courseName", courseName);
            Log.e("TAG", courseName);

            */

            // 일단 알림을 띄우지는 않음.
            /*intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), noticeTitle.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);



            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "TestChannel")
                    .setSmallIcon(R.drawable.baseline_announcement_black_24dp)
                    .setContentTitle(courseName)
                    .setContentText(noticeTitle)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(noticeTitle.hashCode(), builder.build());*/

        }


        cursor.close();
        return success;
    }

    public void closeDB() {
        db.close();
    }

    private static void debugLog(String log) {
        Log.e("TAG", log);
    }
}
