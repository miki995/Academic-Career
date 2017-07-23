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

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.miroslav.android.courses.data.CourseContract;
import com.miroslav.android.courses.data.CourseDbHelper;

import static com.miroslav.android.courses.data.CourseContract.CoursesEntry._ID;

/**
 * Displays list of courses that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the course data loader
     */
    private static final int COURSE_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    private CourseCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
                updateAverage();
            }
        });

        updateAverage();
        // Find the ListView which will be populated with the course data
        ListView courseListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        courseListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of course data in the Cursor.
        // There is no course data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new CourseCursorAdapter(this);
        courseListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        courseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific course that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link CoursesEntry#CONTENT_URI}.
                // For miroslav, the URI would be "content://com.miroslav.android.courses/courses/2"
                // if the course with ID 2 was clicked on.
                Uri currentCourseUri = ContentUris.withAppendedId(CourseContract.CoursesEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentCourseUri);
                updateAverage();


                // Launch the {@link EditorActivity} to display the data for the current course.
                startActivity(intent);

            }
        });

        // Kick off the loader

        getLoaderManager().initLoader(COURSE_LOADER, null, this);


    }

    private void updateAverage() {
        TextView averageGrade = findViewById(R.id.grade);
        TextView sumCredits = findViewById(R.id.credit);

        TextView averageGradeLabel = findViewById(R.id.grade_label);
        TextView sumCreditsLabel = findViewById(R.id.credit_label);


        CourseDbHelper db = new CourseDbHelper(this);
        SQLiteDatabase sqLiteDatabase = db.getReadableDatabase();

        String query = "SELECT AVG(" + CourseContract.CoursesEntry.COLUMN_COURSE_GRADE + "),"
                + "SUM(" + CourseContract.CoursesEntry.COLUMN_COURSE_CREDIT + ")"
                + "FROM " + CourseContract.CoursesEntry.TABLE_NAME + ";";


        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {

            String courseAverageGrade = cursor.getString(0);
            String courseCreditsSum = cursor.getString(1);

            averageGradeLabel.setText(courseAverageGrade);
            sumCreditsLabel.setText(courseCreditsSum);

            averageGrade.setText(R.string.average_quotation);
            sumCredits.setText(R.string.sum_quotation);
        }


        cursor.close();
    }

    /**
     * Helper method to delete all courses in the database.
     */
    private void deleteAllCourses() {
        int rowsDeleted = getContentResolver().delete(CourseContract.CoursesEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from course database");
        updateAverage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);


        // Return true to display menu
        return true;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msga);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the course.
               deleteAllCourses();
            }
        });


        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the course.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            case R.id.contact_developer:
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mirmaximus.wixsite.com/maximus"));
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.contact_info,  Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                updateAverage();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                _ID,
                CourseContract.CoursesEntry.COLUMN_COURSE_NAME,
                CourseContract.CoursesEntry.COLUMN_COURSE_CREDIT,
                CourseContract.CoursesEntry.COLUMN_COURSE_GRADE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                CourseContract.CoursesEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link CourseCursorAdapter} with this new cursor containing updated course data
        mCursorAdapter.swapCursor(data);
        updateAverage();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
        updateAverage();
    }
}
