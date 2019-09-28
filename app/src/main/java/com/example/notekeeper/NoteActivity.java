package com.example.notekeeper;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.notekeeper.NotekeeperProviderContract.Courses;
import com.example.notekeeper.NotekeeperProviderContract.Notes;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    public final String TAG = getClass().getSimpleName();

    public static final String NOTE_ID = "com.example.notekeeper.NOTE_ID";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.notekeeper.ORIGINAL_NOTE_TEXT";
    public static final int NOTE_NOT_FOUND = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private Spinner mSpinner;
    private int mNoteId;
    private boolean mIsCancelling;
    private String mOriginalCourseID;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
    private NoteKeeperOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdColumnPosition;
    private int mNoteTitleColumnPosition;
    private int mNoteTextColumnPosition;
    private SimpleCursorAdapter mCursorAdapter;
    private Cursor mCourseCursor;
    private boolean mLoadedNotes;
    private boolean mLoadedCourses;
    private Uri mNoteRowUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        mSpinner = findViewById(R.id.spinner_courses);

        mCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[] {CourseInfoEntry.COLUMN_COURSE_TITLE}, new int[] {android.R.id.text1}, 0);
        mCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        readDisplayStateContent();
        mSpinner.setAdapter(mCursorAdapter);
        getLoaderManager().initLoader(LOADER_COURSES, null, this);

        /*if (savedInstanceState == null) {
            storeOriginalNoteState();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
        }*/

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if (!mIsNewNote)
            getLoaderManager().initLoader(LOADER_NOTES, null, this);


        Log.d(TAG, "OnCreate");
    }

    private void loadFromDatabase() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(mNoteId)};

        String[] columns = {NoteInfoEntry.COLUMN_COURSE_ID,
                            NoteInfoEntry.COLUMN_NOTE_TITLE,
                            NoteInfoEntry.COLUMN_NOTE_TEXT};

        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, columns,
                selection, selectionArgs, null, null, null);

        mCourseIdColumnPosition = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitleColumnPosition = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextColumnPosition = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();

        displayNote();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalCourseID = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalCourseID);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

    private void storeOriginalNoteState() {
        mNoteCursor.moveToFirst();
        if (mIsNewNote)
            return;
        mOriginalCourseID = mNoteCursor.getString(mCourseIdColumnPosition);
        mOriginalNoteTitle = mNoteCursor.getString(mNoteTitleColumnPosition);
        mOriginalNoteText = mNoteCursor.getString(mNoteTextColumnPosition);
    }

    private void displayNote() {
        String courseId = mNoteCursor.getString(mCourseIdColumnPosition);
        String noteTitle = mNoteCursor.getString(mNoteTitleColumnPosition);
        String noteText = mNoteCursor.getString(mNoteTextColumnPosition);

        int index = getCoursePositionInCursor(courseId);

        mSpinner.setSelection(index);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);
    }
    private void setUpArrayAdapter() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] columns = new String[] {CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID};

        mCourseCursor = db.query(CourseInfoEntry.TABLE_NAME, columns,
                null, null, null, null,
                CourseInfoEntry.COLUMN_COURSE_TITLE);
        mCursorAdapter.changeCursor(mCourseCursor);
    }
    private int getCoursePositionInCursor(String courseId) {
        boolean move = mCourseCursor.moveToFirst();
        int courseIndex = 0;
        int courseIdPos = mCourseCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String cursorCourseId = mCourseCursor.getString(courseIdPos);

        while (move) {
            if (courseId.equals(cursorCourseId))
                break;
            move = mCourseCursor.moveToNext();
            ++courseIndex;
            if (courseIndex >= mCourseCursor.getCount()) {
                return 0;
            }
            cursorCourseId = mCourseCursor.getString(courseIdPos);
        }

            return courseIndex;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling){
         if (mIsNewNote) {
             removeFromDatabase();
             Log.i(TAG, "user is cancelling a new note");
         } else {
             //restoreOriginalValues();
         }
        } else {
            saveState();
            Log.i(TAG, "user is saving note at position: " + mNoteId);
        }
        Log.d(TAG, "OnPause");
    }

    private void removeFromDatabase() {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {String.valueOf(mNoteId)};
        //final String[] selectionArgs = {""};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();

                int check = db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                mDbOpenHelper.close();
                return null;
            }
        };
        task.execute();
    }

    private void restoreOriginalValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalCourseID);
        mNote.setCourse(course);
        mNote.setTitle(mOriginalNoteTitle);
        mNote.setText(mOriginalNoteText);
    }

    private void saveState() {
        String courseId = getCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();

        saveToDatabase(courseId, noteTitle, noteText);
    }

    private String getCourseId() {
        int selectedPosition = mSpinner.getSelectedItemPosition();
        Cursor cursor = mCursorAdapter.getCursor();
        int columnPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);

        cursor.moveToPosition(selectedPosition);

        return cursor.getString(columnPos);
    }

    private void saveToDatabase(String courseId, String noteTitle, String noteText) {
        final ContentValues values = new ContentValues();

        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {String.valueOf(mNoteId)};
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();

        db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
        mDbOpenHelper.close();
    }

    private void readDisplayStateContent() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, NOTE_NOT_FOUND);
        mIsNewNote = mNoteId == NOTE_NOT_FOUND;

        if (mIsNewNote) {
            creteNewNote();
        }

        //mNote = DataManager.getInstance().getNotes().get(mNoteId);
    }

    private void creteNewNote() {
        final ContentValues values = new ContentValues();

        values.put(Notes.COLUMN_COURSE_ID, "");
        values.put(Notes.COLUMN_NOTE_TITLE, "");
        values.put(Notes.COLUMN_NOTE_TEXT, "");

        mNoteRowUri = getContentResolver().insert(Notes.CONTENT_URI, values);

        //TODO: IMPLEMENT MULTITHREADING METHODS
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendMail();
            return true;
        }
        else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
            Log.i(TAG, "user is cancelling at position: " + mNoteId);
        }
        else if (id == R.id.action_next) {
            moveNext();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        MenuItem item = menu.findItem(R.id.action_next);
        item.setEnabled(mNoteId < lastNoteIndex);

        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveState();

        ++mNoteId;
        mNote = DataManager.getInstance().getNotes().get(mNoteId);
        storeOriginalNoteState();

        displayNote();

        invalidateOptionsMenu();
    }

    private void sendMail() {
        CourseInfo course = (CourseInfo) mSpinner.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String body = mTextNoteText.getText().toString();
        String text = "Check out what I just learnt from the pluralsight course \""
                + course + "\"\n" + body;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rcf2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);


    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if (id == LOADER_NOTES)
            loader = queryForNote();
        else if (id == LOADER_COURSES)
            loader = queryForCourses();


        return loader;
    }

    private CursorLoader queryForCourses() {
        mLoadedCourses = false;
        Uri uri = Courses.CONTENT_URI;
        String[] columns = new String[] {Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID};

        return new CursorLoader(this, uri, columns, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
    }

    private CursorLoader queryForNote() {
        mLoadedNotes = false;
        Uri uri = Notes.CONTENT_URI;
        String selection = Notes._ID + " = ?";
        String[] selectionArgs = {String.valueOf(mNoteId)};
        String[] columns = {Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT};

        return new CursorLoader(this, uri, columns, selection, selectionArgs, null);

        //TODO: IMPLEMENT MULTITHREADING METHODS
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES) {
            setUpNoteDisplay(data);
        }
        else if (loader.getId() == LOADER_COURSES) {
            setUpCourseDisplay(data);
        }
    }

    private void setUpCourseDisplay(Cursor data) {
        mCourseCursor = data;
        mLoadedCourses = true;
        mCursorAdapter.changeCursor(mCourseCursor);

            if (mLoadedNotes && mLoadedCourses)
                displayNote();
        }

    private void setUpNoteDisplay(Cursor data) {
        mNoteCursor = data;
        mLoadedNotes = true;
        mCourseIdColumnPosition = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitleColumnPosition = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextColumnPosition = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
            if (mLoadedNotes && mLoadedCourses)
                displayNote();
        }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES)
            mNoteCursor.close();
        if (loader.getId() == LOADER_COURSES)
            mCursorAdapter.changeCursor(null);
    }
}