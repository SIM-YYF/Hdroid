package com.hdroid.bitmap.xutil.callback;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

public interface BitmapSetter<T extends View> {
    void setBitmap(T container, Bitmap bitmap);

    void setDrawable(T container, Drawable drawable);

    Drawable getDrawable(T container);
}
