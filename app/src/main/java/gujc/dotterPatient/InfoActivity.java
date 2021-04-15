package gujc.dotterPatient;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import gujc.dotterPatient.fragment.InfoFragment;
import gujc.dotterPatient.fragment.UserFragment;

public class InfoActivity extends AppCompatActivity {

    private UserFragment userFragment;
    private FragmentTransaction tran;
    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        userFragment = new UserFragment();
        fm = getSupportFragmentManager();

        tran = fm.beginTransaction();
        tran.replace(R.id.mainFragment, userFragment).commit();

    }
}
