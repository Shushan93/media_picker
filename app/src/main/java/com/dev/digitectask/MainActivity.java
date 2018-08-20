package com.dev.digitectask;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dev.digitectask.adapter.SelectedFilesAdapter;
import com.dev.digitectask.utils.Constants;
import com.dev.digitectask.utils.ImageCompressor;
import com.dev.digitectask.utils.Utils;
import com.dev.digitectask.video_compress.VideoResolutionChanger;
import com.erikagtierrez.multiple_media_picker.Gallery;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
//
//import org.ffmpeg.android.Clip;
//import org.ffmpeg.android.FfmpegController;
//import org.ffmpeg.android.ShellUtils;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String SELECTED_FILES = "selected_files";
    private static final int OPEN_MEDIA_PICKER = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 45;

    private ArrayList<String> mFiles;
    private ArrayList<String> mTmpFiles;
    private StorageReference storageReference;

    private Disposable mDisposable;

    @BindView(R.id.selected_files_rv)
    RecyclerView mSelectedFilesRv;
    private Disposable mConverterDisposable;
    private SelectedFilesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        if (savedInstanceState != null) {
            mFiles = savedInstanceState.getStringArrayList(SELECTED_FILES);
        }
        initRecyclerView();
    }

    private void initRecyclerView() {
        if (mFiles != null) {
            if (mAdapter == null) {
                mSelectedFilesRv.setHasFixedSize(true);
                mSelectedFilesRv.setLayoutManager(new LinearLayoutManager(this));
                mAdapter = new SelectedFilesAdapter(this, mFiles);
                mSelectedFilesRv.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @OnClick(R.id.select_media_btn)
    public void onSelectMediaBtnClick() {
        if (mFiles != null && mFiles.size() != 0) {
            final ProgressDialog progressDialog = showProgressDialog(getString(R.string.uploading_message));
            mDisposable = FirebaseHelper.uploadImage(mFiles, storageReference)
                    .subscribe(result -> {
                        closeProgress(progressDialog);
                        showMessage(result ? R.string.success_message : R.string.failed_message);
                    });
        } else {
            if (hasPermission()) {
                openChooser();
            }
        }
    }

    private void closeProgress(ProgressDialog progressDialog) {
        progressDialog.cancel();
    }

    @NonNull
    private ProgressDialog showProgressDialog(String s) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(s);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTmpFiles != null) Utils.removeTmpFiles(mTmpFiles);
        if (mDisposable != null) mDisposable.dispose();
        if (mConverterDisposable != null) mConverterDisposable.dispose();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(SELECTED_FILES, mFiles);
    }

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

    private void showMessage(@StringRes int message) {
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
                ProgressDialog progressDialog = showProgressDialog(getString(R.string.compressing_message));
                mConverterDisposable = checkResolutionsAndCompress(mFiles)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(list -> {
                            closeProgress(progressDialog);
                            mFiles = list;
                            initRecyclerView();
                        });
            }
        }
    }

    /**
     * This function checks images sizes and videos resolutions
     * and replace oversize files
     */
    private Single<ArrayList<String>> checkResolutionsAndCompress(ArrayList<String> files) {
        return Single.create(emitter -> {
            for (int i = 0; i < files.size(); i++) {
                String path = files.get(i);
                String mediaType = Utils.getMediaType("file://" + path);
                if (mediaType.equals("image")) {
                    long length = Utils.getFileSize(path);
                    if (length > Constants.MAX_IMAGE_SIZE) {
                        String newImgPath = ImageCompressor.compressImage(this, path);
                        files.set(i, newImgPath);
                        addForRemoving(newImgPath);
                    }
                } else {
                    if (Utils.isResolutionExceeded(path, Constants.MAX_VIDEO_W, Constants.MAX_VIDEO_H)) {
                        try {
                            String pathToReEncodedFile =
                                    new VideoResolutionChanger().changeResolution(new File(path));
                            files.set(i, pathToReEncodedFile);
                            addForRemoving(pathToReEncodedFile);
                        } catch (Throwable t) {

                        }

                    }
                }
            }
            emitter.onSuccess(files);
        });
    }


    private void addForRemoving(String newImgPath) {
        if (mTmpFiles == null) mTmpFiles = new ArrayList<>();
        mTmpFiles.add(newImgPath);
    }

}
