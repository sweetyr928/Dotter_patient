package gujc.dotterPatient.fragment;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import gujc.dotterPatient.R;
import gujc.dotterPatient.chat.ChatActivity;
import gujc.dotterPatient.common.FirestoreAdapter;
import gujc.dotterPatient.model.Board;
import gujc.dotterPatient.model.UserModel;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class BoardFragment extends Fragment {

    private FirestoreAdapter firestoreAdapter;
    private static final String TAG = "BoardFragment";
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String fuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private UserModel user;
    public String doctor="";
    public String hospital="";
    ProgressDialog pd;
    private FirebaseFirestore db;
    private Board board;
    public String boardid="";
    RecyclerView recyclerView;

    public BoardFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firestoreAdapter != null) {
            firestoreAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firestoreAdapter != null) {
            firestoreAdapter.stopListening();
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);

        firestoreAdapter = new BoardFragment.RecyclerViewAdapter(FirebaseFirestore.getInstance().collection("Board").orderBy("timestamp")); // orderby 추가해야함

        recyclerView = view.findViewById(R.id.recyclerview);
        //recyclerView.setLayoutManager( new LinearLayoutManager((inflater.getContext()),LinearLayoutManager.HORIZONTAL, false));

        LinearLayoutManager manager = new LinearLayoutManager(inflater.getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        manager.setReverseLayout(false);
        //manager.setStackFromEnd(false);
        recyclerView.setLayoutManager(manager); // timestamp 순으로 출력
        LinearSnapHelper linearSnapHelper = new SnapHelperOneByOne();
        linearSnapHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(firestoreAdapter);

        return view;
    }

    //아이템 넘길 때 부드럽게 넘어갈 수 있도록
    public class SnapHelperOneByOne extends LinearSnapHelper {

        @Override
        public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {

            if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
                return RecyclerView.NO_POSITION;
            }

            final View currentView = findSnapView(layoutManager);

            if (currentView == null) {
                return RecyclerView.NO_POSITION;
            }

            LinearLayoutManager myLayoutManager = (LinearLayoutManager) layoutManager;

            int position1 = myLayoutManager.findFirstVisibleItemPosition();
            int position2 = myLayoutManager.findLastVisibleItemPosition();

            int currentPosition = layoutManager.getPosition(currentView);

            if (velocityX > 400) {
                currentPosition = position2;
            } else if (velocityX < 400) {
                currentPosition = position1;
            }

            if (currentPosition == RecyclerView.NO_POSITION) {
                return RecyclerView.NO_POSITION;
            }

            return currentPosition;
        }
    }

    class RecyclerViewAdapter extends FirestoreAdapter<CustomViewHolder> {
        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));
        private StorageReference storageReference;
        private String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        RecyclerViewAdapter(Query query) {
            super(query);
            storageReference  = FirebaseStorage.getInstance().getReference();
        }

        @Override
        public BoardFragment.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BoardFragment.CustomViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_board, parent, false));
        }

        @Override
        public void onBindViewHolder(BoardFragment.CustomViewHolder viewHolder, int position) {
            final DocumentSnapshot documentSnapshot = getSnapshot(position);
            final Board board = documentSnapshot.toObject(Board.class);
            boardid = documentSnapshot.getId();

            DocumentReference ref = FirebaseFirestore.getInstance().collection("users").document(fuser);
            ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    user = documentSnapshot.toObject(UserModel.class);
                    doctor = user.getUsernm();
                    hospital = user.getUsermsg();
                }
            });

            assert board != null;
            if (board.isMatch() || board.getStatus()==2) {
                viewHolder.itemView.setVisibility(View.GONE);
                viewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }
            else{
                viewHolder.user_name.setText(board.getName());
                viewHolder.user_title.setText(board.getTitle());
            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    AlertDialog.Builder oDialog = new AlertDialog.Builder(getContext(),
                            android.R.style.Theme_DeviceDefault_Light_Dialog);

                    oDialog.setMessage("매칭을 요청하시겠습니까?")
                            .setTitle("          알림")
                            .setPositiveButton("아니오", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Log.i("Dialog", "취소");
                                    Toast.makeText(getContext(), "취소", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNeutralButton("예", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {

                                    documentSnapshot.getReference().update("doctor", doctor);
                                    documentSnapshot.getReference().update("hospital", hospital);
                                    documentSnapshot.getReference().update("doctorid", fuser);
                                    documentSnapshot.getReference().update("request", true);

                                    pd = ProgressDialog.show(getContext(), "", "매칭수락을 기다리는 중 입니다...");
                                    toMatch(boardid);

                                }
                            })
                            .setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.
                            .show();

                }
            }); //누르면 채팅
        }
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView user_photo;
        public TextView user_name;
        public TextView user_title;

        CustomViewHolder(View view) {
            super(view);
            user_photo = view.findViewById(R.id.user_photo);
            user_name = view.findViewById(R.id.user_name);
            user_title = view.findViewById(R.id.user_title);
        }
    }

    private void toMatch(final String bid)
    {
        final DocumentReference docRef = db.getInstance().collection("Board").document(bid);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                board = snapshot.toObject(Board.class);

                int status = board.getStatus();

                if (status==3&&board.isMatch()){
                    pd.dismiss();
                    Intent intent = new Intent(getView().getContext(), ChatActivity.class);
                    intent.putExtra("toUid", board.getId());
                    intent.putExtra("roomID", bid);
                    intent.putExtra("toTitle",board.getTitle());
                    startActivity(intent);
                    //Toast.makeText(getContext(), "매칭이 수락되었습니다.", Toast.LENGTH_LONG).show();
                }
                else if(status==2&& !board.isMatch()){
                    pd.dismiss();
                    Toast.makeText(getContext(), "매칭이 거절되었습니다.", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

}