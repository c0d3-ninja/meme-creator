package com.thugdroid.libs.stickerimageview;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;


public abstract class StickerView extends FrameLayout {

    public static final String TAG = "com.thugdroid.libs.stickerimageview";
    public static final long ANIMATE_DURATION =250;

    private static final int minWidth=10,minHeight=10;
    private static final int opacityColor= Color.parseColor("#33000000");

    private BorderView iv_border;
    private ImageView iv_scale;
    private ImageView iv_delete;
    private ImageView iv_rotate;

    // For scalling
    private float scale_orgX = -1, scale_orgY = -1;
    // For moving
    private float move_orgX =-1, move_orgY = -1;

    private  float rotDownX=0,rotDownY=0,rotMovX=0,rotMovY=0;

    private double centerX, centerY;

    private final static int BUTTON_SIZE_DP = 30;
    private final static int SELF_SIZE_DP = 100;

    private boolean isAnimate = true;

    private int viewWidth,viewHeight;

    private int maxWidth,maxHeight;

   private float tempRotation=0.0f;


   private Rect windowRect =new Rect();
    StickerViewTouchListener stickerViewTouchListener;

    public StickerView(Context context, int parentWidth, int parentHeight){
        this(context);
        maxWidth = parentWidth;
        maxHeight= parentHeight;
    }

    public StickerView(Context context) {
        super(context);
    }

    public  void redraw(int viewWidth,int viewHeight){
        this.viewWidth=viewWidth;
        this.viewHeight=viewHeight;
        init(getContext());
    }
    private void init(Context context){
        this.iv_border = new BorderView(context);
        this.iv_scale = new ImageView(context);
        this.iv_delete = new ImageView(context);
        this.iv_rotate = new ImageView(context);

        this.iv_scale.setImageResource(R.drawable.resize);
        this.iv_delete.setImageResource(R.drawable.close);
        this.iv_rotate.setImageResource(R.drawable.rotate);

        this.setTag("DraggableViewGroup");
        this.iv_border.setTag("iv_border");
        this.iv_scale.setTag("iv_scale");
        this.iv_delete.setTag("iv_delete");
        this.iv_rotate.setTag("iv_rotate");
        int margin = convertDpToPixel(BUTTON_SIZE_DP, getContext())/2;
        int size = convertDpToPixel(SELF_SIZE_DP, getContext());

        LayoutParams this_params =
                new LayoutParams(
                        viewWidth,
                        viewHeight
                );
        this_params.gravity = Gravity.CENTER;

        LayoutParams iv_main_params =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );

        iv_main_params.setMargins(margin,margin,margin,margin);

        LayoutParams iv_border_params =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
        iv_border_params.setMargins(margin,margin,margin,margin);

        LayoutParams iv_scale_params =
                new LayoutParams(
                        convertDpToPixel(BUTTON_SIZE_DP, getContext()),
                        convertDpToPixel(BUTTON_SIZE_DP, getContext())
                );
        iv_scale_params.gravity = Gravity.BOTTOM | Gravity.RIGHT;


        LayoutParams iv_delete_params =
                new LayoutParams(
                        convertDpToPixel(BUTTON_SIZE_DP, getContext()),
                        convertDpToPixel(BUTTON_SIZE_DP, getContext())
                );
        iv_delete_params.gravity = Gravity.TOP | Gravity.LEFT;

        LayoutParams iv_rotate_params =
                new LayoutParams(
                        convertDpToPixel(BUTTON_SIZE_DP, getContext()),
                        convertDpToPixel(BUTTON_SIZE_DP, getContext())
                );
        iv_rotate_params.gravity = Gravity.TOP | Gravity.RIGHT;

        this.setLayoutParams(this_params);
        this.addView(getMainView(), iv_main_params);
        this.addView(iv_border, iv_border_params);
        this.addView(iv_scale, iv_scale_params);
        this.addView(iv_delete, iv_delete_params);
        this.addView(iv_rotate, iv_rotate_params);
        this.setOnTouchListener(mTouchListener);
        this.iv_scale.setOnTouchListener(mTouchListener);
        this.iv_rotate.setOnTouchListener(mTouchListener);
        this.iv_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(StickerView.this.getParent()!=null){
                    if(isAnimate){
                        animate().alpha(0f).setDuration(ANIMATE_DURATION)
                                .setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animator) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animator) {
                                        ViewGroup myCanvas = ((ViewGroup)StickerView.this.getParent());
                                        myCanvas.removeView(StickerView.this);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animator) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animator) {

                                    }
                                });

                    }
                }else{
                    ViewGroup myCanvas = ((ViewGroup)StickerView.this.getParent());
                    myCanvas.removeView(StickerView.this);
                }
                if(stickerViewTouchListener!=null){
                    stickerViewTouchListener.onModify();
                }

            }
        });

//        this.iv_flip.setOnClickListener(new OnClickListener(){
//
//            @Override
//            public void onClick(View view) {
//                Log.v(TAG, "flip the view");
//
//                View mainView = getMainView();
//                mainView.setRotationY(mainView.getRotationY() == -180f? 0f: -180f);
//                mainView.invalidate();
//                requestLayout();
//            }
//        });

    }

    public boolean isFlip(){
        return getMainView().getRotationY() == -180f;
    }

    protected abstract View getMainView();

    public void setStickerViewTouchListener(StickerViewTouchListener stickerViewTouchListener) {
        this.stickerViewTouchListener = stickerViewTouchListener;
    }

    private OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {

            if(view.getTag().equals("DraggableViewGroup")) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setControlItemsHidden(true);
                        move_orgX = event.getRawX();
                        move_orgY = event.getRawY();
                        if(stickerViewTouchListener!=null){
                            stickerViewTouchListener.onTouch();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float offsetX = event.getRawX() - move_orgX;
                        float offsetY = event.getRawY() - move_orgY;
                        StickerView.this.setX(StickerView.this.getX()+offsetX);
                        StickerView.this.setY(StickerView.this.getY() + offsetY);
                        move_orgX = event.getRawX();
                        move_orgY = event.getRawY();
                        if(stickerViewTouchListener!=null){
                            stickerViewTouchListener.onModify();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        setControlItemsHidden(false);
                        break;
                }
            }else if(view.getTag().equals("iv_rotate")){
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        rotDownX = event.getRawX();
                        rotDownY=event.getRawY();
                        StickerView.this.getGlobalVisibleRect(windowRect);
                        centerX =(StickerView.this.windowRect.left+StickerView.this.windowRect.right)/2;
                        centerY = (StickerView.this.windowRect.top+StickerView.this.windowRect.bottom)/2;
                        tempRotation=getRotation();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //rotate
                        rotMovX = event.getRawX();
                        rotMovY=event.getRawY();
                        double angle3=(Math.atan2(rotMovY - centerY, rotMovX - centerX) * 180 / Math.PI)-(Math.atan2(rotDownY - centerY, rotDownX - centerX) * 180 / Math.PI);
                        setRotation((float) angle3+tempRotation);
                        if(stickerViewTouchListener!=null){
                            stickerViewTouchListener.onModify();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        rotDownX=rotDownY=rotMovX=rotMovY=0;
                        break;
                }
            }
            else if(view.getTag().equals("iv_scale")){
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();

                        centerX = StickerView.this.getX()+
                                ((View)StickerView.this.getParent()).getX()+
                                (float)StickerView.this.getWidth()/2;
                        int result = 0;
                        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            result = getResources().getDimensionPixelSize(resourceId);
                        }
                        double statusBarHeight = result;
                        centerY = StickerView.this.getY()+
                                ((View)StickerView.this.getParent()).getY()+
                                statusBarHeight+
                                (float)StickerView.this.getHeight()/2;

                        break;
                    case MotionEvent.ACTION_MOVE:

                        double length1 = getLength(centerX, centerY, scale_orgX, scale_orgY);
                        double length2 = getLength(centerX, centerY, event.getRawX(), event.getRawY());

                        if(length2 > length1
                                ) {
                            int width = StickerView.this.getLayoutParams().width;
                            int height = StickerView.this.getLayoutParams().height;
                            if(width>=maxWidth || height>=maxHeight){
                                break;
                            }
                            //scale up
                            double offsetX = Math.abs(event.getRawX() - scale_orgX);
                            double offsetY = Math.abs(event.getRawY() - scale_orgY);
                            int offset =(int) Math.max(offsetX, offsetY);
                            StickerView.this.getLayoutParams().width = width+ offset;
                            StickerView.this.getLayoutParams().height = height+ offset;
                        }else if(length2 < length1
                                ) {
                            int width = StickerView.this.getLayoutParams().width;
                            int height = StickerView.this.getLayoutParams().height;
                            if(width<=minWidth || height<=minHeight){
                                break;
                            }
                            //scale down
                            double offsetX = Math.abs(event.getRawX() - scale_orgX);
                            double offsetY = Math.abs(event.getRawY() - scale_orgY);
                            int offset =(int) Math.max(offsetX, offsetY);
                            StickerView.this.getLayoutParams().width = width-offset;
                            StickerView.this.getLayoutParams().height =height-offset;
                        }

                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();

                        postInvalidate();
                        requestLayout();
                        if(stickerViewTouchListener!=null){
                            stickerViewTouchListener.onModify();
                        }
                        break;
                }
            }
            return true;
        }
    };


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private double getLength(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow(y2-y1, 2)+ Math.pow(x2-x1, 2));
    }

    private float[] getRelativePos(float absX, float absY){
        float [] pos = new float[]{
                absX-((View)this.getParent()).getX(),
                absY-((View)this.getParent()).getY()
        };
        return pos;
    }

    public void setControlItemsHidden(boolean isHidden){
        if(isAnimate){
            if(isHidden) {
                iv_border.animate()
                        .alpha(0f).setDuration(ANIMATE_DURATION);
                iv_scale.animate()
                        .alpha(0f).setDuration(ANIMATE_DURATION);
                iv_delete.animate()
                        .alpha(0f).setDuration(ANIMATE_DURATION);
                iv_rotate.animate()
                        .alpha(0f).setDuration(ANIMATE_DURATION);

            }else{
                iv_border.animate()
                        .alpha(1f).setDuration(ANIMATE_DURATION);
                iv_scale.animate()
                        .alpha(1f).setDuration(ANIMATE_DURATION);
                iv_delete.animate()
                        .alpha(1f).setDuration(ANIMATE_DURATION);
                iv_rotate.animate()
                        .alpha(1f).setDuration(ANIMATE_DURATION);
            }
            return;
        }
        if(isHidden) {

            iv_border.setVisibility(View.INVISIBLE);
            iv_scale.setVisibility(View.INVISIBLE);
            iv_delete.setVisibility(View.INVISIBLE);
            iv_rotate.setVisibility(View.INVISIBLE);
        }else{
            iv_border.setVisibility(View.VISIBLE);
            iv_scale.setVisibility(View.VISIBLE);
            iv_delete.setVisibility(View.VISIBLE);
            iv_rotate.setVisibility(View.VISIBLE);

        }
    }



    private class BorderView extends View {

        public BorderView(Context context) {
            super(context);
        }

        public BorderView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public BorderView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // Draw sticker border
            LayoutParams params = (LayoutParams)this.getLayoutParams();
            Rect border = new Rect();
            border.left = this.getLeft()-params.leftMargin;
            border.top = this.getTop()-params.topMargin;
            border.right = this.getRight()-params.rightMargin;
            border.bottom = this.getBottom()-params.bottomMargin;
            Paint borderPaint = new Paint();
            borderPaint.setStrokeWidth(6);
            borderPaint.setColor(Color.WHITE);
            borderPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(border, borderPaint);
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setColor(opacityColor);
            canvas.drawRect(border,p);

        }
    }

    private static int convertDpToPixel(float dp, Context context){
        return (int) (dp * (context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }


    public  void setAnimationContext(boolean isAnimate){
        this.isAnimate = isAnimate;
    }

    public boolean getAnimationContext(){
        return this.isAnimate;
    }

    public interface StickerViewTouchListener{
        void onTouch();
        void onModify();
    }
}