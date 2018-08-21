package com.dev.digitectask.media;

import android.content.Context;

import com.dev.digitectask.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PresenterImpl implements IPresenter {

    private IView mView;
    private IModel mModel;
    private StorageReference mStorageReference;
    private Disposable mUploadDisposable, mConverterDisposable;


    PresenterImpl(IView mView) {
        this.mView = mView;
        mModel = new ModelImpl();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        mStorageReference = storage.getReference();
    }

    @Override
    public void uploadToFirebase(ArrayList<String> files) {
        mView.showProgressDialog(R.string.uploading_message);
        mUploadDisposable = mModel.uploadImage(files, mStorageReference)
                .subscribe(result -> {
                    mView.stopProgressDialog();
                    mView.showMessage(result ? R.string.success_message : R.string.failed_message);
                });
    }

    @Override
    public void checkResolutionsAndCompress(Context context, ArrayList<String> files) {

        mView.showProgressDialog(R.string.compressing_message);
        mConverterDisposable = mModel.getDisposableForCheckingFiles(context, files)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(list -> {
                    mView.stopProgressDialog();
                    mView.updateView(list);
                });
    }

    @Override
    public void onActivityDestroy() {

        mModel.removeTempFiles();
        if (mUploadDisposable != null) mUploadDisposable.dispose();
        if (mConverterDisposable != null) mConverterDisposable.dispose();
    }
}
