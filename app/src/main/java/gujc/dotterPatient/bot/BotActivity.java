package gujc.dotterPatient.bot;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import gujc.dotterPatient.R;
import gujc.dotterPatient.fragment.BotFragment;

public class BotActivity extends AppCompatActivity {
    private BotFragment botFragment;
    private FragmentTransaction tran;
    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        botFragment = new BotFragment();
        fm = getSupportFragmentManager();

        tran = fm.beginTransaction();
        tran.replace(R.id.mainFragment, botFragment).commit();

    }


}