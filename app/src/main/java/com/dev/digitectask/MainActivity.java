package com.dev.digitectask;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dev.digitectask.adapter.SelectedFilesAdapter;
import com.dev.digitectask.utils.Constants;
import com.erikagtierrez.multiple_media_picker.Gallery;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements BaseView {

    private static final String SELECTED_FILES = "selected_files";
    private static final int OPEN_MEDIA_PICKER = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 45;

    private ArrayList<String> mFiles = new ArrayList<>(); // all selected files

    private SelectedFilesAdapter mAdapter;

    private ProgressDialog mProgressDialog;
    private BasePresenter mPresenter;

    @BindView(R.id.selected_files_rv)
    RecyclerView mSelectedFilesRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPresenter = new PresenterImpl(this);
        if (savedInstanceState != null) {
            mFiles = savedInstanceState.getStringArrayList(SELECTED_FILES);
        }
        updateView(mFiles);
    }

    @OnClick(R.id.select_media_btn)
    public void onSelectMediaBtnClick() {
        if (mFiles.size() != 0) {
            mPresenter.uploadToFirebase(mFiles);
        } else {
            if (hasPermission()) {
                openChooser();
            }
        }
    }

    @Override
    public void showProgressDialog(int message) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(message);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

    }

    @Override
    public void stopProgressDialog() {
        if (mProgressDialog != null) mProgressDialog.cancel();
    }

    @Override
    public void updateView(ArrayList<String> files) {
        mFiles = files;
        mSelectedFilesRv.setHasFixedSize(true);
        mSelectedFilesRv.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SelectedFilesAdapter(this, mFiles);
        mSelectedFilesRv.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onActivityDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(SELECTED_FILES, mFiles);
    }

    /**
     * This function opens media picker library for choosing files to upload
     */
    private void openChooser() {
        Intent intent = Gallery.newIntent(this, getString(R.string.select_media),
                Gallery.IMAGE_AND_VIDEO, Constants.MAX_SELECTION);
        startActivityForResult(intent, OPEN_MEDIA_PICKER);
    }

    private boolean hasPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showMessage(R.string.permission_denied);
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            }
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void showMessage(@StringRes int message) {
        Snackbar.make(mSelectedFilesRv, message, Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openChooser();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OPEN_MEDIA_PICKER) {
            if (resultCode == RESULT_OK && data != null) {
                mFiles = data.getStringArrayListExtra("result");
                mPresenter.checkResolutionsAndCompress(this, mFiles);
            }
        }
    }

}
