package gujc.dotterPatient.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import gujc.dotterPatient.R;
import gujc.dotterPatient.common.Util9;
import gujc.dotterPatient.model.ChatModel;
import gujc.dotterPatient.model.ChatRoomModel;
import gujc.dotterPatient.model.Message;
import gujc.dotterPatient.model.NotificationModel;
import gujc.dotterPatient.model.UserModel;
import gujc.dotterPatient.photoview.ViewPagerActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

public class ChatFragment extends Fragment {
    //브랜치 테스트1
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_FILE = 2;
    private static String rootPath = Util9.getRootPath() + "/DirectTalk9/";

    private Button sendBtn;
    private EditText msg_input;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateFormatHour = new SimpleDateFormat("aa hh:mm");
    private String roomID;
    private String myUid;
    private String toUid;
    private String toTitle;
    private String toPhone;
    private Map<String, UserModel> userList = new HashMap<>();

    private ListenerRegistration listenerRegistration;
    private FirebaseFirestore firestore=null;
    private StorageReference storageReference;
    private LinearLayoutManager linearLayoutManager;

    private ProgressDialog progressDialog = null;
    private Integer userCount = 0;
    private String broomid = null;

    public ChatFragment() {
    }

    public static final ChatFragment getInstance(String toUid, String roomID, String toTitle,String toPhone) {
        ChatFragment f = new ChatFragment();
        Bundle bdl = new Bundle();
        bdl.putString("toUid", toUid);
        bdl.putString("roomID", roomID);
        bdl.putString("toTitle", toTitle);
        bdl.putString("toPhone",toPhone);
        f.setArguments(bdl);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        msg_input = view.findViewById(R.id.msg_input);
        sendBtn = view.findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(sendBtnClickListener);

        view.findViewById(R.id.imageBtn).setOnClickListener(imageBtnClickListener);
        view.findViewById(R.id.fileBtn).setOnClickListener(fileBtnClickListener);
        view.findViewById(R.id.msg_input).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    Util9.hideKeyboard(getActivity());
                }
            }
        });

        if (getArguments() != null) {
            roomID = getArguments().getString("roomID");
            toUid = getArguments().getString("toUid");
            toTitle = getArguments().getString("toTitle");
            toPhone = getArguments().getString("toPhone");
        }

        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        dateFormatDay.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        dateFormatHour.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /*
         two user: roomid or uid talking
         multi user: roomid
         */
        if (!"".equals(roomID) && roomID != null) { // existing room (multi user)
            setChatRoom(roomID);
        } else if (!"".equals(toUid) && toUid != null) {                     // find existing room for two user
            findChatRoom(toUid);
        }

        if (roomID == null) {                                                     // new room for two user
            getUserInfoFromServer(myUid);
            getUserInfoFromServer(toUid);
            userCount = 2;
        }

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (mAdapter != null & bottom < oldBottom) {
                    final int lastAdapterItem = mAdapter.getItemCount() - 1;
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            int recyclerViewPositionOffset = -1000000;
                            View bottomView = linearLayoutManager.findViewByPosition(lastAdapterItem);
                            if (bottomView != null) {
                                recyclerViewPositionOffset = 0 - bottomView.getHeight();
                            }
                            linearLayoutManager.scrollToPositionWithOffset(lastAdapterItem, recyclerViewPositionOffset);
                        }
                    });
                }
            }
        });

        final DocumentReference rooms = firestore.collection("rooms").document(roomID);
        rooms.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    documentSnapshot.getData();
                } else {
                    CreateChattingRoom(rooms);
                    setChatRoom(roomID);
                }
            }
        });

        //문진요약
        TextView btitle = view.findViewById(R.id.btitle);
        final TextView bresult = view.findViewById(R.id.bresult);
        if (toTitle== null) {
            rooms.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    ChatRoomModel board = documentSnapshot.toObject(ChatRoomModel.class);
                    String board2 = board.getBoard();
                    bresult.setText(board2);

                }
            });
        } else {
            bresult.setText(toTitle);
        }

        bresult.bringToFront();
        btitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bresult.getVisibility() == View.GONE){
                    bresult.setVisibility(View.VISIBLE);
                }else{
                    bresult.setVisibility(View.GONE);
                }
            }
        });

        return view;
    }

    void phoneRequest()
    {
        final DocumentReference rooms = firestore.collection("rooms").document(roomID);
        rooms.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable final DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                ChatRoomModel chatRoomModel = snapshot.toObject(ChatRoomModel.class);
                int request = chatRoomModel.getIdentification();

                if(request==2)
                {
                    AlertDialog.Builder oDialog = new AlertDialog.Builder(getContext(),
                            android.R.style.Theme_DeviceDefault_Light_Dialog);

                    oDialog.setMessage("전화를 통해 본인확인을 요청하였습니다. 수락하시겠습니까?")
                            .setTitle("      개인정보 요청알림")
                            .setPositiveButton("아니오", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Log.i("Dialog", "취소");
                                    snapshot.getReference().update("request",4);
                                }
                            })
                            .setNeutralButton("예", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {

                                    snapshot.getReference().update("request",3);
                                }
                            })
                            .setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.
                            .show();
                }

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    // get a user info
    void getUserInfoFromServer(String id) {
        firestore.collection("users").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserModel userModel = documentSnapshot.toObject(UserModel.class);
                userList.put(userModel.getUid(), userModel);
                if (roomID != null & userCount == userList.size()) {
                    mAdapter = new RecyclerViewAdapter();
                    recyclerView.setAdapter(mAdapter);
                }
            }
        });
    }

    // Returns the room ID after locating the chatting room with the user ID.
    void findChatRoom(final String toUid) {
        firestore.collection("rooms").whereGreaterThanOrEqualTo("users." + myUid, 0).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Long> users = (Map<String, Long>) document.get("users");
                            if (users.size() == 2 & users.get(toUid) != null) {
                                setChatRoom(document.getId());
                                break;
                            }
                        }
                    }
                });
    }

    // get user list in a chatting room
    void setChatRoom(String rid) {
        roomID = rid;
        firestore.collection("rooms").document(roomID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    return;
                }
                DocumentSnapshot document = task.getResult();
                Map<String, Long> users = (Map<String, Long>) document.get("users");
                if (users == null) {
                    getUserInfoFromServer(myUid);
                    getUserInfoFromServer(toUid);
                    userCount = 2;
                } else {
                    for (String key : users.keySet()) {
                        getUserInfoFromServer(key);
                    }
                    userCount = users.size();
                }
                //users.put(myUid, (long) 0);
                //document.getReference().update("users", users);

            }
        });
    }

    void setUnread2Read() {
        if (roomID == null) return;

        firestore.collection("rooms").document(roomID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    return;
                }
                DocumentSnapshot document = task.getResult();
                Map<String, Long> users = (Map<String, Long>) document.get("users");

                users.put(myUid, (long) 0);
                document.getReference().update("users", users);
            }
        });
    }

    public void CreateChattingRoom(final DocumentReference room) {
        Map<String, Integer> users = new HashMap<>();
        String title = "";
        users.put(myUid, 0);
        users.put(toUid, 0);

        Map<String, Object> data = new HashMap<>();
        data.put("title", null);
        data.put("users", users);
        data.put("board", toTitle);
        data.put("phone",toPhone);
        data.put("request",1);

        room.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mAdapter = new RecyclerViewAdapter();
                    recyclerView.setAdapter(mAdapter);
                }
            }
        });
    }

    public Map<String, UserModel> getUserList() {
        return userList;
    }

    Button.OnClickListener sendBtnClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            String msg = msg_input.getText().toString();
            sendMessage(msg, "0", null);
            sendGCM();
            msg_input.setText("");
        }
    };

    private void sendMessage(final String msg, String msgtype, final ChatModel.FileInfo fileinfo) {
        sendBtn.setEnabled(false);

        if (roomID == null) {             // create chatting room for two user
            roomID = firestore.collection("rooms").document().getId();
            CreateChattingRoom(firestore.collection("rooms").document(roomID));
        }

        final Map<String, Object> messages = new HashMap<>();
        messages.put("uid", myUid);
        messages.put("msg", msg);
        messages.put("msgtype", msgtype);
        messages.put("timestamp", FieldValue.serverTimestamp());
        if (fileinfo != null) {
            messages.put("filename", fileinfo.filename);
            messages.put("filesize", fileinfo.filesize);
        }

        final DocumentReference docRef = firestore.collection("rooms").document(roomID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    return;
                }

                WriteBatch batch = firestore.batch();

                // save last message
                batch.set(docRef, messages, SetOptions.merge());

                // save message
                List<String> readUsers = new ArrayList();
                readUsers.add(myUid);
                messages.put("readUsers", readUsers);//new String[]{myUid} );
                batch.set(docRef.collection("messages").document(), messages);

                // inc unread message count
                DocumentSnapshot document = task.getResult();
                Map<String, Long> users = (Map<String, Long>) document.get("users");

                for (String key : users.keySet()) {
                    if (!myUid.equals(key)) users.put(key, users.get(key) + 1);
                }
                document.getReference().update("users", users);

                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            sendBtn.setEnabled(true);
                        }
                    }
                });
            }

        });
    }

    //알림보내는 함수
    void sendGCM() {
        Gson gson = new Gson();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.notification.title = userList.get(myUid).getUsernm();
        notificationModel.notification.body = msg_input.getText().toString();
        notificationModel.data.title = userList.get(myUid).getUsernm();
        notificationModel.data.body = msg_input.getText().toString();

        for (Map.Entry<String, UserModel> elem : userList.entrySet()) {
            if (myUid.equals(elem.getValue().getUid())) continue;
            notificationModel.to = elem.getValue().getToken();
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel));
            Request request = new Request.Builder()
                    .header("Content-Type", "application/json")
                    .addHeader("Authorization", "key=AAAA_wzV2o8:APA91bG-2kSNeNC18kd4AoB04Vdl1rPNSYiTg0aCnly8h9vOobXPPRd5XU0HC2-Nuxv2NEBmCj-2WeDv7HnEXxDkMY35kBosExObC4uGWTifDZ-qepLpEYXJZ7iH8rxhQKqE2-CKVLq6")
                    .url("https://fcm.googleapis.com/fcm/send")
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                }
            });
        }
    }

    // choose image
    Button.OnClickListener imageBtnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, PICK_FROM_ALBUM);
        }
    };

    // choose file
    Button.OnClickListener fileBtnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FROM_FILE);
        }
    };

    // uploading image / file
    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        Uri fileUri = data.getData();
        final String filename = Util9.getUniqueValue();

        showProgressDialog("Uploading selected File.");
        final ChatModel.FileInfo fileinfo = getFileDetailFromUri(getContext(), fileUri);

        storageReference.child("files/" + filename).putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                sendMessage(filename, Integer.toString(requestCode), fileinfo);
                hideProgressDialog();
            }
        });
        if (requestCode != PICK_FROM_ALBUM) {
            return;
        }

        // small image
        Glide.with(getContext())
                .asBitmap()
                .load(fileUri)
                .apply(new RequestOptions().override(150, 150))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();
                        storageReference.child("filesmall/" + filename).putBytes(data);
                    }
                });
    }

    // get file name and size from Uri
    public static ChatModel.FileInfo getFileDetailFromUri(final Context context, final Uri uri) {
        if (uri == null) {
            return null;
        }

        ChatModel.FileInfo fileDetail = new ChatModel.FileInfo();
        // File Scheme.
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            File file = new File(uri.getPath());
            fileDetail.filename = file.getName();
            fileDetail.filesize = Util9.size2String(file.length());
        }
        // Content Scheme.
        else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            Cursor returnCursor =
                    context.getContentResolver().query(uri, null, null, null, null);
            if (returnCursor != null && returnCursor.moveToFirst()) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                fileDetail.filename = returnCursor.getString(nameIndex);
                fileDetail.filesize = Util9.size2String(returnCursor.getLong(sizeIndex));
                returnCursor.close();
            }
        }

        return fileDetail;
    }

    public void showProgressDialog(String title ) {
        if (progressDialog==null) {
            progressDialog = new ProgressDialog(getContext());
        }
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(title);
        progressDialog.setMessage("Please wait..");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    public void setProgressDialog(int value) {
        progressDialog.setProgress(value);
    }
    public void hideProgressDialog() {
        progressDialog.dismiss();
    }
    // =======================================================================================

    //리사이클뷰 어댑터
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));

        List<Message> messageList;
        String beforeDay = null;
        MessageViewHolder beforeViewHolder;

        RecyclerViewAdapter() {
            File dir = new File(rootPath);
            if (!dir.exists()) {
                if (!Util9.isPermissionGranted(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    return;
                }
                dir.mkdirs();
            }

            messageList = new ArrayList<Message>();
            setUnread2Read();
            startListening();
        }

        //채팅내용가져오기
        public void startListening() {
            beforeDay = null;
            messageList.clear();

            CollectionReference roomRef = firestore.collection("rooms").document(roomID).collection("messages");
            // my chatting room information
            listenerRegistration = roomRef.orderBy("timestamp").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {return;}

                    Message message;
                    for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
                        switch (change.getType()) {
                            case ADDED:
                                message = change.getDocument().toObject(Message.class);
                                //if (message.msg !=null & message.timestamp == null) {continue;} // FieldValue.serverTimestamp is so late

                                if (message.getReadUsers().indexOf(myUid) == -1) {
                                    message.getReadUsers().add(myUid);
                                    change.getDocument().getReference().update("readUsers", message.getReadUsers());
                                }
                                messageList.add(message);
                                notifyItemInserted(change.getNewIndex());
                                break;
                            case MODIFIED:
                                message = change.getDocument().toObject(Message.class);
                                messageList.set(change.getOldIndex(), message);
                                notifyItemChanged(change.getOldIndex());
                                break;
                            case REMOVED:
                                messageList.remove(change.getOldIndex());
                                notifyItemRemoved(change.getOldIndex());
                                break;
                        }
                    }
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            });
        }

        public void stopListening() {
            if (listenerRegistration != null) {
                listenerRegistration.remove();
                listenerRegistration = null;
            }

            messageList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            Message message = messageList.get(position);
            if (myUid.equals(message.getUid()) ) {
                switch(message.getMsgtype()){
                    case "1": return R.layout.item_chatimage_right;
                    case "2": return R.layout.item_chatfile_right;
                    default:  return R.layout.item_chatmsg_right;
                }
            } else {
                switch(message.getMsgtype()){
                    case "1": return R.layout.item_chatimage_left;
                    case "2": return R.layout.item_chatfile_left;
                    default:  return R.layout.item_chatmsg_left;
                }
            }
        }

        //자신의 정보
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
            final Message message = messageList.get(position);

            setReadCounter(message, messageViewHolder.read_counter);

            if ("0".equals(message.getMsgtype())) {                                      // text message
                messageViewHolder.msg_item.setText(message.getMsg());
            } else
            if ("2".equals(message.getMsgtype())) {                                      // file transfer
                messageViewHolder.msg_item.setText(message.getFilename() + "\n" + message.getFilesize());
                messageViewHolder.filename = message.getFilename();
                messageViewHolder.realname = message.getMsg();
                File file = new File(rootPath + message.getFilename());
                if(file.exists()) {
                    messageViewHolder.button_item.setText("Open File");
                } else {
                    messageViewHolder.button_item.setText("Download");
                }
            } else {                                                                // image transfer
                messageViewHolder.realname = message.getMsg();
                Glide.with(getContext())
                        .load(storageReference.child("filesmall/"+message.getMsg()))
                        .apply(new RequestOptions().override(1000, 1000))
                        .into(messageViewHolder.img_item);
            }

            if (! myUid.equals(message.getUid())) {
                UserModel userModel = userList.get(message.getUid());
                messageViewHolder.msg_name.setText(userModel.getUsernm());

                if (userModel.getUserphoto()==null) {
                    Glide.with(getContext()).load(R.drawable.user)
                            .apply(requestOptions)
                            .into(messageViewHolder.user_photo);
                } else{
                    Glide.with(getContext())
                            .load(storageReference.child("userPhoto/"+userModel.getUserphoto()))
                            .apply(requestOptions)
                            .into(messageViewHolder.user_photo);
                }
            }
            messageViewHolder.divider.setVisibility(View.INVISIBLE);
            messageViewHolder.divider.getLayoutParams().height = 0;
            messageViewHolder.timestamp.setText("");
            if (message.getTimestamp()==null) {return;}

            String day = dateFormatDay.format( message.getTimestamp());
            String timestamp = dateFormatHour.format( message.getTimestamp());
            messageViewHolder.timestamp.setText(timestamp);

            if (position==0) {
                messageViewHolder.divider_date.setText(day);
                messageViewHolder.divider.setVisibility(View.VISIBLE);
                messageViewHolder.divider.getLayoutParams().height = 60;
            } else {
                Message beforeMsg = messageList.get(position - 1);
                String beforeDay = dateFormatDay.format( beforeMsg.getTimestamp() );

                if (!day.equals(beforeDay) && beforeDay != null) {
                    messageViewHolder.divider_date.setText(day);
                    messageViewHolder.divider.setVisibility(View.VISIBLE);
                    messageViewHolder.divider.getLayoutParams().height = 60;
                }
            }
            /*messageViewHolder.timestamp.setText("");
            if (message.getTimestamp()==null) {return;}

            String day = dateFormatDay.format( message.getTimestamp());
            String timestamp = dateFormatHour.format( message.getTimestamp());

            messageViewHolder.timestamp.setText(timestamp);

            if (position==0) {
                messageViewHolder.divider_date.setText(day);
                messageViewHolder.divider.setVisibility(View.VISIBLE);
                messageViewHolder.divider.getLayoutParams().height = 60;
            };
            if (!day.equals(beforeDay) && beforeDay!=null) {
                beforeViewHolder.divider_date.setText(beforeDay);
                beforeViewHolder.divider.setVisibility(View.VISIBLE);
                beforeViewHolder.divider.getLayoutParams().height = 60;
            }
            beforeViewHolder = messageViewHolder;
            beforeDay = day;*/
        }

        void setReadCounter (Message message, final TextView textView) {
            int cnt = userCount - message.getReadUsers().size();
            if (cnt > 0) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(String.valueOf(cnt));
            } else {
                textView.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }


    }

    //메시지 xml연결
    private class MessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView user_photo;
        public TextView msg_item;
        public ImageView img_item;          // only item_chatimage_
        public TextView msg_name;
        public TextView timestamp;
        public TextView read_counter;
        public LinearLayout divider;
        public TextView divider_date;
        public TextView button_item;            // only item_chatfile_
        public LinearLayout msgLine_item;       // only item_chatfile_
        public String filename;
        public String realname;

        public MessageViewHolder(View view) {
            super(view);
            user_photo = view.findViewById(R.id.user_photo);
            msg_item = view.findViewById(R.id.msg_item);
            img_item = view.findViewById(R.id.img_item);
            timestamp = view.findViewById(R.id.timestamp);
            msg_name = view.findViewById(R.id.msg_name);
            read_counter = view.findViewById(R.id.read_counter);
            divider = view.findViewById(R.id.divider);
            divider_date = view.findViewById(R.id.divider_date);
            button_item = view.findViewById(R.id.button_item);
            msgLine_item = view.findViewById(R.id.msgLine_item);        // for file
            if (msgLine_item!=null) {
                msgLine_item.setOnClickListener(downloadClickListener);
            }
            if (img_item!=null) {                                       // for image
                img_item.setOnClickListener(imageClickListener);
            }
        }
        // file download and open
        Button.OnClickListener downloadClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                if ("Download".equals(button_item.getText())) {
                    download();
                } else {
                    openWith();
                }
            }
            public void download() {
                if (!Util9.isPermissionGranted(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    return ;
                }
                showProgressDialog("Downloading File.");

                final File localFile = new File(rootPath, filename);

                storageReference.child("files/"+realname).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        button_item.setText("Open File");
                        hideProgressDialog();
                        Log.e("DirectTalk9 ","local file created " +localFile.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("DirectTalk9 ","local file not created  " +exception.toString());
                    }
                });
            }

            public void openWith() {
                File newFile = new File(rootPath + filename);
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String ext = newFile.getName().substring(newFile.getName().lastIndexOf(".") + 1);
                String type = mime.getMimeTypeFromExtension(ext);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(getContext(), getActivity().getPackageName() + ".provider", newFile);

                    List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        getActivity().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }else {
                    uri = Uri.fromFile(newFile);
                }
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, type);//"application/vnd.android.package-archive");
                startActivity(Intent.createChooser(intent, "Your title"));
            }
        };
        // photo view
        Button.OnClickListener imageClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ViewPagerActivity.class);
                intent.putExtra("roomID", roomID);
                intent.putExtra("realname", realname);
                startActivity(intent);
            }
        };
    }

    public void backPressed() {
    }
}
