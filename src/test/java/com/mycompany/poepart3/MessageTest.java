package com.mycompany.poepart3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for Part 3, using the five test messages from the brief.
 */
public class MessageTest {

    Message msg;

    @BeforeEach
    public void setUp() {
        msg = new Message();

        // Test Data Message 1 - Sent
        msg.addMessage("Developer", "+27834557896", "Did you get the cake?", "Sent");
        // Test Data Message 2 - Stored
        msg.addMessage("Developer", "+27838884567",
                "Where are you? You are late! I have asked you to be on time.", "Stored");
        // Test Data Message 3 - Disregard
        msg.addMessage("Developer", "+27834484567", "Yohoooo, I am at your gate.", "Disregard");
        // Test Data Message 4 - Sent
        msg.addMessage("Developer", "0838884567", "It is dinner time!", "Sent");
        // Test Data Message 5 - Stored
        msg.addMessage("Developer", "+27838884567", "Ok, I am leaving without you.", "Stored");
    }

    @Test
    public void sentMessagesArrayCorrectlyPopulated() {
        ArrayList<String> expected = new ArrayList<>(
                Arrays.asList("Did you get the cake?", "It is dinner time!"));
        assertEquals(expected, msg.sentMessages);
    }

    @Test
    public void disregardedMessagesArrayCorrectlyPopulated() {
        ArrayList<String> expected = new ArrayList<>(
                Arrays.asList("Yohoooo, I am at your gate."));
        assertEquals(expected, msg.disregardedMessages);
    }

    @Test
    public void storedMessagesArrayCorrectlyPopulated() {
        ArrayList<String> expected = new ArrayList<>(Arrays.asList(
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."));
        assertEquals(expected, msg.storedMessages);
    }

    @Test
    public void messageHashAndIdArraysPopulated() {
        // messages 1, 2, 4 and 5 are kept (message 3 was disregarded)
        assertEquals(4, msg.msgHash.size());
        assertEquals(4, msg.messageIdsList.size());
        assertEquals(10, msg.messageIdsList.get(0).length());
        assertTrue(msg.msgHash.get(0).endsWith(":1:DIDCAKE?"));
        assertTrue(msg.msgHash.get(1).endsWith(":2:WHERETIME."));
    }

    @Test
    public void displayLongestMessage() {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                msg.getLongestStoredMessage());
    }

    @Test
    public void searchForMessageId() {
        String id = msg.messageIdsList.get(2); // message 4
        assertEquals("It is dinner time!", msg.getMessageById(id));
        assertEquals("0838884567", msg.recipientNumber.get(msg.findIndexByMessageId(id)));
    }

    @Test
    public void searchForMessageIdNotFound() {
        assertNull(msg.getMessageById("0000000000"));
    }

    @Test
    public void searchAllMessagesForRecipient() {
        ArrayList<String> expected = new ArrayList<>(Arrays.asList(
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."));
        assertEquals(expected, msg.getMessagesByRecipient("+27838884567"));
    }

    @Test
    public void deleteMessageUsingHash() {
        String hash = msg.msgHash.get(1); // test message 2
        assertEquals("Message \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                msg.deleteMessageUsingHash(hash));
        assertEquals(1, msg.storedMessages.size());
        assertEquals(3, msg.msgHash.size());
    }

    @Test
    public void deleteMessageUsingUnknownHash() {
        assertEquals("Message not found", msg.deleteMessageUsingHash("00:0:NONE"));
    }

    @Test
    public void displayReportShowsHashRecipientAndMessage() {
        String report = msg.buildReport("Sent");
        assertTrue(report.contains("Message Hash: " + msg.msgHash.get(0)));
        assertTrue(report.contains("Recipient: +27834557896"));
        assertTrue(report.contains("Message: Did you get the cake?"));
        assertTrue(report.contains("Message: It is dinner time!"));
        assertTrue(report.contains("Total messages: 2"));
    }

    @Test
    public void storedMessagesSurviveJsonRoundTrip(@TempDir File dir) {
        String path = new File(dir, "stored.json").getPath();
        assertTrue(msg.saveStoredMessagesToJson(path));

        Message fresh = new Message();
        assertEquals(2, fresh.loadStoredMessagesFromJson(path));
        assertEquals(msg.storedMessages, fresh.storedMessages);
        assertEquals(msg.getMessagesByRecipient("+27838884567"),
                fresh.getMessagesByRecipient("+27838884567"));
        // loading the same file again must not create duplicates
        assertEquals(0, fresh.loadStoredMessagesFromJson(path));
    }
}