package com.dev.digitectask;

import android.content.Context;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PresenterImpl implements BasePresenter {

    private BaseView mView;
    private BaseModel mModel;
    private StorageReference mStorageReference;

    PresenterImpl(BaseView mView) {
        this.mView = mView;
        mModel = new ModelImpl();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        mStorageReference = storage.getReference();
    }

    @Override
    public Disposable uploadToFirebase(ArrayList<String> files) {
        mView.showProgressDialog(R.string.uploading_message);
        return mModel.uploadImage(files, mStorageReference)
                .subscribe(result -> {
                    mView.stopProgressDialog();
                    mView.showMessage(result ? R.string.success_message : R.string.failed_message);
                });
    }

    @Override
    public Disposable checkResolutionsAndCompress(Context context, ArrayList<String> files) {

        mView.showProgressDialog(R.string.compressing_message);
        return mModel.getDisposableForCheckingFiles(context, files)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(list -> {
                    mView.stopProgressDialog();
                    mView.updateView(list);
                });
    }

    @Override
    public void removeTempFiles() {
        mModel.removeTempFiles();
    }
}
