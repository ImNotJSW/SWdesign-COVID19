//package swdesign.eight.deco_r;
//
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.VibrationEffect;
//import android.os.Vibrator;
//
//import androidx.core.app.NotificationCompat;
//
//public class AlarmService extends Service {
//
//    AlarmThread thread;
//
//    public AlarmService() {
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//
//        //throw new UnsupportedOperationException("Not yet implemented");
//        return null;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        //Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        myServiceHandler handler = new myServiceHandler();
//        thread = new AlarmThread(handler);
//        thread.start();
//        return START_STICKY;
//    }
//
//    //서비스가 종료될 때 할 작업
//
//    public void onDestroy() {
//        thread.stopForever();
//        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌
//    }
//
//    class myServiceHandler extends Handler {
//        @Override
//        public void handleMessage(android.os.Message msg) {
//            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                vibrator.vibrate(VibrationEffect.createOneShot(1000, 19));
//            } else {
//                vibrator.vibrate(1000);
//            }
//
////            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
////            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
////            ringtone.play();
//
//            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            NotificationCompat.Builder builder = null; //상단알림 프로그램 객체 생성
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//버전 비교를 통해 알림 코드생성
//                manager.createNotificationChannel(new NotificationChannel(
//                        CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
//                ));
//                builder = new NotificationCompat.Builder(MyService.this, CHANNEL_ID);
//            } else {
//                builder = new NotificationCompat.Builder(MyService.this);
//            }
//
//            is_entered = true;
//            if(is_entered) { //핀이 반경원안에 들어왔을 경우
//                builder.setContentTitle("위험합니다");//알림제목
//                builder.setContentText("확진자 반경 내에 접근했습니다");//알림내용
//            } else { //핀이 반경원안에서 나갔을 경우
//                builder.setContentTitle("안전합니다");//알림제목
//                builder.setContentText("확진자 반경 내에서 벗어났습니다");//알림내용
//            }
//
//            builder.setSmallIcon(android.R.drawable.ic_menu_view);
//            Notification noti = builder.build();
//
//            manager.notify(1, noti);
//
//        }
//
//    }
//}
