package com.example.calendar2;

// 보완할 점
// 1. 앱을 껐다 키거나 달력을 전환하고 나서도 EventDecorator 유지하기
// 2. 배당금 추가하기에서 모든 항목을 입력하지 않았을 경우 추가하기 버튼이 작동하지 않도록 해야 함
// 3. 삭제하기 다이얼로그


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    // MainActivity.java 에서 사용할 변수 선언
    Button btn_plus;
    Button btn_delete;
    Button btn_calendar1;
    Button btn_calendar2;
    MaterialCalendarView calendar1;
    TextView tv_content;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // layout의 변수 연결
        btn_plus = (Button) findViewById(R.id.btn_plus);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        btn_calendar1 = (Button) findViewById(R.id.btn_calendar1);
        btn_calendar2 = (Button) findViewById(R.id.btn_calendar2);
        calendar1 = (MaterialCalendarView) findViewById(R.id.calendar1);
        tv_content = (TextView) findViewById(R.id.tv_content);

        // 배당금 달력 꾸미기
        calendar1.addDecorators(new SaturdayDecorator(), new SundayDecorator(), new MySelectorDecorator(this), new TodayDecorator());

        // 배당금 추가하기
        dialog = new Dialog(this);


        // 배당금 삭제하기
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CalendarDay selectedDate = new CalendarDay(calendar1.getSelectedDate().getYear(), calendar1.getSelectedDate().getMonth(), calendar1.getSelectedDate().getDay());

                String FirebaseKey = selectedDate.toString();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference();

                databaseReference.child(selectedDate.toString()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        String FirebaseValue = snapshot.getValue(String.class);

                        if (FirebaseValue == null) {
                            return;
                        }

                        else {
                            DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseKey);
                            databaseReference.setValue(null);

                            calendar1.removeDecorator(new EventDecorator(Color.RED, Collections.singleton(selectedDate)));

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


//                CalendarDay selectedDate = new CalendarDay(calendar1.getSelectedDate().getYear(), calendar1.getSelectedDate().getMonth(), calendar1.getSelectedDate().getDay());
//
//                String FirebaseKey = selectedDate.toString();
//                String FirebaseValue = null;
//                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//                DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseKey);
//                databaseReference.setValue(FirebaseValue);
//
//                calendar1.removeDecorator(new EventDecorator(Color.RED, Collections.singleton(selectedDate)));
//
//                // EventDecorator 가 삭제된 걸 반영하기 위해 MainActivity(배당금 달력) 새로고침
//
//                finish();
//                overridePendingTransition(0, 0);
//                startActivity(getIntent());
//                overridePendingTransition(0, 0);

            }
        });

        // 배당금 일정 띄우기
        calendar1.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                CalendarDay selectedDate = new CalendarDay(calendar1.getSelectedDate().getYear(), calendar1.getSelectedDate().getMonth(), calendar1.getSelectedDate().getDay());

                String FirebaseKey = selectedDate.toString();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference();

                databaseReference.child(selectedDate.toString()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        String FirebaseValue = snapshot.getValue(String.class);

                        if (FirebaseValue == null) {
                            tv_content.setText("배당금 지급 일정이 없습니다");
                        }

                        else {

                            tv_content.setText(FirebaseValue);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });

            }
        });



        // 배당금 달력 → 배당금 달력 화면 전환하기
        btn_calendar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_calendar1 = new Intent(getApplicationContext(), MainActivity.class);
                //finish();
                startActivity(intent_calendar1);

            }
        });

        // 배당금 달력 → 공모주 달력 화면 전환하기
        btn_calendar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_calendar2 = new Intent(getApplicationContext(), SubActivity.class);
                finish();
                startActivity(intent_calendar2);
            }
        });

    }

    public void ShowPopup(View v) {

        // activity_popup 에서 사용할 변수 선언
        AutoCompleteTextView autoCompleteTextView_name;
        EditText et_count, et_price;
        DatePicker dp_date;
        Button btn_plus;



        // activity_popup 띄우기
        dialog.setContentView(R.layout.plus_devidend);

        // layout과 activity 끼리 객체 연결결
        autoCompleteTextView_name = (AutoCompleteTextView) dialog.findViewById(R.id.autoCompleteTextView_name);
        et_count = (EditText) dialog.findViewById(R.id.et_count);
        et_price = (EditText) dialog.findViewById(R.id.et_price);
        dp_date = (DatePicker) dialog.findViewById(R.id.dp_date);
        btn_plus = (Button) dialog.findViewById(R.id.btn_plus);

        // 종목명 입력받기
        List list = new ArrayList<>();

        InputStream myInput;
        AssetManager assetManager = getAssets();
        try {
            myInput = assetManager.open("names.xls");
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            Iterator<Row> rowIter = mySheet.rowIterator();
            int rowno = 0;

            while (rowIter.hasNext()) {
                Log.e(TAG, "row no" + rowno);
                HSSFRow myRow = (HSSFRow) rowIter.next();

                if (rowno != 0) {
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    int colno = 0;
                    String c1 = "", c2 = "", c3 = "", c4 = "", c5 = "", c6 = "", c7 = "", c8 = "", c9 = "", c10 = "", c11 = "", c12 = "";

                    while (cellIter.hasNext()) {
                        HSSFCell myCell= (HSSFCell) cellIter.next();

                        if (colno == 0) {
                            c1 = myCell.toString();
                        } else if (colno == 1) {
                            c2 = myCell.toString();
                        } else if (colno == 2) {
                            c3 = myCell.toString();
                        } else if (colno == 3) {
                            c4 = myCell.toString();
                            list.add(c4 + " ");
                        } else if (colno == 4) {
                            c5 = myCell.toString();
                        } else if (colno == 5) {
                            c6 = myCell.toString();
                        } else if (colno == 6) {
                            c7 = myCell.toString();
                        } else if (colno == 7) {
                            c8 = myCell.toString();
                        } else if (colno == 8) {
                            c9 = myCell.toString();
                        } else if (colno == 9) {
                            c10 = myCell.toString();
                        } else if (colno == 10) {
                            c11 = myCell.toString();
                        } else if (colno == 11) {
                            c12 = myCell.toString();

                        }
                        colno++;
                        Log.e(TAG, "Index:" + myCell.getColumnIndex() + "--" + myCell.toString());
                    }


                }
                rowno++;
            }


        } catch (IOException e) {
            Log.e(TAG, "error" + e.toString());
        }

        autoCompleteTextView_name.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, list));


        // 추가하기 버튼 누르면 다이얼로그 닫힘
        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String input_name;
                String input_count;
                String input_price;
                CalendarDay input_date;

                input_name = autoCompleteTextView_name.getText().toString();
                input_count = et_count.getText().toString();
                input_price = et_price.getText().toString();
                input_date = new CalendarDay(dp_date.getYear(), dp_date.getMonth(), dp_date.getDayOfMonth());

                calendar1.addDecorators(new EventDecorator(Color.RED, Collections.singleton(input_date)));

                String FirebaseKey = input_date.toString();
                String FirebaseValue = input_name + " 배당금 " + Integer.parseInt(input_count)*Integer.parseInt(input_price) + " 원";
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseKey);
                databaseReference.setValue(FirebaseValue);

                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

    }

}