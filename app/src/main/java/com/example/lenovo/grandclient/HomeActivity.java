package com.example.lenovo.grandclient;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    private String return_msg;
    private String sendMessage;
    private Button callButton;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    String partnerNumber;
    String myPhoneNumber;
    SharedPreferences setting;
    int connectCount = 0;

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent=new Intent(this.getIntent());
        callButton = (Button) findViewById(R.id.callButton);

        setting = getSharedPreferences("setting", 0);

        //백그라운드에서 서버에 현재 좌표 보내주는 서비스 실행
        Intent serviceIntent = new Intent(
                getApplicationContext(),//현재제어권자
                MyService.class); // 이동할 컴포넌트
        startService(serviceIntent); // 서비스 시작

        //전화 걸기 권한 체크
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            // 권한 없다면 권한 요청

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
        }else{
            // 권한 있음
        }

        // 서버로부터 연동된 전화번호 받아오기
        myPhoneNumber = setting.getString("myPhoneNumber", "null");
        sendMessage = myPhoneNumber + "20";
        Log.d("TCP", "1");

        //TCP 쓰레드 생성
        TCPClient tcpThread = new TCPClient(sendMessage, connectCount, HomeActivity.this);
        Thread thread = new Thread(tcpThread);
        thread.start();

        try {
            thread.join();
            Log.d("TCP", "2");
        } catch (Exception e) {

        }
        Log.d("TCP", "3");

        if(tcpThread.getReturnMessage() == null)
        {
            partnerNumber = "error";
        }
        else
        {
            partnerNumber = tcpThread.getReturnMessage().substring(2, 13); // 헤더 빼기
            partnerNumber = partnerNumber.substring(0, 3).concat("-").concat(partnerNumber.substring(3,7)).concat("-").concat(partnerNumber.substring(7,11));
            Log.d("TCP", "partnerNumber : " + partnerNumber);
        }

//
        //전화 걸기 버튼 이벤트 리스너
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:010-9350-0510"));
                if(partnerNumber == "error")
                {
                    Toast.makeText(HomeActivity.this, "서버에 연결 시도 중입니다.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+partnerNumber));
                    try {
                        Log.d("TCP", "before call");
                        startActivity(intent);
                        Log.d("TCP", "call");
                    }
                    catch(Exception e) {
                        Log.d("TCP", "printstackTrace");
                        e.printStackTrace();
                    }
                }

            }
        });

    }
}
