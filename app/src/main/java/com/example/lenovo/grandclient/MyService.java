package com.example.lenovo.grandclient;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.util.Iterator;

// 서비스 클래스를 구현하려면, Service 를 상속받는다
public class MyService extends Service {
    //MediaPlayer mp; // 음악 재생을 위한 객체
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    long startTime;
    boolean firstCount = true;
    boolean isInHome = false;

    String myPhoneNumber;
    SharedPreferences setting;

    double latitudeDiff;
    double longitudeDiff;

    private int connectCount = 0;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setting = getSharedPreferences("setting", 0);

        // Acquire a reference to the system Location Manager
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isGPSEnabled == false) {
            Toast.makeText(getApplicationContext(), "GPS를 켜주세요", Toast.LENGTH_LONG).show();
        }
        // 네트워크 프로바이더 사용가능여부
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                double lat;
                double lng;
                float accuracy;
                boolean isAccurate = true;

                if (firstCount == true)
                    Toast.makeText(getApplicationContext(), "기기를 8자 모양으로 돌리시면 정확도가 상승합니다.", Toast.LENGTH_LONG).show();

                if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                    //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
                    lng = location.getLongitude();    //경도
                    lat = location.getLatitude();         //위도
                    accuracy = location.getAccuracy();        //신뢰도
                    if (accuracy > 25)
                        isAccurate = false;
                } else {
                    //Network 위치제공자에 의한 위치변화
                    //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
                    lng = location.getLongitude();    //경도
                    lat = location.getLatitude();         //위도
                    accuracy = location.getAccuracy();        //신뢰도
                    if (accuracy > 25)
                        isAccurate = false;
                }

                //처음 실행했거나 1분이 지나면 서버에 보내기
                long currentTime = System.currentTimeMillis();
                Log.d("test", "isAccurate : " + String.valueOf(isAccurate));

                if (firstCount == true || isAccurate == true) {
                    firstCount = false;


                    //소수점 이하 6자리로 자르기
                    lat *= 1000000;
                    lat = Math.round(lat);
                    lat /= 1000000;

                    lng *= 1000000;
                    lng = Math.round(lng);
                    lng /= 1000000;

                    //서버에 보낼 형식으로 가공하기
                    String sendLatitude = String.valueOf(lat);
                    String sendLongitude = String.valueOf(lng);

                    //소수점 앞이 세 자리가 아닐 때 세 자리로 만들어주기
                    switch (sendLatitude.indexOf(".")) {
                        case 0:
                            sendLatitude = "000".concat(sendLatitude);
                            break;
                        case 1:
                            sendLatitude = "00".concat(sendLatitude);
                            break;
                        case 2:
                            sendLatitude = "0".concat(sendLatitude);
                            break;
                        default:
                            break;
                    }

                    switch (sendLongitude.indexOf(".")) {
                        case 0:
                            sendLatitude = "000".concat(sendLongitude);
                            break;
                        case 1:
                            sendLatitude = "00".concat(sendLongitude);
                            break;
                        case 2:
                            sendLatitude = "0".concat(sendLongitude);
                            break;
                        default:
                            break;
                    }

                    //소수점 아래를 6자리로 만들어주기
                    if (sendLatitude.length() != 10) {
                        int length = sendLatitude.length();
                        for (int i = 0; i < 10 - length; i++) {
                            Log.d("test", String.valueOf(sendLatitude.length()));
                            sendLatitude = sendLatitude.concat("0");
                        }
                    }

                    //소수점 아래를 6자리로 만들어주기
                    if (sendLongitude.length() != 10) {
                        int length = sendLongitude.length();
                        for (int i = 0; i < 10 - length; i++)
                            sendLongitude = sendLongitude.concat("0");
                    }

                    //서버에 GrandClient의 집 위치를 요청
                    myPhoneNumber = setting.getString("myPhoneNumber", "null");
                    String sendMessage = myPhoneNumber + "50";
                    Log.d("test", sendMessage);
                    TCPClient tcpThread = new TCPClient(sendMessage, connectCount, getApplicationContext());
                    Thread thread = new Thread(tcpThread);
                    thread.start();

                    try {
                        thread.join();
                        Log.d("TCP", "try in MyService");
                    } catch (Exception e) {
                        Log.d("TCP", "error in MyService");
                    }

                    //서버에서 보낸 Message를 해독
                    String returnMessage = tcpThread.getReturnMessage();
                    Log.d("test", "return in myservice : " + returnMessage);

                    if(returnMessage == null)
                    {
                        //서버와 연결 안 됨
                    }
                    else
                    {
                        if("50FAILURE".equals(returnMessage))
                        {
                            latitudeDiff = 999;
                            longitudeDiff = 999;
                            Log.d("test", "7777777777777777");
                        }
                        else
                        {

                            returnMessage = returnMessage.substring(2, 22);
                            Log.d("test", returnMessage);
                            String tempLatitude = returnMessage.substring(0, 10);
                            String tempLongitude = returnMessage.substring(10, 20);

                            Log.d("test", "123123213213");
                            Double homeLatitude = Double.parseDouble(tempLatitude);
                            Double homeLongitude = Double.parseDouble(tempLongitude);

                            //집에 있을 때 실내라서 GPS가 튈 가능성이 있음. 만약 집에 있을 때 집 위치와 차이가 많이 나면 서버에 보내지 않음
                            latitudeDiff = Math.abs(Double.parseDouble(sendLatitude) - homeLatitude);
                            longitudeDiff = Math.abs(Double.parseDouble(sendLongitude) - homeLongitude);

                            Log.d("test", "5555555555555");
                        }

                        if (latitudeDiff < 0.0005 && longitudeDiff < 0.0005) // 집에 들어왔다고 판단
                        {
                            isInHome = true;
                            Log.d("test", "inside home");
                        }
                        else {
                            isInHome = false;
                            Log.d("test", "outside home");
                        }

                        //집에 있는 경우 오차 검사
                        if(isInHome == true)
                        {
                            //집 위치와 오차가 많이 나면 서버에 보내지 않음
                            if(latitudeDiff > 0.0005 || longitudeDiff > 0.0005)
                            {
                                Log.d("test", "오차 심함");
                            }
                            else
                            {
                                sendMessage = myPhoneNumber + "40" + sendLatitude + sendLongitude;
                                Log.d("test", sendMessage);
                                tcpThread = new TCPClient(sendMessage, connectCount, getApplicationContext());
                                thread = new Thread(tcpThread);
                                thread.start();
                            }
                        }
                        //집 밖에 있는 경우 오차 검사 하지 않고 보냄
                        else
                        {
                            sendMessage = myPhoneNumber + "40" + sendLatitude + sendLongitude;
                            Log.d("test", sendMessage);
                            tcpThread = new TCPClient(sendMessage, connectCount, getApplicationContext());
                            thread = new Thread(tcpThread);
                            thread.start();
                        }
                    }

                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("test", "onStatusChanged");
            }

            public void onProviderEnabled(String provider) {
                Log.d("test", "onProviderEnabled");

            }

            public void onProviderDisabled(String provider) {
                Log.d("test", "onProviderDisabled");
                Toast.makeText(getApplicationContext(), "GPS를 켜주세요", Toast.LENGTH_LONG).show();
            }
        };


        int permissionCheck = ContextCompat.checkSelfPermission(MyService.this, Manifest.permission.ACCESS_FINE_LOCATION);

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListener);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        Log.d("test", "서비스의 onStartCommand");
        //mp.start(); // 노래 시작

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 실행
        //mp.stop(); // 음악 종료
        Log.d("test", "서비스의 onDestroy");
    }
}