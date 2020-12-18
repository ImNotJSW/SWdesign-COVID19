package swdesign.eight.deco_r;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    //RadioButton 객체와 클릭 리스너 정의
    private RadioButton muteBtn, vibrationBtn, soundBtn;
    RadioButton.OnClickListener alarmSelectClickListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Toast.makeText(SettingActivity.this, "" + muteBtn.isChecked() + vibrationBtn.isChecked() + soundBtn.isChecked(), Toast.LENGTH_SHORT).show();
        }
    };

    //EditText 객체 정의
    EditText circleSize_text, updateInterval_text;


    //설정 적용, 취소 버튼 정의
    Button acceptBtn, denyBtn;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //이전 엑티비티로부터 전달받은 Intent
        Intent prevIntent = getIntent();

        //RadioButton 클릭 리스너 적용
        muteBtn = findViewById(R.id.radioBtn1);
        vibrationBtn = findViewById(R.id.radioBtn2);
        soundBtn = findViewById(R.id.radioBtn3);
        muteBtn.setOnClickListener(alarmSelectClickListener);
        vibrationBtn.setOnClickListener(alarmSelectClickListener);
        soundBtn.setOnClickListener(alarmSelectClickListener);
        switch (prevIntent.getIntExtra("alarmType", 3)) { //기본적으로 기존 값에 체크되어 있음
            case 1:
                muteBtn.setChecked(true);
                break;
            case 2:
                vibrationBtn.setChecked(true);
                break;
            case 3:
                soundBtn.setChecked(true);
        }


        //EditText 적용
        circleSize_text = findViewById(R.id.circleSize_editText);
        updateInterval_text = findViewById(R.id.updateInterval_editText);
        circleSize_text.setText(prevIntent.getDoubleExtra("circleSize", 100.0) + ""); //기본적으로 기존 값이 들어가있음
        updateInterval_text.setText(prevIntent.getIntExtra("updateInterval", 12) + "");


        //설정 적용, 취소하는 버튼 리스너 적용
        acceptBtn = findViewById(R.id.acceptBtn);
        acceptBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent에 결과값을 넣고 반환할 것임
                Intent returnIntent = new Intent();

                //알림 설정 적용하기
                int alarmType;
                if (muteBtn.isChecked()) alarmType = 1;
                else if (vibrationBtn.isChecked()) alarmType = 2;
                else alarmType = 3;
                returnIntent.putExtra("alarmType", alarmType);


                String wanted_circleSize = circleSize_text.getText().toString();
                String wanted_updateInterval = updateInterval_text.getText().toString();
                //원 범위 입력 검사 (숫자 범위, 입력 여부)
                try {
                    double circleSize = Double.parseDouble(wanted_circleSize);
                    if (circleSize <= 0 || circleSize > 10000)
                        throw new Exception("범위 초과");
                    returnIntent.putExtra("circleSize", circleSize);
                } catch (Exception e) {
                    Toast.makeText(SettingActivity.this, "원 크기 설정값 오류", Toast.LENGTH_SHORT).show();
                    return;
                }

                //업데이트 주기 입력 검사 (숫자 범위, 입력 여부)
                try {
                    int updateInterval = Integer.parseInt(wanted_updateInterval);
                    if (updateInterval <= 0)
                        throw new Exception("범위 초과");
                    returnIntent.putExtra("updateInterval", updateInterval);
                } catch (Exception e) {
                    Toast.makeText(SettingActivity.this, "업데이트 주기 설정값 오류", Toast.LENGTH_SHORT).show();
                    return;
                }

                //현재 엑티비티의 결과를 설정하고, 엑티비티를 끝냄
                setResult(101, returnIntent);
                finish();
            }
        });

        denyBtn = findViewById(R.id.denyBtn);
        denyBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



    }
}

