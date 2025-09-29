package com.spmorangle.crm.taskmanagement.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Comment Events Tests")
class CommentEventsTest {

    @Nested
    @DisplayName("CommentCreatedEvent Tests")
    class CommentCreatedEventTests {

        @Test
        @DisplayName("Should create event with valid parameters")
        void commentCreatedEvent_WithValidParameters_ShouldCreateCorrectly() {
            // Arrange & Act
            CommentCreatedEvent event = new CommentCreatedEvent(1L, 10L);

            // Assert
            assertEquals(1L, event.getCommentId());
            assertEquals(10L, event.getAuthorId());
            assertEquals("COMMENT_CREATED", event.getEventType());
        }

        @Test
        @DisplayName("Should create event with zero and negative IDs")
        void commentCreatedEvent_WithZeroAndNegativeIds_ShouldCreateCorrectly() {
            // Arrange & Act
            CommentCreatedEvent zeroEvent = new CommentCreatedEvent(0L, 0L);
            CommentCreatedEvent negativeEvent = new CommentCreatedEvent(-1L, -2L);

            // Assert
            assertEquals(0L, zeroEvent.getCommentId());
            assertEquals(0L, zeroEvent.getAuthorId());
            assertEquals("COMMENT_CREATED", zeroEvent.getEventType());

            assertEquals(-1L, negativeEvent.getCommentId());
            assertEquals(-2L, negativeEvent.getAuthorId());
            assertEquals("COMMENT_CREATED", negativeEvent.getEventType());
        }

        @Test
        @DisplayName("Should create event with null IDs")
        void commentCreatedEvent_WithNullIds_ShouldCreateCorrectly() {
            // Arrange & Act
            CommentCreatedEvent event = new CommentCreatedEvent(null, null);

            // Assert
            assertNull(event.getCommentId());
            assertNull(event.getAuthorId());
            assertEquals("COMMENT_CREATED", event.getEventType());
        }

        @Test
        @DisplayName("Should be instance of CommentEvent")
        void commentCreatedEvent_ShouldBeInstanceOfCommentEvent() {
            // Arrange & Act
            CommentCreatedEvent event = new CommentCreatedEvent(1L, 10L);

            // Assert
            assertInstanceOf(CommentEvent.class, event);
        }

        @Test
        @DisplayName("Should have immutable event type")
        void commentCreatedEvent_ShouldHaveImmutableEventType() {
            // Arrange & Act
            CommentCreatedEvent event1 = new CommentCreatedEvent(1L, 10L);
            CommentCreatedEvent event2 = new CommentCreatedEvent(2L, 20L);

            // Assert
            assertEquals("COMMENT_CREATED", event1.getEventType());
            assertEquals("COMMENT_CREATED", event2.getEventType());
            assertEquals(event1.getEventType(), event2.getEventType());
        }
    }

    @Nested
    @DisplayName("CommentEditedEvent Tests")
    class CommentEditedEventTests {

        @Test
        @DisplayName("Should create event with valid parameters")
        void commentEditedEvent_WithValidParameters_ShouldCreateCorrectly() {
            // Arrange & Act
            CommentEditedEvent event = new CommentEditedEvent(5L, 15L);

            // Assert
            assertEquals(5L, event.getCommentId());
            assertEquals(15L, event.getAuthorId());
            assertEquals("COMMENT_EDITED", event.getEventType());
        }

        @Test
        @DisplayName("Should create event with zero and negative IDs")
        void commentEditedEvent_WithZeroAndNegativeIds_ShouldCreateCorrectly() {
            // Arrange & Act
            CommentEditedEvent zeroEvent = new CommentEditedEvent(0L, 0L);
            CommentEditedEvent negativeEvent = new CommentEditedEvent(-5L, -10L);

            // Assert
            assertEquals(0L, zeroEvent.getCommentId());
            assertEquals(0L, zeroEvent.getAuthorId());
            assertEquals("COMMENT_EDITED", zeroEvent.getEventType());

            assertEquals(-5L, negativeEvent.getCommentId());
            assertEquals(-10L, negativeEvent.getAuthorId());
            assertEquals("COMMENT_EDITED", negativeEvent.getEventType());
        }

        @Test
        @DisplayName("Should create event with null IDs")
        void commentEditedEvent_WithNullIds_ShouldCreateCorrectly() {
            // Arrange & Act
            CommentEditedEvent event = new CommentEditedEvent(null, null);

            // Assert
            assertNull(event.getCommentId());
            assertNull(event.getAuthorId());
            assertEquals("COMMENT_EDITED", event.getEventType());
        }

        @Test
        @DisplayName("Should be instance of CommentEvent")
        void commentEditedEvent_ShouldBeInstanceOfCommentEvent() {
            // Arrange & Act
            CommentEditedEvent event = new CommentEditedEvent(5L, 15L);

            // Assert
            assertInstanceOf(CommentEvent.class, event);
        }

        @Test
        @DisplayName("Should have immutable event type")
        void commentEditedEvent_ShouldHaveImmutableEventType() {
            // Arrange & Act
            CommentEditedEvent event1 = new CommentEditedEvent(1L, 10L);
            CommentEditedEvent event2 = new CommentEditedEvent(2L, 20L);

            // Assert
            assertEquals("COMMENT_EDITED", event1.getEventType());
            assertEquals("COMMENT_EDITED", event2.getEventType());
            assertEquals(event1.getEventType(), event2.getEventType());
        }
    }

    @Nested
    @DisplayName("MentionEvent Tests")
    class MentionEventTests {

        @Test
        @DisplayName("Should create event with valid parameters and mentions")
        void mentionEvent_WithValidParametersAndMentions_ShouldCreateCorrectly() {
            // Arrange
            List<Long> mentionedUsers = Arrays.asList(20L, 30L, 40L);

            // Act
            MentionEvent event = new MentionEvent(8L, 18L, mentionedUsers);

            // Assert
            assertEquals(8L, event.getCommentId());
            assertEquals(18L, event.getAuthorId());
            assertEquals("MENTION", event.getEventType());
            assertEquals(mentionedUsers, event.getMentionedUserIds());
            assertEquals(3, event.getMentionedUserIds().size());
        }

        @Test
        @DisplayName("Should create event with empty mentions list")
        void mentionEvent_WithEmptyMentionsList_ShouldCreateCorrectly() {
            // Arrange
            List<Long> emptyMentions = Collections.emptyList();

            // Act
            MentionEvent event = new MentionEvent(8L, 18L, emptyMentions);

            // Assert
            assertEquals(8L, event.getCommentId());
            assertEquals(18L, event.getAuthorId());
            assertEquals("MENTION", event.getEventType());
            assertTrue(event.getMentionedUserIds().isEmpty());
        }

        @Test
        @DisplayName("Should create event with null mentions list")
        void mentionEvent_WithNullMentionsList_ShouldCreateCorrectly() {
            // Arrange & Act
            MentionEvent event = new MentionEvent(8L, 18L, null);

            // Assert
            assertEquals(8L, event.getCommentId());
            assertEquals(18L, event.getAuthorId());
            assertEquals("MENTION", event.getEventType());
            assertNull(event.getMentionedUserIds());
        }

        @Test
        @DisplayName("Should create event with single mention")
        void mentionEvent_WithSingleMention_ShouldCreateCorrectly() {
            // Arrange
            List<Long> singleMention = Collections.singletonList(25L);

            // Act
            MentionEvent event = new MentionEvent(8L, 18L, singleMention);

            // Assert
            assertEquals(8L, event.getCommentId());
            assertEquals(18L, event.getAuthorId());
            assertEquals("MENTION", event.getEventType());
            assertEquals(singleMention, event.getMentionedUserIds());
            assertEquals(1, event.getMentionedUserIds().size());
            assertEquals(25L, event.getMentionedUserIds().get(0));
        }

        @Test
        @DisplayName("Should create event with duplicate mentions")
        void mentionEvent_WithDuplicateMentions_ShouldCreateCorrectly() {
            // Arrange
            List<Long> duplicateMentions = Arrays.asList(20L, 30L, 20L, 30L, 40L);

            // Act
            MentionEvent event = new MentionEvent(8L, 18L, duplicateMentions);

            // Assert
            assertEquals(8L, event.getCommentId());
            assertEquals(18L, event.getAuthorId());
            assertEquals("MENTION", event.getEventType());
            assertEquals(duplicateMentions, event.getMentionedUserIds());
            assertEquals(5, event.getMentionedUserIds().size()); // Preserves duplicates
        }

        @Test
        @DisplayName("Should create event with zero and negative IDs")
        void mentionEvent_WithZeroAndNegativeIds_ShouldCreateCorrectly() {
            // Arrange
            List<Long> mentionsWithEdgeCases = Arrays.asList(0L, -1L, -5L, 10L);

            // Act
            MentionEvent zeroEvent = new MentionEvent(0L, 0L, mentionsWithEdgeCases);
            MentionEvent negativeEvent = new MentionEvent(-1L, -2L, mentionsWithEdgeCases);

            // Assert
            assertEquals(0L, zeroEvent.getCommentId());
            assertEquals(0L, zeroEvent.getAuthorId());
            assertEquals(mentionsWithEdgeCases, zeroEvent.getMentionedUserIds());

            assertEquals(-1L, negativeEvent.getCommentId());
            assertEquals(-2L, negativeEvent.getAuthorId());
            assertEquals(mentionsWithEdgeCases, negativeEvent.getMentionedUserIds());
        }

        @Test
        @DisplayName("Should create event with null comment and author IDs")
        void mentionEvent_WithNullCommentAndAuthorIds_ShouldCreateCorrectly() {
            // Arrange
            List<Long> mentionedUsers = Arrays.asList(20L, 30L);

            // Act
            MentionEvent event = new MentionEvent(null, null, mentionedUsers);

            // Assert
            assertNull(event.getCommentId());
            assertNull(event.getAuthorId());
            assertEquals("MENTION", event.getEventType());
            assertEquals(mentionedUsers, event.getMentionedUserIds());
        }

        @Test
        @DisplayName("Should be instance of CommentEvent")
        void mentionEvent_ShouldBeInstanceOfCommentEvent() {
            // Arrange
            List<Long> mentionedUsers = Arrays.asList(20L, 30L);

            // Act
            MentionEvent event = new MentionEvent(8L, 18L, mentionedUsers);

            // Assert
            assertInstanceOf(CommentEvent.class, event);
        }

        @Test
        @DisplayName("Should have immutable event type")
        void mentionEvent_ShouldHaveImmutableEventType() {
            // Arrange
            List<Long> mentions1 = Arrays.asList(20L, 30L);
            List<Long> mentions2 = Arrays.asList(40L, 50L);

            // Act
            MentionEvent event1 = new MentionEvent(1L, 10L, mentions1);
            MentionEvent event2 = new MentionEvent(2L, 20L, mentions2);

            // Assert
            assertEquals("MENTION", event1.getEventType());
            assertEquals("MENTION", event2.getEventType());
            assertEquals(event1.getEventType(), event2.getEventType());
        }

        @Test
        @DisplayName("Should handle large mentions list")
        void mentionEvent_WithLargeMentionsList_ShouldCreateCorrectly() {
            // Arrange
            List<Long> largeMentionsList = java.util.stream.IntStream.range(1, 101)
                    .mapToLong(i -> (long) i)
                    .boxed()
                    .collect(java.util.stream.Collectors.toList());

            // Act
            MentionEvent event = new MentionEvent(1L, 10L, largeMentionsList);

            // Assert
            assertEquals(1L, event.getCommentId());
            assertEquals(10L, event.getAuthorId());
            assertEquals("MENTION", event.getEventType());
            assertEquals(100, event.getMentionedUserIds().size());
            assertEquals(largeMentionsList, event.getMentionedUserIds());
        }
    }

    @Nested
    @DisplayName("Event Hierarchy Tests")
    class EventHierarchyTests {

        @Test
        @DisplayName("All events should have different event types")
        void allEvents_ShouldHaveDifferentEventTypes() {
            // Arrange & Act
            CommentCreatedEvent createdEvent = new CommentCreatedEvent(1L, 10L);
            CommentEditedEvent editedEvent = new CommentEditedEvent(1L, 10L);
            MentionEvent mentionEvent = new MentionEvent(1L, 10L, Arrays.asList(20L));

            // Assert
            assertNotEquals(createdEvent.getEventType(), editedEvent.getEventType());
            assertNotEquals(createdEvent.getEventType(), mentionEvent.getEventType());
            assertNotEquals(editedEvent.getEventType(), mentionEvent.getEventType());

            assertEquals("COMMENT_CREATED", createdEvent.getEventType());
            assertEquals("COMMENT_EDITED", editedEvent.getEventType());
            assertEquals("MENTION", mentionEvent.getEventType());
        }

        @Test
        @DisplayName("All events should extend CommentEvent")
        void allEvents_ShouldExtendCommentEvent() {
            // Arrange & Act
            CommentCreatedEvent createdEvent = new CommentCreatedEvent(1L, 10L);
            CommentEditedEvent editedEvent = new CommentEditedEvent(1L, 10L);
            MentionEvent mentionEvent = new MentionEvent(1L, 10L, Arrays.asList(20L));

            // Assert
            assertInstanceOf(CommentEvent.class, createdEvent);
            assertInstanceOf(CommentEvent.class, editedEvent);
            assertInstanceOf(CommentEvent.class, mentionEvent);
        }

        @Test
        @DisplayName("Events with same parameters should have same base properties")
        void eventsWithSameParameters_ShouldHaveSameBaseProperties() {
            // Arrange
            Long commentId = 100L;
            Long authorId = 200L;

            // Act
            CommentCreatedEvent createdEvent = new CommentCreatedEvent(commentId, authorId);
            CommentEditedEvent editedEvent = new CommentEditedEvent(commentId, authorId);
            MentionEvent mentionEvent = new MentionEvent(commentId, authorId, Arrays.asList(300L));

            // Assert
            assertEquals(commentId, createdEvent.getCommentId());
            assertEquals(commentId, editedEvent.getCommentId());
            assertEquals(commentId, mentionEvent.getCommentId());

            assertEquals(authorId, createdEvent.getAuthorId());
            assertEquals(authorId, editedEvent.getAuthorId());
            assertEquals(authorId, mentionEvent.getAuthorId());
        }
    }
}