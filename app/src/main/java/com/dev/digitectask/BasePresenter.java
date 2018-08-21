package com.dev.digitectask;

import android.content.Context;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

public interface BasePresenter {
    void uploadToFirebase(ArrayList<String> files);
    void checkResolutionsAndCompress(Context context, ArrayList<String> files);

    void onActivityDestroy();
}
