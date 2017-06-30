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

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Displays a selectable list of years.
 */
public class MonthPickerView extends ListView implements OnItemClickListener, OnDateChangedListener {
    private static final String TAG = "MonthPickerView";

    private final DatePickerController mController;
    private MonthAdapter mAdapter;
    private int mViewSize;
    private int mChildSize;
    private Calendar minDate;
    private Calendar maxDate;
    private  TextView mSelectedViewT;
    private Context ctx;
    //private TextViewWithCircularIndicator mSelectedView;


    /**
     * @param context
     */
    public MonthPickerView(Context context, DatePickerController controller) {
        super(context);
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
        this.ctx=context;
        init();
        setOnItemClickListener(this);
        setSelector(new StateListDrawable());
        setDividerHeight(0);
        onDateChanged();
    }

    public void init() {
        String[] months = new DateFormatSymbols().getMonths();
        ArrayList<String> monthsList = new ArrayList<>();
        int awalBulan,akhirBulan;
        if(mController.getMinYear()>=mController.getCurrentYear()){
            awalBulan=mController.getMinMonth();
        }else{
            awalBulan=0;
        }
        if(mController.getMaxYear()<=mController.getCurrentYear()){
            akhirBulan=mController.getMaxMonth();
        }else{
            akhirBulan=11;
        }
        
        for (int month = awalBulan; month <= akhirBulan; month++) {
            monthsList .add(months[month]);
        }
        mAdapter = new MonthAdapter(ctx, R.layout.mdtp_year_label_text_view, monthsList);
        setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mController.tryVibrate();
        TextView clickedView=(TextView)view;
        if(clickedView!=null){
            mController.onMonthSelected(getMonthFromTextView(clickedView));
            mAdapter.notifyDataSetChanged();
        }

    }

    private static int getMonthFromTextView(TextView view) {
        String[] months = new DateFormatSymbols().getMonths();
        int index = -1;
        for (int i=0;i<months.length;i++) {
            if (months[i].equals(view.getText().toString())) {
                index = i;
                break;
            }
        }
       // Log.e("isi index",index+","+view.getText().toString());
        return index;// Integer.valueOf(view.getText().toString());

    }

    private class MonthAdapter extends ArrayAdapter<String> {

        public MonthAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView v=(TextView)super.getView(position, convertView, parent);
            int month = getMonthFromTextView(v);
            boolean selected = mController.getSelectedDay().month == month;
           // v.drawIndicator(selected);
           // Log.e("selected",String.valueOf(selected)+":"+month+","+mController.getSelectedDay().month+"-"+mController.getAccentColor());
            if (selected) {
                //int warna=mController.getAccentColor();
                v.setBackgroundResource(R.color.mdtp_accent_color);
                mSelectedViewT = v;
            }else{
                v.setBackgroundResource(0);
            }
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
        postSetSelectionCentered(mController.getSelectedDay().month);
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
