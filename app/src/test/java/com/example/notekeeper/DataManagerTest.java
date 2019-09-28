package com.example.notekeeper;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataManagerTest {

    public static DataManager sDm;

    @BeforeClass
    public static void setDm() {
        sDm = DataManager.getInstance();
    }

    @Before
    public void setUp() {
        sDm.getNotes().clear();
        sDm.initializeExampleNotes();
    }

    @Test
    public void createNewNote() {
        CourseInfo course = sDm.getCourse("android_async");
        String title = "This is the note title";
        String text = "This is the note body";

        int noteIndex = sDm.createNewNote();
        NoteInfo note = sDm.getNotes().get(noteIndex);
        note.setCourse(course);
        note.setTitle(title);
        note.setText(text);

        NoteInfo compareNote = sDm.getNotes().get(noteIndex);

        assertEquals(course, compareNote.getCourse());
        assertEquals(title, compareNote.getTitle());
        assertEquals(text, compareNote.getText());
    }

    @Test
    public void findSimilarNotes() {
        CourseInfo course = sDm.getCourse("android_async");
        String title = "This is the note title";
        String text1 = "This is the note body";
        String text2 = "This is the second note body";

        int noteIndex = sDm.createNewNote();
        NoteInfo note1 = sDm.getNotes().get(noteIndex);
        note1.setCourse(course);
        note1.setTitle(title);
        note1.setText(text1);

        int noteIndex2 = sDm.createNewNote();
        NoteInfo note2 = sDm.getNotes().get(noteIndex2);
        note2.setCourse(course);
        note2.setTitle(title);
        note2.setText(text2);

        int compareNoteIndex1 = sDm.findNote(note1);
        assertEquals(noteIndex, compareNoteIndex1);

        int compareNoteIndex2 = sDm.findNote(note2);
        assertEquals(noteIndex2, compareNoteIndex2);
    }

    @Test
    public void newNoteOneStepCreation() {
        CourseInfo course = sDm.getCourse("android_async");
        String title = "This is the note title";
        String text = "This is the note text";

        int noteIndex = sDm.createNewNote(course, title, text);

        NoteInfo compareNote = sDm.getNotes().get(noteIndex);

        assertEquals(course, compareNote.getCourse());
        assertEquals(title, compareNote.getTitle());
        assertEquals(text, compareNote.getText());
    }
}