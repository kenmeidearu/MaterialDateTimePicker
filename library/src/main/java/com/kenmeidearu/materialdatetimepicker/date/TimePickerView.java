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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kenmeidearu.materialdatetimepicker.R;
import com.kenmeidearu.materialdatetimepicker.date.DatePickerDialog.OnDateChangedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a selectable list of years.
 */
public class TimePickerView extends ListView implements OnItemClickListener, OnDateChangedListener {
    private static final String TAG = "TimePickerView";

    private final DatePickerController mController;
    private TimeAdapter mAdapter;
    private int mViewSize;
    private int mChildSize;
    private String typeControl;
    boolean AMPM;
    private TextViewWithCircularIndicator mSelectedView;

    /**
     * @param context
     */
    public TimePickerView(Context context, DatePickerController controller, String tc, boolean AmPM) {
        super(context);
        typeControl=tc;
        AMPM=AmPM;

        mController = controller;
        mController.registerOnDateChangedListener(this);
        ViewGroup.LayoutParams frame = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        setLayoutParams(frame);
        Resources res = context.getResources();
        mViewSize = res.getDimensionPixelOffset(R.dimen.mdtp_date_picker_view_animator_height);
        mChildSize = res.getDimensionPixelOffset(R.dimen.mdtp_year_label_height);
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(mChildSize / 3);
        init(context);
        setOnItemClickListener(this);
        setSelector(new StateListDrawable());
        setDividerHeight(0);
        onDateChanged();
    }

    private void init(Context context) {
        ArrayList<String> datatime = new ArrayList<>();
        if(!AMPM){
            switch (typeControl.toUpperCase()){
                case "HOUR":
                    for (int a = 1; a <= 12; a++) {
                        datatime.add(String.format(Locale.getDefault(),"%d", a));
                    }
                    break;
                case "MINUTE":
                    for (int a = 0; a <= 59; a++) {
                        if(a<10){
                            datatime.add("0"+String.format(Locale.getDefault(),"%d", a));
                        }else{
                            datatime.add(String.format(Locale.getDefault(),"%d", a));
                        }
                    }
                    break;
                case "SECOND":
                    for (int a = 0; a <= 59; a++) {
                        if(a<10){
                            datatime.add("0"+String.format(Locale.getDefault(),"%d", a));
                        }else{
                            datatime.add(String.format(Locale.getDefault(),"%d", a));
                        }
                    }
                    break;
            }
        }else{
            switch (typeControl.toUpperCase()){
                case "HOUR":
                    for (int a = 0; a <= 23; a++) {
                        if(a<10){
                            datatime.add("0"+String.format(Locale.getDefault(),"%d", a));
                        }else{
                            datatime.add(String.format(Locale.getDefault(),"%d", a));
                        }

                    }
                    break;
                case "MINUTE":
                    for (int a = 0; a <= 59; a++) {
                        if(a<10){
                            datatime.add("0"+String.format(Locale.getDefault(),"%d", a));
                        }else{
                            datatime.add(String.format(Locale.getDefault(),"%d", a));
                        }
                    }
                    break;
                case "SECOND":
                    for (int a = 0; a <= 59; a++) {
                        if(a<10){
                            datatime.add("0"+String.format(Locale.getDefault(),"%d", a));
                        }else{
                            datatime.add(String.format(Locale.getDefault(),"%d", a));
                        }
                    }
                    break;
            }
        }

        mAdapter = new TimeAdapter(context, R.layout.mdtp_year_label_text_view, datatime);
        setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mController.tryVibrate();
        TextViewWithCircularIndicator clickedView = (TextViewWithCircularIndicator) view;
        if (clickedView != null) {
            if (clickedView != mSelectedView) {
                if (mSelectedView != null) {
                    mSelectedView.drawIndicator(false);
                    mSelectedView.requestLayout();
                }
                clickedView.drawIndicator(true);
                clickedView.requestLayout();
                mSelectedView = clickedView;
            }
            mController.onTimeSelected(getTimeFromTextView(clickedView),typeControl.toUpperCase());
            mAdapter.notifyDataSetChanged();
        }
    }

    private static int getTimeFromTextView(TextView view) {
        return Integer.valueOf(view.getText().toString());
    }

    private class TimeAdapter extends ArrayAdapter<String> {

        public TimeAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextViewWithCircularIndicator v = (TextViewWithCircularIndicator)
                    super.getView(position, convertView, parent);
            v.setAccentColor(mController.getAccentColor(), mController.isThemeDark());
            v.requestLayout();
            int time = getTimeFromTextView(v);
            //Log.e("isi time","isi"+time);
            boolean selected=false;
            switch (typeControl.toUpperCase()){
                case "HOUR":
                    if(!AMPM) {
                        selected = mController.getSelectedDay().getHour()%12 == time;
                    }else{
                        selected = mController.getSelectedDay().getHour() == time;
                    }
                    break;
                case "MINUTE":
                    selected =mController.getSelectedDay().getMinute()== time;
                    break;
                case "SECOND":
                    selected =mController.getSelectedDay().getSecond()== time;
                    break;
            }
            v.drawIndicator(selected);
            if (selected) {
                mSelectedView = v;
            }
           // Log.e("isi hour",time+","+mController.getSelectedDay().getHour()+","+mController.getSelectedDay().getHour()%12);
            return v;
        }
    }

    public void postSetSelectionCentered(final int position) {
        postSetSelectionFromTop(position, mViewSize / 2 - mChildSize / 2);
    }

    public void postSetSelectionFromTop(final int position, final int offset) {
        post(new Runnable() {

            @Override
            public void run() {
                setSelectionFromTop(position, offset);
                requestLayout();
            }
        });
    }

    public int getFirstPositionOffset() {
        final View firstChild = getChildAt(0);
        if (firstChild == null) {
            return 0;
        }
        return firstChild.getTop();
    }

    @Override
    public void onDateChanged() {
        mAdapter.notifyDataSetChanged();
        int rubah=0;

            switch (typeControl.toUpperCase()){
                case "HOUR":
                    if(!AMPM) {
                        rubah = mController.getSelectedDay().getHour()%12;
                    }else{
                        rubah = mController.getSelectedDay().getHour();
                    }
                    break;
                case "MINUTE":
                    rubah=mController.getSelectedDay().getMinute();
                    break;
                case "SECOND":
                    rubah=mController.getSelectedDay().getSecond();
                    break;
            }

        Log.e("isi dari atas"," sisinya: "+rubah);
        postSetSelectionCentered(rubah);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            event.setFromIndex(0);
            event.setToIndex(0);
        }
    }
}
