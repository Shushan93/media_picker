package com.dev.digitectask;

import android.content.Context;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

public interface BasePresenter {
    Disposable uploadToFirebase(ArrayList<String> files);
    Disposable checkResolutionsAndCompress(Context context, ArrayList<String> files);

    void removeTempFiles();
}
