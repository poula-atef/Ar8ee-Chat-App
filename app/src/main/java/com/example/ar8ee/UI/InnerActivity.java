package com.example.ar8ee.UI;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ar8ee.Classes.ChatClass;
import com.example.ar8ee.Classes.UserClass;
import com.example.ar8ee.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.tabs.TabLayout;

import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.ar8ee.fragments.Chats;
import com.example.ar8ee.fragments.ProfileFragment;
import com.example.ar8ee.fragments.Users;
import de.hdodenhof.circleimageview.CircleImageView;
import com.example.ar8ee.fragments.postsFragment;

public class InnerActivity extends AppCompatActivity {
    public static boolean where = false;

    CircleImageView image;
    TextView username;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inner);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        setComponents();
        refreshComponents();
        setToolBar();
        setCurrentUserNameAndImage();
        setFrontFragments();

    }

    private void setComponents() {
        image = (CircleImageView) findViewById(R.id.profile_image);
        username = (TextView)findViewById(R.id.uname);
    }

    private void setFrontFragments() {
        final TabLayout table = (TabLayout)findViewById(R.id.tab_layout);
        final ViewPager viewPager = (ViewPager)findViewById(R.id.view_pager);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ViewPagerAdapter viewAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                int counter=0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    ChatClass cc = data.getValue(ChatClass.class);
                    if(cc.getRecever().equals(firebaseUser.getUid()) && cc.getSeen().equals("false")){
                        counter++;
                    }
                }
                if(counter == 0){
                    viewAdapter.addFragment(new Chats(),"Chats");
                }
                else
                    viewAdapter.addFragment(new Chats(),"(" + counter +") Chats");

                viewAdapter.addFragment(new Users(),"Users");
                viewAdapter.addFragment(new ProfileFragment(),"Profile");
                viewAdapter.addFragment(new postsFragment(),"Ra8ee");
                viewAdapter.addFragment(new postsFragment("myPosts"),"My Ra8ee");


                viewPager.setAdapter(viewAdapter);

                table.setupWithViewPager(viewPager);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setCurrentUserNameAndImage() {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserClass userClass = dataSnapshot.getValue(UserClass.class);
                username.setText(userClass.getUsername());
                if(userClass.getImageURL().equals("default")){
                    image.setImageResource(R.mipmap.ic_launcher_round);
                }
                else{
                    Glide.with(getApplicationContext()).load(userClass.getImageURL()).into(image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.inner_toolBar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.bodyColorLight));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            where = true;
            setStatus("offline");
            startActivity(new Intent(InnerActivity.this,MainActivity.class));
            finish();
            return true;
        }
        return false;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<Fragment>fragments;
        private ArrayList<String>titles;

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment,String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void status(String status){
        if(firebaseUser != null){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("status");
        reference.setValue(status);
        }
    }

    private void setStatus(final String status){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("status");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    final HashMap<Object,Object> stat = (HashMap<Object, Object>) snapshot.getValue();
                    for(Object obj:stat.keySet()){
                        String str = obj.toString();
                        ref.child(str).child(firebaseUser.getUid()).setValue(status);
                    }
                }
                if(dataSnapshot.getValue() == null){
                    ref.child(firebaseUser.getUid())
                            .child(firebaseUser.getUid())
                            .child(firebaseUser.getUid())
                            .setValue(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        setStatus("ready");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
        setStatus("offline");
    }

    public void refreshComponents(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean dark = sharedPreferences.getBoolean("mode_key",false);
        if(dark){
        findViewById(R.id.view_pager).setBackgroundColor(getResources().getColor(R.color.bodyColorDark));
        findViewById(R.id.tab_layout).setBackgroundColor(getResources().getColor(R.color.barColorDark));
        findViewById(R.id.inner_toolBar).setBackgroundColor(getResources().getColor(R.color.barColorDark));
        ((TextView)findViewById(R.id.app_logo)).setTextColor(getResources().getColor(R.color.bodyColorLight));
        }
        else{
            findViewById(R.id.view_pager).setBackgroundColor(getResources().getColor(R.color.bodyColorLight));
            findViewById(R.id.tab_layout).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            findViewById(R.id.inner_toolBar).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            ((TextView)findViewById(R.id.app_logo)).setTextColor(getResources().getColor(R.color.ChatBackgroundColor));
        }
    }
}
