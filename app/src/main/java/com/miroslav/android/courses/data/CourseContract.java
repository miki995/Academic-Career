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
package com.miroslav.android.courses.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Courses app.
 */
public final class CourseContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private CourseContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.miroslav.android.courses";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.miroslav.android.courses/courses/ is a valid path for
     * looking at courses data. content://com.miroslav.android.courses/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_COURSES = "courses";

    /**
     * Inner class that defines constant values for the courses database table.
     * Each entry in the table represents a single course.
     */
    public static final class CoursesEntry implements BaseColumns {

        /**
         * The content URI to access the course data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_COURSES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of courses.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single course.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSES;

        /**
         * Name of database table for courses
         */
        public final static String TABLE_NAME = "courses";

        /**
         * Unique ID number for the course (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the course.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_COURSE_NAME = "name";

        /**
         * Credits that course obtain.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_COURSE_CREDIT = "credits";

        /**
         * Grade obtained at course.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_COURSE_GRADE = "grade";


        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public static boolean isValid(int value, int min, int max) {
            return value >= min && value <= max;
        }


    }

}

