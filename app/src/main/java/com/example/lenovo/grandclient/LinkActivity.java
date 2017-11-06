package com.example.lenovo.grandclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class LinkActivity extends AppCompatActivity {

    private String return_msg;
    private String sendMessage;
    private EditText partnerNumberEditText;
    private int connectCount = 0;
    private ImageView notice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);

        Intent intent = new Intent(this.getIntent());

        partnerNumberEditText = (EditText) findViewById(R.id.partnerNumberEditText);
        notice = (ImageView) findViewById(R.id.notice);


        //연동 확인 버튼
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //내 전화번호 받아오기
                TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                String myPhoneNumber = telManager.getLine1Number();
                if (myPhoneNumber.startsWith("+82")) {
                    myPhoneNumber = myPhoneNumber.replace("+82", "0");
                }

                final String myNumber = myPhoneNumber;
                final String partnerNumber = partnerNumberEditText.getText().toString();
                sendMessage = myPhoneNumber + "80" + partnerNumber + myPhoneNumber;

                //아무것도 입력 안 했을 때
                if (partnerNumber.length() == 0) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            LinkActivity.this);

                    // 제목셋팅
                    alertDialogBuilder.setTitle("연동 실패");

                    // AlertDialog 셋팅
                    alertDialogBuilder
                            .setMessage("정보를 입력해주세요.")
                            .setCancelable(false)
                            .setPositiveButton("확인",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                    // 다이얼로그 생성
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // 다이얼로그 보여주기
                    alertDialog.show();
                } else {
                    //입력 잘 됐을 때

                    //TCP 쓰레드 생성
                    TCPClient tcpThread = new TCPClient(sendMessage, connectCount, LinkActivity.this);
                    Thread thread = new Thread(tcpThread);
                    thread.start();

                    try {
                        thread.join();

                    } catch (Exception e) {

                    }

                    return_msg = tcpThread.getReturnMessage();

                    if (return_msg == null) {
                        //서버 연결 안 됐을 때

                    } else {
                        //연동 성공 시
                        if ("80SUCCESS".equals(return_msg)) {
                            //연동 성공
                            Log.d("TCP", return_msg);

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    LinkActivity.this);

                            // 제목셋팅
                            alertDialogBuilder.setTitle("연동 성공");

                            // AlertDialog 셋팅
                            alertDialogBuilder
                                    .setMessage("연동에 성공했습니다.\n" +
                                            "보호자 어플리케이션을 다시 시작해 주세요.")
                                    .setCancelable(false)
                                    .setPositiveButton("성공",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog, int id) {

                                                    // 프로그램을 종료한다
                                                    Intent intent = new Intent(LinkActivity.this, HomeActivity.class);
                                                    //intent.putExtra(“text”,String.valueOf(editText.getText()));
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });

                            // 다이얼로그 생성
                            AlertDialog alertDialog = alertDialogBuilder.create();

                            // 다이얼로그 보여주기
                            alertDialog.show();
                        }
                        else if ("81FAILURE".equals(return_msg))
                        {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    LinkActivity.this);

                            // 제목셋팅
                            alertDialogBuilder.setTitle("연동 실패");

                            // AlertDialog 셋팅
                            alertDialogBuilder
                                    .setMessage("보호자가 이미 다른 번호와 연동되어 있습니다.")
                                    .setCancelable(false)
                                    .setPositiveButton("취소",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                            // 다이얼로그 생성
                            AlertDialog alertDialog = alertDialogBuilder.create();

                            // 다이얼로그 보여주기
                            alertDialog.show();
                        }
                        else {
                            //연동 실패
                            Log.d("TCP", "fail" + return_msg);

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    LinkActivity.this);

                            // 제목셋팅
                            alertDialogBuilder.setTitle("연동 실패");

                            // AlertDialog 셋팅
                            alertDialogBuilder
                                    .setMessage("가입되지 않은 번호입니다.")
                                    .setCancelable(false)
                                    .setPositiveButton("취소",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                            // 다이얼로그 생성
                            AlertDialog alertDialog = alertDialogBuilder.create();

                            // 다이얼로그 보여주기
                            alertDialog.show();
                        }
                    }
                }
            }
        });
    }
}


