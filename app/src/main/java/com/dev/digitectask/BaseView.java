package com.dev.digitectask;

import android.support.annotation.StringRes;

import java.util.ArrayList;

public interface BaseView {

    void showProgressDialog(@StringRes int message);
    void stopProgressDialog();
    void updateView(ArrayList <String> files);
    void showMessage(@StringRes int message);
}
