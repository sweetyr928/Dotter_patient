package gujc.dotterPatient.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import gujc.dotterPatient.ChartinfoActivity;
import gujc.dotterPatient.R;
import gujc.dotterPatient.bot.BotAdapter;
import gujc.dotterPatient.common.FirestoreAdapter;
import gujc.dotterPatient.model.Board;
import gujc.dotterPatient.model.UserModel;

public class ChartFragment extends Fragment {
    private Board chart;
    private String myuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String dname;
    private String timestamp1;
    private LinearLayoutManager manager;
    private RecyclerView recyclerView;
    private FirestoreAdapter firestoreAdapter;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    public ChartFragment() {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(inflater.getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(false);
        recyclerView.setLayoutManager(manager);
        firestoreAdapter = new Adapter(FirebaseFirestore.getInstance()
                .collection("Board").whereEqualTo("match", true).whereEqualTo("id", myuid).orderBy("timestamp"));
        recyclerView.setAdapter(firestoreAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(getContext()));
        recyclerView.smoothScrollToPosition(0);

        return view;
    }
    public class LinearLayoutManagerWithSmoothScroller extends LinearLayoutManager {

        public LinearLayoutManagerWithSmoothScroller(Context context) {
            super(context, VERTICAL, false);
        }

        public LinearLayoutManagerWithSmoothScroller(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                           int position) {
            RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(recyclerView.getContext());
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }

        private class TopSnappedSmoothScroller extends LinearSmoothScroller {
            public TopSnappedSmoothScroller(Context context) {
                super(context);

            }

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return LinearLayoutManagerWithSmoothScroller.this
                        .computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;
            }
        }
    }

    class Adapter extends FirestoreAdapter<Holder> {
        private StorageReference storageReference;
        ArrayList<Board> board;

        Adapter(Query query) {
            super(query);
            storageReference = FirebaseStorage.getInstance().getReference();
        }

        @Override
        public ChartFragment.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ChartFragment.Holder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chart, parent, false));
        }

        @Override
        public void onBindViewHolder(ChartFragment.Holder holder, int position) {
            final DocumentSnapshot documentSnapshot = getSnapshot(position);
            final Board board = documentSnapshot.toObject(Board.class);

            holder.name.setText(board.getDoctor()+" 선생님");
            timestamp1 = simpleDateFormat.format(board.getTimestamp());
            holder.timestamp.setText(timestamp1);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getView().getContext(), ChartinfoActivity.class);
                    intent.putExtra("doctor", board.getDoctor());
                    intent.putExtra("hospital", board.getHospital());
                    intent.putExtra("timestamp", timestamp1);
                    intent.putExtra("board", board.getTitle());
                    intent.putExtra("phone", board.getName());

                    startActivity(intent);
                }
            });

        }

    }

    private class Holder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView timestamp;

        public Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }
}
