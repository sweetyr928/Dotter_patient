package gujc.dotterPatient.bot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import gujc.dotterPatient.R;
import gujc.dotterPatient.model.Chatbot;

public class BotAdapter extends RecyclerView.Adapter<BotAdapter.Holder> {
    public static final int msgtype_left = 0;
    public static final int msgtype_right = 1;

    private Context context;
    private ArrayList<Chatbot> arrayList = new ArrayList<>();
    private String fuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public BotAdapter(ArrayList<Chatbot> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public BotAdapter.Holder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        if (viewType == msgtype_left) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_botbtn, parent, false);
            return new Holder(view);
        }else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_botmsg_right, parent, false);
            return new Holder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final BotAdapter.Holder holder, int position) {
        holder.botname.setText(arrayList.get(position).getName());
        holder.botcurrent.setText(arrayList.get(position).getCurrent());
//        holder.botname.setVisibility(View.GONE);

//누르면 토스트띄워줌
//        holder.itemView.setTag(position);
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String curname = holder.botname.getText().toString();
//                Toast.makeText(view.getContext(),curname,Toast.LENGTH_SHORT).show();
//            }
//        });
//메시지 길게누르면 삭제
//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                remove(holder.getAdapterPosition());
//                return true;
//            }
//        });


    }

    @Override
    public int getItemCount() {
        return (null != arrayList ? arrayList.size() : 0);
    }
//메시지 삭제
//    public void remove(int position){
//        try{
//            arrayList.remove(position);
//            notifyItemRemoved(position);
//        }catch (IndexOutOfBoundsException e){
//            e.printStackTrace();
//        }
//    }

    public class Holder extends RecyclerView.ViewHolder {
        protected TextView botname;
        protected TextView botcurrent;
        protected Button btn1;
        protected Button btn2;

        public Holder(@NonNull View itemView) {
            super(itemView);
            this.botname = (TextView) itemView.findViewById(R.id.msg_name);
            this.botcurrent = (TextView) itemView.findViewById(R.id.msg_current);
            this.btn1 = (Button) itemView.findViewById(R.id.button1);
            this.btn2 = (Button) itemView.findViewById(R.id.button2);
        }
    }

    @Override
    public int getItemViewType(int position) {
        String fuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (arrayList.get(position).getName().equals("나")){
            return msgtype_right;
        }else {
            return msgtype_left;
        }
    }

    public void notifyAdapter(){
        notifyDataSetChanged();
    }
}