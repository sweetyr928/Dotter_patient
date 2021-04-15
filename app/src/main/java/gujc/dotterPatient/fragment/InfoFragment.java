package gujc.dotterPatient.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;

import gujc.dotterPatient.InfoActivity;
import gujc.dotterPatient.R;
import gujc.dotterPatient.UserPWActivity;

public class InfoFragment extends Fragment {

    public InfoFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        Button infobtn = view.findViewById(R.id.toUserinfobtn);
        Button paybtn = view.findViewById(R.id.toPayinfobtn);
        Button qnabtn = view.findViewById(R.id.toQnAbtn);
        infobtn.setOnClickListener(infoBtnClickListener);
        paybtn.setOnClickListener(payBtnClickListener);
        qnabtn.setOnClickListener(qnaBtnClickListener);

        return view;
    }

    Button.OnClickListener infoBtnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            startActivity(new Intent(getActivity(), InfoActivity.class));
        }
    };

    Button.OnClickListener payBtnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            //startActivity(new Intent(getActivity(), UserPWActivity.class));
        }
    };

    Button.OnClickListener qnaBtnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            //startActivity(new Intent(getActivity(), UserPWActivity.class));
        }
    };

}
