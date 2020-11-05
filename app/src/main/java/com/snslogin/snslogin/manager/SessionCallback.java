package com.snslogin.snslogin.manager;

import android.content.Context;
import android.util.Log;

import com.kakao.auth.ISessionCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;
import com.snslogin.snslogin.LoginSingleTon;
import com.snslogin.snslogin.listener.LoginListener;


import java.util.ArrayList;
import java.util.List;

public class SessionCallback implements ISessionCallback {
    private Context context;

    private LoginListener loginListener;
    public SessionCallback(Context context,LoginListener loginListener){
        this.context = context;
        this.loginListener=loginListener;
    }
    // 로그인에 성공한 상태
    @Override
    public void onSessionOpened() {
        requestMe();
    }

    // 로그인에 실패한 상태
    @Override
    public void onSessionOpenFailed(KakaoException exception) {
        Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
    }

    // 사용자 정보 요청
    public void requestMe() {
            List<String> key = new ArrayList<>();
            key.add("kakao_account.email");
            key.add("kakao_account.profile");
            key.add("kakao_account.age_range");
            key.add("kakao_account.birthday");
            key.add("kakao_account.gender");
        UserManagement.getInstance()
                .me(key,new MeV2ResponseCallback() {
                    @Override
                    public void onSessionClosed(ErrorResult errorResult) {
                        Log.e("KAKAO_API", "세션이 닫혀 있음: " + errorResult);
                    }

                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        Log.e("KAKAO_API", "사용자 정보 요청 실패: " + errorResult);
                    }

                    @Override
                    public void onSuccess(MeV2Response result) {
                        Log.i("KAKAO_API", "사용자 아이디: " + result.getId());
                        //oast.makeText(context,"asdfasdf",Toast.LENGTH_LONG).show();
                        loginListener.loginSuccess();
                        UserAccount kakaoAccount = result.getKakaoAccount();
                        LoginSingleTon.getInstance().setKakaoAccount(result.getKakaoAccount());
                        if (kakaoAccount != null) {

                            // 이메일
                            String email = kakaoAccount.getEmail();

                            if (email == null) {
                                Log.i("KAKAO_API", "email: " + email);

                            } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.FALSE) {
                                Log.d("카카오","이메일");
                                // 동의 요청 후 이메일 획득 가능
                                // 단, 선택 동의로 설정되어 있다면 서비스 이용 시나리오 상에서 반드시 필요한 경우에만 요청해야 합니다.

                            } else if (kakaoAccount.genderNeedsAgreement() == OptionalBoolean.FALSE){
                                Log.d("카카오","성별");
                                // 이메일 획득 불가
                            } else if (kakaoAccount.ageRangeNeedsAgreement() == OptionalBoolean.FALSE){
                                Log.d("카카오","연령대");
                            }

                            // 프로필
                            Profile profile = kakaoAccount.getProfile();

                            if (profile != null) {

                                Log.d("KAKAO_API", "nickname: " + profile.getNickname());
                                Log.d("KAKAO_API", "profile image: " + profile.getProfileImageUrl());
                                Log.d("KAKAO_API", "thumbnail image: " + profile.getThumbnailImageUrl());
                                Log.d("카카오 해쉬",kakaoAccount.hasAgeRange()+"");
                                Log.d("카카오 해쉬",kakaoAccount.hasBirthday()+"");
                                Log.d("카카오 해쉬",kakaoAccount.hasGender()+"");

                                loginListener.kakaoUserData(profile,kakaoAccount.getEmail(),kakaoAccount.getAgeRange(),
                                        kakaoAccount.getBirthday(),kakaoAccount.getGender()
                                       );

                            } else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) {
                                // 동의 요청 후 프로필 정보 획득 가능

                            } else {
                                // 프로필 획득 불가
                            }
                        }
                    }
                });
    }
}