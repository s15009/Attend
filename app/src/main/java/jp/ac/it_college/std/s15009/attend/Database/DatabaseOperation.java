package jp.ac.it_college.std.s15009.attend.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import jp.ac.it_college.std.s15009.attend.Database.AttendDBHelper.Columns;


/**
 * Database操作系はこっちでやる
 */
public class DatabaseOperation {
    private AttendDBHelper mDbhelper;

    public DatabaseOperation(AttendDBHelper helper) {
        this.mDbhelper = helper;
    }


    //既にカードが登録されていないか確認
    public boolean SerachID(String idm) {
        SQLiteDatabase db = mDbhelper.getReadableDatabase();

        String Where = Columns.CARD_INFORMATION + "=?";
        String[] args = {idm};
        Cursor cursor = db.query(Columns.TABLE_STUDENT,
                null, Where, args, null, null, null);
        if (cursor.moveToFirst()) {
            cursor.close();
            return false;
        } else {
            cursor.close();
            return true;
        }
    }


    //学生情報登録
    public boolean insertData(DataStr dataStr) {
        ContentValues values = new ContentValues();

        values.put(Columns.STUDENT_NUM, dataStr.getStudent_id());
        values.put(Columns.NAME, dataStr.getStudent_name());
        values.put(Columns.CARD_INFORMATION, dataStr.getCard_id());

        long ret;

        try (SQLiteDatabase wdb = mDbhelper.getWritableDatabase()) {
            ret = wdb.insert(Columns.TABLE_STUDENT, null, values);
        }
        return ret != -1;
    }

    //Primary key get
    public Integer get_primary(String idm) {
        SQLiteDatabase db_q = mDbhelper.getReadableDatabase();


        String where = Columns.CARD_INFORMATION + "=?";
        String[] args = {idm};
        Cursor cursor = db_q.query(Columns.TABLE_STUDENT,
                null, where, args, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getInt
                    (cursor.getColumnIndex(Columns.STUDENT_ID));
        } else {
            return -1;
        }
    }

    // TODO:transaction 日付の登録の仕方を変える
    public void Attend_scan(Integer idm, boolean isChecked) {
        SQLiteDatabase db = mDbhelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Columns.STUDENT_FOREIGN, idm);
        values.put(Columns.ATTEND_FLAG, isChecked);

        long ret;

        try {
            ret = db.insert(Columns.TABLE_ATTEND, null, values);
        } finally {
            db.close();
        }
        if (ret == -1) {
            Log.d("database", "failed");
        } else {
            Log.d("database", "success");
        }
    }

    //トランザクションテーブル確認用
    public void test_data(Integer id) {
        SQLiteDatabase db = mDbhelper.getReadableDatabase();
        String num = id.toString();

        String sql = "select * from " + Columns.TABLE_ATTEND + " where " +
                Columns.STUDENT_FOREIGN + " = " + num + " ORDER BY " +
                Columns.CURRENT_TIME + " DESC";

        Cursor c = db.rawQuery(sql, null);


//        String where = AttendDBHelper.Columns.STUDENT_FOREIGN + "=?";
//        String[] args = {num};
//        String[] columns = {Columns.STUDENT_FOREIGN, Columns.CURRENT_TIME, Columns.ATTEND_FLAG };
//        Cursor c = db.query(AttendDBHelper.Columns.TABLE_ATTEND,
//                null, where, args, null, null, Columns.CURRENT_TIME + " DESC");

        if (c.moveToFirst()) {
            do {
                Integer cardid = c.getInt(c.getColumnIndex(Columns.STUDENT_FOREIGN));
                String time = c.getString(c.getColumnIndex(Columns.CURRENT_TIME));
                String flag = c.getString(c.getColumnIndex(Columns.ATTEND_FLAG));

                Log.d("test log", cardid + " " + time + " " + flag);

            } while (c.moveToNext());
        }
    }

    //時間取得 例外投げるよ
    public void get_current_time() throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

        Calendar calendar = Calendar.getInstance();
        //String now = sdf.format(calendar.getTime());
        long now = calendar.getTimeInMillis();
        Log.d("time", now + " ");
        Calendar cale = Calendar.getInstance();
        cale.setTimeInMillis(now);
        String nono = sdf.format(cale.getTime());
        Log.d("time", nono);
    }


}
