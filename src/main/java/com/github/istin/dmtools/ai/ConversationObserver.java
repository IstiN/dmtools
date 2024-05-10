package com.github.istin.dmtools.ai;

import java.util.ArrayList;
import java.util.List;

public class ConversationObserver {

    public static class Message {

        private String author;
        private String text;

        public Message(String author, String text) {
            this.text = text;
            this.author = author;
        }

        public String getAuthor() {
            return author;
        }

        public String getText() {
            return text;
        }
    }

    private List<Message> messages = new ArrayList<>();

    public void addMessage(Message message) {
        messages.add(message);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public String printAndClear() {
        StringBuffer stringBuffer = new StringBuffer();
        for (Message message : messages) {
            stringBuffer.append(message.getAuthor() + "\n").append(message.getText() + "\n").append("=================\n");
            System.out.println(stringBuffer);
        }
        messages.clear();
        return stringBuffer.toString();
    }

}
