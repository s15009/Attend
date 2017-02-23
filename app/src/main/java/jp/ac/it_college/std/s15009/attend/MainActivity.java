package jp.ac.it_college.std.s15009.attend;


import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import jp.ac.it_college.std.s15009.attend.Database.AttendDBHelper;
import jp.ac.it_college.std.s15009.attend.Database.DataStr;
import jp.ac.it_college.std.s15009.attend.Database.DatabaseOperation;


public class MainActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener {

    private IntentFilter[] intentFilters;
    private String[][] techlistarray;
    private PendingIntent pendingIntent;
    private NfcAdapter nfcAdapter;
    private boolean isChecked = true;
    private Handler mHandler;
    private DatabaseOperation dataope;
    private ToggleButton attend;
    private ToggleButton back_school;
    private SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy'年'MM'月'dd'日' kk'時'mm'分'ss'秒'");
    private SimpleDateFormat month = new SimpleDateFormat("yyyy-MM-dd");
    private int ok_sound;
    private int ng_sound;
    private SoundPool soundPool;
    private RecyclerView mRecycler;
    private ArrayList<Stu_info> stu_data = new ArrayList<>();

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

        //スキャン出来るカードの種類設定
        techlistarray = new String[][]{new String[]{NfcF.class.getName()}};
        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        //database系
        AttendDBHelper mDbhelper = new AttendDBHelper(getApplicationContext());
        dataope = new DatabaseOperation(mDbhelper);

        //Button達
        attend = (ToggleButton)findViewById(R.id.attending_button);
        back_school = (ToggleButton)findViewById(R.id.backschool_button);
        attend.setOnCheckedChangeListener(this);
        back_school.setOnCheckedChangeListener(this);

        //音声用
        AudioAttributes attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attr)
                .setMaxStreams(2)
                .build();
        ok_sound = soundPool.load(getApplicationContext(),R.raw.scan_ok ,1);
        ng_sound = soundPool.load(getApplicationContext(),R.raw.scan_ng ,1);

        //recylerview 系 TODO:ここで日付出席サーチ
        mRecycler = (RecyclerView)findViewById(R.id.recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager
                (this, LinearLayoutManager.VERTICAL, false));
        Dummydata();
        RecyclerAdapter adapter = new RecyclerAdapter(this, stu_data);
        mRecycler.setAdapter(adapter);




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

                        ((TextView)findViewById(R.id.current_time)).setText(now);
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
        if(tag == null){
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

            if(dataope.Attend_scan(Pkey, isChecked)){
                Calendar ca = Calendar.getInstance();
                String to = month.format(ca.getTime());
                Log.d("time", "time is:" + to);
                dataope.getData(to);
                //サウンド再生
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        soundPool.play(ok_sound, 1f, 1f, 0, 0, 1);
                    }
                }, 1000);
                Toast.makeText(this, "出席しました", Toast.LENGTH_SHORT);
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        soundPool.play(ng_sound, 1f, 1f, 0, 0, 1);
                    }
                }, 1000);
                Toast.makeText(this,"既にスキャン済みです", Toast.LENGTH_SHORT).show();
            }
            dataope.test_data(Pkey);
        }

    }

    @Override
    protected void onPause(){
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);

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

    //dialog
    private void Data_save_dialog(final String idm) {

        LayoutInflater layoutInflater = (LayoutInflater)
                this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.
                inflate(R.layout.dialog_data, (ViewGroup) findViewById(R.id.dialog_layout));


        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("学生情報登録");
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


    //Toggle Button 系
    @Override
    public void onCheckedChanged(CompoundButton compButton, boolean state) {
        switch (compButton.getId()){
            case R.id.attending_button:
                if(state){
                    isChecked = true;
                    back_school.setChecked(false);
                } else {
                    isChecked = false;
                    back_school.setChecked(true);
                }
                break;
            case R.id.backschool_button:
                if (state){
                    isChecked = false;
                    attend.setChecked(false);
                } else {
                    isChecked = true;
                    attend.setChecked(true);
                }
                break;
        }
    }

    private void Dummydata(){
        Stu_info in = new Stu_info();
        in.setNum_stu("s15009");
        in.setName_stu("前川和輝");
        stu_data.add(in);

        Stu_info in2 = new Stu_info();
        in2.setNum_stu("s18006");
        in2.setName_stu("田中太郎");
        stu_data.add(in2);

        Stu_info in3 = new Stu_info();
        in3.setNum_stu("n19008");
        in3.setName_stu("熱斗和悪");
        stu_data.add(in3);

        Stu_info in4 = new Stu_info();
        in4.setNum_stu("c18010");
        in4.setName_stu("栗瑛太");
        stu_data.add(in4);
    }
}
