package com.dev.digitectask.media;

import android.content.Context;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import io.reactivex.Single;

public interface IModel {

    Single<Boolean> uploadImage(final ArrayList<String> filePaths, final StorageReference storageReference);
    Single<ArrayList<String>> getDisposableForCheckingFiles(Context context, ArrayList<String> files);

    void removeTempFiles();
}
