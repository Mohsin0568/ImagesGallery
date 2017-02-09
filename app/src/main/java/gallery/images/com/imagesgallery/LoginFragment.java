package gallery.images.com.imagesgallery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.File;
import java.util.ArrayList;

import gallery.images.com.imagesgallery.util.AsyncTask;
import gallery.images.com.imagesgallery.util.SharedPrefUtil;
import gallery.images.com.imagesgallery.util.contentResolver.ImagePathProvider;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class LoginFragment extends Fragment {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private InterstitialAd interstitialAd ;

    //private TextView signinText, skipSignIn ;
    private SignInButton signIn ;
    private Context context ;
    private ActionBar actionBar ;
    private int afterLogin ;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    CallbackManager callbackManager ;
    LoginButton facebookLogin ;

    ArrayList<String> allImagespath = new ArrayList<String>();

    private onAdLoadedListener adLoadedListener;


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        actionBar = ((MainActivity)context).getSupportActionBar();
        actionBar.setTitle("Sign In");
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        //skipSignIn = (TextView) v.findViewById(R.id.skip_signin_text);
        signIn = (SignInButton) v.findViewById(R.id.google_signin_button);
        facebookLogin = (LoginButton) v.findViewById(R.id.facebook_signin_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build();

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(context, "SignIn failed due to Connectivity", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    loadAd();
                    processScreens();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };


        callbackManager = CallbackManager.Factory.create();
        facebookLogin.setReadPermissions("email","public_profile");
        facebookLogin.setFragment(this);
        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook login result " + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
        return v ;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                System.out.println("Sign in success");
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(context, "SignIn failed", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        //showProgressDialog();
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Signing in");
        pd.setCancelable(false);
        pd.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        pd.cancel();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        this.context = context ;
        try{
            adLoadedListener = (onAdLoadedListener) context ;
        }
        catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnAdLoadedListener");
        }
    }

    public void loadAd(){
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(getString(R.string.banner_ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("FDC0ADAFE56D618FA094310491FBDC3C").build();
        interstitialAd.loadAd(adRequest);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
        adLoadedListener.setInterstitialAd(interstitialAd);
    }

    public void processScreens(){
        getAllImgagesPath();
        boolean imagesAvail = false;
        if (allImagespath.size() > 0) {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            GridViewImages gridViewImages = new GridViewImages();
            transaction.replace(R.id.fragment_body, gridViewImages);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            NoImagesFragment noImagesFragment = new NoImagesFragment();
            transaction.replace(R.id.fragment_body, noImagesFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public void getAllImgagesPath() {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();
        Cursor c = getActivity().managedQuery(ImagePathProvider.CONTENT_URI, null, null, null, "PATH");
        if (c.moveToFirst()) {
            do {
                String names[] = c.getColumnNames();
                String path = c.getString(c.getColumnIndex("path"));
                File f = new File(path);
                if (f.exists())
                    allImagespath.add(path);
            } while (c.moveToNext());
        }
        pd.cancel();
    }

    public interface onAdLoadedListener{
        public void setInterstitialAd(InterstitialAd interstitialAd);
    }
}
