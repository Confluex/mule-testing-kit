package com.confluex.mule.test.event

import groovy.util.logging.Slf4j
import org.mule.api.MuleMessage
import org.mule.api.context.notification.ServerNotification
import org.mule.context.notification.EndpointMessageNotification
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

abstract class BaseBlockingMessageListener<T extends ServerNotification> extends BaseBlockingEventListener<T> {
    private ConcurrentLinkedQueue<MuleMessage> messageQueue = new ConcurrentLinkedQueue<MuleMessage>()

    BaseBlockingMessageListener(Integer expectedCount = 1) {
        super(expectedCount)
    }

    abstract protected MuleMessage getMessage(T notification)

    @Override
    void onMatched(T notification) {
        putMessage getMessage(notification)
    }

    /**
     * Block until the expected number of message notifications have occurred.
     *
     * @param timeout the number of ms to wait until we give up
     * @return true if all expected messages have flowed through. false if timed out.
     */
    public Boolean waitForMessages(long timeout = 10000) {
        return super.waitForEvents(timeout)
    }

    public List<MuleMessage> getMessages() {
        new LinkedList<MuleMessage>(messageQueue)
    }

    protected void putMessage(MuleMessage message) {
        messageQueue.offer message
    }

}
