package com.snslogin.snslogin;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.kakao.auth.AuthType;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.response.model.AgeRange;
import com.kakao.usermgmt.response.model.Gender;
import com.kakao.usermgmt.response.model.Profile;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.snslogin.snslogin.databinding.ActivityMainBinding;
import com.snslogin.snslogin.listener.LoginListener;
import com.snslogin.snslogin.manager.FLoginCallback;
import com.snslogin.snslogin.manager.SessionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements LoginListener {
    private ActivityMainBinding binding;


    private String loginPage;
    private Context context;
    private SessionCallback s; //kakao
    private CallbackManager callbackManager; //face
    private FLoginCallback fLoginCallback; //face

    private OAuthLogin oAuthLogin; //naver
    private JSONObject naverData;

    private FirebaseAuth mAuth = null;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getAppKeyHash();
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        binding.setLoginactivity(this);

        context = getApplicationContext();

        s = new SessionCallback(this, this);
        callbackManager = CallbackManager.Factory.create();
        fLoginCallback = new FLoginCallback(this, this);


        Session session;
        session = Session.getCurrentSession();
        session.addCallback(s);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)

                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.naverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginPage="naver";
                naverApi();

            }
        });
        binding.googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginPage="google";
                signIn();
                loginSuccess();
            }
        });

        binding.kakaoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "asdfsadf", Toast.LENGTH_LONG).show();
                session.open(AuthType.KAKAO_ACCOUNT, MainActivity.this);


            }
        });

        binding.faceBtn.setReadPermissions(Arrays.asList("public_profile", "email","user_gender","user_birthday"));
        binding.faceBtn.registerCallback(callbackManager, fLoginCallback);

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.userImg.setVisibility(View.GONE);
                binding.userInfo.setText("");
                binding.loginBox.setVisibility(View.VISIBLE);
                binding.btnLogout.setVisibility(View.GONE);
                UserManagement.getInstance()
                        .requestLogout(new LogoutResponseCallback() {
                            @Override
                            public void onCompleteLogout() {
                                Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                            }

                        });
                LoginManager.getInstance().logOut();
                try {
                    if (loginPage.equals("naver")) {
                        oAuthLogin.logout(context);
                    } else if (loginPage.equals("google")) {
                        mAuth.signOut();
                    } else if (loginPage.equals("")) {

                    }
                }catch (NullPointerException e){

                }

            }
        });
    }
    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("Hash key", something);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }

    @Override
    public void loginSuccess() {
        binding.loginBox.setVisibility(View.GONE);
        binding.btnLogout.setVisibility(View.VISIBLE);
    }

    @Override
    public void kakaoUserData(Profile profile, String email, AgeRange age_range, String birthday, Gender gender) {
        binding.userImg.setVisibility(View.VISIBLE);
        try {
            String st = "이름 : " + profile.getNickname() + "\n" +
                    "이메일 : " + email + "\n" +
                    "나이(연령대) : " + age_range.getValue() + "\n" +
                    "생일 : " + birthday + "\n" +
                    "성별 : " + gender.getValue() + "\n";
//            Log.d("카카오 데잍어",RankSingleTon.getInstance().getKakaoAccount().getBirthday()+"   "+RankSingleTon.getInstance().getProfile().getNickname()+"");

            Glide.with(this).load(profile.getProfileImageUrl()).placeholder(R.drawable.logo_placeholder)
                    .into(binding.userImg);


            binding.userInfo.setText(st);
        }catch (NullPointerException e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void facebookUserData(String id, String name, String email, String gender, String birthday) {
        binding.userInfo.setText("클라이언트 ID : "+id + "\n"
                +"이름 : "+ name + "\n"
                +       "이메일 : "+ email+"\n"
                +"성별 : "+gender+"\n"
                +"생일 : " +birthday);
    }

    @Override
    public void naverUserData() {

    }

    @Override
    public void googleUserData() {

    }


    public void naverApi() {
        oAuthLogin = OAuthLogin.getInstance();
        oAuthLogin.init(
                context
                , getString(R.string.naver_client_id)
                , getString(R.string.naver_client_secret)
                , getString(R.string.naver_client_name)
        );

        @SuppressLint("HandlerLeak")
        OAuthLoginHandler oAuthLoginHandler = new OAuthLoginHandler() {
            @Override
            public void run(boolean success) {
                if (success) {
                    String accessToken = oAuthLogin.getAccessToken(context);
                    String refreshToken = oAuthLogin.getRefreshToken(context);
                    long expiresAt = oAuthLogin.getExpiresAt(context);
                    String tokenType = oAuthLogin.getTokenType(context);
                    Log.d("NaverAccessToken", accessToken);


                    new Thread() {
                        public void run() {
                            try {
                                String aaa = oAuthLogin.requestApi(context, accessToken, "https://openapi.naver.com/v1/nid/me");
                                JSONObject jsonObject = new JSONObject(aaa);
                                String data = "이름 : " + jsonObject.getJSONObject("response").getString("name")
                                        + "\n이메일 : " + jsonObject.getJSONObject("response").getString("email")
                                        + "\n생일 : " + jsonObject.getJSONObject("response").getString("birthday")
                                        + "\n닉네임 : " + jsonObject.getJSONObject("response").getString("nickname")
                                        + "\n연령대 : " + jsonObject.getJSONObject("response").getString("age")
                                        + "\n성별 : " + jsonObject.getJSONObject("response").getString("gender");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.userInfo.setText(data);
                                    }
                                });
//                                        binding.incdraw.userInfo.setText(data);
//                                name = result.getJSONObject("response").getString("name");

                                Log.d("NaverProfileData", "name : " + data);

                            } catch (JSONException e) {
                                Log.d("NaverProfileData", "error : " + e.getMessage());
                            }
                        }
                    }.start();


                } else {
                    String errorCode = oAuthLogin
                            .getLastErrorCode(context).getCode();
                    String errorDesc = oAuthLogin.getLastErrorDesc(context);
                    Toast.makeText(MainActivity.this, "errorCode:" + errorCode
                            + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
                }

            }
        };
        oAuthLogin.startOauthLoginActivity(MainActivity.this, oAuthLoginHandler);
        loginSuccess();

    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handlesSignInResult(task);
        }
    }

    private void handlesSignInResult(Task<GoogleSignInAccount> completedTask){
        try{
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
        }catch (ApiException a){
            updateUI(null);
        }

    }


    private void updateUI(GoogleSignInAccount user) { //update ui code here
        if (user != null) {
            binding.userInfo.setText(
                    "이메일 : "+user.getEmail()
                            +"\n이름 : "+user.getDisplayName()

            );
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserManagement.getInstance()
                .requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                    }

                });

        LoginManager.getInstance().logOut();
        try {
            if (loginPage.equals("naver")) {
                oAuthLogin.logout(context);
            } else if (loginPage.equals("google")) {
                mAuth.signOut();
            } else if (loginPage.equals("")) {

            }
        }catch (NullPointerException e){

        }
    }

}

