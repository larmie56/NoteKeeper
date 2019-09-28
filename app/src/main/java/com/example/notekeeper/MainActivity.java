package com.example.notekeeper;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import com.example.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.notekeeper.NotekeeperProviderContract.Courses;
import com.example.notekeeper.NotekeeperProviderContract.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks {

    public static final int LOADER_JOINED_TABLE = 0;
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private RecyclerView mRecyclerNotes;
    private LinearLayoutManager mLinearLayoutManager;
    private GridLayoutManager mGridLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private NoteKeeperOpenHelper mDbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        initializeDisplayContent();
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_JOINED_TABLE, null, this);
        updateNavHeader();
    }

    private void loaddFromDatabase() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        final String[] noteColumns = {NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry._ID};
        final String order = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
        final Cursor noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                null, null, null, null, order);

        mNoteRecyclerAdapter.changeCursor(noteCursor);
    }

    private void updateNavHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View navHeader = navigationView.getHeaderView(0);
        TextView userDisplayName = navHeader.findViewById(R.id.text_user_display_name);
        TextView userEmail = navHeader.findViewById(R.id.text_user_email);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        userDisplayName.setText(pref.getString("user_display_name", ""));
        userEmail.setText(pref.getString("user_email", ""));

    }


    private void initializeDisplayContent() {
        DataManager.loadFromDatabase(mDbOpenHelper);
        
        mRecyclerNotes = findViewById(R.id.list_items);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mGridLayoutManager = new GridLayoutManager(this, 2);

        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, null);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, courses);
        displayNote();
    }



    private void displayNote() {
        mRecyclerNotes.setLayoutManager(mLinearLayoutManager);
        mRecyclerNotes.setAdapter(mNoteRecyclerAdapter);

        setEnabled(R.id.nav_notes);
    }

    private void setEnabled(int id) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setEnabled(true);
    }

    private void displayCourse() {
        mRecyclerNotes.setLayoutManager(mGridLayoutManager);
        mRecyclerNotes.setAdapter(mCourseRecyclerAdapter);

        setEnabled(R.id.nav_courses);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.i(getClass().getSimpleName(), "user clicked on settings menu item");
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            // Handle the camera action
            displayNote();
        } else if (id == R.id.nav_courses) {
            displayCourse();
        } else if (id == R.id.nav_share) {
            //handleSelection(R.string.nav_share_message);
            handleShare();
        } else if (id == R.id.nav_send) {
            handleSelection(R.string.nav_send_message);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleShare() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String message = pref.getString("user_favourite_social_media", "");

        Snackbar.make(mRecyclerNotes, "Share to - " + message, Snackbar.LENGTH_LONG).show();

    }

    private void handleSelection(int id) {
        Snackbar.make(mRecyclerNotes, id, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;

        if (id == LOADER_JOINED_TABLE) {
            loader = loadFromDatabase();
        }

        return loader;
    }

    private CursorLoader loadFromDatabase() {
        Uri uri = Notes.NOTES_EXPANDED_URI;
        final String[] noteColumns = {Notes._ID,
                Notes.COLUMN_NOTE_TITLE,
                Courses.COLUMN_COURSE_TITLE};
        final String order = Courses.COLUMN_COURSE_TITLE + "," + Notes.COLUMN_NOTE_TITLE;

        return new CursorLoader(this, uri, noteColumns, null, null, order);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        if (loader.getId() == LOADER_JOINED_TABLE)
            mNoteRecyclerAdapter.changeCursor(((Cursor) data));

    }

    @Override
    public void onLoaderReset(Loader loader) {
        mNoteRecyclerAdapter.changeCursor(null);

    }
}
