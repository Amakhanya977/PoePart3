# PROG5121 PoE Part 3 – Messaging App

A Java (Maven) desktop app that registers and logs in a user, then lets them send, store and disregard messages, and manage them using arrays and JSON.

**Student:** YOUR NAME  |  **Student number:** YOUR STUDENT NUMBER

## Features
- Register and log in (username, password and cell phone validation)
- Send messages: choose **Send**, **Disregard** or **Store** for each one
- Five arrays populated from user input: sent messages, disregarded messages, stored messages, message hashes and message IDs
- Stored messages are saved to `storedMessages.json` and read back into the arrays when the app starts
- **Message management** menu: disregarded messages, stored messages, delete by hash, full report, search, longest message
- **Stored Messages** menu (main menu option 4):
  - a. Display sender and recipient of all stored messages
  - b. Display the longest stored message
  - c. Search for a message ID and display the recipient and message
  - d. Search for all messages to a particular recipient
  - e. Delete a message using its hash
  - f. Display a report of all stored messages

## How to run
1. Requires **Java 25** and **Maven**.
2. Clone the repository and open it in NetBeans (or any IDE).
3. Run `com.mycompany.poepart3.PoePart3`.

## How to run the tests
