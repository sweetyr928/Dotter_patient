package gujc.dotterPatient.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gujc.dotterPatient.CustomDialog;
import gujc.dotterPatient.R;
import gujc.dotterPatient.bot.BotAdapter;
import gujc.dotterPatient.model.Board;
import gujc.dotterPatient.model.Chatbot;
import gujc.dotterPatient.model.UserModel;

public class BotFragment extends Fragment {
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<Chatbot> arrayList = new ArrayList<>();
    private BotAdapter botAdapter;
    private UserModel user;
    private Board board;
    private String fuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String mcurrent = "nmtest";
    private String ucurrent = "";
    private String bcurrent = "";
    private List<String> arrayboard = new ArrayList<String>();
    private String doctor = "";
    private String doctorid = "";
    private String hospital = "";
    private String title = "";
    private String phoneNum = "";
    public String roomid;
    private FirebaseFirestore firebase = FirebaseFirestore.getInstance();
    private boolean request;
    ProgressDialog pd1;

    public BotFragment() {
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bot, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        arrayList = new ArrayList<>();
        botAdapter = new BotAdapter(arrayList);
        recyclerView.setAdapter(botAdapter);

        final Button button1 = (Button) view.findViewById(R.id.button1);
        final Button button2 = (Button) view.findViewById(R.id.button2);
        final Button button3 = (Button) view.findViewById(R.id.button3);
        final Button button4 = (Button) view.findViewById(R.id.button4);

        DocumentReference ref = FirebaseFirestore.getInstance().collection("users").document(fuser);
        ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(UserModel.class);
                Chatbot chatbot = new Chatbot("bot", "안녕하세요" + user.getUsernm() + "님이 맞나요?");
                arrayList.add(chatbot);
                botAdapter.notifyDataSetChanged();
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chatbot chatbot = new Chatbot(fuser, mcurrent);
                switch (chatbot.getCurrent()) {
                    case "nmtest": {
                        mcurrent = "nmyes";
                        ucurrent = "네";
                        bcurrent = "어디가 아프신가요?";
                        arrayboard.add("문진");
                        button1.setText("상체");
                        button2.setText("하체");
                        break;
                    }
                    case "nmyes": {
                        mcurrent = "dnl";
                        ucurrent = "상체";
                        bcurrent = "더 자세히 알려주세요1";
                        arrayboard.add(ucurrent);
                        button1.setText("머리");
                        button2.setText("가슴");
                        button3.setText("배");
                        button3.setVisibility(View.VISIBLE);
                        break;
                    }
                    case "dnl": {
                        mcurrent = "head";
                        ucurrent = "머리";
                        bcurrent = "더 자세히 알려주세요2";
                        arrayboard.add(ucurrent);
                        button1.setText("얼굴");
                        button2.setText("얼굴 외");
                        button3.setVisibility(View.GONE);
                        break;
                    }
                    case "head": {
                        mcurrent = "face";
                        ucurrent = "얼굴";
                        bcurrent = "더 자세히 알려주세요3";
                        arrayboard.add(ucurrent);
                        button1.setText("안과");
                        button2.setText("이비인후과");
                        button3.setText("치과");
                        button3.setVisibility(View.VISIBLE);
                        break;
                    }
                    case "face": {
                        mcurrent = "card";
                        ucurrent = "안과";
                        bcurrent = "결제";
                        arrayboard.add(ucurrent);
                        button1.setText("결제");
                        break;
                    }

                    //버튼2-1
                    case "nmno": {
                        mcurrent = "addman";
                        ucurrent = "추가";
                        bcurrent = "미완";
                        arrayboard.add(ucurrent);
                        button1.setText("다시 시작");
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);
                        button4.setVisibility(View.GONE);
                        break;
                    }
                    case "dkfo": {
                        mcurrent = "leg";
                        ucurrent = "다리";
                        bcurrent = "더 자세히 알려주세요2";
                        arrayboard.add(ucurrent);
                        button1.setText("종아리");
                        button2.setText("발");
                        break;
                    }
                    case "leg": {
                        mcurrent = "card";
                        ucurrent = "종아리";
                        bcurrent = "결제";
                        arrayboard.add(ucurrent);
                        button1.setText("결제");
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);
                        button4.setVisibility(View.GONE);
                        break;
                    }
                    case "ear": {
                        mcurrent = "card";
                        ucurrent = "귀";
                        bcurrent = "결제";
                        arrayboard.add(ucurrent);
                        button1.setText("결제");
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);
                        button4.setVisibility(View.GONE);
                        break;
                    }

                    //버튼3-1
                    case "stomach": {
                        mcurrent = "card";
                        ucurrent = "윗배";
                        bcurrent = "결제";
                        arrayboard.add(ucurrent);
                        button1.setText("결제");
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);
                        break;
                    }
                    //결제
                    case "card": {
                        mcurrent = "cyes";
                        ucurrent = "";
                        bcurrent = "이카드로 진행할까요?";
                        button1.setText("yes");
                        button2.setText("no");
                        button2.setVisibility(View.VISIBLE);
                        break;
                    }
                    case "cyes": {
                        ucurrent = "";
                        bcurrent = "매칭시작을 눌러주세요";
                        mcurrent = "match";
                        button1.setText("의사 매칭하기");
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);
                        button4.setVisibility(View.GONE);

                        break;
                    }
                    case "match": {
                        //button1.setVisibility(View.GONE);
                        roomid = firebase.collection("Board").document().getId();
                        CreateBoard(firebase.collection("Board").document(roomid));
                        String strboard = String.valueOf(arrayboard);
                        Map<String, Object> boardcur = new HashMap<>();
                        boardcur.put("title", strboard);
                        boardcur.put("id", fuser);
                        boardcur.put("timestamp", new Timestamp(new Date()));
                        boardcur.put("name", user.getUsernm() + " 님");
                        boardcur.put("match", false);
                        boardcur.put("request", false);
                        boardcur.put("doctor", "none");
                        boardcur.put("hospital", "none");
                        boardcur.put("doctorid", "none");
                        boardcur.put("status", 1);
                        boardcur.put("identification", 1);
                        boardcur.put("phoneNum", user.getPhone());

                        firebase.collection("Board").document(roomid).set(boardcur)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });
                        pd1 = ProgressDialog.show(getContext(), "", "매칭 중");
                        toDialog();

                        break;
                    }
                    default: {
                        break;
                    }
                }
                chatbot = new Chatbot(fuser, ucurrent);
                Chatbot chatbot1 = new Chatbot("bot", bcurrent);
                if (ucurrent.equals("")) {
                    arrayList.add(chatbot1);
                } else if (bcurrent.equals("")) {
                    arrayList.add(chatbot);
                } else {
                    arrayList.add(chatbot);
                    arrayList.add(chatbot1);
                }
                botAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(arrayList.size() - 1);


            }
        });

        //버튼2
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chatbot chatbot = new Chatbot(fuser, mcurrent);
                switch (chatbot.getCurrent()) {
                    case "nmtest": {
                        mcurrent = "nmno";
                        ucurrent = "아니오";
                        bcurrent = "사람을 추가 할까요?";
                        arrayboard.add("문진");
                        button1.setText("추가");
                        button2.setText("아니오");
                        break;
                    }
                    case "nmno": {
                        mcurrent = "addman";
                        ucurrent = "추가";
                        bcurrent = "";
                        arrayboard.add(ucurrent);
                        button1.setText("추가");
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);
                        button4.setVisibility(View.GONE);
                        break;
                    }
                    case "nmyes": {
                        mcurrent = "dkfo";
                        ucurrent = "하체";
                        bcurrent = "더 자세히 알려주세요1";
                        arrayboard.add(ucurrent);
                        button1.setText("다리");
                        button2.setText("발");
                        break;
                    }
                    case "dkfo": {
                        mcurrent = "card";
                        ucurrent = "발";
                        bcurrent = "결제";
                        arrayboard.add(ucurrent);
                        button1.setText("결제");
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);
                        button4.setVisibility(View.GONE);
                        break;
                    }
                    case "dnl": {
                        mcurrent = "card";
                        ucurrent = "가슴";
                        bcurrent = "결제";
                        arrayboard.add(ucurrent);
                        button1.setText("결제");
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);
                        button4.setVisibility(View.GONE);
                        break;
                    }
                    case "head": {
                        mcurrent = "card";
                        ucurrent = "얼굴 외";
                        bcurrent = "결제";
                        arrayboard.add(ucurrent);
                        button1.setText("결제");
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);
                        break;
                    }
                    case "face": {
                        mcurrent = "card";
                        ucurrent = "이비인후과";
                        bcurrent = "결제";
                        arrayboard.add(ucurrent);
                        button1.setText("결제");
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);
                        break;
                    }
                    case "stomach": {
                        mcurrent = "card";
                        ucurrent = "아랫배";
                        bcurrent = "결제";
                        arrayboard.add(ucurrent);
                        button1.setText("결제");
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);
                        break;
                    }
                    case "card": {
                        ucurrent = "cno";
                        bcurrent = "카드등록";
                        mcurrent = "카드등록";
                        button1.setText("카드등록하기");
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);
                        button4.setVisibility(View.GONE);

                        break;
                    }
                    default: {
                        break;
                    }
                }
                chatbot = new Chatbot(fuser, ucurrent);
                Chatbot chatbot1 = new Chatbot("bot", bcurrent);
                if (ucurrent.equals("")) {
                    arrayList.add(chatbot1);
                } else if (bcurrent.equals("")) {
                    arrayList.add(chatbot);
                } else {
                    arrayList.add(chatbot);
                    arrayList.add(chatbot1);
                }
                botAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(arrayList.size() - 1);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chatbot chatbot = new Chatbot(fuser, mcurrent);
                switch (chatbot.getCurrent()) {
                    case "dnl": {
                        mcurrent = "stomach";
                        ucurrent = "배";
                        bcurrent = "더 자세히 알려주세요2";
                        arrayboard.add(ucurrent);
                        button1.setText("윗배");
                        button2.setText("아랫배");
                        button3.setVisibility(View.GONE);
                        break;
                    }
                    case "face": {
                        mcurrent = "card";
                        ucurrent = "치과";
                        bcurrent = "결제";
                        arrayboard.add(ucurrent);
                        button1.setText("결제");
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);
                        break;
                    }

                    default: {
                        break;
                    }
                }
                chatbot = new Chatbot(fuser, ucurrent);
                Chatbot chatbot1 = new Chatbot("bot", bcurrent);
                if (ucurrent.equals("")) {
                    arrayList.add(chatbot1);
                } else if (bcurrent.equals("")) {
                    arrayList.add(chatbot);
                } else {
                    arrayList.add(chatbot);
                    arrayList.add(chatbot1);
                }
                botAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(arrayList.size() - 1);
            }
        });

        return view;

    }

    public void CreateBoard(final DocumentReference room) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", null);

        room.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
    }

    private void toDialog() {
        final DocumentReference docRef = firebase.collection("Board").document(roomid);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                board = snapshot.toObject(Board.class);

                doctor = board.getDoctor();
                hospital = board.getHospital();
                request = board.isRequest();
                doctorid = board.getDoctorid();
                title = board.getTitle();
                phoneNum = board.getPhoneNum();

                if (request) {
                    pd1.dismiss();
                    CustomDialog customDialog = new CustomDialog(getContext());
                    customDialog.callFunction(roomid, doctor, hospital, doctorid, title,phoneNum);
                }
            }
        });
    }

}