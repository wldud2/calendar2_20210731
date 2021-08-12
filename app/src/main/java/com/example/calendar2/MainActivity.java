package com.example.calendar2;

// 보완할 점
// 1. 앱을 껐다 키거나 달력을 전환하고 나서도 EventDecorator 유지하기 → ok
// 2. 배당금 추가하기에서 모든 항목을 입력하지 않았을 경우 추가하기 버튼이 작동하지 않도록 해야 함 → ok
// 3. 삭제하기 다이얼로그 → ok
// 4. 다른 핸드폰에서도 파이어 베이스 데이터 추가/삭제 업데이트 되어야 함 → ok
// 5. 하루에 배당금 여러개 → ok
// 6. 공모주 달력 완성하기 → 관리페이지? → ok
// 7. 배당금 입력 내용 수정하기 → xx
// 8. 로그인 → 민주

// 9. 배당금 추가하기 다이얼로그가 닫힌 뒤에 버튼이 안보임 → AndroidManifest.xml - MainActivity.java 에 android:windowSoftInputMode="adjustPan" 추가 → ok
// 10. 배당금을 읽고 추가하기 하면 종료됨 (NullPoint) → .child("Price").getValue() 값이 null 값이라고 자꾸 NullPoint 오류나서 예외 지정 → 읽은 날에 추가하면 발생하는 듯함 → ok
// https://hashcode.co.kr/questions/10877/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%8A%A4%ED%8A%9C%EB%94%94%EC%98%A4%EA%B0%80-%EA%B0%95%EC%A0%9C%EC%A2%85%EB%A3%8C-%EB%90%A9%EB%8B%88%EB%8B%A4-javalangstring-javalangobjecttostring-on-a-null-object-reference
// https://github.com/Azure-Samples/ms-identity-android-native/issues/17
// https://yeolco.tistory.com/73
// → https://jamesdreaming.tistory.com/42

// 11. Month 인식 이상함 2021 10 4 와 2021 1 13 → ok

// 12. 삭제 후 빨간 점 안 사라짐 (새로고침이 안 먹음) → ok
// 13. 삭제하기 다이얼로그 가로가 짤림 (삭제하기 버튼 짤림 + 스크롤바 안 보임) → ok
// 14. 삭제하기 다이얼로그에 각 배당금이 2번씩 나옴 → ok

// 15. 한 날에 배당금 여러개가 있는 날에는 배당금 삭제하기에서 첫번째 종목만 날짜가 입력됨


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.DataBufferObserverSet;
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

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    // MainActivity.java 에서 사용할 변수 선언
    Button btn_plus;
    Button btn_delete;
    Button btn_calendar1;
    Button btn_calendar2;
    // Button btn_getDate;
    MaterialCalendarView calendar1;
    TextView tv_content;
    Dialog dialog;
    Dialog DeleteDialog;

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

        // EventDecorator 유지하기
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("dividend");

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

                    if (SavedDate.length() == 22 && !String.valueOf(SavedDate.charAt(18)).equals("-")) {
                        //2021 10 4

                        String Year = ClearSavedDate.substring(0,4);
                        String Month = ClearSavedDate.substring(4,6);
                        String Day = String.valueOf(ClearSavedDate.charAt(6));

                        YearList.add(Year);
                        MonthList.add(Month);
                        DayList.add(Day);
                    }

                    if (SavedDate.length() == 22 && String.valueOf(SavedDate.charAt(18)).equals("-")) {
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
                    calendar1.addDecorators(new EventDecorator(Color.RED, Collections.singleton(addDecoDay)));
                    // calendar1.addDecorators(new EventDecorator(Color.RED, Collections.singleton(deco_date)));
                }

//                tv_content.setText(MonthList.toString());



            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });


        // 배당금 달력 꾸미기
        calendar1.addDecorators(new SaturdayDecorator(), new SundayDecorator(), new TodayDecorator());
        // new MySelectorDecorator(this);

        // 배당금 추가하기
        dialog = new Dialog(this);


        // 배당금 삭제하기 → 수정 필요
        //// 다이얼로그 설정
        DeleteDialog = new Dialog(MainActivity.this);
        DeleteDialog.setContentView(R.layout.delete_dividend);

        //// 다이얼로그에서 사용할 객체 선언
        RecyclerView rv;
        LinearLayoutManager linearLayoutManager;
        DeleteAdapter deleteAdapter;
        ArrayList<DeleteInfo> items = new ArrayList<>();
        Button btn_close;

        //// layout과 객체 연결
        rv = (RecyclerView) DeleteDialog.findViewById(R.id.rv);
        btn_close = (Button) DeleteDialog.findViewById(R.id.btn_close);
        linearLayoutManager = new LinearLayoutManager(DeleteDialog.getContext(), RecyclerView.VERTICAL, false);
        deleteAdapter = new DeleteAdapter(items);
        rv.setLayoutManager(linearLayoutManager);
        rv.setAdapter(deleteAdapter);

        //// 스크롤 속도 설정
        rv.setNestedScrollingEnabled(false);

        //// 리사이클러뷰에 사용할 리스트에 데이터 추가
        //// 리사이클러뷰에 사용할 리스트에 데이터 추가
        FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference1 = firebaseDatabase1.getReference("dividend");
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot1) {

                for (DataSnapshot dataSnapshot : snapshot1.getChildren()) {
                    String FirebaseValueDate = dataSnapshot.getKey();
                    String Blank = ""; // CalendarDay{, -, } 제거
                    String DeleteDate= FirebaseValueDate;
                    DeleteDate = DeleteDate.replace("CalendarDay{",Blank);
                    DeleteDate = DeleteDate.replace("-", Blank);
                    DeleteDate = DeleteDate.replace("}", Blank);
//                    System.out.println(FirebaseValueDate);
//                    System.out.println(DeleteDate);
//                    System.out.println("길이는"+FirebaseValueDate.length());
//                    System.out.println("18번째는"+String.valueOf(FirebaseValueDate.charAt(18)));
//                    System.out.println(DeleteDate.indexOf("0"));

                    for (DataSnapshot dataSnapshot1 : snapshot1.child(FirebaseValueDate).getChildren()) {
                        String FirebaseValueName = dataSnapshot1.getKey();

                        for (DataSnapshot dataSnapshot2 : snapshot1.child(FirebaseValueDate).child(FirebaseValueName).getChildren()) {

                            String FirebaseValueCount = snapshot1.child(FirebaseValueDate).child(FirebaseValueName).child("Count").getValue().toString();
                            if (snapshot1.child(FirebaseValueDate).child(FirebaseValueName).child("Price").getValue()!=null) {
                                String FirebaseValuePrice = snapshot1.child(FirebaseValueDate).child(FirebaseValueName).child("Price").getValue().toString();

                                if (FirebaseValueDate.length() == 23) {
                                    //2021 12 12
                                    String DeleteYear = DeleteDate.substring(0,4);
                                    String DeleteMonth = DeleteDate.substring(4,6);
                                    String DeleteDay = DeleteDate.substring(DeleteDate.length()-2, DeleteDate.length());
                                    DeleteDate = DeleteYear + "년 " + DeleteMonth + "월 " + DeleteDay +"일";
                                    items.add(new DeleteInfo(DeleteDate, FirebaseValueName, FirebaseValueCount, FirebaseValuePrice));
                                }

                                if (FirebaseValueDate.length() == 22 && !String.valueOf(FirebaseValueDate.charAt(18)).equals("-")) {
                                    //2021 10 4
                                    String DeleteYear = DeleteDate.substring(0,4);
                                    String DeleteMonth = DeleteDate.substring(4,6);
                                    String DeleteDay = String.valueOf(DeleteDate.charAt(6));;
                                    DeleteDate = DeleteYear + "년 " + DeleteMonth + "월 " + DeleteDay +"일";
                                    items.add(new DeleteInfo(DeleteDate, FirebaseValueName, FirebaseValueCount, FirebaseValuePrice));
                                }

                                if (FirebaseValueDate.length() == 22 && String.valueOf(FirebaseValueDate.charAt(18)).equals("-")) {
                                    // 2021 1 13
                                    String DeleteYear = DeleteDate.substring(0,4);
                                    String DeleteMonth = String.valueOf(DeleteDate.charAt(4));;
                                    String DeleteDay = DeleteDate.substring(DeleteDate.length()-2, DeleteDate.length());
                                    DeleteDate = DeleteYear + "년 " + DeleteMonth + "월 " + DeleteDay +"일";
                                    items.add(new DeleteInfo(DeleteDate, FirebaseValueName, FirebaseValueCount, FirebaseValuePrice));
                                }

                                if (FirebaseValueDate.length() == 21) {
                                    // 2021 8 4
                                    String DeleteYear = DeleteDate.substring(0,4);
                                    String DeleteMonth = String.valueOf(DeleteDate.charAt(4));;
                                    String DeleteDay = String.valueOf(DeleteDate.charAt(5));;
                                    DeleteDate = DeleteYear + "년 " + DeleteMonth + "월 " + DeleteDay +"일";
                                    items.add(new DeleteInfo(DeleteDate, FirebaseValueName, FirebaseValueCount, FirebaseValuePrice));
                                }

                                FirebaseValueName = FirebaseValueName;
                                FirebaseValueCount = FirebaseValueCount;
                                FirebaseValuePrice = FirebaseValuePrice;
                                //items.add(new DeleteInfo(DeleteDate, FirebaseValueName, FirebaseValueCount, FirebaseValuePrice));
                            }



                        }
                    }
                }

                deleteAdapter.notifyDataSetChanged();

                btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int index = 0;
                        for (Iterator<DeleteInfo> it = items.iterator(); it.hasNext(); )
                        {
                            it.next(); // Add this line in your code
                            if (index % 2 != 0)
                            {
                                it.remove();
                            }
                            index++;
                        }

                        // 삭제용 키값 계산 테스트용
//                        String DateTest, DateTest2, DateTestYear, DateTestMonth, DateTestDay;
//                        for (int test=0; test<items.size(); test++) {
//                            DateTest = items.get(test).date;
//                            System.out.println(DateTest);
//                            DateTestYear = DateTest.substring(0,DateTest.indexOf("년"));
//                            DateTestMonth = DateTest.substring(DateTest.indexOf("년"),DateTest.indexOf("월"));
//                            DateTestDay = DateTest.substring(DateTest.indexOf("월"),DateTest.indexOf("일"));
//                            DateTest2 = "CalendarDay{"+DateTestYear+"-"+DateTestMonth+"-"+DateTestDay+"}";
//                            DateTest2 = DateTest2.replace("년","");
//                            DateTest2 = DateTest2.replace("월","");
//                            DateTest2 = DateTest2.replace("일","");
//                            DateTest2 = DateTest2.replace(" ","");
//                            System.out.println(DateTest2);
//                        }

                        // 아이템리스트에서 홀수번째 삭제하기 초기 코드 → 315 줄로 대체
//                        int itemsize = items.size();
//                        for (int index=0; index<itemsize; index++) {
//                            if (index%2 != 0) {
//                                items.remove(index);
//                            }
//                            System.out.println(index+"번째 아이템"+items.get(index).name);
//                            System.out.println("2로 나누었을 때 나머지는 "+index%2+"\n");
//                            if (!String.valueOf(index%2).equals("0")) {
//                                System.out.println("없어질 요소는"+index+"번째\n");
//                                items.remove(index);
//                            }
//                        }

                        DeleteDialog.show();

                        // 다이얼로그 크기 조절 : https://stackoverflow.com/questions/28513616/android-get-full-width-for-custom-dialog
                        // Window window = DeleteDialog.getWindow();
                        // window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, 1500);

                        // 다이얼로그 크기 조절 : https://www.masterqna.com/android/20340/%EC%BB%A4%EC%8A%A4%ED%85%80-%EB%8B%A4%EC%9D%B4%EC%96%BC%EB%A1%9C%EA%B7%B8%EC%9D%98-%ED%81%AC%EA%B8%B0%EB%B3%80%EA%B2%BD
                        // WindowManager.LayoutParams wm = new WindowManager.LayoutParams();
                        // wm.copyFrom(DeleteDialog.getWindow().getAttributes());
                        // wm.width = 800;
                        // wm.height = 500;

                        DeleteDialog.setCanceledOnTouchOutside(false);

                        btn_close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                deleteAdapter.notifyDataSetChanged();

                                // 닫기 버튼 클릭하면 다이얼로그 닫히고 MainActivity 새로고침 (빨간점 사라짐)
                                // DeleteDialog.dismiss();
                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(getIntent());
                                overridePendingTransition(0, 0);



                            }
                        });

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });




        // 배당금 일정 띄우기
        calendar1.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                CalendarDay selectedDate = new CalendarDay(calendar1.getSelectedDate().getYear(), calendar1.getSelectedDate().getMonth()+1, calendar1.getSelectedDate().getDay());

                String FirebaseKeyRoot = "dividend"; // 키 : 배당금
                String FirebaseKeyDate = selectedDate.toString(); // 키 : 날짜

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseKeyRoot).child(FirebaseKeyDate); // 날짜를 키로 하는 레퍼런스

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {


                        List KeyName = new ArrayList<>(); // 날짜가 가지고 있는 종목명을 가진 리스트
                        List Value = new ArrayList<>(); // 날짜의 종목별 배당금을 저장할 리스트

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) { // 날짜가 가지고 있는 종목명을 가진 리스트

                            String FirebaseKeyName = dataSnapshot.getKey().toString();
                            KeyName.add(FirebaseKeyName);


//                            String FirebaseValue = dataSnapshot.getKey().toString();
//                            list.add(FirebaseValue);

                        }

                        for (int i=0; i<KeyName.size(); i++) { // 날짜의 종목별 배당금을 저장할 리스트

                            String FirebaseValueName = String.valueOf(KeyName.get(i));
                            String FirebaseValueCount = snapshot.child(FirebaseValueName).child("Count").getValue().toString();
                            if (snapshot.child(FirebaseValueName).child("Price").getValue()!=null) { // 이 값이 null 값이라고 자꾸 NullPoint 오류나서 예외 지정 → 읽은 날에 추가하면 발생하는 듯함
                                String FirebaseValuePrice = snapshot.child(FirebaseValueName).child("Price").getValue().toString();
                                String FirebaseValue = FirebaseValueName + " 배당금 : " + Integer.parseInt(FirebaseValueCount)*Integer.parseInt(FirebaseValuePrice) + "원\n";
                                Value.add(FirebaseValue);
                            }
//                            String FirebaseValuePrice = snapshot.child(FirebaseValueName).child("Price").getValue().toString();
//                            String FirebaseValue = FirebaseValueName + "배당금" + Integer.parseInt(FirebaseValueCount)*Integer.parseInt(FirebaseValuePrice) + "원";
//                            Value.add(FirebaseValue);

                        }

                        List test = new ArrayList<>(); // 배당금 일정이 없는 날을 선별하기 위한 빈 리스트
                        if (Value.toString().equals(test.toString())) {
                            tv_content.setText("배당금 지급 일정이 없습니다");
                        }
                        else {
                            tv_content.setText(Value.toString()); // 날짜의 종목별 배당금을 모두 띄움
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });


//                String FirebaseKey = selectedDate.toString();
//                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//                DatabaseReference databaseReference = firebaseDatabase.getReference();

//                databaseReference.child(selectedDate.toString()).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot snapshot) {
//
//                        String FirebaseValue = snapshot.getValue(String.class);
//
//                        if (FirebaseValue == null) {
//                            tv_content.setText("배당금 지급 일정이 없습니다");
//                        }
//
//                        else {
//
//                            tv_content.setText(FirebaseValue);
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError error) {
//
//                    }
//                });

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
        dialog.setContentView(R.layout.plus_dividend);

        // layout과 activity 끼리 객체 연결
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

                String input_name; // 입력한 종목영
                String input_count; // 입력한 보유 주 수
                String input_price; // 입력한 1주당 배당금
                CalendarDay input_date; // 입력한 배당금 지급일
                CalendarDay deco_date; //점 찍는 데 사용할 날짜

                input_name = autoCompleteTextView_name.getText().toString();
                input_count = et_count.getText().toString();
                input_price = et_price.getText().toString();
                input_date = new CalendarDay(dp_date.getYear(), dp_date.getMonth()+1, dp_date.getDayOfMonth());
                deco_date = new CalendarDay(dp_date.getYear(), dp_date.getMonth(), dp_date.getDayOfMonth());

                if (input_name.length()!=0 && input_count.length()!=0 && input_price.length()!=0) {

                    calendar1.addDecorators(new EventDecorator(Color.RED, Collections.singleton(deco_date)));

                    String FirebaseKeyRoot = "dividend";
                    String FirebaseKeyDate = input_date.toString();
                    String FirebaseKeyName = input_name;
                    String FirebaseValueCount = input_count;
                    String FirebaseValuePrice = input_price;

                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseKeyRoot).child(FirebaseKeyDate).child(FirebaseKeyName);
                    databaseReference.child("Count").setValue(FirebaseValueCount);
                    databaseReference.child("Price").setValue(FirebaseValuePrice);

                    dialog.dismiss();

                } else {
                    Toast.makeText(getApplicationContext(), "모두 입력해주세요", Toast.LENGTH_SHORT).show();
                }

//                calendar1.addDecorators(new EventDecorator(Color.RED, Collections.singleton(deco_date)));
//
//                String FirebaseKeyRoot = "dividend";
//                String FirebaseKeyDate = input_date.toString();
//                String FirebaseKeyName = input_name;
//                String FirebaseValueCount = input_count;
//                String FirebaseValuePrice = input_price;
//
//                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//                DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseKeyRoot).child(FirebaseKeyDate).child(FirebaseKeyName);
//                databaseReference.child("Count").setValue(FirebaseValueCount);
//                databaseReference.child("Price").setValue(FirebaseValuePrice);


//                String FirebaseKey = input_date.toString();
//                String FirebaseKey2 = input_name;
//
//                String FirebaseValue = input_name + " 배당금 " + Integer.parseInt(input_count)*Integer.parseInt(input_price) + " 원";
//                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//                DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseKey).child(FirebaseKey2);
//                databaseReference.setValue(FirebaseValue);

//                dialog.dismiss();
            }
        });

        dialog.show();
        // dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

    }

}