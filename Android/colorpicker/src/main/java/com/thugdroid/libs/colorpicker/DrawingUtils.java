package com.thugdroid.libs.colorpicker;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DrawingUtils {

 public static int dpToPx(Context c, float dipValue) {
    DisplayMetrics metrics = c.getResources().getDisplayMetrics();
    float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    int res = (int) (val + 0.5); // Round
    // Ensure at least 1 pixel if val was > 0
    return res == 0 && val > 0 ? 1 : res;
  }
    public static String getHexColor(int color){
        return String.format("#%08X", (0xFFFFFFFF & color));
    }
}
