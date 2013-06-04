package com.confluex.mule.test.event

import org.mule.api.context.notification.EndpointMessageNotificationListener
import org.mule.context.notification.EndpointMessageNotification
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * Adds some simple logging around endpoint notifications to help diagnose activity and performance problems.
 */

class LoggingEndpointListener implements EndpointMessageNotificationListener<EndpointMessageNotification> {
    Logger log = LoggerFactory.getLogger(this.class)
    String endpointName
    Boolean logPayload = false
    Lock lock = new ReentrantLock()

    @Override
    void onNotification(EndpointMessageNotification notification) {
        if (!endpointName || notification.endpoint == endpointName) {
            log.debug("endpoint={},id={},flow={},action={}", notification.endpoint, notification.source.uniqueId, notification.flowConstruct.name, notification.actionName)
            //don't invoke payloadAsString
            if (logPayload) {
                log.debug("endpoint={},id={},payload={}", notification.endpoint, notification.source.uniqueId, notification.source.payloadAsString)
            }
        }

    }
}
