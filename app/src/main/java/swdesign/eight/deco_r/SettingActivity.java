package swdesign.eight.deco_r;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    int circleSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        onclik(
//                setCircleSize();
//        )
    }

    public void setCircleSize() {
        //circleSize변경 로직
    }

    public int gerCircleSize() {
        //로직
        return circleSize;
    }

}
