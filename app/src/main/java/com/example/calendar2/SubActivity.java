package com.example.calendar2;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

public class SubActivity extends AppCompatActivity {

    Button btn_calendar1;
    Button btn_calendar2;
    MaterialCalendarView calendar2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        // layout의 변수 연결
        btn_calendar1 = (Button) findViewById(R.id.btn_calendar1);
        btn_calendar2 = (Button) findViewById(R.id.btn_calendar2);
        calendar2 = (MaterialCalendarView) findViewById(R.id.calendar2);
        //calendar = (MaterialCalendarView) findViewById(R.id.calendar);

        // 공모주 달력
        calendar2.addDecorators(new SaturdayDecorator(), new SundayDecorator(), new MySelectorDecorator(this), new TodayDecorator());

        // 공모주 달력 → 배당금 달력 화면 전환하기
        btn_calendar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_calendar1 = new Intent(getApplicationContext(), MainActivity.class);
                finish();
                startActivity(intent_calendar1);
            }
        });

        // 공모주 달력 → 공모주 달력 화면 전환하기
        btn_calendar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_calendar2 = new Intent(getApplicationContext(), SubActivity.class);
                //finish();
                startActivity(intent_calendar2);
            }
        });

    }


}
