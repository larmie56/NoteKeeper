package com.example.notekeeper;

import android.provider.BaseColumns;

public final class NoteKeeperDatabaseContract {

    private NoteKeeperDatabaseContract() {}



    public static final class CourseInfoEntry implements BaseColumns {

        public static final String TABLE_NAME = "course_info";
        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_COURSE_TITLE = "course_title";


        // CREATE TABLE course_info (course_id, course_title)

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_COURSE_ID + " TEXT UNIQUE NOT NULL, "
                        + COLUMN_COURSE_TITLE + " TEXT NOT NULL)";

        // CREATE INDEX course_info_index1 ON course_info (course_title)

        private static final String INDEX_NAME = TABLE_NAME + "_index1";

        public static final String SQL_INDEX_TABLE =
                "CREATE INDEX " + INDEX_NAME + " ON " + TABLE_NAME + " (" + COLUMN_COURSE_TITLE + ")";

        public static final String getQName(String append) {
            return TABLE_NAME + "." + append;
        }
    }

    public static final class NoteInfoEntry implements BaseColumns {

        public static final String TABLE_NAME = "note_info";
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
        public static final String COLUMN_COURSE_ID = "course_id";

        public static final String getQName(String append) {
            return TABLE_NAME + "." + append;
        }

        // CREATE TABLE note_info (note_title, note_text, course_id)

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_NOTE_TITLE + " TEXT NOT NULL, " + COLUMN_NOTE_TEXT + " TEXT, "
                        + COLUMN_COURSE_ID + " TEXT NOT NULL)";

        // CREATE INDEX note_info_index1 ON note_info (note_title)

        private static final String INDEX_NAME = TABLE_NAME + "_index1";

        public static final String SQL_INDEX_TABLE =
                "CREATE INDEX " + INDEX_NAME + " ON " + TABLE_NAME + " (" + COLUMN_NOTE_TITLE + ")";
    }
}
