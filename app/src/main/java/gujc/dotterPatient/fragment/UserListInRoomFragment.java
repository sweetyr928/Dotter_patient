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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gujc.dotterPatient.R;
import gujc.dotterPatient.chat.ChatActivity;
import gujc.dotterPatient.chat.SelectUserActivity;
import gujc.dotterPatient.model.Board;
import gujc.dotterPatient.model.ChatRoomModel;
import gujc.dotterPatient.model.UserModel;

public class UserListInRoomFragment extends Fragment {
    private String roomID;
    private List<UserModel> userModels;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    public int request = 0;

    public UserListInRoomFragment() {
    }

    public static final UserListInRoomFragment getInstance(String roomID, Map<String, UserModel> userModels) {
        List<UserModel> users = new ArrayList();
        for( Map.Entry<String, UserModel> elem : userModels.entrySet() ){
            users.add(elem.getValue());
        }

        UserListInRoomFragment f = new UserListInRoomFragment();
        f.setUserList(users);
        Bundle bdl = new Bundle();
        bdl.putString("roomID", roomID);
        f.setArguments(bdl);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userlistinroom, container, false);
        if (getArguments() != null) {
            roomID = getArguments().getString("roomID");
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager( new LinearLayoutManager((inflater.getContext())));
        recyclerView.setAdapter(new UserFragmentRecyclerViewAdapter());

        view.findViewById(R.id.addContactBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                phoneRequest();
            }
        });

        return view;
    }

    void phoneRequest()
    {
        final DocumentReference rooms = db.getInstance().collection("rooms").document(roomID);
        rooms.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable final DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                ChatRoomModel chatRoomModel = snapshot.toObject(ChatRoomModel.class);
                request = chatRoomModel.getIdrequest();

                if(request==1) {
                    AlertDialog.Builder oDialog = new AlertDialog.Builder(getContext(),
                            android.R.style.Theme_DeviceDefault_Light_Dialog);

                    oDialog.setMessage("본인확인을 위해 전화번호의 공개유무를 선택해주세요.")
                            .setTitle("     개인정보 요청알림")
                            .setPositiveButton("비공개", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    snapshot.getReference().update("idrequest", 2);
                                    Toast.makeText(getContext(), "전화번호가 비공개 처리 되었습니다.", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNeutralButton("공개", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    snapshot.getReference().update("idrequest", 3);
                                    Toast.makeText(getContext(), "전화번호가 공개되었습니다.", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.
                            .show();
                }
            }
        });
    }

    public void setUserList(List<UserModel> users) {
        userModels = users;
    }

    class UserFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private StorageReference storageReference;
        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));

        public UserFragmentRecyclerViewAdapter() {
            storageReference  = FirebaseStorage.getInstance().getReference();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final UserModel user = userModels.get(position);
            CustomViewHolder customViewHolder = (CustomViewHolder) holder;
            customViewHolder.user_name.setText(user.getUsernm());
            //customViewHolder.user_msg.setText(user.getUsermsg());

            if (user.getUserphoto()==null) {
                Glide.with(getActivity()).load(R.drawable.user)
                        .apply(requestOptions)
                        .into(customViewHolder.user_photo);
            } else{
                Glide.with(getActivity())
                        .load(storageReference.child("userPhoto/"+user.getUserphoto()))
                        .apply(requestOptions)
                        .into(customViewHolder.user_photo);
            }
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView user_photo;
        public TextView user_name;
        public TextView user_msg;

        public CustomViewHolder(View view) {
            super(view);
            user_photo = view.findViewById(R.id.user_photo);
            user_name = view.findViewById(R.id.user_name);
            user_msg = view.findViewById(R.id.user_msg);
            user_msg.setVisibility(View.GONE);
        }
    }
}