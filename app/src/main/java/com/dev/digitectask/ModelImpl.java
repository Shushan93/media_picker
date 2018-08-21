package com.dev.digitectask;

import android.content.Context;
import android.net.Uri;

import com.dev.digitectask.utils.Constants;
import com.dev.digitectask.utils.ImageCompressor;
import com.dev.digitectask.utils.Utils;
import com.dev.digitectask.video_compress.VideoResolutionChanger;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import io.reactivex.Single;

public class ModelImpl implements BaseModel {

    private ArrayList<String> mTmpFiles; // keep all compressed files here for removing after closing app

    public Single<Boolean> uploadImage(final ArrayList<String> filePaths, final StorageReference storageReference) {
        return Single.create(emitter -> {
            if (filePaths != null) {
                for (int i = 0; i < filePaths.size(); i++) {
                    Uri filePath = Uri.parse("file://" + filePaths.get(0));
                    StorageReference ref = storageReference.child("media/" + UUID.randomUUID().toString());
                    ref.putFile(filePath)
                            .addOnSuccessListener(taskSnapshot -> emitter.onSuccess(true))
                            .addOnFailureListener(e -> emitter.onSuccess(false));
                }
            }
        });
    }

    /**
     * This function checks images sizes and videos resolutions
     * and replace oversize files
     */
    public Single<ArrayList<String>> getDisposableForCheckingFiles(Context context, ArrayList<String> files) {
        return Single.create(emitter -> {
            for (int i = 0; i < files.size(); i++) {
                String path = files.get(i);
                String mediaType = Utils.getMediaType("file://" + path);
                if (mediaType.equals("image")) {
                    long length = Utils.getFileSize(path);
                    if (length > Constants.MAX_IMAGE_SIZE) {
                        String newImgPath = ImageCompressor.compressImage(context, path);
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
                        } catch (Throwable ignored) {

                        }
                    }
                }
            }
            emitter.onSuccess(files);
        });
    }

    @Override
    public void removeTempFiles() {
        if (mTmpFiles != null) Utils.removeTmpFiles(mTmpFiles);
    }


    private void addForRemoving(String newImgPath) {
        if (mTmpFiles == null) mTmpFiles = new ArrayList<>();
        mTmpFiles.add(newImgPath);
    }
}

