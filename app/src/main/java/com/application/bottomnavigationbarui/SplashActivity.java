package com.application.bottomnavigationbarui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.application.bottomnavigationbarui.fragments.DatabaseConfigurationFragment;
import com.application.android_ui_templete1.templates.intro.TutorialAdapter;
import com.application.android_ui_templete1.templates.intro.TutorialData;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {

    String url1 = "https://raw.githubusercontent.com/DevFrogora/SubmeterBilling3LayerProject/refs/heads/main/Hosting_Images/guide_01.png";
    String url2 = "https://raw.githubusercontent.com/DevFrogora/SubmeterBilling3LayerProject/refs/heads/main/Hosting_Images/guide_02.png";
    String url3 = "https://raw.githubusercontent.com/DevFrogora/SubmeterBilling3LayerProject/refs/heads/main/Hosting_Images/guide_03.png";
    String url4 = "https://raw.githubusercontent.com/DevFrogora/SubmeterBilling3LayerProject/refs/heads/main/Hosting_Images/guide_04.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }


        String prefName = DatabaseConfigurationFragment.PUBLIC_PREFS;
        boolean isGuideSeen = getBooleanFromSharedPreferences(prefName, "isGuideSeen");
//        isGuideSeen = true;
        // 2. State Verification and Routing Logic Execution
        Intent targetIntent;
        if (isGuideSeen) {
            // Force Setup: If MPIN doesn't exist, launch MainActivity where the Setup fragment loads
            targetIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(targetIntent);
            finish();
        }

        // we are creating array list for storing our image urls.
        ArrayList<TutorialData> tutorialDataArrayList = new ArrayList<>();

        // initializing the slider view.
        SliderView sliderView = findViewById(R.id.slider);

        // adding the urls inside array list
        tutorialDataArrayList.add(new TutorialData(url1));
        tutorialDataArrayList.add(new TutorialData(url2));
        tutorialDataArrayList.add(new TutorialData(url3));
        tutorialDataArrayList.add(new TutorialData(url4));

        // passing this array list inside our adapter class.
        TutorialAdapter adapter = new TutorialAdapter(this, tutorialDataArrayList);

        // below method is used to set auto cycle direction in left to
        // right direction you can change according to requirement.
        sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);

        // below method is used to
        // setadapter to sliderview.
        sliderView.setSliderAdapter(adapter);

        // below method is use to set
        // scroll time in seconds.
        sliderView.setScrollTimeInSec(3);

        // to set it scrollable automatically
        // we use below method.
        sliderView.setAutoCycle(true);

        // to start autocycle below method is used.
        sliderView.startAutoCycle();
        // 3. Launch next context and evict Splash from memory stack


        findViewById(R.id.btnFinish).setOnClickListener(view -> {
            setBooleanInSharedPreferences(prefName, "isGuideSeen", true);
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean getBooleanFromSharedPreferences(String prefName, String keyName) {
        SharedPreferences sharedPreferences = getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(keyName, false);
    }

    private void setBooleanInSharedPreferences(String prefName, String keyName, boolean value){
        SharedPreferences prefs = getSharedPreferences(prefName, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(keyName, value)
                .apply();
    }
}