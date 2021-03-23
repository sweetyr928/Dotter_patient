package gujc.dotterPatient.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import gujc.dotterPatient.R;
import gujc.dotterPatient.bot.BotActivity;

public class WriteFragment extends Fragment {

    private FirebaseFirestore firestore=null;
    private FirebaseUser myUid;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_write, container, false);

        firestore = FirebaseFirestore.getInstance();
        myUid = FirebaseAuth.getInstance().getCurrentUser();

        Button button = (Button) rootView.findViewById(R.id.chatbot);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BotActivity.class);
                startActivity(intent);
            }

        });

        return rootView;

    }
}
