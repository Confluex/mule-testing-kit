package com.confluex.mule.test.event

import groovy.util.logging.Slf4j
import org.mule.api.MuleMessage
import org.mule.api.context.notification.EndpointMessageNotificationListener
import org.mule.context.notification.EndpointMessageNotification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * This can be useful for testing events without having to modify your config files or mock out
 * fake endpoints. You can attach this listener to endpoints and block until the required number
 * of messages have passed through the endpoint.
 */
@Slf4j
public class BlockingEndpointListener extends BaseBlockingListener<EndpointMessageNotification> implements EndpointMessageNotificationListener<EndpointMessageNotification> {

    final String endpointName
    List<Integer> actions = [EndpointMessageNotification.MESSAGE_DISPATCHED, EndpointMessageNotification.MESSAGE_SENT]

    /**
     * Creates a new listener and count down latch.
     *
     * @param endpointName the name of the GLOBAL endpoint
     * @param expectedCount the number of expected messages (default = 1)
     */
    public BlockingEndpointListener(String endpointName, Integer expectedCount = 1) {
        super(expectedCount)
        this.endpointName = endpointName;
    }

    @Override
    protected boolean matches(EndpointMessageNotification notification) {
        endpointName == notification.immutableEndpoint.name &&
                actions.contains(notification.action)
    }

    @Override
    protected MuleMessage getMessage(EndpointMessageNotification notification) {
        notification.source
    }
}