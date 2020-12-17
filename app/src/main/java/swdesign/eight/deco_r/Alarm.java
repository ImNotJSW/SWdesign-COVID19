package swdesign.eight.deco_r;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;


public class Alarm extends AppCompatActivity {

    Integer alarm_number = 3;//1은 무음 2는 진동 3은 소리

    boolean before_entered=false;//전에 들어와있던 신호
    boolean is_entered;

    NotificationManager manager;



    private static String CHANNEL_ID = "channer1";
    private static String CHANNEL_NAME = "channer1";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DistanceCalculator distanceCalculator = new DistanceCalculator();//distance calculator에서 받아오기
        is_entered = distanceCalculator.compareLocation();


        if (before_entered != is_entered)//before와 is가 같지않을경우
        {
            if (is_entered == true) {
                if (alarm_number == 1) {
                    showNoti1();//클릭하면 메소드 실행
                }

                if (alarm_number == 2) {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(1000, 19));
                    } else {
                        vibrator.vibrate(1000);
                    }
                    showNoti1();
                }


                if (alarm_number == 3) {
                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
                    ringtone.play();
                    showNoti1();
                }
            }
            else
            {
                if (alarm_number == 1) {
                    showNoti2();//클릭하면 메소드 실행
                }

                if (alarm_number == 2) {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(1000, 19));
                    } else {
                        vibrator.vibrate(1000);
                    }
                    showNoti2();
                }


                if (alarm_number == 3) {
                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
                    ringtone.play();
                    showNoti2();
                }
            }
            before_entered = is_entered;
        }
    }
    public void showNoti1() {

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = null; //상단알림 프로그램 객체 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//버전 비교를 통해 알림 코드생성
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
            ));
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        builder.setContentTitle("위험합니다");//알림제목
        builder.setContentText("확진자 반경 내에 접근했습니다");//알림내용
        builder.setSmallIcon(android.R.drawable.ic_menu_view);
        Notification noti = builder.build();

        manager.notify(1, noti);
    }

    public void showNoti2() {

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = null; //상단알림 프로그램 객체 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//버전 비교를 통해 알림 코드생성
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
            ));
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        builder.setContentTitle("안전합니다");//알림제목
        builder.setContentText("확진자 반경 내에 벗어났습니다");//알림내용
        builder.setSmallIcon(android.R.drawable.ic_menu_view);
        Notification noti = builder.build();

        manager.notify(1, noti);
    }
}