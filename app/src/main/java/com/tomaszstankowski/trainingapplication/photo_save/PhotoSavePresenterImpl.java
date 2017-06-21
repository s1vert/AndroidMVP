package com.tomaszstankowski.trainingapplication.photo_save;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;

import com.tomaszstankowski.trainingapplication.R;
import com.tomaszstankowski.trainingapplication.model.Photo;
import com.tomaszstankowski.trainingapplication.util.ImageManager;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * Presenter responding to PhotoSaveActivity calls
 */
@Singleton
public class PhotoSavePresenterImpl implements PhotoSavePresenter, PhotoSaveInteractor.OnPhotoSaveListener {
    private static final String TEMP_IMAGE_PATH = "TEMP_IMAGE_PATH";
    private static final String IMAGE_URI = "IMAGE_URI";
    private static final String PHOTO = "PHOTO";

    private PhotoSaveInteractor mInteractor;
    private ImageManager mManager;
    private PhotoSaveView mView;
    private Photo mPhoto;

    @Inject
    PhotoSavePresenterImpl(PhotoSaveInteractor interactor, ImageManager manager) {
        mInteractor = interactor;
        mManager = manager;
    }

    @Override
    public void onCreateView(PhotoSaveView view) {
        mView = view;
        Activity activity = mView.getActivityContext();
        Photo photo = activity.getIntent().getParcelableExtra(PHOTO);
        //null if photo has just been captured
        if (photo != null) {
            mPhoto = photo;
            Uri image = activity.getIntent().getParcelableExtra(IMAGE_URI);
            mView.updateView(photo.title, photo.desc, image, false);
        } else {
            //make sure previous photo not there
            mPhoto = null;
            String path = activity.getIntent().getStringExtra(TEMP_IMAGE_PATH);
            Uri image = mManager.getImageUriFromFile(new File(path));
            mView.updateView(null, null, image, true);
        }
    }

    @Override
    public void onDestroyView() {
        mView = null;
    }

    @Override
    public void onSaveButtonClicked(String title, String desc) {
        Activity activity = mView.getActivityContext();
        //saving captured photo
        if (mPhoto == null) {
            String path = activity.getIntent().getStringExtra(TEMP_IMAGE_PATH);
            mPhoto = new Photo(title, desc, "admin");
            new AsyncTask<String, Void, Uri>() {
                @Override
                protected Uri doInBackground(String... params) {
                    return mManager.compressImage(params[0]);
                }

                @Override
                protected void onPostExecute(Uri result) {
                    mInteractor.savePhoto(mPhoto, result, PhotoSavePresenterImpl.this);
                }
            }.execute(path);
        }
        //editing already existing photo
        else {
            mPhoto.title = title;
            mPhoto.desc = desc;
            mInteractor.editPhoto(mPhoto, this);
        }

        mView.showProgressBar();
    }

    @Override
    public void onBackButtonClicked() {
        mView.getActivityContext().finish();
    }

    @Override
    public void onSaveSuccess() {
        if (mView == null)
            return;
        Activity activity = mView.getActivityContext();
        if (activity.getParent() == null) {
            activity.setResult(Activity.RESULT_OK);
        } else {
            activity.getParent().setResult(Activity.RESULT_OK);
        }
        activity.finish();
    }

    @Override
    public void onSaveError() {
        if (mView == null)
            return;
        Activity activity = mView.getActivityContext();
        mView.showMessage(activity.getString(R.string.save_error));
        activity.finish();
    }
}
