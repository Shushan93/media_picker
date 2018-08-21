package com.dev.digitectask.media;

import android.support.annotation.StringRes;

import java.util.ArrayList;

public interface IView {

    void showProgressDialog(@StringRes int message);
    void stopProgressDialog();
    void updateView(ArrayList <String> files);
    void showMessage(@StringRes int message);
}
