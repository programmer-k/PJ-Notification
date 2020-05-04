package org.steinsapk.pjnotification.object;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Calendar;

public class Database {
    private SQLiteDatabase db;

    public Database(SQLiteDatabase db) {
        this.db = db;

        db.execSQL("CREATE TABLE IF NOT EXISTS NOTICE(COURSENAME TEXT, NOTICETITLE TEXT, NOTICECONTENTS TEXT, TIME INTEGER, NOTICELINK TEXT, ATTACHMENTFILES TEXT, BOARDNAME TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS COURSE(COURSENAME TEXT, COURSELINK TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS ITEM(COURSENAME TEXT, ITEMNAME TEXT, ITEMCONTENTS TEXT, ITEMLINK TEXT, ITEMATTRIBUTE TEXT);");
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

    public void clearCourse() {
        db.execSQL("DELETE FROM COURSE");
    }

    public Cursor getCourseList() {
        return db.rawQuery("SELECT DISTINCT COURSENAME FROM COURSE;", null);
    }

    public Cursor getCourseNameAndLink() {
        return db.rawQuery("SELECT * FROM COURSE;", null);
    }

    public boolean insertItem(String courseName, String itemName, String itemContents, String itemLink, String itemAttribute) {
        boolean success = false;

        // 쿼리를 할 때, '가 있으면, 에러가 생겨서 에러가 난다. escape sequence로 '를 하나 더 넣어줘야 한다.
        courseName = courseName.replaceAll("'", "''");
        itemName = itemName.replaceAll("'", "''");
        itemContents = itemContents.replaceAll("'", "''");
        itemLink = itemLink.replaceAll("'", "''");
        itemAttribute = itemAttribute.replaceAll("'", "''");

        /* 데이터가 있는지 확인 후 없으면 넣는다. */

        // Query
        Cursor cursor = db.rawQuery("SELECT * FROM ITEM WHERE ITEMNAME='" + itemName + "' AND COURSENAME='" + courseName +"';", null);

        // Query 한 데이터가 없다면, INSERT
        if (!cursor.moveToNext()) {
            db.execSQL("INSERT INTO ITEM VALUES('" + courseName + "', '" + itemName + "', '" + itemContents + "', '" + itemLink + "', '" + itemAttribute + "');");
            // debugLog(itemName);
            success = true;
        }

        cursor.close();
        return success;
    }

    public boolean insertNotice(String courseName, String noticeTitle, String noticeContents, long time, String noticeLink, String attachmentFiles, String boardName) {
        boolean success = false;

        // 쿼리를 할 때, '가 있으면, 에러가 생겨서 에러가 난다. escape sequence로 '를 하나 더 넣어줘야 한다.
        courseName = courseName.replaceAll("'", "''");
        noticeTitle = noticeTitle.replaceAll("'", "''");
        noticeContents = noticeContents.replaceAll("'", "''");
        attachmentFiles = attachmentFiles.replaceAll("'", "''");
        noticeLink = noticeLink.replaceAll("'", "''");
        boardName = boardName.replaceAll("'", "''");

        /* 데이터가 있는지 확인 후 없으면 넣는다. */

        // Query
        Cursor cursor = db.rawQuery("SELECT * FROM NOTICE WHERE NOTICETITLE='" + noticeTitle + "' AND NOTICECONTENTS='" + noticeContents + "' AND COURSENAME='" + courseName + "' AND BOARDNAME='" + boardName + "';", null);

        // Query 한 데이터가 없다면, INSERT
        if (!cursor.moveToNext()) {
            db.execSQL("INSERT INTO NOTICE VALUES('" + courseName + "', '" + noticeTitle + "', '" + noticeContents + "', '" + time + "', '" + noticeLink + "', '" + attachmentFiles + "', '" + boardName +"');");
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

    public boolean isNoticeTitleExist(String courseName, String noticeTitle, String boardName) {
        boolean ret;

        courseName = courseName.replaceAll("'", "''");
        noticeTitle = noticeTitle.replaceAll("'", "''");
        boardName = boardName.replaceAll("'", "''");

        Cursor cursor = db.rawQuery("SELECT NOTICETITLE FROM NOTICE WHERE COURSENAME=? AND BOARDNAME=? AND NOTICETITLE=?", new String[] { courseName, boardName, noticeTitle });

        if (cursor.getCount() > 0)
            ret = true;
        else
            ret = false;

        cursor.close();
        return ret;
    }

    public boolean isItemExist(String courseName, String itemName) {
        boolean ret;
        courseName = courseName.replaceAll("'", "''");
        itemName = itemName.replaceAll("'", "''");

        // Query
        Cursor cursor = db.rawQuery("SELECT * FROM ITEM WHERE ITEMNAME='" + itemName + "' AND COURSENAME='" + courseName +"';", null);

        // Query 한 데이터가 없다면, INSERT
        if (cursor.getCount() == 0) {
            ret = false;
        } else
            ret = true;

        cursor.close();
    }

    public void closeDB() {
        db.close();
    }

    private static void debugLog(String log) {
        Log.e("TAG", log);
    }

    public static SQLiteDatabase openDatabase(Context context) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);

        return context.openOrCreateDatabase("database"  + year + (month >= 2 && month < 8 ? "1st" : "2nd")+".db", 0,null);
    }
}
