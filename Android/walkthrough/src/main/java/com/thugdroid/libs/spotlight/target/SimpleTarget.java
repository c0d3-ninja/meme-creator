package com.thugdroid.libs.spotlight.target;

import android.animation.TimeInterpolator;
import android.app.Activity;
import android.graphics.PointF;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.thugdroid.libs.spotlight.OnTargetStateChangedListener;
import com.thugdroid.libs.spotlight.R;
import com.thugdroid.libs.spotlight.shape.Shape;


/**
 * SimpleTarget will set a simple overlay.
 * If you set your customized overlay, consider using {@link CustomTarget} instead.
 **/
public class SimpleTarget extends Target {

  private SimpleTarget(Shape shape, PointF point, View overlay, long duration,
                       TimeInterpolator animation, OnTargetStateChangedListener listener) {
    super(shape, point, overlay, duration, animation, listener);
  }

  public static class Builder extends AbstractTargetBuilder<Builder, SimpleTarget> {

    @Override
    protected Builder self() {
      return this;
    }

    private CharSequence title;
    private CharSequence description;
    private PointF overlayPoint;
    private int imageResource;

    public Builder(@NonNull Activity context) {
      super(context);
    }

    public Builder setTitle(CharSequence title) {
      this.title = title;
      return this;
    }

    public Builder setDescription(CharSequence description) {
      this.description = description;
      return this;
    }

    public Builder setImageResource(int imageResource){
      this.imageResource=imageResource;
      return this;
    }

    public Builder setOverlayPoint(PointF overlayPoint) {
      this.overlayPoint = overlayPoint;
      return this;
    }

    public Builder setOverlayPoint(float x, float y) {
      this.overlayPoint = new PointF(x, y);
      return this;
    }

    @Override
    public SimpleTarget build() {
      ViewGroup root = new FrameLayout(getContext());
      View overlay = getContext().getLayoutInflater().inflate(R.layout.layout_spotlight, root);
      TextView titleView = overlay.findViewById(R.id.title);
      TextView descriptionView = overlay.findViewById(R.id.description);
      ImageView imageView = overlay.findViewById(R.id.image);
      LinearLayout layout = overlay.findViewById(R.id.container);
      if (title != null) {
        titleView.setText(title);
      }
      if (description != null) {
        descriptionView.setText(description);
      }
      if(imageResource!=0 ){
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(imageResource);
      }
      if (overlayPoint != null) {
        layout.setX(overlayPoint.x);
        layout.setY(overlayPoint.y);
      }
      return new SimpleTarget(shape, point, overlay, duration, animation, listener);
    }
  }
}