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

    int alarm_type = 3;//1은 무음 2는 진동 3은 소리

    boolean before_entered;//전에 들어와있던 신호
    boolean is_entered;//distance calculator에서 받아오기

    NotificationManager manager;

    private static String CHANNEL_ID = "channer1";
    private static String CHANNEL_NAME = "channer1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DistanceCalculator distanceCalculator = new DistanceCalculator();

        before_entered = distanceCalculator.compareLocation();
        is_entered = before_entered;

        while(true) {
            //나중: alarm_type을 설정 클래스에서 get해오는 부분
            if (before_entered != is_entered) { //반경원 상태변화 발생 시(반경원 안에 핀이 들어오거나, 나갔을 경우)
                if (alarm_type == 2) { //알림 타입이 진동일 경우
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(1000, 19));
                    } else {
                        vibrator.vibrate(1000);
                    }
                }
                else if (alarm_type == 3) { //알림 타입이 소리일경우
                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
                    ringtone.play();
                }
                showNoti(is_entered); //푸시 알림 진행
                before_entered = is_entered;
            }
            is_entered = distanceCalculator.compareLocation();
            //나중: 일정 시간 딜레이 시키는 부분
        }
    }

    public void showNoti(boolean is_entered) {

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

        if(is_entered) { //핀이 반경원안에 들어왔을 경우
            builder.setContentTitle("위험합니다");//알림제목
            builder.setContentText("확진자 반경 내에 접근했습니다");//알림내용
        } else { //핀이 반경원안에서 나갔을 경우
            builder.setContentTitle("안전합니다");//알림제목
            builder.setContentText("확진자 반경 내에서 벗어났습니다");//알림내용
        }

        builder.setSmallIcon(android.R.drawable.ic_menu_view);
        Notification noti = builder.build();

        manager.notify(1, noti);
    }
}