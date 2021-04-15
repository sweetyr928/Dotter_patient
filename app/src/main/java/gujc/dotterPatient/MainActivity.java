package gujc.dotterPatient;

import android.content.Intent;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rahimlis.badgedtablayout.BadgedTabLayout;

import java.util.HashMap;
import java.util.Map;

import gujc.dotterPatient.fragment.BoardFragment;
import gujc.dotterPatient.fragment.ChartFragment;
import gujc.dotterPatient.fragment.ChatRoomFragment;
import gujc.dotterPatient.fragment.InfoFragment;
import gujc.dotterPatient.fragment.UserFragment;
import gujc.dotterPatient.fragment.WriteFragment;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private FirebaseFirestore firestore=null;
    private ListenerRegistration listenerRegistration;
    private ViewPager mViewPager;
    private BadgedTabLayout tabLayout;
    int counter = 0;
    private String myUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.setIcon(0, R.drawable.account);
        tabLayout.setIcon(1, R.drawable.baseline_chat_black_18dp);
        tabLayout.setIcon(2, R.drawable.ic_floatingbutton_chart);
        tabLayout.setIcon(3,R.drawable.setting);

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore = FirebaseFirestore.getInstance();

        if (counter > 0)
        {
            tabLayout.setBadgeText(1, String.valueOf(counter));
        }
        else
        {
            tabLayout.setBadgeText(1, null);
        }


        sendRegistrationToServer();

    }

    void sendRegistrationToServer() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        FirebaseFirestore.getInstance().collection("users").document(uid).set(map, SetOptions.merge());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
            this.finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new WriteFragment();
                case 1: return new ChatRoomFragment();
                case 2: return new ChartFragment();
                default: return new InfoFragment();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}