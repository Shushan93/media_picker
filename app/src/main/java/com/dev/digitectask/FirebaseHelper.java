package com.dev.digitectask;

import android.net.Uri;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.UUID;

import io.reactivex.Single;

public class FirebaseHelper {
    public static Single<Boolean> uploadImage(final ArrayList<String> filePaths, final StorageReference storageReference) {

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

}

