/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.kenmeidearu.materialdatetimepicker.date;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kenmeidearu.materialdatetimepicker.HapticFeedbackController;
import com.kenmeidearu.materialdatetimepicker.R;
import com.kenmeidearu.materialdatetimepicker.TypefaceHelper;
import com.kenmeidearu.materialdatetimepicker.Utils;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;

/**
 * Dialog allowing users to select a date.
 */
public class DatePickerDialog extends DialogFragment implements
         OnClickListener, DatePickerController {

    private static final int UNINITIALIZED = -1;
    private static final int MONTH_AND_DAY_VIEW = 0;
    private static final int YEAR_VIEW = 1;
    private static final int MONTH_VIEW = 2;

    private static final String KEY_INITIAL_TIME = "initial_time";
    private static final String KEY_IS_24_HOUR_VIEW = "is_24_hour_view";
    private static final String KEY_ENABLE_SECONDS = "enable_seconds";
    private static final String KEY_ENABLE_MINUTES = "enable_minutes";

    private static final String KEY_SELECTED_YEAR = "year";
    private static final String KEY_SELECTED_MONTH = "month";
    private static final String KEY_SELECTED_DAY = "day";
    private static final String KEY_SELECTABLE_HOUR="hour";
    private static final String KEY_LIST_POSITION = "list_position";
    private static final String KEY_WEEK_START = "week_start";
    private static final String KEY_YEAR_START = "year_start";
    private static final String KEY_YEAR_END = "year_end";
    private static final String KEY_CURRENT_VIEW = "current_view";
    private static final String KEY_LIST_POSITION_OFFSET = "list_position_offset";
    private static final String KEY_MIN_DATE = "min_date";
    private static final String KEY_MAX_DATE = "max_date";
    private static final String KEY_HIGHLIGHTED_DAYS = "highlighted_days";
    private static final String KEY_SELECTABLE_DAYS = "selectable_days";
    private static final String KEY_DISABLED_DAYS = "disabled_days";
    private static final String KEY_THEME_DARK = "theme_dark";
    private static final String KEY_THEME_DARK_CHANGED = "theme_dark_changed";
    private static final String KEY_ACCENT = "accent";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_DISMISS = "dismiss";
    private static final String KEY_AUTO_DISMISS = "auto_dismiss";
    private static final String KEY_DEFAULT_VIEW = "default_view";
    private static final String KEY_TITLE = "title";
    private static final String KEY_OK_RESID = "ok_resid";
    private static final String KEY_OK_STRING = "ok_string";
    private static final String KEY_CANCEL_RESID = "cancel_resid";
    private static final String KEY_CANCEL_STRING = "cancel_string";


    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;
   // private static final int DEFAULT_START_MONTH = 0;
   // private static final int DEFAULT_END_MONTH = 11;


    private static final int ANIMATION_DURATION = 300;
    private static final int ANIMATION_DELAY = 500;

    private static SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy", Locale.getDefault());
    private static SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMM", Locale.getDefault());
    private static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd", Locale.getDefault());

    private final Calendar mCalendar = trimToMidnight(Calendar.getInstance());
    private OnDateSetListener mCallBack;
    private HashSet<OnDateChangedListener> mListeners = new HashSet<>();
    private DialogInterface.OnCancelListener mOnCancelListener;
    private DialogInterface.OnDismissListener mOnDismissListener;

    private AccessibleDateAnimator mAnimator;

    private TextView mDayOfWeekView;
    private String mDoublePlaceholderText;
    private LinearLayout mMonthAndDayView;
    private  RelativeLayout timeDisplayView;
    private TextView mSelectedMonthTextView;
    private TextView mSelectedDayTextView;
    private TextView mYearView;
    private DayPickerView mDayPickerView;
    private YearPickerView mYearPickerView;
    private MonthPickerView mMonthPickerView;
    private TimePickerView mHourPickerView;
    private TimePickerView mMinutePickerView;
    private TimePickerView mSecondPickerView;


    private int mCurrentView = UNINITIALIZED;

    private int mWeekStart = mCalendar.getFirstDayOfWeek();
    private int mMinYear = DEFAULT_START_YEAR;
    private int mMaxYear = DEFAULT_END_YEAR;
   // private int mMinMonth = DEFAULT_START_MONTH;
   // private int mMaxMonth = DEFAULT_END_MONTH;
    private String mTitle;
    private Calendar mMinDate;
    private Calendar mMaxDate;
    private Calendar[] highlightedDays;
    private Calendar[] selectableDays;
    private Calendar[] disabledDays;
    private boolean mThemeDark = false;
    private boolean mThemeDarkChanged = false;
    private int mAccentColor = -1;
    private boolean mVibrate = true;
    private boolean mDismissOnPause = false;
    private boolean mAutoDismiss = false;
    private int mDefaultView = MONTH_AND_DAY_VIEW;
    private int mOkResid = R.string.mdtp_ok;
    private String mOkString;
    private int mCancelResid = R.string.mdtp_cancel;
    private String mCancelString;

    private HapticFeedbackController mHapticFeedbackController;

    private boolean mDelayAnimation = true;

    // Accessibility strings.
    private String mDayPickerDescription;
    private String mSelectDay;
    private String mMonthPickerDescription;
    private String mSelectMonth;
    private String mYearPickerDescription;
    private String mSelectYear;


    //time dialog
    private Timepoint mCurrentTime;
    public static final int HOUR_INDEX = 3;
    public static final int MINUTE_INDEX = 4;
    public static final int SECOND_INDEX = 5;
    public static final int AM = 6;
    public static final int PM = 7;
    private TextView mHourView;
    private TextView mHourSpaceView;
    private TextView mMinuteView;
    private TextView mMinuteSpaceView;
    private TextView mSecondView;
    private TextView mSecondSpaceView;
    private TextView mAmPmTextView;
    private View mAmPmHitspace;
    //private RadialPickerLayoutDate mTimePicker;

    private String mAmText;
    private String mPmText;

    private Timepoint mInitialTime;
    private boolean mIs24HourMode;
    private boolean mEnableSeconds;
    private boolean mEnableMinutes;
    private boolean mOnlyDate;

    // Accessibility strings.
    private String mHourPickerDescription;
    private String mSelectHours;
    private String mMinutePickerDescription;
    private String mSelectMinutes;
    private String mSecondPickerDescription;
    private String mSelectSeconds;
    //end time dialog
    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateSetListener {

        /**
         * @param view        The view associated with this listener.
         * @param year        The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility
         *                    with {@link java.util.Calendar}.
         * @param dayOfMonth  The day of the month that was set.
         */
        void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth,int hourOfDay,int minute,int second);
    }

    /**
     * The callback used to notify other date picker components of a change in selected date.
     */
    public interface OnDateChangedListener {

        void onDateChanged();
    }


    public DatePickerDialog() {
        // Empty constructor required for dialog fragment.
    }

    /**
     * @param callBack    How the parent is notified that the date is set.
     * @param year        The initial year of the dialog.
     * @param monthOfYear The initial month of the dialog.
     * @param dayOfMonth  The initial day of the dialog.
     */
    //nanti tambahkan untuk jam
    public static DatePickerDialog newInstance(OnDateSetListener callBack, int year,
                                               int monthOfYear,int dayOfMonth,
                                               int hourOfDay, int minute, boolean is24HourMode) {
        DatePickerDialog ret = new DatePickerDialog();
        ret.initialize(callBack, year, monthOfYear, dayOfMonth,hourOfDay,minute,0,is24HourMode);
        return ret;
    }

    public void initialize(OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth,
                           int hourOfDay, int minute,int second, boolean is24HourMode) {
        mCallBack = callBack;
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, monthOfYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        mInitialTime = new Timepoint(hourOfDay, minute, second);
        mIs24HourMode = is24HourMode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        activity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mCurrentView = UNINITIALIZED;
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_INITIAL_TIME)
                && savedInstanceState.containsKey(KEY_IS_24_HOUR_VIEW)) {
            mCalendar.set(Calendar.YEAR, savedInstanceState.getInt(KEY_SELECTED_YEAR));
            mCalendar.set(Calendar.MONTH, savedInstanceState.getInt(KEY_SELECTED_MONTH));
            mCalendar.set(Calendar.DAY_OF_MONTH, savedInstanceState.getInt(KEY_SELECTED_DAY));
            mDefaultView = savedInstanceState.getInt(KEY_DEFAULT_VIEW);
            //time
            mInitialTime = savedInstanceState.getParcelable(KEY_INITIAL_TIME);
            mIs24HourMode = savedInstanceState.getBoolean(KEY_IS_24_HOUR_VIEW);
            mEnableSeconds = savedInstanceState.getBoolean(KEY_ENABLE_SECONDS);
            mEnableMinutes = savedInstanceState.getBoolean(KEY_ENABLE_MINUTES);

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_YEAR, mCalendar.get(Calendar.YEAR));
        outState.putInt(KEY_SELECTED_MONTH, mCalendar.get(Calendar.MONTH));
        outState.putInt(KEY_SELECTED_DAY, mCalendar.get(Calendar.DAY_OF_MONTH));
        outState.putInt(KEY_SELECTABLE_HOUR,mCalendar.get(Calendar.HOUR_OF_DAY));
        outState.putInt(KEY_WEEK_START, mWeekStart);
        outState.putInt(KEY_YEAR_START, mMinYear);
        outState.putInt(KEY_YEAR_END, mMaxYear);
        outState.putInt(KEY_CURRENT_VIEW, mCurrentView);
        int listPosition = -1;
        if (mCurrentView == MONTH_AND_DAY_VIEW) {
            listPosition = mDayPickerView.getMostVisiblePosition();
        }else if (mCurrentView == MONTH_VIEW) {
            listPosition = mMonthPickerView.getFirstVisiblePosition();
            outState.putInt(KEY_LIST_POSITION_OFFSET, mMonthPickerView.getFirstPositionOffset());
        }else if (mCurrentView == YEAR_VIEW) {
            listPosition = mYearPickerView.getFirstVisiblePosition();
            outState.putInt(KEY_LIST_POSITION_OFFSET, mYearPickerView.getFirstPositionOffset());
        }else if(mCurrentView== HOUR_INDEX){
            listPosition= mHourPickerView.getFirstVisiblePosition();
            outState.putInt(KEY_LIST_POSITION_OFFSET, mHourPickerView.getFirstPositionOffset());
        }else if(mCurrentView==MINUTE_INDEX){
            listPosition=mMinutePickerView.getFirstVisiblePosition();
            outState.putInt(KEY_LIST_POSITION_OFFSET, mMinutePickerView.getFirstPositionOffset());
        }else if(mCurrentView==SECOND_INDEX){
            listPosition=mSecondPickerView.getFirstVisiblePosition();
            outState.putInt(KEY_LIST_POSITION_OFFSET, mSecondPickerView.getFirstPositionOffset());
        }
        outState.putInt(KEY_LIST_POSITION, listPosition);
        outState.putSerializable(KEY_MIN_DATE, mMinDate);
        outState.putSerializable(KEY_MAX_DATE, mMaxDate);
        outState.putSerializable(KEY_HIGHLIGHTED_DAYS, highlightedDays);
        outState.putSerializable(KEY_SELECTABLE_DAYS, selectableDays);
        outState.putSerializable(KEY_DISABLED_DAYS, disabledDays);
        outState.putBoolean(KEY_THEME_DARK, mThemeDark);
        outState.putBoolean(KEY_THEME_DARK_CHANGED, mThemeDarkChanged);
        outState.putInt(KEY_ACCENT, mAccentColor);
        outState.putBoolean(KEY_VIBRATE, mVibrate);
        outState.putBoolean(KEY_DISMISS, mDismissOnPause);
        outState.putBoolean(KEY_AUTO_DISMISS, mAutoDismiss);
        outState.putInt(KEY_DEFAULT_VIEW, mDefaultView);
        outState.putString(KEY_TITLE, mTitle);
        outState.putInt(KEY_OK_RESID, mOkResid);
        outState.putString(KEY_OK_STRING, mOkString);
        outState.putInt(KEY_CANCEL_RESID, mCancelResid);
        outState.putString(KEY_CANCEL_STRING, mCancelString);
        //time
        outState.putBoolean(KEY_IS_24_HOUR_VIEW, mIs24HourMode);
        outState.putParcelable(KEY_INITIAL_TIME, mInitialTime);
        outState.putBoolean(KEY_ENABLE_SECONDS, mEnableSeconds);
        outState.putBoolean(KEY_ENABLE_MINUTES, mEnableMinutes);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // All options have been set at this point: round the initial selection if necessary
        setToNearestDate(mCalendar);

        View view = inflater.inflate(R.layout.mdtp_date_picker_dialog, container, false);
        timeDisplayView=(RelativeLayout) view.findViewById(R.id.time_display);
        mDayOfWeekView = (TextView) view.findViewById(R.id.date_picker_header);
        mMonthAndDayView = (LinearLayout) view.findViewById(R.id.date_picker_month_and_year);
        mSelectedMonthTextView = (TextView) view.findViewById(R.id.date_picker_month);
        mSelectedMonthTextView.setOnClickListener(this);
        mSelectedDayTextView = (TextView) view.findViewById(R.id.date_picker_day);
        mSelectedDayTextView.setOnClickListener(this);

        mYearView = (TextView) view.findViewById(R.id.date_picker_year);
        mYearView.setOnClickListener(this);

        int listPosition = -1;
        int listPositionOffset = 0;
        int currentView = mDefaultView;
        if (savedInstanceState != null) {
            mWeekStart = savedInstanceState.getInt(KEY_WEEK_START);
            mMinYear = savedInstanceState.getInt(KEY_YEAR_START);
            mMaxYear = savedInstanceState.getInt(KEY_YEAR_END);
            currentView = savedInstanceState.getInt(KEY_CURRENT_VIEW);
            listPosition = savedInstanceState.getInt(KEY_LIST_POSITION);
            listPositionOffset = savedInstanceState.getInt(KEY_LIST_POSITION_OFFSET);
            mMinDate = (Calendar) savedInstanceState.getSerializable(KEY_MIN_DATE);
            mMaxDate = (Calendar) savedInstanceState.getSerializable(KEY_MAX_DATE);
            highlightedDays = (Calendar[]) savedInstanceState.getSerializable(KEY_HIGHLIGHTED_DAYS);
            selectableDays = (Calendar[]) savedInstanceState.getSerializable(KEY_SELECTABLE_DAYS);
            disabledDays = (Calendar[]) savedInstanceState.getSerializable(KEY_DISABLED_DAYS);
            mThemeDark = savedInstanceState.getBoolean(KEY_THEME_DARK);
            mThemeDarkChanged = savedInstanceState.getBoolean(KEY_THEME_DARK_CHANGED);
            mAccentColor = savedInstanceState.getInt(KEY_ACCENT);
            mVibrate = savedInstanceState.getBoolean(KEY_VIBRATE);
            mDismissOnPause = savedInstanceState.getBoolean(KEY_DISMISS);
            mAutoDismiss = savedInstanceState.getBoolean(KEY_AUTO_DISMISS);
            mTitle = savedInstanceState.getString(KEY_TITLE);
            mOkResid = savedInstanceState.getInt(KEY_OK_RESID);
            mOkString = savedInstanceState.getString(KEY_OK_STRING);
            mCancelResid = savedInstanceState.getInt(KEY_CANCEL_RESID);
            mCancelString = savedInstanceState.getString(KEY_CANCEL_STRING);
        }

        final Activity activity = getActivity();
        mDayPickerView = new SimpleDayPickerView(activity, this);
        mYearPickerView = new YearPickerView(activity, this);
        mMonthPickerView = new MonthPickerView(activity, this);
        mHourPickerView =new TimePickerView(activity,this,"HOUR",mIs24HourMode);
        mMinutePickerView =new TimePickerView(activity,this,"MINUTE",mIs24HourMode);
        mSecondPickerView =new TimePickerView(activity,this,"SECOND",mIs24HourMode);

        // if theme mode has not been set by java code, check if it is specified in Style.xml
        if (!mThemeDarkChanged) {
            mThemeDark = Utils.isDarkTheme(activity, mThemeDark);
        }

        Resources res = getResources();
        mDayPickerDescription = res.getString(R.string.mdtp_day_picker_description);
        mSelectDay = res.getString(R.string.mdtp_select_day);
        mMonthPickerDescription = res.getString(R.string.mdtp_month_picker_description);
        mSelectMonth = res.getString(R.string.mdtp_select_month);
        mYearPickerDescription = res.getString(R.string.mdtp_year_picker_description);
        mSelectYear = res.getString(R.string.mdtp_select_year);
        //add for timer dialog
        mHourPickerDescription = res.getString(R.string.mdtp_hour_picker_description);
        mSelectHours = res.getString(R.string.mdtp_select_hours);
        mMinutePickerDescription = res.getString(R.string.mdtp_minute_picker_description);
        mSelectMinutes = res.getString(R.string.mdtp_select_minutes);
        mSecondPickerDescription = res.getString(R.string.mdtp_second_picker_description);
        mSelectSeconds = res.getString(R.string.mdtp_select_seconds);

        mHourView = (TextView) view.findViewById(R.id.hours);
        mHourView.setOnClickListener(this);
        mHourSpaceView = (TextView) view.findViewById(R.id.hour_space);
        mMinuteSpaceView = (TextView) view.findViewById(R.id.minutes_space);
        mMinuteView = (TextView) view.findViewById(R.id.minutes);
        mMinuteView.setOnClickListener(this);
        mSecondSpaceView = (TextView) view.findViewById(R.id.seconds_space);
        mSecondView = (TextView) view.findViewById(R.id.seconds);
        mSecondView.setOnClickListener(this);
        mAmPmTextView = (TextView) view.findViewById(R.id.ampm_label);
        mAmPmTextView.setOnClickListener(this);
        String[] amPmTexts = new DateFormatSymbols().getAmPmStrings();
        mAmText = amPmTexts[0];
        mPmText = amPmTexts[1];

        // Enable or disable the AM/PM view.
        mAmPmHitspace = view.findViewById(R.id.ampm_hitspace);
        if (mIs24HourMode) {
            mAmPmTextView.setVisibility(View.GONE);
        } else {
            mAmPmTextView.setVisibility(View.VISIBLE);
            updateAmPmDisplay(mInitialTime.isAM() ? AM : PM);
            mAmPmHitspace.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    tryVibrate();
                    int amOrPm = getIsCurrentlyAmOrPm();
                    if (amOrPm == AM) {
                        amOrPm = PM;
                    } else if (amOrPm == PM) {
                        amOrPm = AM;
                    }
                    setAmOrPm(amOrPm);
                }
            });
        }

        // Disable seconds picker
        if (!mEnableSeconds) {
            mSecondView.setVisibility(View.GONE);
            view.findViewById(R.id.separator_seconds).setVisibility(View.GONE);
        }

        // Disable minutes picker
        if (!mEnableMinutes) {
            mMinuteSpaceView.setVisibility(View.GONE);
            view.findViewById(R.id.separator).setVisibility(View.GONE);
        }

        // Center stuff depending on what's visible
        //only enable date
        if(mOnlyDate){
            timeDisplayView.setVisibility(View.GONE);
        }
        if (mIs24HourMode && !mEnableSeconds && mEnableMinutes) {
            // center first separator
            RelativeLayout.LayoutParams paramsSeparator = new RelativeLayout.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT
            );
            paramsSeparator.addRule(RelativeLayout.CENTER_IN_PARENT);
            TextView separatorView = (TextView) view.findViewById(R.id.separator);
            separatorView.setLayoutParams(paramsSeparator);
        } else if (!mEnableMinutes && !mEnableSeconds) {
            // center the hour
            RelativeLayout.LayoutParams paramsHour = new RelativeLayout.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT
            );
            paramsHour.addRule(RelativeLayout.CENTER_IN_PARENT);
            mHourSpaceView.setLayoutParams(paramsHour);

            if (!mIs24HourMode) {
                RelativeLayout.LayoutParams paramsAmPm = new RelativeLayout.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT
                );
                paramsAmPm.addRule(RelativeLayout.RIGHT_OF, R.id.hour_space);
                paramsAmPm.addRule(RelativeLayout.ALIGN_BASELINE, R.id.hour_space);
                mAmPmTextView.setLayoutParams(paramsAmPm);
            }
        } else if (mEnableSeconds) {
            // link separator to minutes
            final View separator = view.findViewById(R.id.separator);
            RelativeLayout.LayoutParams paramsSeparator = new RelativeLayout.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT
            );
            paramsSeparator.addRule(RelativeLayout.LEFT_OF, R.id.minutes_space);
            paramsSeparator.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            separator.setLayoutParams(paramsSeparator);

            if (!mIs24HourMode) {
                // center minutes
                RelativeLayout.LayoutParams paramsMinutes = new RelativeLayout.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT
                );
                paramsMinutes.addRule(RelativeLayout.CENTER_IN_PARENT);
                mMinuteSpaceView.setLayoutParams(paramsMinutes);
            } else {
                // move minutes to right of center
                RelativeLayout.LayoutParams paramsMinutes = new RelativeLayout.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT
                );
                paramsMinutes.addRule(RelativeLayout.RIGHT_OF, R.id.center_view);
                mMinuteSpaceView.setLayoutParams(paramsMinutes);
            }
        }



        //end timer
        int bgColorResource = mThemeDark ? R.color.mdtp_date_picker_view_animator_dark_theme : R.color.mdtp_date_picker_view_animator;
        view.setBackgroundColor(ContextCompat.getColor(activity, bgColorResource));

        mAnimator = (AccessibleDateAnimator) view.findViewById(R.id.animator);
        mAnimator.addView(mDayPickerView);
        mAnimator.addView(mYearPickerView);
        mAnimator.addView(mMonthPickerView);
        mAnimator.addView(mHourPickerView);
        mAnimator.addView(mMinutePickerView);
        mAnimator.addView(mSecondPickerView);
        mAnimator.setTimeMilis(mInitialTime);
        mAnimator.setDateMillis(mCalendar.getTimeInMillis());
        // TODO: Replace with animation decided upon by the design team.
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIMATION_DURATION);
        mAnimator.setInAnimation(animation);
        // TODO: Replace with animation decided upon by the design team.
        Animation animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(ANIMATION_DURATION);
        mAnimator.setOutAnimation(animation2);

        Button okButton = (Button) view.findViewById(R.id.ok);
        okButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                tryVibrate();
                notifyOnDateListener();
                dismiss();
            }
        });
        okButton.setTypeface(TypefaceHelper.get(activity, "Roboto-Medium"));
        if (mOkString != null) okButton.setText(mOkString);
        else okButton.setText(mOkResid);

        Button cancelButton = (Button) view.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tryVibrate();
                if (getDialog() != null) getDialog().cancel();
            }
        });
        cancelButton.setTypeface(TypefaceHelper.get(activity, "Roboto-Medium"));
        if (mCancelString != null) cancelButton.setText(mCancelString);
        else cancelButton.setText(mCancelResid);
        cancelButton.setVisibility(isCancelable() ? View.VISIBLE : View.GONE);

        // If an accent color has not been set manually, get it from the context
        if (mAccentColor == -1) {
            mAccentColor = Utils.getAccentColorFromThemeIfAvailable(getActivity());
        }
        if (mDayOfWeekView != null)
            mDayOfWeekView.setBackgroundColor(Utils.darkenColor(mAccentColor));
        view.findViewById(R.id.day_picker_selected_date_layout).setBackgroundColor(mAccentColor);
        okButton.setTextColor(mAccentColor);
        cancelButton.setTextColor(mAccentColor);

        if (getDialog() == null) {
            view.findViewById(R.id.done_background).setVisibility(View.GONE);
        }

        updateDisplay(false);
        setCurrentView(currentView);

        if (listPosition != -1) {
            if (currentView == MONTH_AND_DAY_VIEW) {
                mDayPickerView.postSetSelection(listPosition);
            } else if (currentView == MONTH_VIEW) {
                mMonthPickerView.postSetSelectionFromTop(listPosition, listPositionOffset);
            } else if (currentView == YEAR_VIEW) {
                mYearPickerView.postSetSelectionFromTop(listPosition, listPositionOffset);
            } else if(currentView==HOUR_INDEX){
                mHourPickerView.postSetSelectionFromTop(listPosition,listPositionOffset);
            }else if(currentView==MINUTE_INDEX){
                mMinutePickerView.postSetSelectionFromTop(listPosition,listPositionOffset);
            }else if(currentView==SECOND_INDEX){
                mSecondPickerView.postSetSelectionFromTop(listPosition,listPositionOffset);
            }
        }

        mHapticFeedbackController = new HapticFeedbackController(activity);
        return view;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewGroup viewGroup = (ViewGroup) getView();
        if (viewGroup != null) {
            viewGroup.removeAllViewsInLayout();
            View view = onCreateView(getActivity().getLayoutInflater(), viewGroup, null);
            viewGroup.addView(view);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHapticFeedbackController.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHapticFeedbackController.stop();
        if (mDismissOnPause) dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mOnCancelListener != null) mOnCancelListener.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) mOnDismissListener.onDismiss(dialog);
    }
    // Show either Hours or Minutes.

    private void setCurrentView(final int viewIndex) {
        long millis = mCalendar.getTimeInMillis();
        switch (viewIndex) {
            case MONTH_AND_DAY_VIEW:
                ObjectAnimator pulseAnimator = Utils.getPulseAnimator(mDayOfWeekView, 0.9f,
                        1.05f);
                if (mDelayAnimation) {
                    pulseAnimator.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mDayPickerView.onDateChanged();
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.setSelected(true);
                    mSelectedDayTextView.setSelected(true);//tadinya month and day view
                    mSelectedMonthTextView.setSelected(false);
                    mYearView.setSelected(false);
                    mHourView.setSelected(false);
                    mMinuteView.setSelected(false);
                    mSecondView.setSelected(false);
                    mAmPmTextView.setSelected(false);
                    mHourView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mMinuteView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mSecondView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mAnimator.setDisplayedChild(MONTH_AND_DAY_VIEW);
                    mCurrentView = viewIndex;
                }
                pulseAnimator.start();

                int flags = DateUtils.FORMAT_SHOW_DATE;
                String dayString = DateUtils.formatDateTime(getActivity(), millis, flags);
                mAnimator.setContentDescription(mDayPickerDescription + ": " + dayString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectDay);
                break;
            case MONTH_VIEW:
                pulseAnimator = Utils.getPulseAnimator(mSelectedMonthTextView, 0.85f, 1.1f);
                if (mDelayAnimation) {
                    pulseAnimator.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mMonthPickerView.onDateChanged();
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.setSelected(true);
                    mSelectedDayTextView.setSelected(false);
                    mSelectedMonthTextView.setSelected(true);
                    mYearView.setSelected(false);
                    mHourView.setSelected(false);
                    mMinuteView.setSelected(false);
                    mSecondView.setSelected(false);
                    mAmPmTextView.setSelected(false);
                    mHourView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mMinuteView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mSecondView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mAnimator.setDisplayedChild(MONTH_VIEW);
                    mCurrentView = viewIndex;
                }
                pulseAnimator.start();

                CharSequence monthString = MONTH_FORMAT.format(millis);
                mAnimator.setContentDescription(mMonthPickerDescription + ": " + monthString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectMonth);
                break;
            case YEAR_VIEW:
                pulseAnimator = Utils.getPulseAnimator(mYearView, 0.85f, 1.1f);
                if (mDelayAnimation) {
                    pulseAnimator.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mYearPickerView.onDateChanged();
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.setSelected(false);
                    mSelectedDayTextView.setSelected(false);
                    mSelectedMonthTextView.setSelected(false);
                    mYearView.setSelected(true);
                    mHourView.setSelected(false);
                    mMinuteView.setSelected(false);
                    mSecondView.setSelected(false);
                    mAmPmTextView.setSelected(false);
                    mHourView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mMinuteView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mSecondView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mAnimator.setDisplayedChild(YEAR_VIEW);
                    mCurrentView = viewIndex;
                }
                pulseAnimator.start();

                CharSequence yearString = YEAR_FORMAT.format(millis);
                mAnimator.setContentDescription(mYearPickerDescription + ": " + yearString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectYear);
                break;
            case HOUR_INDEX:
                pulseAnimator = Utils.getPulseAnimator(mHourView, 0.85f, 1.1f);
                if (mDelayAnimation) {
                    pulseAnimator.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mHourPickerView.onDateChanged();
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.setSelected(true);
                    mSelectedDayTextView.setSelected(false);
                    mSelectedMonthTextView.setSelected(false);
                    mYearView.setSelected(false);
                    mHourView.setSelected(true);
                    mMinuteView.setSelected(false);
                    mSecondView.setSelected(false);
                    mAmPmTextView.setSelected(false);
                    mHourView.setTextColor(getResources().getColor(R.color.mdtp_white));
                    mMinuteView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mSecondView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mAnimator.setDisplayedChild(HOUR_INDEX);
                    mCurrentView = viewIndex;
                }
                pulseAnimator.start();

                CharSequence hourString = MONTH_FORMAT.format(millis);
                mAnimator.setContentDescription(mHourPickerDescription + ": " + hourString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectHours);
                break;
            case MINUTE_INDEX:
                pulseAnimator = Utils.getPulseAnimator(mMinuteView, 0.85f, 1.1f);
                if (mDelayAnimation) {
                    pulseAnimator.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mMinutePickerView.onDateChanged();
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.setSelected(true);
                    mSelectedDayTextView.setSelected(false);
                    mSelectedMonthTextView.setSelected(false);
                    mYearView.setSelected(false);
                    mHourView.setSelected(false);
                    mMinuteView.setSelected(true);
                    mSecondView.setSelected(false);
                    mAmPmTextView.setSelected(false);
                    mHourView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mMinuteView.setTextColor(getResources().getColor(R.color.mdtp_white));
                    mSecondView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mAnimator.setDisplayedChild(MINUTE_INDEX);
                    mCurrentView = viewIndex;
                }
                pulseAnimator.start();

                CharSequence minuteString = MONTH_FORMAT.format(millis);
                mAnimator.setContentDescription(mMinutePickerDescription + ": " + minuteString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectMinutes);
                break;
            case SECOND_INDEX:
                pulseAnimator = Utils.getPulseAnimator(mSecondView, 0.85f, 1.1f);
                if (mDelayAnimation) {
                    pulseAnimator.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mSecondPickerView.onDateChanged();
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.setSelected(true);
                    mSelectedDayTextView.setSelected(false);
                    mSelectedMonthTextView.setSelected(false);
                    mYearView.setSelected(false);
                    mHourView.setSelected(false);
                    mMinuteView.setSelected(false);
                    mSecondView.setSelected(true);
                    mAmPmTextView.setSelected(false);
                    mHourView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mMinuteView.setTextColor(getResources().getColor(R.color.mdtp_accent_color_focused));
                    mSecondView.setTextColor(getResources().getColor(R.color.mdtp_white));
                    mAnimator.setDisplayedChild(SECOND_INDEX);
                    mCurrentView = viewIndex;
                }
                pulseAnimator.start();

                CharSequence secondString = MONTH_FORMAT.format(millis);
                mAnimator.setContentDescription(mSecondPickerDescription + ": " + secondString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectSeconds);
                break;
        }
    }

    private void updateDisplay(boolean announce) {
        if (mDayOfWeekView != null) {
            if (mTitle != null) mDayOfWeekView.setText(mTitle.toUpperCase(Locale.getDefault()));
            else {
                mDayOfWeekView.setText(mCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
                        Locale.getDefault()).toUpperCase(Locale.getDefault()));
            }
        }

        mSelectedMonthTextView.setText(mCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
                Locale.getDefault()).toUpperCase(Locale.getDefault()));
        mSelectedDayTextView.setText(DAY_FORMAT.format(mCalendar.getTime()));
        mYearView.setText(YEAR_FORMAT.format(mCalendar.getTime()));
        setHour(mInitialTime.getHour());
        setMinute(mInitialTime.getMinute());
        setSecond(mInitialTime.getSecond());
        //mHourView.setText(HOUR_FORMAT.format(mCalendar.getTime()));


        // Accessibility.
        long millis = mCalendar.getTimeInMillis();
        mAnimator.setDateMillis(millis);
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR;
        String monthAndDayText = DateUtils.formatDateTime(getActivity(), millis, flags);
        mDayOfWeekView.setContentDescription(monthAndDayText);//tadinya month and day view

        if (announce) {
            flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
            String fullDateText = DateUtils.formatDateTime(getActivity(), millis, flags);
            Utils.tryAccessibilityAnnounce(mAnimator, fullDateText);
        }
    }

    /**
     * Set whether the device should vibrate when touching fields
     *
     * @param vibrate true if the device should vibrate when touching a field
     */
    public void vibrate(boolean vibrate) {
        mVibrate = vibrate;
    }

    /**
     * Set whether the picker should dismiss itself when being paused or whether it should try to survive an orientation change
     *
     * @param dismissOnPause true if the dialog should dismiss itself when it's pausing
     */
    public void dismissOnPause(boolean dismissOnPause) {
        mDismissOnPause = dismissOnPause;
    }

    /**
     * Set whether the picker should dismiss itself when a day is selected
     *
     * @param autoDismiss true if the dialog should dismiss itself when a day is selected
     */
    @SuppressWarnings("unused")
    public void autoDismiss(boolean autoDismiss) {
        mAutoDismiss = autoDismiss;
    }

    /**
     * Set whether the dark theme should be used
     *
     * @param themeDark true if the dark theme should be used, false if the default theme should be used
     */
    public void setThemeDark(boolean themeDark) {
        mThemeDark = themeDark;
        mThemeDarkChanged = true;
    }

    /**
     * Returns true when the dark theme should be used
     *
     * @return true if the dark theme should be used, false if the default theme should be used
     */
    @Override
    public boolean isThemeDark() {
        return mThemeDark;
    }

    /**
     * Set the accent color of this dialog
     *
     * @param color the accent color you want
     */
    @SuppressWarnings("unused")
    public void setAccentColor(String color) {
        mAccentColor = Color.parseColor(color);
    }

    /**
     * Set the accent color of this dialog
     *
     * @param color the accent color you want
     */
    public void setAccentColor(@ColorInt int color) {
        mAccentColor = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Get the accent color of this dialog
     *
     * @return accent color
     */
    @Override
    public int getAccentColor() {
        return mAccentColor;
    }

    /**
     * Set whether the year picker of the month and day picker is shown first
     *
     * @param yearPicker boolean
     */
    public void showYearPickerFirst(boolean yearPicker) {
        mDefaultView = yearPicker ? YEAR_VIEW : MONTH_AND_DAY_VIEW;
    }

    public void showMonthPickerFirst(boolean monthPicker) {
        mDefaultView = monthPicker ? MONTH_VIEW : MONTH_AND_DAY_VIEW;
    }
    public  void dateOnly(boolean dateonly){
        mOnlyDate = dateonly;
    }
    @SuppressWarnings("unused")
    public void setFirstDayOfWeek(int startOfWeek) {
        if (startOfWeek < Calendar.SUNDAY || startOfWeek > Calendar.SATURDAY) {
            throw new IllegalArgumentException("Value must be between Calendar.SUNDAY and " +
                    "Calendar.SATURDAY");
        }
        mWeekStart = startOfWeek;
        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }
    }

    @SuppressWarnings("unused")
    public void setYearRange(int startYear, int endYear) {
        if (endYear < startYear) {
            throw new IllegalArgumentException("Year end must be larger than or equal to year start");
        }

        mMinYear = startYear;
        mMaxYear = endYear;
        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }
    }

    /**
     * Sets the minimal date supported by this DatePicker. Dates before (but not including) the
     * specified date will be disallowed from being selected.
     *
     * @param calendar a Calendar object set to the year, month, day desired as the mindate.
     */
    @SuppressWarnings("unused")
    public void setMinDate(Calendar calendar) {
        mMinDate = trimToMidnight(calendar);

        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }
    }

    /**
     * @return The minimal date supported by this DatePicker. Null if it has not been set.
     */
    @SuppressWarnings("unused")
    public Calendar getMinDate() {
        return mMinDate;
    }

    /**
     * Sets the minimal date supported by this DatePicker. Dates after (but not including) the
     * specified date will be disallowed from being selected.
     *
     * @param calendar a Calendar object set to the year, month, day desired as the maxdate.
     */
    @SuppressWarnings("unused")
    public void setMaxDate(Calendar calendar) {
        mMaxDate = trimToMidnight(calendar);

        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }
    }

    /**
     * @return The maximal date supported by this DatePicker. Null if it has not been set.
     */
    @SuppressWarnings("unused")
    public Calendar getMaxDate() {
        return mMaxDate;
    }

    /**
     * Sets an array of dates which should be highlighted when the picker is drawn
     *
     * @param highlightedDays an Array of Calendar objects containing the dates to be highlighted
     */
    @SuppressWarnings("unused")
    public void setHighlightedDays(Calendar[] highlightedDays) {
        Arrays.sort(highlightedDays);
        for (Calendar highlightedDay : highlightedDays) trimToMidnight(highlightedDay);
        this.highlightedDays = highlightedDays;
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    /**
     * @return The list of dates, as Calendar Objects, which should be highlighted. null is no dates should be highlighted
     */
    @Override
    public Calendar[] getHighlightedDays() {
        return highlightedDays;
    }

    /**
     * Sets a list of days which are the only valid selections.
     * Setting this value will take precedence over using setMinDate() and setMaxDate()
     *
     * @param selectableDays an Array of Calendar Objects containing the selectable dates
     */
    @SuppressWarnings("unused")
    public void setSelectableDays(Calendar[] selectableDays) {
        Arrays.sort(selectableDays);
        for (Calendar selectableDay : selectableDays) trimToMidnight(selectableDay);
        this.selectableDays = selectableDays;
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    /**
     * @return an Array of Calendar objects containing the list with selectable items. null if no restriction is set
     */
    @SuppressWarnings("unused")
    public Calendar[] getSelectableDays() {
        return selectableDays;
    }

    /**
     * Sets a list of days that are not selectable in the picker
     * Setting this value will take precedence over using setMinDate() and setMaxDate(), but stacks with setSelectableDays()
     *
     * @param disabledDays an Array of Calendar Objects containing the disabled dates
     */
    @SuppressWarnings("unused")
    public void setDisabledDays(Calendar[] disabledDays) {
        Arrays.sort(disabledDays);
        for (Calendar disabledDay : disabledDays) trimToMidnight(disabledDay);
        this.disabledDays = disabledDays;
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    /**
     * @return an Array of Calendar objects containing the list of days that are not selectable. null if no restriction is set
     */
    @SuppressWarnings("unused")
    public Calendar[] getDisabledDays() {
        return disabledDays;
    }

    /**
     * Set a title to be displayed instead of the weekday
     *
     * @param title String - The title to be displayed
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * Set the label for the Ok button (max 12 characters)
     *
     * @param okString A literal String to be used as the Ok button label
     */
    @SuppressWarnings("unused")
    public void setOkText(String okString) {
        mOkString = okString;
    }

    /**
     * Set the label for the Ok button (max 12 characters)
     *
     * @param okResid A resource ID to be used as the Ok button label
     */
    @SuppressWarnings("unused")
    public void setOkText(@StringRes int okResid) {
        mOkString = null;
        mOkResid = okResid;
    }

    /**
     * Set the label for the Cancel button (max 12 characters)
     *
     * @param cancelString A literal String to be used as the Cancel button label
     */
    @SuppressWarnings("unused")
    public void setCancelText(String cancelString) {
        mCancelString = cancelString;
    }

    /**
     * Set the label for the Cancel button (max 12 characters)
     *
     * @param cancelResid A resource ID to be used as the Cancel button label
     */
    @SuppressWarnings("unused")
    public void setCancelText(@StringRes int cancelResid) {
        mCancelString = null;
        mCancelResid = cancelResid;
    }

    @SuppressWarnings("unused")
    public void setOnDateSetListener(OnDateSetListener listener) {
        mCallBack = listener;
    }

    @SuppressWarnings("unused")
    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
    }

    @SuppressWarnings("unused")
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    // If the newly selected month / year does not contain the currently selected day number,
    // change the selected day number to the last day of the selected month or year.
    //      e.g. Switching from Mar to Apr when Mar 31 is selected -> Apr 30
    //      e.g. Switching from 2012 to 2013 when Feb 29, 2012 is selected -> Feb 28, 2013
    private void adjustDayInMonthIfNeeded(Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (day > daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, daysInMonth);
        }
        setToNearestDate(calendar);
    }

    @Override
    public void onClick(View v) {
        tryVibrate();
        if (v.getId() == R.id.date_picker_year) {
            setCurrentView(YEAR_VIEW);
        } else if (v.getId() == R.id.date_picker_day) {
            setCurrentView(MONTH_AND_DAY_VIEW);
        } else if (v.getId() == R.id.date_picker_month) {
            setCurrentView(MONTH_VIEW);
        }else if(v.getId()==R.id.hours){
            setCurrentView(HOUR_INDEX);
        }else if(v.getId()==R.id.minutes){
            setCurrentView(MINUTE_INDEX);
        }else if(v.getId()==R.id.seconds){
            setCurrentView(SECOND_INDEX);
        }else if(v.getId()==R.id.ampm_label){
            int apm;
            if(getIsCurrentlyAmOrPm()==AM){
                apm=PM;
            }else{
                apm=AM;
            }
            updateAmPmDisplay(apm);

        }
    }

    @Override
    public void onYearSelected(int year) {
        mCalendar.set(Calendar.YEAR, year);
        adjustDayInMonthIfNeeded(mCalendar);
        updatePickers();
        setCurrentView(MONTH_AND_DAY_VIEW);
        updateDisplay(true);
    }

    @Override
    public void onTimeSelected(int value,String type) {
        switch (type.toUpperCase()){
            case "HOUR":
                mInitialTime = new Timepoint(value, mInitialTime.getMinute(), mInitialTime.getSecond());
                break;
            case  "MINUTE":
                mInitialTime = new Timepoint(mInitialTime.getHour(), value, mInitialTime.getSecond());
                break;
            case "SECOND":
                mInitialTime = new Timepoint(mInitialTime.getHour(), mInitialTime.getMinute(), value);
                break;
            default:
                mInitialTime = new Timepoint(value, mInitialTime.getMinute(), mInitialTime.getSecond());
                break;
        }


        updatePickers();
        setCurrentView(MONTH_AND_DAY_VIEW);
        updateDisplay(true);
    }
    @Override
    public void onMonthSelected(int month) {
        mCalendar.set(Calendar.MONTH, month);
        adjustDayInMonthIfNeeded(mCalendar);
        updatePickers();
        setCurrentView(MONTH_AND_DAY_VIEW);
        updateDisplay(true);
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        updatePickers();
        updateDisplay(true);
        if (mAutoDismiss) {
            notifyOnDateListener();
            dismiss();
        }
    }

    private void updatePickers() {
        for (OnDateChangedListener listener : mListeners) listener.onDateChanged();
    }


    @Override
    public MonthAdapter.CalendarDay getSelectedDay() {
        return new MonthAdapter.CalendarDay(mCalendar,mInitialTime);
    }

    @Override
    public Calendar getStartDate() {
        if (selectableDays != null) return selectableDays[0];
        if (mMinDate != null) return mMinDate;
        Calendar output = Calendar.getInstance();
        output.set(Calendar.YEAR, mMinYear);
        output.set(Calendar.DAY_OF_MONTH, 1);
        output.set(Calendar.MONTH, Calendar.JANUARY);
        return output;
    }

    @Override
    public Calendar getEndDate() {
        if (selectableDays != null) return selectableDays[selectableDays.length - 1];
        if (mMaxDate != null) return mMaxDate;
        Calendar output = Calendar.getInstance();
        output.set(Calendar.YEAR, mMaxYear);
        output.set(Calendar.DAY_OF_MONTH, 31);
        output.set(Calendar.MONTH, Calendar.DECEMBER);
        return output;
    }

    @Override
    public int getMinYear() {
        if (selectableDays != null) return selectableDays[0].get(Calendar.YEAR);
        // Ensure no years can be selected outside of the given minimum date
        return mMinDate != null && mMinDate.get(Calendar.YEAR) > mMinYear ? mMinDate.get(Calendar.YEAR) : mMinYear;
    }

    @Override
    public int getMaxYear() {
        if (selectableDays != null)
            return selectableDays[selectableDays.length - 1].get(Calendar.YEAR);
        // Ensure no years can be selected outside of the given maximum date
        return mMaxDate != null && mMaxDate.get(Calendar.YEAR) < mMaxYear ? mMaxDate.get(Calendar.YEAR) : mMaxYear;
    }

    @Override
    public int getMinMonth() {
        if (selectableDays != null) return selectableDays[0].get(Calendar.MONTH);
        // Ensure no years can be selected outside of the given minimum date
        return mMinDate != null && mMinDate.get(Calendar.MONTH) > 0 ? mMinDate.get(Calendar.MONTH) : 0;
        //return  mMinDate!=null?mMinDate.get(Calendar.MONTH):mCalendar.get(Calendar.MONTH) ;
    }

    @Override
    public int getMaxMonth() {
        if (selectableDays != null)
            return selectableDays[selectableDays.length - 1].get(Calendar.MONTH);
        // Ensure no years can be selected outside of the given maximum date
        String[] months = new DateFormatSymbols().getMonths();
        return mMaxDate != null && mMaxDate.get(Calendar.MONTH) < months.length-1 ? mMaxDate.get(Calendar.MONTH) : months.length-1;
    }
    /**
     * @return true if the specified year/month/day are within the selectable days or the range set by minDate and maxDate.
     * If one or either have not been set, they are considered as Integer.MIN_VALUE and
     * Integer.MAX_VALUE.
     */
    @Override
    public boolean isOutOfRange(int year, int month, int day) {
        return isDisabled(year, month, day) || !isSelectable(year, month, day);
    }

    @SuppressWarnings("unused")
    public boolean isOutOfRange(Calendar calendar) {
        return isOutOfRange(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private boolean isDisabled(int year, int month, int day) {
        return containsDate(disabledDays, year, month, day) || isBeforeMin(year, month, day) || isAfterMax(year, month, day);
    }

    private boolean isDisabled(Calendar c) {
        return isDisabled(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }

    private boolean isSelectable(int year, int month, int day) {
        return selectableDays == null || containsDate(selectableDays, year, month, day);
    }

    /**
     * Checks whether the given data is contained in an array of dates
     *
     * @param dates Calendar[] which contents we want to search
     * @param year  the year as an int
     * @param month the month as an int
     * @param day   the day as an int
     * @return true if the data is present in the array
     */
    private boolean containsDate(Calendar[] dates, int year, int month, int day) {
        if (dates == null) return false;
        for (Calendar c : dates) {
            if (year < c.get(Calendar.YEAR)) break;
            if (year > c.get(Calendar.YEAR)) continue;
            if (month < c.get(Calendar.MONTH)) break;
            if (month > c.get(Calendar.MONTH)) continue;
            if (day < c.get(Calendar.DAY_OF_MONTH)) break;
            if (day > c.get(Calendar.DAY_OF_MONTH)) continue;
            return true;
        }
        return false;
    }

    private boolean isBeforeMin(int year, int month, int day) {
        if (mMinDate == null) {
            return false;
        }

        if (year < mMinDate.get(Calendar.YEAR)) {
            return true;
        } else if (year > mMinDate.get(Calendar.YEAR)) {
            return false;
        }

        if (month < mMinDate.get(Calendar.MONTH)) {
            return true;
        } else if (month > mMinDate.get(Calendar.MONTH)) {
            return false;
        }

        if (day < mMinDate.get(Calendar.DAY_OF_MONTH)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isBeforeMin(Calendar calendar) {
        return isBeforeMin(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private boolean isAfterMax(int year, int month, int day) {
        if (mMaxDate == null) {
            return false;
        }

        if (year > mMaxDate.get(Calendar.YEAR)) {
            return true;
        } else if (year < mMaxDate.get(Calendar.YEAR)) {
            return false;
        }

        if (month > mMaxDate.get(Calendar.MONTH)) {
            return true;
        } else if (month < mMaxDate.get(Calendar.MONTH)) {
            return false;
        }

        if (day > mMaxDate.get(Calendar.DAY_OF_MONTH)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isAfterMax(Calendar calendar) {
        return isAfterMax(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private void setToNearestDate(Calendar calendar) {
        if (selectableDays != null) {
            long distance = Long.MAX_VALUE;
            Calendar currentBest = calendar;
            for (Calendar c : selectableDays) {
                long newDistance = Math.abs(calendar.getTimeInMillis() - c.getTimeInMillis());
                if (newDistance < distance && !isDisabled(c)) {
                    distance = newDistance;
                    currentBest = c;
                } else break;
            }
            calendar.setTimeInMillis(currentBest.getTimeInMillis());
            return;
        }

        if (disabledDays != null) {
            Calendar forwardDate = (Calendar) calendar.clone();
            Calendar backwardDate = (Calendar) calendar.clone();
            while (isDisabled(forwardDate) && isDisabled(backwardDate)) {
                forwardDate.add(Calendar.DAY_OF_MONTH, 1);
                backwardDate.add(Calendar.DAY_OF_MONTH, -1);
            }
            if (!isDisabled(backwardDate)) {
                calendar.setTimeInMillis(backwardDate.getTimeInMillis());
                return;
            }
            if (!isDisabled(forwardDate)) {
                calendar.setTimeInMillis(forwardDate.getTimeInMillis());
                return;
            }
        }


        if (isBeforeMin(calendar)) {
            calendar.setTimeInMillis(mMinDate.getTimeInMillis());
            return;
        }

        if (isAfterMax(calendar)) {
            calendar.setTimeInMillis(mMaxDate.getTimeInMillis());
            return;
        }
    }

    /**
     * Trims off all time information, effectively setting it to midnight
     * Makes it easier to compare at just the day level
     *
     * @param calendar The Calendar object to trim
     * @return The trimmed Calendar object
     */
    private Calendar trimToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    @Override
    public int getFirstDayOfWeek() {
        return mWeekStart;
    }

    @Override
    public void registerOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void tryVibrate() {
        if (mVibrate) mHapticFeedbackController.tryVibrate();
    }

    public void notifyOnDateListener() {
        if (mCallBack != null) {
            mCallBack.onDateSet(DatePickerDialog.this, mCalendar.get(Calendar.YEAR),
                    mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH),
                    mInitialTime.getHour(),mInitialTime.getMinute(),mInitialTime.getSecond());
        }
    }


    /**
     * Set whether an additional picker for seconds should be shown
     * Will enable minutes picker as well if seconds picker should be shown
     * @param enableSeconds true if the seconds picker should be shown
     */
    public void enableSeconds(boolean enableSeconds) {
        if (enableSeconds) mEnableMinutes = true;
        mEnableSeconds = enableSeconds;
    }

    /**
     * Set whether the picker for minutes should be shown
     * Will disable seconds if minutes are disbled
     * @param enableMinutes true if minutes picker should be shown
     */
    public void enableMinutes(boolean enableMinutes) {
        if (!enableMinutes) mEnableSeconds = false;
        mEnableMinutes = enableMinutes;
    }

    private void setHour(int value) {
        String format;
        if (mIs24HourMode) {
            format = "%02d";
        } else {
            format = "%d";
            value = value % 12;
            if (value == 0) {
                value = 12;
            }
        }

        CharSequence text = String.format(format, value);
        mHourView.setText(text);
        mHourSpaceView.setText(text);

    }

    private void setMinute(int value) {
        if (value == 60) {
            value = 0;
        }
        CharSequence text = String.format(Locale.getDefault(), "%02d", value);
        mMinuteView.setText(text);
        mMinuteSpaceView.setText(text);
    }

    private void setSecond(int value) {
        if(value == 60) {
            value = 0;
        }
        CharSequence text = String.format(Locale.getDefault(), "%02d", value);
        mSecondView.setText(text);
        mSecondSpaceView.setText(text);
    }

    private void updateAmPmDisplay(int amOrPm) {
        if (amOrPm == AM) {
            mAmPmTextView.setText(mAmText);
            mAmPmHitspace.setContentDescription(mAmText);
        } else if (amOrPm == PM){
            mAmPmTextView.setText(mPmText);
            mAmPmHitspace.setContentDescription(mPmText);
        } else {
            mAmPmTextView.setText(mDoublePlaceholderText);
        }
        setAmOrPm(amOrPm);
    }

   public int getIsCurrentlyAmOrPm() {
       if (mInitialTime.isAM()) {
           return AM;
       } else if (mInitialTime.isPM()) {
           return PM;
       }
       return -1;
   }
    public void setAmOrPm(int amOrPm) {
        Timepoint newSelection = new Timepoint(mInitialTime);
        if(amOrPm == AM) newSelection.setAM();
        else if(amOrPm == PM) newSelection.setPM();
        mInitialTime = newSelection;

    }

}
