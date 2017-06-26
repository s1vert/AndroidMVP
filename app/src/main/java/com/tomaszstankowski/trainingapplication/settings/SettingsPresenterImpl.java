package com.tomaszstankowski.trainingapplication.settings;


import android.content.Intent;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.tomaszstankowski.trainingapplication.login.LoginActivity;
import com.tomaszstankowski.trainingapplication.model.User;

import javax.inject.Inject;

public class SettingsPresenterImpl implements SettingsPresenter,
        SettingsInteractor.OnUserProfileUpdateListener {
    private SettingsView mView;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private SettingsInteractor mInteractor;

    @Inject
    public SettingsPresenterImpl(SettingsInteractor interactor) {
        mInteractor = interactor;
    }

    @Override
    public void onCreateView(SettingsView view) {
        mView = view;
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null && firebaseUser.getDisplayName() != null) {
            mView.update(firebaseUser.getDisplayName());
        }
    }

    @Override
    public void onDestroyView() {
        mView = null;
    }

    @Override
    public void onLogOutButtonClicked() {
        AuthUI.getInstance()
                .signOut(mView.getActivity())
                .addOnCompleteListener(task -> {
                    if (mView != null) {
                        Intent intent = new Intent(mView.getActivity(), LoginActivity.class);
                        intent.putExtra(LoginActivity.REQUEST_CODE, LoginActivity.REQUEST_CODE_LOGGED_OUT);
                        mView.getActivity().startActivityForResult(
                                intent,
                                LoginActivity.REQUEST_CODE_LOGGED_OUT);
                    }
                });
    }

    @Override
    public void onSaveButtonClicked(String username) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null && firebaseUser.getDisplayName() != null) {
            if (firebaseUser.getDisplayName().equals(username)) {
                mView.showMessage(SettingsView.Message.UNCHANGED);
            } else {
                UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build();
                firebaseUser.updateProfile(changeRequest)
                        .addOnSuccessListener(aVoid -> {
                            User user = new User(
                                    firebaseUser.getDisplayName(),
                                    firebaseUser.getUid(),
                                    firebaseUser.getEmail()
                            );
                            mInteractor.updateUserProfile(user, this);
                        })
                        .addOnFailureListener(e -> {
                            mView.showMessage(SettingsView.Message.ERROR);
                            mView.hideProgressbar();
                        });
                mView.showProgressbar();
            }
        } else {
            mView.showMessage(SettingsView.Message.ERROR);
        }
    }

    @Override
    public void onUserProfileUpdateSuccess() {
        mView.showMessage(SettingsView.Message.SUCCESS);
        mView.hideProgressbar();
    }

    @Override
    public void onUserProfileUpdateFailure() {
        mView.showMessage(SettingsView.Message.ERROR);
        mView.hideProgressbar();
    }
}
