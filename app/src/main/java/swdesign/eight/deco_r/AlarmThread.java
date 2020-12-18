package swdesign.eight.deco_r;

import android.os.Handler;
import android.os.Message;

public class AlarmThread extends Thread{
    Handler handler;
    boolean isRun = true;

    public AlarmThread(Handler handler){
        this.handler = handler;
    }

    public void stopForever(){
        synchronized (this) {
            this.isRun = false;
        }
    }

    public void run(){
        //반복적으로 수행할 작업을 한다.
        while(isRun){
            handler.sendEmptyMessage(0);//쓰레드에 있는 핸들러에게 메세지를 보냄
            try{
                Thread.sleep(5000); //5초씩 쉰다.
            }catch (Exception e) {}
        }
    }
}
