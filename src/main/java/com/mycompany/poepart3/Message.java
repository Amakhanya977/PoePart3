/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.poepart3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.Font;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Handles sending, storing, disregarding, searching and reporting on messages.
 *
 * @author Student
 */
public class Message {

    // ---------- Arrays required by Part 3 ----------
    // Messages that were sent
    ArrayList<String> sentMessages = new ArrayList<>();
    // Messages that were disregarded
    ArrayList<String> disregardedMessages = new ArrayList<>();
    // Messages that were stored (also read back from the JSON file)
    ArrayList<String> storedMessages = new ArrayList<>();
    // Hash of every sent / stored message (parallel to messageTexts)
    ArrayList<String> msgHash = new ArrayList<>();
    // ID of every sent / stored message (parallel to messageTexts)
    ArrayList<String> messageIdsList = new ArrayList<>();

    // ---------- Parallel arrays (same index = same message) ----------
    // Text of every sent or stored message
    ArrayList<String> messageTexts = new ArrayList<>();
    ArrayList<String> recipientNumber = new ArrayList<>();
    ArrayList<String> senders = new ArrayList<>();
    // "Sent" or "Stored"
    ArrayList<String> messageStatus = new ArrayList<>();

    HashSet<String> messageIds = new HashSet<>();

    Random random = new Random();

    int numMessageSent = 0;

    // Name of the JSON file that stored messages are written to / read from
    static final String JSON_FILE = "storedMessages.json";

    /**
     * Simple holder that Gson converts to / from JSON.
     */
    static class StoredRecord {

        String messageId;
        String messageHash;
        String sender;
        String recipient;
        String message;

        StoredRecord(String messageId, String messageHash, String sender,
                String recipient, String message) {
            this.messageId = messageId;
            this.messageHash = messageHash;
            this.sender = sender;
            this.recipient = recipient;
            this.message = message;
        }
    }

    // =====================================================================
    //  Sending messages (GUI)
    // =====================================================================
    public void sendingMessages() {

        int numberOfMessages = 0;

        while (true) {
            String input = JOptionPane.showInputDialog(null,
                    "How many messages do you want to send?",
                    "Send Messages", JOptionPane.QUESTION_MESSAGE);

            if (input == null) {
                return;
            }

            try {
                numberOfMessages = Integer.parseInt(input.trim());
                if (numberOfMessages < 1) {
                    JOptionPane.showMessageDialog(null,
                            "Please enter a number greater than 0.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Please enter a valid number.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        for (int i = 1; i <= numberOfMessages; i++) {

            String recipient;
            while (true) {
                recipient = JOptionPane.showInputDialog(null,
                        "MESSAGE " + i + " of " + numberOfMessages
                      + "\n\nEnter recipient cell number:",
                        "Recipient", JOptionPane.QUESTION_MESSAGE);

                if (recipient == null) {
                    return;
                }

                String recipientResult = checkRecipientCell(recipient.trim());

                if (recipientResult.equals("Valid number")) {
                    recipient = recipient.trim();
                    break;
                }

                JOptionPane.showMessageDialog(null, recipientResult,
                        "Invalid number", JOptionPane.ERROR_MESSAGE);
            }

            String messageText;
            while (true) {
                messageText = JOptionPane.showInputDialog(null,
                        "Enter your message (1 to 250 characters):",
                        "Message", JOptionPane.QUESTION_MESSAGE);

                if (messageText == null) {
                    return;
                }

                if (messageText.length() > 250 || messageText.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "Please enter a message of 1 to 250 characters.",
                            "Invalid message", JOptionPane.ERROR_MESSAGE);
                } else {
                    break;
                }
            }

            String sender = JOptionPane.showInputDialog(null,
                    "Enter sender name:", "Sender", JOptionPane.QUESTION_MESSAGE);

            if (sender == null || sender.trim().isEmpty()) {
                sender = "Unknown";
            }

            String status = SentMessage();

            // Fills the arrays according to the user's choice
            addMessage(sender.trim(), recipient, messageText, status);

            if (status.equals("Stored")) {
                saveStoredMessagesToJson(JSON_FILE);
            }
        }

        printMessages();

        JOptionPane.showMessageDialog(null,
                "Total messages sent: " + returnTotalMessages(),
                "Total", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Puts a message into the correct arrays depending on its flag. Contains no
     * dialogs so it can be unit tested.
     *
     * @param flag "Sent", "Stored" or "Disregard" / "Disregarded"
     * @return the message hash, or an empty string if the message was
     * disregarded
     */
    public String addMessage(String sender, String recipient, String text, String flag) {

        if (flag.toLowerCase().startsWith("disregard")) {
            disregardedMessages.add(text);
            return "";
        }

        String messageID = generateMessageID();
        String hash = createMessageHash(messageID, messageTexts.size() + 1, text);

        messageTexts.add(text);
        recipientNumber.add(recipient);
        senders.add(sender);
        messageIdsList.add(messageID);
        messageIds.add(messageID);
        msgHash.add(hash);

        if (flag.equalsIgnoreCase("Sent")) {
            sentMessages.add(text);
            messageStatus.add("Sent");
            numMessageSent++;
        } else {
            storedMessages.add(text);
            messageStatus.add("Stored");
        }

        return hash;
    }

    public int returnTotalMessages() {
        return numMessageSent;
    }

    public String createMessageHash(String messageID, int messageNumber, String messageText) {

        String firstTwoNumbers = messageID.substring(0, 2);

        String cleanMessage = messageText.trim();

        String[] words = cleanMessage.split("\\s+");

        String firstWord = words[0];

        String lastWord = words[words.length - 1];

        String hash = firstTwoNumbers + ":" + messageNumber + ":" + firstWord + lastWord;

        return hash.toUpperCase();
    }

    public String generateMessageID() {

        String messageID;

        do {
            long number = 1000000000L + random.nextLong(9000000000L);
            messageID = String.valueOf(number);
        } while (messageIds.contains(messageID));

        return messageID;
    }

    public String checkRecipientCell(String cellphone) {

        if (cellphone.matches("^\\+27[0-9]{9}$") || cellphone.matches("^0[0-9]{9}$")) {
            return "Valid number";
        } else {
            return "Invalid number";
        }
    }

    /**
     * Asks the user what to do with a message. Only returns the choice; the
     * arrays are filled by addMessage().
     */
    public String SentMessage() {

        Object[] options = {"Send", "Disregard", "Store"};

        while (true) {

            int choice = JOptionPane.showOptionDialog(null,
                    "Choose what you want to do with this message!",
                    "Message Options",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            switch (choice) {

                case 0 -> {
                    JOptionPane.showMessageDialog(null, "Message successfully sent",
                            "Sent", JOptionPane.INFORMATION_MESSAGE);
                    return "Sent";
                }

                case 1 -> {
                    int deleteChoice = JOptionPane.showConfirmDialog(null,
                            "Do you want to delete this message?",
                            "Disregard Message", JOptionPane.YES_NO_OPTION);

                    if (deleteChoice == JOptionPane.YES_OPTION) {
                        JOptionPane.showMessageDialog(null, "Message deleted",
                                "Deleted", JOptionPane.INFORMATION_MESSAGE);
                        return "Disregarded";
                    }
                    // user said No: show the options again
                }

                case 2 -> {
                    JOptionPane.showMessageDialog(null, "Message successfully stored",
                            "Stored", JOptionPane.INFORMATION_MESSAGE);
                    return "Stored";
                }

                default -> {
                    JOptionPane.showMessageDialog(null, "Invalid option",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return "Disregarded";
                }
            }
        }
    }

    public String printMessages() {

        StringBuilder allMessages = new StringBuilder();

        if (messageTexts.isEmpty()) {
            allMessages.append("No messages have been sent yet.");
        } else {
            for (int i = 0; i < messageTexts.size(); i++) {

                int messageNumber = i + 1;

                allMessages.append("\n==============================\n");
                allMessages.append("Message Number: ").append(messageNumber).append("\n");
                allMessages.append("Message ID: ").append(messageIdsList.get(i)).append("\n");
                allMessages.append("Recipient: ").append(recipientNumber.get(i)).append("\n");
                allMessages.append("Message: ").append(messageTexts.get(i)).append("\n");
                allMessages.append("Message Hash: ").append(msgHash.get(i)).append("\n");
                allMessages.append("Status: ").append(messageStatus.get(i)).append("\n");
            }
        }

        showMessageArea(allMessages.toString(), "All Messages");

        return allMessages.toString();
    }

    // =====================================================================
    //  JSON (Gson)
    // =====================================================================
    /*
     * Reading and writing JSON with Gson was done by following the Gson User
     * Guide: https://github.com/google/gson/blob/main/UserGuide.md
     * (sections "Collections Examples" and "Serializing and Deserializing
     * Generic Types" - TypeToken).
     */
    /**
     * Writes every stored message to the JSON file.
     */
    public boolean saveStoredMessagesToJson(String filePath) {

        List<StoredRecord> records = new ArrayList<>();

        for (int i = 0; i < messageTexts.size(); i++) {
            if (messageStatus.get(i).equals("Stored")) {
                records.add(new StoredRecord(messageIdsList.get(i), msgHash.get(i),
                        senders.get(i), recipientNumber.get(i), messageTexts.get(i)));
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(records, writer);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not save stored messages: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Reads the JSON file into the arrays. Messages whose ID is already in the
     * arrays are skipped so nothing is loaded twice.
     *
     * @return number of messages read in
     */
    public int loadStoredMessagesFromJson(String filePath) {

        File file = new File(filePath);

        if (!file.exists()) {
            return 0;
        }

        Type listType = new TypeToken<ArrayList<StoredRecord>>() {
        }.getType();

        int loaded = 0;

        try (FileReader reader = new FileReader(file)) {

            List<StoredRecord> records = new Gson().fromJson(reader, listType);

            if (records == null) {
                return 0;
            }

            for (StoredRecord r : records) {

                if (messageIds.contains(r.messageId)) {
                    continue;
                }

                messageTexts.add(r.message);
                recipientNumber.add(r.recipient);
                senders.add(r.sender);
                messageIdsList.add(r.messageId);
                messageIds.add(r.messageId);
                msgHash.add(r.messageHash);
                messageStatus.add("Stored");
                storedMessages.add(r.message);
                loaded++;
            }

        } catch (IOException | RuntimeException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not read " + filePath + ": " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }

        return loaded;
    }

    /**
     * Reads the JSON file into the arrays and shows the loaded messages to the
     * user.
     */
    public void loadAndDisplayStoredMessages() {

        int loaded = loadStoredMessagesFromJson(JSON_FILE);

        if (loaded == 0) {
            return;
        }

        StringBuilder text = new StringBuilder("Loaded " + loaded
                + " stored message(s) from " + JSON_FILE + ":\n\n");

        for (int i = 0; i < storedMessages.size(); i++) {
            text.append(i + 1).append(". ").append(storedMessages.get(i)).append("\n");
        }

        showMessageArea(text.toString(), "Stored Messages Loaded");
    }

    // =====================================================================
    //  Core logic (no dialogs - used by the menus and the unit tests)
    // =====================================================================
    /**
     * @return sender and recipient lines for every stored message
     */
    public ArrayList<String> getStoredSendersAndRecipients() {

        ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < messageTexts.size(); i++) {
            if (messageStatus.get(i).equals("Stored")) {
                result.add("Sender: " + senders.get(i)
                        + " | Recipient: " + recipientNumber.get(i));
            }
        }
        return result;
    }

    /**
     * @return the longest stored message, or "" when nothing is stored
     */
    public String getLongestStoredMessage() {

        String longest = "";

        for (String text : storedMessages) {
            if (text.length() > longest.length()) {
                longest = text;
            }
        }
        return longest;
    }

    /**
     * @return index of the message with this ID, or -1
     */
    public int findIndexByMessageId(String messageId) {
        return messageIdsList.indexOf(messageId);
    }

    /**
     * @return the message text for this ID, or null if the ID does not exist
     */
    public String getMessageById(String messageId) {

        int index = findIndexByMessageId(messageId);

        if (index == -1) {
            return null;
        }
        return messageTexts.get(index);
    }

    /**
     * @return every message (sent or stored) for the recipient
     */
    public ArrayList<String> getMessagesByRecipient(String recipient) {

        ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < messageTexts.size(); i++) {
            if (recipientNumber.get(i).equals(recipient)) {
                result.add(messageTexts.get(i));
            }
        }
        return result;
    }

    /**
     * Removes a message using its hash and moves it to the disregarded array.
     *
     * @return confirmation text, or "Message not found" if the hash is unknown
     */
    public String deleteMessageUsingHash(String hash) {

        int index = msgHash.indexOf(hash);

        if (index == -1) {
            return "Message not found";
        }

        String text = messageTexts.get(index);

        if (messageStatus.get(index).equals("Sent")) {
            sentMessages.remove(text);
        } else {
            storedMessages.remove(text);
        }

        disregardedMessages.add(text);
        messageIds.remove(messageIdsList.get(index));

        messageTexts.remove(index);
        recipientNumber.remove(index);
        senders.remove(index);
        messageIdsList.remove(index);
        msgHash.remove(index);
        messageStatus.remove(index);

        return "Message \"" + text + "\" successfully deleted.";
    }

    /**
     * Builds a report of the full details of messages.
     *
     * @param status "Sent" or "Stored" to filter, or null for all messages
     */
    public String buildReport(String status) {

        StringBuilder report = new StringBuilder();
        report.append("========== Message Report ==========\n");

        int count = 0;

        for (int i = 0; i < messageTexts.size(); i++) {

            if (status != null && !messageStatus.get(i).equals(status)) {
                continue;
            }

            count++;
            report.append("\n------------------------------\n");
            report.append("Message Hash: ").append(msgHash.get(i)).append("\n");
            report.append("Message ID: ").append(messageIdsList.get(i)).append("\n");
            report.append("Sender: ").append(senders.get(i)).append("\n");
            report.append("Recipient: ").append(recipientNumber.get(i)).append("\n");
            report.append("Message: ").append(messageTexts.get(i)).append("\n");
            report.append("Status: ").append(messageStatus.get(i)).append("\n");
        }

        report.append("\nTotal messages: ").append(count);

        return report.toString();
    }

    // =====================================================================
    //  Stored Messages menu (main menu option 4)
    // =====================================================================
    public void storedMessagesMenu() {

        while (true) {

            String option = JOptionPane.showInputDialog(null, """
                                                              a. Display sender and recipient of all stored messages
                                                              b. Display the longest stored message
                                                              c. Search for a message ID
                                                              d. Search for all messages to a recipient
                                                              e. Delete a message using its hash
                                                              f. Display a report of all stored messages
                                                              g. Back to main menu
                                                              
                                                              Choose an option (a-g):""",
                    "Stored Messages", JOptionPane.QUESTION_MESSAGE);

            if (option == null) {
                return;
            }

            switch (option.trim().toLowerCase()) {
                case "a" -> displayStoredSendersAndRecipients();
                case "b" -> displayLongestStoredMessage();
                case "c" -> searchByMessageIdMenu();
                case "d" -> searchByRecipientMenu();
                case "e" -> deleteByHashMenu();
                case "f" -> displayStoredReport();
                case "g" -> {
                    return;
                }
                default -> JOptionPane.showMessageDialog(null,
                        "Invalid choice.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void displayStoredSendersAndRecipients() {

        ArrayList<String> lines = getStoredSendersAndRecipients();

        if (lines.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No stored messages to display",
                    "Stored Messages", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder text = new StringBuilder("========== Stored Messages ==========\n\n");
        for (int i = 0; i < lines.size(); i++) {
            text.append(i + 1).append(". ").append(lines.get(i)).append("\n");
        }
        showMessageArea(text.toString(), "Senders and Recipients");
    }

    private void displayLongestStoredMessage() {

        String longest = getLongestStoredMessage();

        if (longest.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No stored messages to analyze",
                    "Longest Message", JOptionPane.WARNING_MESSAGE);
            return;
        }

        showMessageArea("Longest stored message (" + longest.length()
                + " characters):\n\n" + longest, "Longest Stored Message");
    }

    private void searchByMessageIdMenu() {

        String id = JOptionPane.showInputDialog(null, "Enter the message ID to search:",
                "Search Message ID", JOptionPane.QUESTION_MESSAGE);

        if (id == null) {
            return;
        }

        int index = findIndexByMessageId(id.trim());

        if (index == -1) {
            JOptionPane.showMessageDialog(null, "No message found with ID: " + id,
                    "Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        showMessageArea("Recipient: " + recipientNumber.get(index)
                + "\nMessage: " + messageTexts.get(index), "Message Found");
    }

    private void searchByRecipientMenu() {

        String recipient = JOptionPane.showInputDialog(null, "Enter the recipient number:",
                "Search Recipient", JOptionPane.QUESTION_MESSAGE);

        if (recipient == null) {
            return;
        }

        ArrayList<String> found = getMessagesByRecipient(recipient.trim());

        if (found.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No messages found for " + recipient,
                    "Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder text = new StringBuilder("Messages for " + recipient.trim() + ":\n\n");
        for (int i = 0; i < found.size(); i++) {
            text.append(i + 1).append(". ").append(found.get(i)).append("\n");
        }
        showMessageArea(text.toString(), "Recipient Messages");
    }

    private void deleteByHashMenu() {

        String hash = JOptionPane.showInputDialog(null, "Enter the message hash to delete:",
                "Delete Message", JOptionPane.QUESTION_MESSAGE);

        if (hash == null) {
            return;
        }

        String result = deleteMessageUsingHash(hash.trim());

        // keep the JSON file in step with the stored array
        saveStoredMessagesToJson(JSON_FILE);

        JOptionPane.showMessageDialog(null, result, "Delete Message",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void displayStoredReport() {

        if (storedMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No stored messages to display",
                    "Stored Report", JOptionPane.WARNING_MESSAGE);
            return;
        }

        showMessageArea(buildReport("Stored"), "Stored Messages Report");
    }

    // =====================================================================
    //  Message management menu (existing, updated to the new arrays)
    // =====================================================================
    public void messageManagementMenu() {

        while (true) {

            String option = JOptionPane.showInputDialog(null, """
                                                              1. Show disregarded messages
                                                              2. Show all stored messages
                                                              3. Delete a message by Hash ID
                                                              4. Display full report
                                                              5. Search for a message
                                                              6. Display the longest message
                                                              7. Back to main menu
                                                              
                                                              Choose an option (1-7):""",
                    "Message Management", JOptionPane.QUESTION_MESSAGE);

            if (option == null) {
                return;
            }

            switch (option.trim()) {
                case "1" -> showDisregardedMessages();
                case "2" -> showAllStoredMessages();
                case "3" -> deleteMessageByHash();
                case "4" -> displayFullReport();
                case "5" -> searchMessageByHashId();
                case "6" -> findLongestMessage();
                case "7" -> {
                    return;
                }
                default -> JOptionPane.showMessageDialog(null,
                        "Invalid choice.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void findLongestMessage() {

        if (messageTexts.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No sent message to analyze the longest",
                    "Longest Message", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maxLength = -1;
        int index = -1;

        for (int i = 0; i < messageTexts.size(); i++) {

            int currentLength = messageTexts.get(i).length();

            if (currentLength > maxLength) {
                maxLength = currentLength;
                index = i;
            }
        }

        if (index != -1) {

            StringBuilder details = new StringBuilder();
            details.append("========== LONGEST MESSAGE ==========\n\n");
            details.append("Length: ").append(maxLength).append(" characters long\n");
            details.append("Message ID: ").append(messageIdsList.get(index)).append("\n");
            details.append("Sender: ").append(senders.get(index)).append("\n");
            details.append("Recipient: ").append(recipientNumber.get(index)).append("\n");
            details.append("Message: ").append(messageTexts.get(index));

            showMessageArea(details.toString(), "Longest Message");
        }
    }

    public void searchMessageByHashId() {

        if (messageTexts.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No messages to search",
                    "Search", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String hashId = JOptionPane.showInputDialog(null,
                "Enter the Hash ID to search:",
                "Search Message", JOptionPane.QUESTION_MESSAGE);

        if (hashId == null) {
            return;
        }

        if (hashId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Hash ID cannot be empty.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int index = msgHash.indexOf(hashId.trim());

        if (index == -1) {
            JOptionPane.showMessageDialog(null,
                    "No message exists with this Hash ID: " + hashId,
                    "Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("========== Message Details ==========\n\n");
        details.append("Hash ID: ").append(msgHash.get(index)).append("\n");
        details.append("Message ID: ").append(messageIdsList.get(index)).append("\n");
        details.append("From: ").append(senders.get(index)).append("\n");
        details.append("To: ").append(recipientNumber.get(index)).append("\n");
        details.append("Message: ").append(messageTexts.get(index)).append("\n");
        details.append("Length: ").append(messageTexts.get(index).length())
              .append(" characters long");

        showMessageArea(details.toString(), "Message Details");
    }

    public void displayFullReport() {

        if (messageTexts.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No messages to display.",
                    "Full Report", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder report = new StringBuilder();
        report.append("========== Full Message Report ==========\n\n");

        report.append(String.format("| %-5s | %-15s | %-15s | %-20s | %-12s | %-10s |%n",
                "No.", "Sender", "Recipient", "Message", "Message ID", "Hash ID"));

        for (int i = 0; i < messageTexts.size(); i++) {

            String message = messageTexts.get(i);

            if (message.length() > 20) {
                message = message.substring(0, 17) + "...";
            }

            report.append(String.format("| %-5d | %-15s | %-15s | %-20s | %-12s | %-10s |%n",
                    i + 1,
                    senders.get(i),
                    recipientNumber.get(i),
                    message,
                    messageIdsList.get(i),
                    msgHash.get(i)));
        }

        report.append("\nTotal Sent Messages: ").append(sentMessages.size()).append("\n");
        report.append("Total Disregarded Messages: ").append(disregardedMessages.size()).append("\n");
        report.append("Total Stored Messages: ").append(storedMessages.size());

        showMessageArea(report.toString(), "Full Message Report");
    }

    public void deleteMessageByHash() {

        String hash = JOptionPane.showInputDialog(null,
                "Enter Hash ID to delete:",
                "Delete Message", JOptionPane.QUESTION_MESSAGE);

        if (hash == null) {
            return;
        }

        if (hash.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Hash ID cannot be empty.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String result = deleteMessageUsingHash(hash.trim());

        saveStoredMessagesToJson(JSON_FILE);

        JOptionPane.showMessageDialog(null, result, "Delete Message",
                result.equals("Message not found")
                        ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    public void showAllStoredMessages() {

        if (storedMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No stored messages to display",
                    "Stored Messages", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder list = new StringBuilder();
        list.append("========== All Stored Messages ==========\n\n");

        for (int i = 0; i < storedMessages.size(); i++) {
            list.append(i + 1).append(". ").append(storedMessages.get(i)).append("\n");
        }

        showMessageArea(list.toString(), "Stored Messages");
    }

    public void showDisregardedMessages() {

        if (disregardedMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No deleted messages yet.",
                    "Disregarded Messages", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder list = new StringBuilder();
        list.append("========== DELETED MESSAGES ==========\n\n");

        for (int i = 0; i < disregardedMessages.size(); i++) {
            list.append(i + 1).append(". ").append(disregardedMessages.get(i)).append("\n");
        }

        showMessageArea(list.toString(), "Disregarded Messages");
    }

    private void showMessageArea(String text, String title) {
        JTextArea area = new JTextArea(text, 20, 45);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(area);
        JOptionPane.showMessageDialog(null, scroll, title, JOptionPane.INFORMATION_MESSAGE);
    }
}