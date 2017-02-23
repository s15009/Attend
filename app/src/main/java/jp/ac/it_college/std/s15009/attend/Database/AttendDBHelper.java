package jp.ac.it_college.std.s15009.attend.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * DataBase helper
 */

public class AttendDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Attend.db";
    private static final int DB_VERSION = 1;

    public interface Columns extends BaseColumns{

        public static final String TABLE_STUDENT = "attend_master";

        public static final String STUDENT_ID = "student_id";
        public static final String STUDENT_NUM = "student_number";
        public static final String NAME = "name";
        public static final String CARD_INFORMATION = "card_information";

        public static final String TABLE_ATTEND = "attend_trans";

        public static final String STUDENT_FOREIGN = "student_foreign";
        public static final String CURRENT_TIME = "current_time_ticking";
        public static final String ATTEND_FLAG = "attend_flag";
        public static final String CURRENT_FORMAT = "current_format_ticking";


    }

    private String student_table_create =
            "create table " + Columns.TABLE_STUDENT + "(" +
                    Columns.STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Columns.STUDENT_NUM + " TEXT, " +
                    Columns.NAME + " TEXT, " +
                    Columns.CARD_INFORMATION + " TEXT UNIQUE)";

    private String attend_table_create =
            "create table " + Columns.TABLE_ATTEND + "(" +
                    Columns.STUDENT_FOREIGN + " integer, " +
                    Columns.CURRENT_TIME + " integer, " +
                    Columns.ATTEND_FLAG + " integer, " +
                    " foreign key(" + Columns.STUDENT_FOREIGN + ") references " +
                    Columns.TABLE_STUDENT + "(" + Columns.STUDENT_ID + "))";



    public AttendDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(student_table_create);
        db.execSQL(attend_table_create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        //外部キーを有効にする
        db.setForeignKeyConstraintsEnabled(true);
    }
}
