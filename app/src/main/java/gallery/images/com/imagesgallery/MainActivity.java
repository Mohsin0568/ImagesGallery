package gallery.images.com.imagesgallery;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends AppCompatActivity implements LoginFragment.onAdLoadedListener {

    private InterstitialAd interstitialAd ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        LoginFragment loginFragment = new LoginFragment();
        transaction.add(R.id.fragment_body, loginFragment);
        transaction.commit();

    }

    public void setInterstitialAd(InterstitialAd interstitialAd){
        this.interstitialAd = interstitialAd ;
    }

    @Override
    public void onBackPressed(){
        Log.d("Back Pressed", "Back Button Pressed");
        if(interstitialAd != null && interstitialAd.isLoaded()){
            Log.d("MAD", "Yes interstital Ad is loaded");
            interstitialAd.show();
        }
        else{
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }
}
