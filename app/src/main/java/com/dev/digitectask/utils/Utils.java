package com.dev.digitectask.utils;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Utils {
    public static float convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public static String getMediaType(String url) {
        String mimeType = getMimeType(url);
        if (mimeType != null) {
            if (mimeType.contains("video")) return "video";
        }
        return "image";
    }

    private static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String getNameFromPath(String path) {
        String name = "";
        String[] split = path.split("/");
        if (split.length != 0) name = split[split.length - 1];
        return name;
    }


    public static long getFileSize(String path) {
        File file = new File(path);
        return file.length();
    }

    public static double getFileSizeInMB(String path) {
        return getFileSize(path) / (1024.0 * 1024.0);
    }

    public static boolean isResolutionExceeded(String uriOfFile, int maxW, int maxH) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uriOfFile);
        int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        retriever.release();
        return width > maxW || height > maxH;
    }


    public static void removeTmpFiles(ArrayList<String> mTmpFiles) {
        for (String path : mTmpFiles) {
            File file = new File(path);
            file.delete();
        }
    }
}
