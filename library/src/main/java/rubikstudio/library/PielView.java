package rubikstudio.library;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.List;

import androidx.core.graphics.ColorUtils;
import rubikstudio.library.model.LuckyItem;

/**
 * Created by kiennguyen on 11/5/16.
 */

public class PielView extends View {
    private RectF mRange = new RectF();
    private int mRadius;

    private Paint mArcPaint;
    private Paint mBackgroundPaint;
    private TextPaint mTextPaint;

    private float mStartAngle = 0;
    private int mCenter;
    private int mPadding;
    private int mTopTextPadding;
    private int mRoundOfNumber = 4;
    private int mEdgeWidth = -1;
    private boolean isRunning = false;

    private int borderColor = 0;
    private int defaultBackgroundColor = 0;
    private Drawable drawableCenterImage;
    private int textColor = 0;

    float viewRotation;
    double fingerRotation;
    double newFingerRotation;

    private List<LuckyItem> mLuckyItemList;

    private PieRotateListener mPieRotateListener;

    public interface PieRotateListener {
        void rotateDone(int index);
    }

    public PielView(Context context) {
        super(context);
    }

    public PielView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPieRotateListener(PieRotateListener listener) {
        this.mPieRotateListener = listener;
    }

    private void init() {
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);


        if (textColor != 0) mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14,
                getResources().getDisplayMetrics()));

        mRange = new RectF(mPadding, mPadding, mPadding + mRadius, mPadding + mRadius);
    }

    public void setData(List<LuckyItem> luckyItemList) {
        this.mLuckyItemList = luckyItemList;
        invalidate();
    }

    public void setPieBackgroundColor(int color) {
        defaultBackgroundColor = color;
        invalidate();
    }

    public void setBorderColor(int color) {
        borderColor = color;
        invalidate();
    }

    public void setTopTextPadding(int padding) {
        mTopTextPadding = padding;
        invalidate();
    }


    public void setPieCenterImage(Drawable drawable) {
        drawableCenterImage = drawable;
        invalidate();
    }


    public void setBorderWidth(int width) {
        mEdgeWidth = width;
        invalidate();
    }

    public void setPieTextColor(int color) {
        textColor = color;
        invalidate();
    }

    private void drawPieBackgroundWithBitmap(Canvas canvas, Bitmap bitmap) {
        canvas.drawBitmap(bitmap, null, new Rect(mPadding / 2, mPadding / 2,
                getMeasuredWidth() - mPadding / 2,
                getMeasuredHeight() - mPadding / 2), null);
    }

    /**
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mLuckyItemList == null) {
            return;
        }

        drawBackgroundColor(canvas, defaultBackgroundColor);

        init();

        float tmpAngle = mStartAngle;
        float sweepAngle = 360f / mLuckyItemList.size();

        for (int i = 0; i < mLuckyItemList.size(); i++) {

            if (mLuckyItemList.get(i).color != 0) {
                mArcPaint.setStyle(Paint.Style.FILL);
                mArcPaint.setColor(mLuckyItemList.get(i).color);
                canvas.drawArc(mRange, tmpAngle, sweepAngle, true, mArcPaint);
            }

            if (borderColor != 0 && mEdgeWidth > 0) {
                mArcPaint.setStyle(Paint.Style.STROKE);
                mArcPaint.setColor(borderColor);
                mArcPaint.setStrokeWidth(mEdgeWidth);
                canvas.drawArc(mRange, tmpAngle, sweepAngle, true, mArcPaint);
            }

            int sliceColor = mLuckyItemList.get(i).color != 0 ? mLuckyItemList.get(i).color : defaultBackgroundColor;

            if (!TextUtils.isEmpty(mLuckyItemList.get(i).topText))
                drawTopText(canvas, tmpAngle, sweepAngle, mLuckyItemList.get(i).topText, sliceColor);
            if (!TextUtils.isEmpty(mLuckyItemList.get(i).secondaryText))
                drawSecondaryText(canvas, tmpAngle, sweepAngle, mLuckyItemList.get(i).secondaryText, sliceColor);

            if (mLuckyItemList.get(i).icon != 0)
                drawImage(canvas, tmpAngle, BitmapFactory.decodeResource(getResources(),
                        mLuckyItemList.get(i).icon));
            tmpAngle += sweepAngle;
        }

        drawCenterImage(canvas, drawableCenterImage);
    }

    private void drawBackgroundColor(Canvas canvas, int color) {
        if (color == 0)
            return;
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(color);
        canvas.drawCircle(mCenter, mCenter, mCenter - 5, mBackgroundPaint);
    }

    /**
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());

        mPadding = getPaddingLeft() == 0 ? 10 : getPaddingLeft();
        mRadius = width - mPadding * 2;

        mCenter = width / 2;

        setMeasuredDimension(width, width);
    }

    /**
     * @param canvas
     * @param tmpAngle
     * @param bitmap
     */
    private void drawImage(Canvas canvas, float tmpAngle, Bitmap bitmap) {
        int imgWidth = mRadius / mLuckyItemList.size();

        float angle = (float) ((tmpAngle + 360f / mLuckyItemList.size() / 2) * Math.PI / 180);

        int x = (int) (mCenter + mRadius / 2 / 2 * Math.cos(angle));
        int y = (int) (mCenter + mRadius / 2 / 2 * Math.sin(angle));

        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2,
                x + imgWidth / 2, y + imgWidth / 2);
        canvas.drawBitmap(bitmap, null, rect, null);
    }

    private void drawCenterImage(Canvas canvas, Drawable drawable) {
        ;
        Bitmap bitmap = LuckyWheelUtils.drawableToBitmap(drawable);
        bitmap = Bitmap.createScaledBitmap(bitmap, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), false);
        canvas.drawBitmap(bitmap, getMeasuredWidth() / 2 - bitmap.getWidth() / 2,
                getMeasuredHeight() / 2 - bitmap.getHeight() / 2, null);
    }

    private boolean isColorDark(int color) {
        double colorValue = ColorUtils.calculateLuminance(color);
        double compareValue = 0.30d;
        return colorValue <= compareValue;
    }


    /**
     * @param canvas
     * @param tmpAngle
     * @param sweepAngle
     * @param mStr
     */
    private void drawTopText(Canvas canvas, float tmpAngle, float sweepAngle, String mStr, int backgroundColor) {
        Path path = new Path();
        path.addArc(mRange, tmpAngle, sweepAngle);

        if (textColor == 0)
            mTextPaint.setColor(isColorDark(backgroundColor) ? 0xffffffff : 0xff000000);

        Typeface typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        mTextPaint.setTypeface(typeface);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setTextSize(25f);
        float textWidth = mTextPaint.measureText(mStr);
        int hOffset = (int) (mRadius * Math.PI / mLuckyItemList.size() / 2 - textWidth / 2);

        int vOffset = mTopTextPadding;

        canvas.drawTextOnPath(mStr, path, hOffset, vOffset, mTextPaint);
    }


    /**
     * @param canvas
     * @param tmpAngle
     * @param sweepAngle
     * @param mStr
     */
    private void drawSecondaryText(Canvas canvas, float tmpAngle, float sweepAngle, String mStr, int backgroundColor) {
        canvas.save();
        int arraySize = mLuckyItemList.size();

        if (textColor == 0)
            mTextPaint.setColor(isColorDark(backgroundColor) ? 0xffffffff : 0xff000000);


        Typeface typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        mTextPaint.setTypeface(typeface);
        mTextPaint.setTextSize(60f);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        float textWidth = mTextPaint.measureText(mStr);

        float initFloat = (tmpAngle + 360f / arraySize / 2);
        float angle = (float) (initFloat * Math.PI / 180);

        int x = (int) (mCenter + mRadius / 2 / 2 * Math.cos(angle));
        int y = (int) (mCenter + mRadius / 2 / 2 * Math.sin(angle));

        RectF rect = new RectF(x + textWidth, y,
                x - textWidth, y);

        Path path = new Path();
        path.addRect(rect, Path.Direction.CW);
        path.close();
        canvas.rotate(initFloat - (arraySize / 9f), x, y);
        canvas.drawTextOnPath(mStr, path, arraySize - 30, arraySize, mTextPaint);
        canvas.restore();
    }

    /**
     * @return
     */
    private float getAngleOfIndexTarget(int index) {
        return (360f / mLuckyItemList.size()) * index;
    }

    /**
     * @param numberOfRound
     */
    public void setRound(int numberOfRound) {
        mRoundOfNumber = numberOfRound;
    }

    /**
     * @param index
     */
    public void rotateTo(final int index) {
        if (isRunning) {
            return;
        }

        //If the staring position is already off 0 degrees, make an illusion that the rotation has smoothly been triggered.
        // But this inital animation will just reset the position of the circle to 0 degreees.
        if (getRotation() != 0.0f) {
            setRotation(getRotation() % 360f);
            //The multiplier is to do a big rotation again if the position is already near 360.
            float multiplier = getRotation() > 200f ? 2 : 1;
            animate()
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(500L)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            isRunning = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            isRunning = false;
                            setRotation(0);
                            rotateTo(index);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    })
                    .rotation(360f * multiplier)
                    .start();
            return;
        }

        float targetAngle = 360f * mRoundOfNumber + 270f - getAngleOfIndexTarget(index) - (360f / mLuckyItemList.size()) / 2;
        animate()
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(mRoundOfNumber * 1000 + 900L)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        isRunning = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isRunning = false;
                        setRotation(getRotation() % 360f);
                        if (mPieRotateListener != null) {
                            mPieRotateListener.rotateDone(index);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                })
                .rotation(targetAngle)
                .start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isRunning) {
            return false;
        }
        final float x = event.getX();
        final float y = event.getY();

        final float xc = getWidth() / 2.0f;
        final float yc = getHeight() / 2.0f;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                viewRotation = getRotation();
                fingerRotation = Math.toDegrees(Math.atan2(x - xc, yc - y));
                break;
            case MotionEvent.ACTION_MOVE:
                newFingerRotation = Math.toDegrees(Math.atan2(x - xc, yc - y));
                setRotation((float) (viewRotation + newFingerRotation - fingerRotation));
                break;
            case MotionEvent.ACTION_UP:
                fingerRotation = newFingerRotation = 0.0f;
                break;
        }
        return true;
    }
}
