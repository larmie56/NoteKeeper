package com.example.notekeeper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.notekeeper.NotekeeperProviderContract.Courses;
import com.example.notekeeper.NotekeeperProviderContract.Notes;

public class NotekeeperProvider extends ContentProvider {
    public NotekeeperProvider() {
    }

    NoteKeeperOpenHelper mDbOpenHelper;
    static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;

    public static final int NOTES = 1;

    public static final int NOTES_EXPANDED = 2;

    static {
        sUriMatcher.addURI(NotekeeperProviderContract.AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(NotekeeperProviderContract.AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(NotekeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();

        long rowId;
        int uriMatch = sUriMatcher.match(uri);
        Uri rowUri = null;

        switch (uriMatch) {
            case COURSES:
                rowId = db.insert(CourseInfoEntry.TABLE_NAME, null,values);
                rowUri = ContentUris.withAppendedId(uri, rowId);
                break;
            case NOTES:
                rowId = db.insert(NoteInfoEntry.TABLE_NAME, null, values);
                rowUri = ContentUris.withAppendedId(Notes.CONTENT_URI, rowId);
                break;
            case NOTES_EXPANDED:
                //Throw exception showing there's no insert into the "notes_expanded" table.
        }

        return rowUri;
    }

    @Override
    public boolean onCreate() {
        mDbOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int value = sUriMatcher.match(uri);

        switch (value) {
            case COURSES:
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case NOTES:
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case NOTES_EXPANDED:
                String[] columns = new String[projection.length];
                for (int index = 0; index < projection.length; index++) {
                    if ((projection[index]).equals(NoteInfoEntry.COLUMN_COURSE_ID) ||
                    projection[index].equals(NoteInfoEntry._ID)) {
                        columns[index] = NoteInfoEntry.getQName(projection[index]);
                    }
                    else {
                        columns[index] = projection[index];
                    }
                }

                final String tableWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME +
                        " ON " + NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                        CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);

                cursor = db.query(tableWithJoin, columns, selection, selectionArgs,
                        null, null, sortOrder);
                break;
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}