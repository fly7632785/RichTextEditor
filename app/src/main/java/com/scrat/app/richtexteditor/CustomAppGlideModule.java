package com.scrat.app.richtexteditor;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.AppGlideModule;

/**
 * created by jafir on 2018/11/19
 */
@GlideModule
public class CustomAppGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);
        String cachePath = context.getExternalCacheDir().getAbsolutePath() + "/Glide";
        ;
        int cacheSize100MegaBytes = 104857600;
        if (cachePath != null) {
            builder.setDiskCache(new DiskLruCacheFactory(cachePath, cacheSize100MegaBytes));
        }
    }
}
