package com.confluex.mule.test.event

import org.junit.Before
import org.junit.Test
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage
import org.mule.context.notification.MessageProcessorNotification
import org.mule.tck.testmodels.mule.TestMessageProcessor

import static org.mockito.Mockito.*

class BlockingMessageProcessorListenerTest {

    BlockingMessageProcessorListener listener

    @Before
    void initListener() {
         listener = new BlockingMessageProcessorListener('theProcessorName')
    }

    @Test
    public void shouldCountdown_WhenMatchingNotificationReceived() {
        assert 1 == listener.latch.count
        listener.onNotification makeNotification('theProcessorName')
        assert 0 == listener.latch.count
    }

    @Test
    void shouldBlockUntilMatchingNotificationReceived() {
        boolean threadBlocked = true
        boolean waitForMessagesResult = false
        def blockedThread = Thread.start {
            waitForMessagesResult = listener.waitForMessages()
            threadBlocked = false
        }

        if (! threadBlocked) throw new RuntimeException('Test code is broken, the thread should still be blocked')

        listener.onNotification makeNotification('theProcessorName')
        blockedThread.join(100)

        assert !threadBlocked
        assert waitForMessagesResult
    }

    @Test
    void shouldStayBlocked_WhenUnmatchedNotificationReceived() {
        boolean threadBlocked = true
        boolean waitForMessagesResult = false
        final def TIMEOUT = 500

        def blockedThread = Thread.start {
            waitForMessagesResult = listener.waitForMessages(TIMEOUT)
            threadBlocked = false
        }

        if (! threadBlocked) throw new RuntimeException('Test code is broken, the thread should still be blocked')

        listener.onNotification makeNotification('aDifferentProcessorName')

        blockedThread.join(100)
        assert threadBlocked // the notification should not have matched, so the thread should still be waiting

        blockedThread.join(TIMEOUT)
        if (threadBlocked) throw new RuntimeException('Test code is broken, the thread should not be blocked anymore because listener.waitForMessages was given a short timeout')

        assert ! waitForMessagesResult // since no matching notification ever happened, waitForMessages should have returned false
    }

    @Test
    void shouldStayBlocked_WhenPreInvokeNotificationForMatchedMessageProcessor() {
        boolean threadBlocked = true
        boolean waitForMessagesResult = false
        final def TIMEOUT = 500

        def blockedThread = Thread.start {
            waitForMessagesResult = listener.waitForMessages(TIMEOUT)
            threadBlocked = false
        }

        if (! threadBlocked) throw new RuntimeException('Test code is broken, the thread should still be blocked')

        def notification = makeNotification('theProcessorName')
        when(notification.action).thenReturn(MessageProcessorNotification.MESSAGE_PROCESSOR_PRE_INVOKE)
        listener.onNotification notification

        blockedThread.join(100)
        assert threadBlocked // the notification should not have matched, so the thread should still be waiting

        blockedThread.join(TIMEOUT)
        if (threadBlocked) throw new RuntimeException('Test code is broken, the thread should not be blocked anymore because listener.waitForMessages was given a short timeout')

        assert ! waitForMessagesResult // since no matching notification ever happened, waitForMessages should have returned false
    }

    @Test
    void shouldRetainMessagesForMatchedEvents() {
        def message1 = mock(MuleMessage)
        def message2 = mock(MuleMessage)
        def message3 = mock(MuleMessage)
        def unwantedMessage = mock(MuleMessage)

        def (notification1, notification3, notification4) = (1..3).collect { makeNotification('theProcessorName') }
        def notification2 = makeNotification('someOtherProcessorName')

        when(notification1.source.message).thenReturn(message1)
        when(notification2.source.message).thenReturn(unwantedMessage)
        when(notification3.source.message).thenReturn(message2)
        when(notification4.source.message).thenReturn(message3)

        [notification1, notification2, notification3, notification4].each { notification ->
            listener.onNotification notification
        }

        [message1, message2, message3].each {
            assert listener.messages.contains(message1)
        }

        assert ! listener.messages.contains(unwantedMessage)
    }

    MessageProcessorNotification makeNotification(String name) {
        def processor = new TestMessageProcessor("stuff to append")
        processor.name = name

        def muleEvent = mock(MuleEvent)
        when(muleEvent.getMessage()).thenReturn(mock(MuleMessage))

        def notification = mock(MessageProcessorNotification)
        when(notification.processor).thenReturn(processor)
        when(notification.action).thenReturn(MessageProcessorNotification.MESSAGE_PROCESSOR_POST_INVOKE)
        when(notification.source).thenReturn(muleEvent)
        return notification
    }
}
