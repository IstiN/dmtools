package com.github.istin.dmtools.microsoft.teams;

import com.github.istin.dmtools.microsoft.teams.model.ChatMessage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for TeamsMessageSimplifier utility class.
 */
public class TeamsMessageSimplifierTest {
    
    private ChatMessage simpleMessage;
    private ChatMessage messageWithReactions;
    private ChatMessage messageWithMentions;
    private ChatMessage messageWithAttachments;
    private ChatMessage systemMessage;
    
    @Before
    public void setUp() {
        // Simple message with basic fields
        JSONObject simpleJson = new JSONObject();
        simpleJson.put("id", "1");
        simpleJson.put("messageType", "message");
        simpleJson.put("createdDateTime", "2025-10-09T10:00:00Z");
        simpleJson.put("from", new JSONObject()
            .put("user", new JSONObject()
                .put("displayName", "John Doe")
                .put("id", "user-1")));
        simpleJson.put("body", new JSONObject()
            .put("content", "<p>Hello <strong>world</strong>!</p>")
            .put("contentType", "html"));
        simpleMessage = new ChatMessage(simpleJson.toString());
        
        // Message with reactions
        JSONObject reactionsJson = new JSONObject();
        reactionsJson.put("id", "2");
        reactionsJson.put("messageType", "message");
        reactionsJson.put("createdDateTime", "2025-10-09T11:00:00Z");
        reactionsJson.put("from", new JSONObject()
            .put("user", new JSONObject()
                .put("displayName", "Jane Smith")
                .put("id", "user-2")));
        reactionsJson.put("body", new JSONObject()
            .put("content", "Great idea!")
            .put("contentType", "text"));
        reactionsJson.put("reactions", new JSONArray()
            .put(new JSONObject()
                .put("reactionType", "❤️")
                .put("user", new JSONObject().put("displayName", "User1")))
            .put(new JSONObject()
                .put("reactionType", "❤️")
                .put("user", new JSONObject().put("displayName", "User2")))
            .put(new JSONObject()
                .put("reactionType", "👍")
                .put("user", new JSONObject().put("displayName", "User3"))));
        messageWithReactions = new ChatMessage(reactionsJson.toString());
        
        // Message with mentions
        JSONObject mentionsJson = new JSONObject();
        mentionsJson.put("id", "3");
        mentionsJson.put("messageType", "message");
        mentionsJson.put("createdDateTime", "2025-10-09T12:00:00Z");
        mentionsJson.put("from", new JSONObject()
            .put("user", new JSONObject()
                .put("displayName", "Bob Johnson")
                .put("id", "user-3")));
        mentionsJson.put("body", new JSONObject()
            .put("content", "Hey @Alice!")
            .put("contentType", "text"));
        mentionsJson.put("mentions", new JSONArray()
            .put(new JSONObject()
                .put("id", 0)
                .put("mentioned", new JSONObject()
                    .put("user", new JSONObject()
                        .put("displayName", "Alice Brown")
                        .put("id", "user-4")))));
        messageWithMentions = new ChatMessage(mentionsJson.toString());
        
        // Message with attachments
        JSONObject attachmentsJson = new JSONObject();
        attachmentsJson.put("id", "4");
        attachmentsJson.put("messageType", "message");
        attachmentsJson.put("createdDateTime", "2025-10-09T13:00:00Z");
        attachmentsJson.put("from", new JSONObject()
            .put("user", new JSONObject()
                .put("displayName", "Charlie Davis")
                .put("id", "user-5")));
        attachmentsJson.put("body", new JSONObject()
            .put("content", "Check this file")
            .put("contentType", "text"));
        attachmentsJson.put("attachments", new JSONArray()
            .put(new JSONObject()
                .put("id", "att-1")
                .put("contentType", "application/pdf")
                .put("name", "document.pdf"))
            .put(new JSONObject()
                .put("id", "att-2")
                .put("contentType", "messageReference")
                .put("content", new JSONObject()
                    .put("messagePreview", "Previous message text here")
                    .toString())));
        messageWithAttachments = new ChatMessage(attachmentsJson.toString());
        
        // System message (should be filtered out)
        JSONObject systemJson = new JSONObject();
        systemJson.put("id", "5");
        systemJson.put("messageType", "systemEventMessage");
        systemJson.put("createdDateTime", "2025-10-09T14:00:00Z");
        systemJson.put("body", new JSONObject()
            .put("content", "<systemEventMessage/>")
            .put("contentType", "html"));
        systemMessage = new ChatMessage(systemJson.toString());
    }
    
    @Test
    public void testSimplifyMessage_BasicMessage() {
        JSONObject result = TeamsMessageSimplifier.simplifyMessage(simpleMessage);
        
        assertNotNull(result);
        assertEquals("John Doe", result.getString("author"));
        assertEquals("2025-10-09T10:00:00Z", result.getString("date"));
        assertEquals("Hello world!", result.getString("body"));
        assertFalse(result.has("reactions"));
        assertFalse(result.has("mentions"));
        assertFalse(result.has("attachments"));
    }
    
    @Test
    public void testSimplifyMessage_SystemMessage() {
        JSONObject result = TeamsMessageSimplifier.simplifyMessage(systemMessage);
        
        // System messages should be filtered out (return null)
        assertNull(result);
    }
    
    @Test
    public void testSimplifyMessage_WithReactions() {
        JSONObject result = TeamsMessageSimplifier.simplifyMessage(messageWithReactions);
        
        assertNotNull(result);
        assertEquals("Jane Smith", result.getString("author"));
        assertEquals("Great idea!", result.getString("body"));
        assertTrue(result.has("reactions"));
        
        JSONArray reactions = result.getJSONArray("reactions");
        assertEquals(2, reactions.length());
        
        // Check that reactions are grouped and counted
        boolean hasHeartWithCount = false;
        boolean hasThumbsUp = false;
        for (int i = 0; i < reactions.length(); i++) {
            String reaction = reactions.getString(i);
            if (reaction.equals("❤️ ×2")) hasHeartWithCount = true;
            if (reaction.equals("👍")) hasThumbsUp = true;
        }
        assertTrue("Should have heart reaction with count", hasHeartWithCount);
        assertTrue("Should have thumbs up reaction", hasThumbsUp);
    }
    
    @Test
    public void testSimplifyMessage_WithMentions() {
        JSONObject result = TeamsMessageSimplifier.simplifyMessage(messageWithMentions);
        
        assertNotNull(result);
        assertEquals("Bob Johnson", result.getString("author"));
        assertEquals("Hey @Alice!", result.getString("body"));
        assertTrue(result.has("mentions"));
        
        JSONArray mentions = result.getJSONArray("mentions");
        assertEquals(1, mentions.length());
        assertEquals("Alice Brown", mentions.getString(0));
    }
    
    @Test
    public void testSimplifyMessage_WithAttachments() {
        JSONObject result = TeamsMessageSimplifier.simplifyMessage(messageWithAttachments);
        
        assertNotNull(result);
        assertEquals("Charlie Davis", result.getString("author"));
        assertEquals("Check this file", result.getString("body"));
        assertTrue(result.has("attachments"));
        
        JSONArray attachments = result.getJSONArray("attachments");
        assertEquals(2, attachments.length());
        assertEquals("document.pdf (application/pdf)", attachments.getString(0));
        assertEquals("Reply: Previous message text here", attachments.getString(1));
    }
    
    @Test
    public void testSimplifyMessages_MultipleMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(simpleMessage);
        messages.add(messageWithReactions);
        messages.add(systemMessage); // Should be filtered out
        messages.add(messageWithMentions);
        
        JSONArray result = TeamsMessageSimplifier.simplifyMessages(messages);
        
        // System message should be filtered out, so expect 3 results
        assertEquals(3, result.length());
        
        // Verify order and content
        assertEquals("John Doe", result.getJSONObject(0).getString("author"));
        assertEquals("Jane Smith", result.getJSONObject(1).getString("author"));
        assertEquals("Bob Johnson", result.getJSONObject(2).getString("author"));
    }
    
    @Test
    public void testCleanHtml_BasicTags() {
        String html = "<p>Hello <strong>world</strong>!</p>";
        String cleaned = TeamsMessageSimplifier.cleanHtml(html);
        assertEquals("Hello world!", cleaned);
    }
    
    @Test
    public void testCleanHtml_Entities() {
        String html = "Hello&nbsp;world&lt;test&gt;&amp;&quot;quote&#39;";
        String cleaned = TeamsMessageSimplifier.cleanHtml(html);
        assertEquals("Hello world<test>&\"quote'", cleaned);
    }
    
    @Test
    public void testCleanHtml_ComplexTags() {
        String html = "<div class='test'><span style='color:red'>Hello</span>&nbsp;<br/>World</div>";
        String cleaned = TeamsMessageSimplifier.cleanHtml(html);
        assertEquals("Hello World", cleaned);
    }
    
    @Test
    public void testCleanHtml_EmptyAndNull() {
        assertEquals("", TeamsMessageSimplifier.cleanHtml(""));
        assertEquals("", TeamsMessageSimplifier.cleanHtml(null));
    }
    
    @Test
    public void testExtractReactions_SingleReaction() {
        List<ChatMessage.Reaction> reactions = new ArrayList<>();
        JSONObject reactionJson = new JSONObject()
            .put("reactionType", "👍")
            .put("user", new JSONObject().put("displayName", "User1"));
        reactions.add(new ChatMessage.Reaction(reactionJson));
        
        JSONArray result = TeamsMessageSimplifier.extractReactions(reactions);
        
        assertEquals(1, result.length());
        assertEquals("👍", result.getString(0));
    }
    
    @Test
    public void testExtractReactions_MultipleOfSameType() {
        List<ChatMessage.Reaction> reactions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            JSONObject reactionJson = new JSONObject()
                .put("reactionType", "❤️")
                .put("user", new JSONObject().put("displayName", "User" + i));
            reactions.add(new ChatMessage.Reaction(reactionJson));
        }
        
        JSONArray result = TeamsMessageSimplifier.extractReactions(reactions);
        
        assertEquals(1, result.length());
        assertEquals("❤️ ×3", result.getString(0));
    }
    
    @Test
    public void testExtractReactions_EmptyList() {
        JSONArray result = TeamsMessageSimplifier.extractReactions(new ArrayList<>());
        assertEquals(0, result.length());
    }
    
    @Test
    public void testExtractMentions_ValidMention() {
        List<ChatMessage.Mention> mentions = new ArrayList<>();
        JSONObject mentionJson = new JSONObject()
            .put("id", 0)
            .put("mentioned", new JSONObject()
                .put("user", new JSONObject()
                    .put("displayName", "Alice")
                    .put("id", "user-1")));
        mentions.add(new ChatMessage.Mention(mentionJson));
        
        JSONArray result = TeamsMessageSimplifier.extractMentions(mentions);
        
        assertEquals(1, result.length());
        assertEquals("Alice", result.getString(0));
    }
    
    @Test
    public void testExtractMentions_MultipleMentions() {
        List<ChatMessage.Mention> mentions = new ArrayList<>();
        String[] names = {"Alice", "Bob", "Charlie"};
        for (String name : names) {
            JSONObject mentionJson = new JSONObject()
                .put("id", 0)
                .put("mentioned", new JSONObject()
                    .put("user", new JSONObject()
                        .put("displayName", name)
                        .put("id", "user-" + name)));
            mentions.add(new ChatMessage.Mention(mentionJson));
        }
        
        JSONArray result = TeamsMessageSimplifier.extractMentions(mentions);
        
        assertEquals(3, result.length());
        assertEquals("Alice", result.getString(0));
        assertEquals("Bob", result.getString(1));
        assertEquals("Charlie", result.getString(2));
    }
    
    @Test
    public void testExtractAttachments_FileAttachment() {
        List<ChatMessage.Attachment> attachments = new ArrayList<>();
        JSONObject attJson = new JSONObject()
            .put("id", "att-1")
            .put("contentType", "image/png")
            .put("name", "screenshot.png");
        attachments.add(new ChatMessage.Attachment(attJson));
        
        JSONArray result = TeamsMessageSimplifier.extractAttachments(attachments);
        
        assertEquals(1, result.length());
        assertEquals("screenshot.png (image/png)", result.getString(0));
    }
    
    @Test
    public void testExtractAttachments_MessageReference() {
        List<ChatMessage.Attachment> attachments = new ArrayList<>();
        JSONObject replyContent = new JSONObject()
            .put("messagePreview", "Original message text");
        JSONObject attJson = new JSONObject()
            .put("id", "att-1")
            .put("contentType", "messageReference")
            .put("content", replyContent.toString());
        attachments.add(new ChatMessage.Attachment(attJson));
        
        JSONArray result = TeamsMessageSimplifier.extractAttachments(attachments);
        
        assertEquals(1, result.length());
        assertEquals("Reply: Original message text", result.getString(0));
    }
    
    @Test
    public void testExtractAttachments_MessageReference_LongPreview() {
        List<ChatMessage.Attachment> attachments = new ArrayList<>();
        String longText = "a".repeat(150); // 150 chars
        JSONObject replyContent = new JSONObject()
            .put("messagePreview", longText);
        JSONObject attJson = new JSONObject()
            .put("id", "att-1")
            .put("contentType", "messageReference")
            .put("content", replyContent.toString());
        attachments.add(new ChatMessage.Attachment(attJson));
        
        JSONArray result = TeamsMessageSimplifier.extractAttachments(attachments);
        
        assertEquals(1, result.length());
        String reply = result.getString(0);
        assertTrue(reply.startsWith("Reply: "));
        assertTrue(reply.endsWith("..."));
        assertTrue(reply.length() <= 110); // "Reply: " + 97 chars + "..."
    }
    
    @Test
    public void testExtractAttachments_AdaptiveCard() {
        List<ChatMessage.Attachment> attachments = new ArrayList<>();
        JSONArray cardBody = new JSONArray()
            .put(new JSONObject()
                .put("type", "TextBlock")
                .put("text", "YouTube Video Title")
                .put("weight", "bolder"))
            .put(new JSONObject()
                .put("type", "TextBlock")
                .put("text", "Description"));
        JSONObject cardJson = new JSONObject()
            .put("type", "AdaptiveCard")
            .put("body", cardBody);
        JSONObject attJson = new JSONObject()
            .put("id", "att-1")
            .put("contentType", "application/vnd.microsoft.card.adaptive")
            .put("content", cardJson.toString());
        attachments.add(new ChatMessage.Attachment(attJson));
        
        JSONArray result = TeamsMessageSimplifier.extractAttachments(attachments);
        
        assertEquals(1, result.length());
        assertEquals("Card: YouTube Video Title", result.getString(0));
    }
    
    @Test
    public void testExtractAttachments_AdaptiveCard_NoTitle() {
        List<ChatMessage.Attachment> attachments = new ArrayList<>();
        JSONObject cardJson = new JSONObject()
            .put("type", "AdaptiveCard")
            .put("body", new JSONArray());
        JSONObject attJson = new JSONObject()
            .put("id", "att-1")
            .put("contentType", "application/vnd.microsoft.card.adaptive")
            .put("content", cardJson.toString());
        attachments.add(new ChatMessage.Attachment(attJson));
        
        JSONArray result = TeamsMessageSimplifier.extractAttachments(attachments);
        
        assertEquals(1, result.length());
        assertEquals("Adaptive Card", result.getString(0));
    }
    
    @Test
    public void testExtractAttachments_NoName() {
        List<ChatMessage.Attachment> attachments = new ArrayList<>();
        JSONObject attJson = new JSONObject()
            .put("id", "att-1")
            .put("contentType", "application/octet-stream");
        attachments.add(new ChatMessage.Attachment(attJson));
        
        JSONArray result = TeamsMessageSimplifier.extractAttachments(attachments);
        
        assertEquals(1, result.length());
        assertEquals("application/octet-stream", result.getString(0));
    }
}

