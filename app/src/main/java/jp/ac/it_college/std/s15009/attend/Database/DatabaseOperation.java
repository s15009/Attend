package jp.ac.it_college.std.s15009.attend.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import jp.ac.it_college.std.s15009.attend.Database.AttendDBHelper.Columns;


/**
 * Database操作系はこっちでやる
 */
public class DatabaseOperation {
    private AttendDBHelper mDbhelper;
    private String format = "yyyy-MM-dd HH:mm:ss";

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

    //transaction table 挿入
    public boolean Attend_scan(Integer idm, boolean isChecked){

        //フラグ判定
        switch (already_attend(idm)){
            case 0:
                //false
                if(isChecked){
                    break;
                } else {
                    return false;
                }
            case 1:
                //true
                if(!isChecked){
                    break;
                } else {
                    return false;
                }
            default:
                break;
        }

        long now_minute = get_current_time();

        ContentValues values = new ContentValues();

        values.put(Columns.STUDENT_FOREIGN, idm);
        values.put(Columns.CURRENT_TIME, now_minute);
        values.put(Columns.ATTEND_FLAG, isChecked);

        long ret;

        try (SQLiteDatabase db = mDbhelper.getWritableDatabase()){
            ret = db.insert(Columns.TABLE_ATTEND, null, values);
        }
        if (ret == -1) {
            Log.d("database", "failed");
            return false;
        } else {
            Log.d("database", "success");
            return true;
        }

    }

    //トランザクションテーブル確認用
    public void test_data(Integer id) {
        SQLiteDatabase db = mDbhelper.getReadableDatabase();
        String num = id.toString();

//        String sql = "select * from " + Columns.TABLE_ATTEND + " where " +
//                Columns.STUDENT_FOREIGN + " = " + num + " ORDER BY " +
//                Columns.CURRENT_TIME + " DESC";

//        Cursor c = db.rawQuery(sql, null);


        String where = AttendDBHelper.Columns.STUDENT_FOREIGN + "=?";
        String[] args = {num};
        //String[] columns = {Columns.STUDENT_FOREIGN, Columns.CURRENT_TIME, Columns.ATTEND_FLAG };
        Cursor c = db.query(AttendDBHelper.Columns.TABLE_ATTEND,
                null, where, args, null, null, Columns.CURRENT_TIME + " DESC");

        if (c.moveToFirst()) {
            do {
                Integer cardid = c.getInt(c.getColumnIndex(Columns.STUDENT_FOREIGN));
                long time = c.getLong(c.getColumnIndex(Columns.CURRENT_TIME));
                String flag = c.getString(c.getColumnIndex(Columns.ATTEND_FLAG));
                String formattime = change_time_inmillis(time);

                Log.d("test log", cardid + " " + formattime + " " + flag);

            } while (c.moveToNext());
        }
        c.close();
    }


    private long get_current_time(){

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        return now;

    }

    private String change_time_inmillis(long minute){
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        Calendar cale = Calendar.getInstance();
        cale.setTimeInMillis(minute);
        String now = sdf.format(cale.getTime());

//        String now = sdf.format(new Date(minute));

        return now;
    }

    //２連続スキャン防止
    private int already_attend(Integer id){

        SQLiteDatabase db = mDbhelper.getReadableDatabase();
        String num = id.toString();
        int check_change;

        String where = AttendDBHelper.Columns.STUDENT_FOREIGN + "=?";
        String[] args = {num};
        Cursor c = db.query(AttendDBHelper.Columns.TABLE_ATTEND,
                null, where, args, null, null, Columns.CURRENT_TIME + " DESC limit 1");

        if (c.moveToFirst()) {
            do {
                int flag = c.getInt(c.getColumnIndex(Columns.ATTEND_FLAG));
                check_change = flag;

                Log.d("flag", "flag is " + check_change);
            } while (c.moveToNext());
        } else {
            //初回処理
            return 2;
        }
        c.close();

        return check_change;
    }
}
