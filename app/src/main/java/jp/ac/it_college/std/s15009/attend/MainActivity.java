package jp.ac.it_college.std.s15009.attend;


import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import jp.ac.it_college.std.s15009.attend.Database.AttendDBHelper;
import jp.ac.it_college.std.s15009.attend.Database.DataStr;
import jp.ac.it_college.std.s15009.attend.Database.DatabaseOperation;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private IntentFilter[] intentFilters;
    private String[][] techlistarray;
    private PendingIntent pendingIntent;
    private NfcAdapter nfcAdapter;
    private boolean isChecked = true;
    private Handler mHandler;
    private Button check;
    private DatabaseOperation dataope;
    private SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy'年'MM'月'dd'日' kk'時'mm'分'ss'秒'");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        //インテントフィルター　newＩIntent に通知
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        ndef.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);

        try {
            ndef.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFilters = new IntentFilter[]{ndef};

        //スキャン出来るカードに種類？
        techlistarray = new String[][]{new String[]{NfcF.class.getName()}};

        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        AttendDBHelper mDbhelper = new AttendDBHelper(getApplicationContext());

        dataope = new DatabaseOperation(mDbhelper);

        check = (Button) findViewById(R.id.state_button);
        check.setOnClickListener(this);

        SQLiteDatabase db = mDbhelper.getWritableDatabase();
        db.close();

        //時刻表示
        mHandler = new Handler(getMainLooper());
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Calendar calendar = Calendar.getInstance();
                        String now = sdf.format(calendar.getTime());

                        ((TextView) findViewById(R.id.current_time)).setText(now);
                    }
                });
            }
        }, 0, 1000);


    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, techlistarray);
    }

    //通知来た時呼ばれるよ
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }

        String id = getIDm(intent);
        Log.d("tag get", id);


        //ダイアログ呼び出し？　新規カードか既存カードか分岐
        if (dataope.SerachID(id)) {
            //ここは登録
            Data_save_dialog(id);
        } else {
            //ここはトランザクションテーブルに記録
            Integer Pkey = dataope.get_primary(id);
            dataope.Attend_scan(Pkey, isChecked);
            Log.d("scan", "出席したよ（仮）");
            dataope.test_data(Pkey);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);

        try {
            dataope.get_current_time();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //id 変換
    private String getIDm(Intent intent) {
        String idm = null;

        StringBuilder idmByte = new StringBuilder();

        byte[] rawIdm = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        if (rawIdm != null) {
            for (byte aRawIdm : rawIdm) {
                idmByte.append(Integer.toHexString(aRawIdm & 0xff));
            }
            idm = idmByte.toString();
        }
        return idm;
    }

    //　TODO:新規カードがスキャンされたらdialog表示してＤＢに保存したい
    private void Data_save_dialog(final String idm) {

        LayoutInflater layoutInflater = (LayoutInflater)
                this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.
                inflate(R.layout.dialog_data, (ViewGroup) findViewById(R.id.dialog_layout));


        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("new data");
        alert.setView(layout);
        alert.setCancelable(false);
        alert.setPositiveButton("O K", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DataStr data = new DataStr();
                EditText idText = (EditText) layout.findViewById(R.id.student_id);
                EditText nameText = (EditText) layout.findViewById(R.id.name);
                data.setStudent_id(idText.getText().toString());
                data.setStudent_name(nameText.getText().toString());
                data.setCard_id(idm);

                if (dataope.insertData(data)) {
                    //ここで音を出したりする？
                    Log.d("insert", "成功");
                } else {
                    //失敗音？
                    Log.d("insert", "失敗");
                }
            }
        });
        alert.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //キャンセル
            }
        });
        alert.create().show();
    }


    //button event
    @Override
    public void onClick(View view) {

        if (view != null) {
            switch (view.getId()) {
                case R.id.state_button:
                    if (isChecked) {
                        check.setText("登校");
                        isChecked = false;
                    } else {
                        check.setText("下校");
                        isChecked = true;
                    }
                    break;
                default:
            }
        }
    }


}
