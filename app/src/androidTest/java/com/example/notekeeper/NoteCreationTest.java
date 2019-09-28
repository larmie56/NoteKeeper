package com.example.notekeeper;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.*;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {

    final public String NOTE_CREATION_TEST = this.getClass().getSimpleName();

    static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp() {
        sDataManager = DataManager.getInstance();
    }

    @Rule
    public ActivityTestRule<NoteListActivity> mNoteListActivityActivityRule =
            new ActivityTestRule(NoteListActivity.class);

    @Test
    public void noteCreationTest() {

        final CourseInfo course = sDataManager.getCourse("java_lang");
        final String title = "This is the title";
        final String text = "This is the text";

        Log.i(NOTE_CREATION_TEST, "Starting test");
        onView(withId(R.id.fab)).perform(click());

        onView(withId(R.id.spinner_courses)).perform(click());

        onData(allOf(instanceOf(CourseInfo.class), equalTo(course))).perform(click());

        onView(withId(R.id.text_note_title)).perform(typeText(title));
        onView(withId(R.id.text_note_text)).perform(typeText(text));

        closeSoftKeyboard();

        onView(withId(R.id.text_note_title)).check(matches(withText(((title)))));
        onView(withId(R.id.text_note_text)).check(matches(withText((text))));

        pressBack();

        int noteIndex = sDataManager.getNotes().size() - 1;
        NoteInfo testNote = sDataManager.getNotes().get(noteIndex);

        assertEquals(course, testNote.getCourse());
        assertEquals(title, testNote.getTitle());
        assertEquals(text, testNote.getText());

        Log.i(NOTE_CREATION_TEST, "End of test");
    }
}