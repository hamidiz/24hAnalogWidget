package info.staticfree.android.twentyfourhour;

/*
 * Some portions Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * A widget that displays the time as a 12-at-the-top 24 hour analog clock. By
 * default, it will show the current time in the current timezone. The displayed
 * time can be set using {@link #setTime(long)} and and
 * {@link #setTimezone(TimeZone)}.
 *
 * @author <a href="mailto:steve@staticfree.info">Steve Pomeroy</a>
 *
 */
public class Analog24HClock extends View {

	private long mTime;
	private boolean mShowNow = true;
	private boolean mShowSeconds = true;

	private static final int UPDATE_INTERVAL = 1000 * 15;

	private Calendar c;
	private Drawable mFace;
	private Drawable mHour;
	private Drawable mMinute;

	private int mDialWidth;
	private int mDialHeight;

	private float mHourRot;
	private float mMinRot;

	private boolean mKeepon = false;
	private int mBottom;
	private int mTop;
	private int mLeft;
	private int mRight;
	private boolean mSizeChanged;

	public Analog24HClock(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public Analog24HClock(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Analog24HClock(Context context) {
		super(context);

		init();
	}

	private void init() {
		final Resources r = getResources();
		mFace = r.getDrawable(R.drawable.clock_face_fixed_sunlight);
		mHour = r.getDrawable(R.drawable.hour_hand);
		mMinute = r.getDrawable(R.drawable.minute_hand);

		c = Calendar.getInstance();

		mDialHeight = mFace.getIntrinsicHeight();
		mDialWidth = mFace.getIntrinsicWidth();
	}

	/**
	 * Sets the currently displayed time in {@link System#currentTimeMillis()}
	 * time. This will clear {@link #setShowNow(boolean)}.
	 *
	 * @param time
	 *            the time to display on the clock
	 */
	public void setTime(long time) {
		mShowNow = false;

		mTime = time;
		updateHands();
		invalidate();
	}

	/**
	 * When set, the current time in the current timezone will be displayed.
	 *
	 * @param showNow
	 */
	public void setShowNow(boolean showNow) {
		mShowNow = showNow;
	}

	/**
	 * When set, the minute hand will move slightly based on the current number
	 * of seconds. If false, the minute hand will snap to the minute ticks.
	 * Note: there is no second hand, this only affects the minute hand.
	 *
	 * @param showSeconds
	 */
	public void setShowSeconds(boolean showSeconds) {
		mShowSeconds = showSeconds;
	}

	/**
	 * Sets the timezone to use when displaying the time.
	 *
	 * @param timezone
	 */
	public void setTimezone(TimeZone timezone) {
		c = Calendar.getInstance(timezone);
	}

	@Override
	protected void onAttachedToWindow() {
		mKeepon = true;
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		mKeepon = false;
		super.onDetachedFromWindow();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mSizeChanged = true;
	}

	// some parts from AnalogClock.java
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		final boolean sizeChanged = mSizeChanged;
		mSizeChanged = false;

		if (mShowNow) {
			mTime = System.currentTimeMillis();
			updateHands();

			if (mKeepon) {
				postInvalidateDelayed(UPDATE_INTERVAL);
			}
		}

		final int availW = mRight - mLeft;
		final int availH = mBottom - mTop;

		final int cX = availW / 2;
		final int cY = availH / 2;

		int w = mDialWidth;
		int h = mDialHeight;

		boolean scaled = false;

		if (availW < w || availH < h) {
			scaled = true;
			final float scale = Math.min((float) availW / (float) w,
					(float) availH / (float) h);
			canvas.save();
			canvas.scale(scale, scale, cX, cY);
		}

		if (sizeChanged) {
			mFace.setBounds(cX - (w / 2), cY - (h / 2), cX + (w / 2), cY
					+ (h / 2));
		}

		mFace.draw(canvas);

		canvas.save();
		canvas.rotate(mHourRot, cX, cY);

		if (sizeChanged) {
			w = mHour.getIntrinsicWidth();
			h = mHour.getIntrinsicHeight();
			mHour.setBounds(cX - (w / 2), cY - (h / 2), cX + (w / 2), cY
					+ (h / 2));
		}
		mHour.draw(canvas);
		canvas.restore();

		canvas.save();
		canvas.rotate(mMinRot, cX, cY);

		if (sizeChanged) {
			w = mMinute.getIntrinsicWidth();
			h = mMinute.getIntrinsicHeight();
			mMinute.setBounds(cX - (w / 2), cY - (h / 2), cX + (w / 2), cY
					+ (h / 2));
		}
		mMinute.draw(canvas);
		canvas.restore();

		if (scaled) {
			canvas.restore();
		}
	}

	// from AnalogClock.java
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		float hScale = 1.0f;
		float vScale = 1.0f;

		if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
			hScale = (float) widthSize / (float) mDialWidth;
		}

		if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
			vScale = (float) heightSize / (float) mDialHeight;
		}

		final float scale = Math.min(hScale, vScale);

		setMeasuredDimension(
				getDefaultSize((int) (mDialWidth * scale), widthMeasureSpec),
				getDefaultSize((int) (mDialHeight * scale), heightMeasureSpec));
	}

	@Override
	protected int getSuggestedMinimumHeight() {
		return mDialHeight;
	}

	@Override
	protected int getSuggestedMinimumWidth() {
		return mDialWidth;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		// because we don't have access to the actual protected fields
		mRight = right;
		mLeft = left;
		mTop = top;
		mBottom = bottom;
	}

	private void updateHands() {
		c.setTimeInMillis(mTime);

		final int h = c.get(Calendar.HOUR_OF_DAY);
		final int m = c.get(Calendar.MINUTE);
		final int s = c.get(Calendar.SECOND);

		mHourRot = ((12 + h) / 24.0f * 360) % 360 + (m / 60.0f) * 360 / 24.0f;
		mMinRot = (m / 60.0f) * 360
				+ (mShowSeconds ? ((s / 60.0f) * 360 / 60.0f) : 0);
	}
}