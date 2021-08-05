package com.example.calendar2;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubActivity extends AppCompatActivity {

    Button btn_calendar1;
    Button btn_calendar2;
    Button btn_delete1;
    MaterialCalendarView calendar2;
    TextView tv_content1;
    Button btn_plus1;
    AutoCompleteTextView autoCompleteTextView_name1;
    EditText et_price1;
    DatePicker dp_date1;
    private Dialog dialog1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        // EventDecorator 유지하기
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("stock");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                List SavedDates = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) { // 저장된 날짜 불러오기
                    String SavedDate = dataSnapshot.getKey().toString();
                    SavedDates.add(SavedDate);
                }

                List YearList = new ArrayList<>();
                List MonthList = new ArrayList<>();
                List DayList = new ArrayList<>();

                for (int i=0; i<SavedDates.size(); i++) {
                    String SavedDate = SavedDates.get(i).toString();

                    String Blank = ""; // CalendarDay{, -, } 제거
                    String ClearSavedDate = SavedDate;
                    ClearSavedDate = ClearSavedDate.replace("CalendarDay{",Blank);
                    ClearSavedDate = ClearSavedDate.replace("-", Blank);
                    ClearSavedDate = ClearSavedDate.replace("}", Blank);

                    if (SavedDate.length() == 23) {
                        //2021 12 12

                        String Year = ClearSavedDate.substring(0,4);
                        String Month = ClearSavedDate.substring(4,6);
                        String Day = ClearSavedDate.substring(ClearSavedDate.length()-2, ClearSavedDate.length());

                        YearList.add(Year);
                        MonthList.add(Month);
                        DayList.add(Day);

                    }

                    if (SavedDate.length() == 22 && String.valueOf(SavedDate.charAt(18)) != "-") {
                        //2021 10 4

                        String Year = ClearSavedDate.substring(0,4);
                        String Month = ClearSavedDate.substring(4,6);
                        String Day = String.valueOf(ClearSavedDate.charAt(6));

                        YearList.add(Year);
                        MonthList.add(Month);
                        DayList.add(Day);
                    }

                    if (SavedDate.length() == 22 && String.valueOf(SavedDate.charAt(18)) == "-") {
                        // 2021 1 13

                        String Year = ClearSavedDate.substring(0,4);
                        String Month = String.valueOf(ClearSavedDate.charAt(4));
                        String Day = ClearSavedDate.substring(ClearSavedDate.length()-2, ClearSavedDate.length());

                        YearList.add(Year);
                        MonthList.add(Month);
                        DayList.add(Day);
                    }

                    if (SavedDate.length() == 21) {
                        // 2021 8 4

                        String Year = ClearSavedDate.substring(0,4);
                        String Month = String.valueOf(ClearSavedDate.charAt(4));
                        String Day = String.valueOf(ClearSavedDate.charAt(5));

                        YearList.add(Year);
                        MonthList.add(Month);
                        DayList.add(Day);

                    }

                    CalendarDay addDecoDay = new CalendarDay(Integer.parseInt(YearList.get(i).toString()), Integer.parseInt(MonthList.get(i).toString())-1, Integer.parseInt(DayList.get(i).toString()));
                    calendar2.addDecorators(new EventDecorator(Color.RED, Collections.singleton(addDecoDay)));
                    // calendar1.addDecorators(new EventDecorator(Color.RED, Collections.singleton(deco_date)));
                }

//                tv_content.setText(MonthList.toString());



            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        // layout의 변수 연결
        btn_calendar1 = (Button) findViewById(R.id.btn_calendar1);
        btn_calendar2 = (Button) findViewById(R.id.btn_calendar2);
        btn_plus1 = (Button) findViewById(R.id.btn_plus1);
        btn_delete1 = (Button) findViewById(R.id.btn_delete1);
        calendar2 = (MaterialCalendarView) findViewById(R.id.calendar2);
        tv_content1 = (TextView) findViewById(R.id.tv_content1);
        //calendar = (MaterialCalendarView) findViewById(R.id.calendar);

        // 공모주 달력
        calendar2.addDecorators(new SaturdayDecorator(), new SundayDecorator(), new TodayDecorator());
        // calendar2.addDecorators(new SaturdayDecorator(), new SundayDecorator(), new MySelectorDecorator(this), new TodayDecorator());

        dialog1 = new Dialog(this);


        btn_delete1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CalendarDay selectedDate = new CalendarDay(calendar2.getSelectedDate().getYear(), calendar2.getSelectedDate().getMonth() + 1, calendar2.getSelectedDate().getDay());
                CalendarDay deco_date = new CalendarDay(calendar2.getSelectedDate().getYear(), calendar2.getSelectedDate().getMonth(), calendar2.getSelectedDate().getDay());

                String FirebaseKey = selectedDate.toString();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference();

                databaseReference.child(selectedDate.toString()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        String FirebaseValue = snapshot.getValue(String.class);

                        if (FirebaseValue == null) {
                            return;
                        } else {
                            DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseKey);
                            databaseReference.setValue(null);

                            calendar2.removeDecorator(new EventDecorator(Color.RED, Collections.singleton(selectedDate)));

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });


            }
        });

        calendar2.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date,
                                       boolean selected) {

                CalendarDay selectedDate = new CalendarDay(calendar2.getSelectedDate().getYear(), calendar2.getSelectedDate().getMonth() + 1, calendar2.getSelectedDate().getDay());

                String FirebaseKeyRoot = "stock"; // 키 : 공모주
                String FirebaseKeyDate = selectedDate.toString(); // 키 : 날짜

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseKeyRoot).child(FirebaseKeyDate); // 날짜를 키로 하는 레퍼런스

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {


                        List KeyName = new ArrayList<>(); // 날짜가 가지고 있는 종목명을 가진 리스트
                        List Value = new ArrayList<>(); // 날짜의 종목별 공모주를 저장할 리스트

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) { // 날짜가 가지고 있는 종목명을 가진 리스트

                            String FirebaseKeyName = dataSnapshot.getKey().toString();
                            KeyName.add(FirebaseKeyName);


//                            String FirebaseValue = dataSnapshot.getKey().toString();
//                            list.add(FirebaseValue);
                        }


                        for (int i = 0; i < KeyName.size(); i++) { // 날짜의 종목별 공모주를 저장할 리스트

                            String FirebaseValueName = String.valueOf(KeyName.get(i));
                            //String FirebaseValueCount = snapshot.child(FirebaseValueName).child("Count").getValue().toString();
                            if (snapshot.child(FirebaseValueName).child("Price").getValue() != null) { // 이 값이 null 값이라고 자꾸 NullPoint 오류나서 예외 지정 → 읽은 날에 추가하면 발생하는 듯함
                                String FirebaseValuePrice = snapshot.child(FirebaseValueName).child("Price").getValue().toString();
                                String FirebaseValue = FirebaseValueName + " 청약 시작 공모액 : " + Integer.parseInt(FirebaseValuePrice) + "원";
                                Value.add(FirebaseValue);
                            }

                        }

                        List test = new ArrayList<>(); // 공모주 일정이 없는 날을 선별하기 위한 빈 리스트
                        if (Value.toString().equals(test.toString())) {
                            tv_content1.setText("공모주 청약종목이 없습니다");
                        } else {
                            tv_content1.setText(Value.toString()); // 날짜의 종목별 공모주를 모두 띄움
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
            }
        });


        // 공모주 달력 → 배당금 달력 화면 전환하기
        btn_calendar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_calendar1 = new Intent(getApplicationContext(), MainActivity.class);
                //finish();
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

    public void ShowPopup(View v) {

        // activity_popup 에서 사용할 변수 선언
        AutoCompleteTextView autoCompleteTextView_name1;
        EditText et_count1, et_price1;
        DatePicker dp_date1;
        Button btn_plus1;

        // activity_popup 띄우기

        dialog1.setContentView(R.layout.plus_stock);

        // layout과 activity 끼리 객체 연결
        autoCompleteTextView_name1 = (AutoCompleteTextView) dialog1.findViewById(R.id.autoCompleteTextView_name1);
        et_price1 = (EditText) dialog1.findViewById(R.id.et_price1);
        dp_date1 = (DatePicker) dialog1.findViewById(R.id.dp_date1);
        btn_plus1 = (Button) dialog1.findViewById(R.id.btn_plus1);

        btn_plus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input_name1; // 입력한 종목영
                String input_price1; // 예상 청약금
                CalendarDay input_date1; // 입력한 배당금 지급일
                CalendarDay deco_date1; //점 찍는 데 사용할 날짜

                //ActionBar.Tab autoCompleteTextView_name1 = null;
                input_name1 = autoCompleteTextView_name1.getText().toString();
                input_price1 = et_price1.getText().toString();
                input_date1 = new CalendarDay(dp_date1.getYear(), dp_date1.getMonth() + 1, dp_date1.getDayOfMonth());
                deco_date1 = new CalendarDay(dp_date1.getYear(), dp_date1.getMonth(), dp_date1.getDayOfMonth());

                if (input_name1.length() != 0 && input_price1.length() != 0) {

                    calendar2.addDecorators(new EventDecorator(Color.RED, Collections.singleton(deco_date1)));

                    String FirebaseKeyRoot = "stock";
                    String FirebaseKeyDate = input_date1.toString();
                    String FirebaseKeyName = input_name1;
                    String FirebaseValuePrice = input_price1;

                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseKeyRoot).child(FirebaseKeyDate).child(FirebaseKeyName);
                    databaseReference.child("Price").setValue(FirebaseValuePrice);
                    dialog1.dismiss();

                } else {
                    Toast.makeText(getApplicationContext(), "모두 입력해주세요", Toast.LENGTH_SHORT).show();
                }

            }
        });
        dialog1.show();
        // dialog.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);



    }
}


