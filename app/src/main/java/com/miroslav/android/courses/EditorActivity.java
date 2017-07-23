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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.miroslav.android.courses.data.CourseContract;
import com.miroslav.android.courses.data.CourseContract.CoursesEntry;

/**
 * Allows user to create a new course or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Identifier for the course data loader
     */
    private static final int EXISTING_COURSE_LOADER = 0;

    /**
     * Content URI for the existing course (null if it's a new course)
     */
    private Uri mCurrentCourseUri;

    /**
     * EditText field to enter the course's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the course's breed
     */
    private EditText mCreditsEditText;

    /**
     * EditText field to enter the course's weight
     */
    private EditText mGradeEditText;


    /**
     * Boolean flag that keeps track of whether the course has been edited (true) or not (false)
     */
    private boolean mCourseHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mcourseHasChanged boolean to true.
     */
    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCourseHasChanged = true;
            return false;
        }
    };


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new course or editing an existing one.
        Intent intent = getIntent();
        mCurrentCourseUri = intent.getData();

        // If the intent DOES NOT contain a course content URI, then we know that we are
        // creating a new course.
        if (mCurrentCourseUri == null) {
            // This is a new course, so change the app bar to say "Add a course"
            setTitle(getString(R.string.editor_activity_title_new_course));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a course that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing course, so change app bar to say "Edit course"
            setTitle(getString(R.string.editor_activity_title_edit_course));

            // Initialize a loader to read the course data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_COURSE_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_course_name);
        mCreditsEditText = findViewById(R.id.edit_course_credit);
        mGradeEditText = findViewById(R.id.edit_course_grade);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mCreditsEditText.setOnTouchListener(mTouchListener);
        mGradeEditText.setOnTouchListener(mTouchListener);


    }


    /**
     * Get user input from editor and save course into database.
     */
    private void saveCourse() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String creditString = mCreditsEditText.getText().toString().trim();
        String gradeString = mGradeEditText.getText().toString().trim();


        // Check if this is supposed to be a new course
        // and check if all the fields in the editor are blank
        if (mCurrentCourseUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(creditString) &&
                TextUtils.isEmpty(gradeString)) {
            // Since no fields were modified, we can return early without creating a new course.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and course attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(CoursesEntry.COLUMN_COURSE_NAME, nameString);
        values.put(CoursesEntry.COLUMN_COURSE_CREDIT, creditString);
        values.put(CoursesEntry.COLUMN_COURSE_GRADE, gradeString);

        Uri newUri = null;

        // Determine if this is a new or existing course by checking if mCurrentcourseUri is null or not
        if (mCurrentCourseUri == null) {
            // This is a NEW course, so insert a new course into the provider,
            // returning the content URI for the new course.

            try {
                newUri = getContentResolver().insert(CourseContract.CoursesEntry.CONTENT_URI, values);

            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_course_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_course_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = 0;
            // Otherwise this is an EXISTING course, so update the course with content URI: mCurrentcourseUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentcourseUri will already identify the correct row in the database that
            // we want to modify.
            try {
                rowsAffected = getContentResolver().update(mCurrentCourseUri, values, null, null);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_course_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_course_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new course, hide the "Delete" menu item.
        if (mCurrentCourseUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save course to database
                saveCourse();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the course hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mCourseHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the course hasn't changed, continue with handling back button press
        if (!mCourseHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all course attributes, define a projection that contains
        // all columns from the course table
        String[] projection = {
                CoursesEntry._ID,
                CourseContract.CoursesEntry.COLUMN_COURSE_NAME,
                CoursesEntry.COLUMN_COURSE_CREDIT,
                CoursesEntry.COLUMN_COURSE_GRADE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentCourseUri,         // Query the content URI for the current course
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of course attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(CoursesEntry.COLUMN_COURSE_NAME);
            int creditColumnIndex = cursor.getColumnIndex(CoursesEntry.COLUMN_COURSE_CREDIT);
            int gradeColumnIndex = cursor.getColumnIndex(CourseContract.CoursesEntry.COLUMN_COURSE_GRADE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int credit = cursor.getInt(creditColumnIndex);
            int grade = cursor.getInt(gradeColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mCreditsEditText.setText(Integer.toString(credit));
            mGradeEditText.setText(Integer.toString(grade));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mCreditsEditText.setText("");
        mGradeEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    /**
     * Prompt the user to confirm that they want to delete this course.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the course.
                deleteCourse();
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

    /**
     * Perform the deletion of the course in the database.
     */
    private void deleteCourse() {
        // Only perform the delete if this is an existing course.
        if (mCurrentCourseUri != null) {

            int rowsDeleted = 0;

            // Call the ContentResolver to delete the course at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentcourseUri
            // content URI already identifies the course that we want.
            try {
                rowsDeleted = getContentResolver().delete(mCurrentCourseUri, null, null);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_course_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_course_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }


        // Close the activity
        finish();
    }
}