package com.example.calendar2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class DeleteAdapter extends RecyclerView.Adapter<DeleteAdapter.Holder> {

    // 리사이클러뷰의 내용(DeleteInfo)으로 사용할 리스트 선언
    ArrayList<DeleteInfo> items = new ArrayList<>();

    public DeleteAdapter(ArrayList<DeleteInfo> items) {
        this.items = items;
    }

    @NonNull
    @Override

    // 홀더를 통해 리사이클러뷰 띄우기
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new Holder(v);
    }

    @Override
    // 홀더를 통해 띄운 리사이클러뷰에 있는 객체(public class Holder~에서 연결함)의 값 할당
    public void onBindViewHolder(@NonNull DeleteAdapter.Holder holder, int position) {
        DeleteInfo item = items.get(position);
        holder.tv_date.setText(item.getDate());
        holder.tv_name.setText(item.getName());
        holder.tv_count.setText(item.getCount());
        holder.tv_price.setText(item.getPrice());
        holder.btn_deleteDividend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    String FirebaseKeyRoot = "dividend";
                    String FirebaseKeyDate = item.getDate();

                    //날짜 키값 계산
                    String myYear = FirebaseKeyDate.substring(0,FirebaseKeyDate.indexOf("년"));
                    String myMonth = FirebaseKeyDate.substring(FirebaseKeyDate.indexOf("년"),FirebaseKeyDate.indexOf("월"));
                    String myDay = FirebaseKeyDate.substring(FirebaseKeyDate.indexOf("월"),FirebaseKeyDate.indexOf("일"));
                    FirebaseKeyDate = "CalendarDay{" + myYear + "-" + myMonth + "-" + myDay + "}";
                    FirebaseKeyDate = FirebaseKeyDate.replace("년","");
                    FirebaseKeyDate = FirebaseKeyDate.replace("월","");
                    FirebaseKeyDate = FirebaseKeyDate.replace("일","");
                    FirebaseKeyDate = FirebaseKeyDate.replace(" ","");
                    System.out.println(FirebaseKeyDate);

                    String FirebaseKeyName = item.getName();
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference(FirebaseKeyRoot).child(FirebaseKeyDate).child(FirebaseKeyName);

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {

                            databaseReference.setValue(null);
                            items.remove(position);
                            notifyItemRemoved(position);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } catch (IndexOutOfBoundsException exception) {
                    exception.printStackTrace();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // item_list의 객체를 DeleteAdapter로 연결
    public class Holder extends RecyclerView.ViewHolder {

        TextView tv_date, tv_name, tv_count, tv_price;
        Button btn_deleteDividend;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_count = itemView.findViewById(R.id.tv_count);
            tv_price = itemView.findViewById(R.id.tv_price);
            btn_deleteDividend = itemView.findViewById(R.id.btn_deleteDividend);
        }
    }
}
