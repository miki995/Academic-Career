/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.miroslav.android.courses;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.miroslav.android.courses.data.CourseContract.CoursesEntry;

/**
 * {@link CourseCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of course data as its data source. This adapter knows
 * how to create list items for each row of course data in the {@link Cursor}.
 */
class CourseCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link CourseCursorAdapter}.
     *
     * @param context The context
     */
    public CourseCursorAdapter(Context context) {
        super(context, null, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the course data (in the current row pointed to by cursor) to the given
     * list item layout. For miroslav, the name for the current course can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.name);
        TextView creditTextView = view.findViewById(R.id.credit);
        TextView gradeTextView = view.findViewById(R.id.grade);


        // Find the columns of course attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(CoursesEntry.COLUMN_COURSE_NAME);
        int creditColumnIndex = cursor.getColumnIndex(CoursesEntry.COLUMN_COURSE_CREDIT);
        int gradeColumnIndex = cursor.getColumnIndex(CoursesEntry.COLUMN_COURSE_GRADE);

        // Read the course attributes from the Cursor for the current course
        String courseName = cursor.getString(nameColumnIndex);
        String courseCredit = cursor.getString(creditColumnIndex) + " " + context.getString(R.string.credits);
        String courseGrade = cursor.getString(gradeColumnIndex);


        // Update the TextViews with the attributes for the current course
        nameTextView.setText(courseName);
        creditTextView.setText(courseCredit);
        gradeTextView.setText(courseGrade);

    }


}
