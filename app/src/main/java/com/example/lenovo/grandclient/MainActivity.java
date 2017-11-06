package com.example.lenovo.grandclient;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private String return_msg;
    private String sendMessage;
    private static final int MY_PERMISSIONS_REQUEST_SEND_LOCATION = 0;
    private static final int MY_PERMISSIONS_READ_PHONE_STATE = 1;
    public static String myPhoneNumber;
    SharedPreferences setting;
    SharedPreferences.Editor editor;
    private int connectCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting에 내 번호 저장
        setting = getSharedPreferences("setting", 0);
        editor= setting.edit();

        //권한 얻기
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // 권한 없음

            Log.d("TCP", "no");

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_READ_PHONE_STATE);
        } else {
            // 권한 있음
            Log.d("TCP", "yes");
        }

        //GPS 권한 체크
        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            // 권한 없음
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_SEND_LOCATION);
        }else{
            // 권한 있음
        }

        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            // 권한 없음
            finish();
        }else{
            // 권한 있음
        }

        if(isServiceRunningCheck() == true)
        {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            //intent.putExtra(“text”,String.valueOf(editText.getText()));
            startActivity(intent);
            finish();
        }
        else
        {
            //연동 할 것이라고 알려주는 다이얼로그
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this);

            // 제목셋팅
            alertDialogBuilder.setTitle("연동");

            // AlertDialog 셋팅
            alertDialogBuilder
                    .setMessage("연동이 되어있는지 확인합니다.")
                    .setCancelable(false)
                    .setPositiveButton("연동",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    link();
                                }
                            });

            // 다이얼로그 생성
            AlertDialog alertDialog = alertDialogBuilder.create();

            // 다이얼로그 보여주기
            alertDialog.show();
        }

    }

    private void link() {

        TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        myPhoneNumber = telManager.getLine1Number();
        if(myPhoneNumber.startsWith("+82")){
            myPhoneNumber = myPhoneNumber.replace("+82", "0");
        }

        editor.putString("myPhoneNumber", myPhoneNumber);
        editor.commit();

        //링크 되어있는지 검사
        sendMessage = myPhoneNumber + "90" + myPhoneNumber;
        Log.d("TCP", sendMessage);
        // TCP 쓰레드 생성
        // TCP 쓰레드 생성
        TCPClient tcpThread = new TCPClient(sendMessage, connectCount, MainActivity.this);
        Thread thread = new Thread(tcpThread);
        thread.start();

        try {
            thread.join();

        } catch (Exception e) {

        }

        return_msg = tcpThread.getReturnMessage();

        //링크 되어있을 때
        if ("90SUCCESS".equals(tcpThread.getReturnMessage())) {
            Log.d("TCP", return_msg);
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            //intent.putExtra(“text”,String.valueOf(editText.getText()));
            startActivity(intent);
            finish();
        }
        //링크 안 되어있을 때
        else if ("90FAILURE".equals(tcpThread.getReturnMessage())){
            Log.d("TCP", "fail" + return_msg);
            Intent intent = new Intent(MainActivity.this, LinkActivity.class);
            //intent.putExtra(“text”,String.valueOf(editText.getText()));
            startActivity(intent);
            Log.d("TCP", "000");
            finish();
            Log.d("TCP", "111");
        }
        else
        {
            //연동 할 것이라고 알려주는 다이얼로그
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this);

            // 제목셋팅
            alertDialogBuilder.setTitle("서버가 꺼져있음");

            // AlertDialog 셋팅
            alertDialogBuilder
                    .setMessage("서버가 꺼져있습니다.\n문의:010-9350-0510")
                    .setCancelable(false)
                    .setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    finish();
                                }
                            });

            // 다이얼로그 생성
            AlertDialog alertDialog = alertDialogBuilder.create();

            // 다이얼로그 보여주기
            alertDialog.show();
        }
    }

    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("package com.example.lenovo.grandclient.MyService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
