package com.example.lenovo.grandclient;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;


import static android.os.SystemClock.sleep;

public class TCPClient implements Runnable {
    private final String serverIP = "210.89.178.111";
    //private final String serverIP = "10.0.2.15";
    private final int serverPort = 9190;    //포트번호
    private Socket inetSocket = null;
    private String msg;
    private String return_msg;
    private int connectCount;
    private Context context;

    // private String return_msg;
    public TCPClient(String _msg, int _connectCount, Context _context) {
        this.msg = _msg;
        this.connectCount = _connectCount;
        this.context = _context;
    }

    public String getReturnMessage() {
        return return_msg;
    }

    public void run() {
        // TODO Auto-generated method stub

        connectServer();
    }

    public void connectServer() {
        // TODO Auto-generated method stub
        try {
            Log.d("TCP", "C: Connecting...");

            inetSocket = new Socket(serverIP, serverPort);
            Log.d("TCP", "inetsocket : " +String.valueOf(inetSocket));
            //inetSocket.connect(socketAddr);

            try {
                Log.d("TCP", "C: Sending: '" + msg + "'");
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(inetSocket.getOutputStream())), true);
                out.println(msg);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(inetSocket.getInputStream()));
                return_msg = in.readLine();
                Log.d("TCP", "C: Server send to me this message -->"
                        + return_msg);

            } catch (Exception e) {
                Log.d("TCP", "catch");
            } finally {
                Log.d("TCP", "finally");
                inetSocket.close();
            }
        } catch (Exception e) {
            //서버 꺼져서 연결 안 됐을때
            Log.d("TCP", "catch in TCPClient");
            Handler mainHandler = new Handler(context.getMainLooper());

            //첫 번째 토스트 메시지
            if(connectCount == 0)
            {
                Log.d("TCP", "1111111");
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("TCP", "222222");
                        Toast.makeText(context, "서버에 연결 시도 중입니다.", Toast.LENGTH_LONG).show();
                    }
                });
                connectCount++;
            }

            if(connectCount > 0 && connectCount < 3)
            {
                Log.d("TCP", "333333");
                //reconnect Runnable
                connectCount++;
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(context, "서버에 연결 시도 중입니다. (" + String.valueOf(connectCount) + "/3)", Toast.LENGTH_LONG).show();

                        TCPClient tcpThread = new TCPClient(msg, connectCount, context);
                        Thread thread = new Thread(tcpThread);
                        thread.start();
                    }
                };
                mainHandler.postDelayed(r, 5000);
            }
            else
            {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "연결에 실패했습니다. 문의 : 010-5092-3233", Toast.LENGTH_LONG).show();
                    }
                });
            }

        }
    }

}