package com.tomaszstankowski.trainingapplication.discover;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.tomaszstankowski.trainingapplication.model.DataBaseAccessor;
import com.tomaszstankowski.trainingapplication.model.Photo;
import com.tomaszstankowski.trainingapplication.model.StorageAccessor;

import javax.inject.Inject;

public class DiscoverInteractorImpl implements DiscoverInteractor, ChildEventListener {
    private DataBaseAccessor mDataAccessor;
    private StorageAccessor mStorageAccessor;

    @Inject
    DiscoverInteractorImpl(DataBaseAccessor dataBaseAccessor, StorageAccessor storageAccessor) {
        mDataAccessor = dataBaseAccessor;
        mStorageAccessor = storageAccessor;
    }

    private Query mQuery;
    private OnRecentPhotosChangeListener mListener;

    @Override
    public void observeRecentPhotos(OnRecentPhotosChangeListener listener) {
        mListener = listener;
        mQuery = mDataAccessor.getRecentPhotos();
        mQuery.addChildEventListener(this);
    }

    @Override
    public void stopObservingRecentPhotos() {
        if (mQuery != null)
            mQuery.removeEventListener(this);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        String key = dataSnapshot.getKey();
        mDataAccessor.getPhoto(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Photo photo = dataSnapshot.getValue(Photo.class);
                if (photo != null) {
                    photo.key = key;
                    StorageReference image = mStorageAccessor.getImage(photo.key);
                    mListener.onPhotoAdded(photo, image);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();
        mListener.onPhotoRemoved(key);
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }
}
