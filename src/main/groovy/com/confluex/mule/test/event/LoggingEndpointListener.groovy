package com.confluex.mule.test.event

import groovy.util.logging.Slf4j
import org.mule.api.context.notification.EndpointMessageNotificationListener
import org.mule.context.notification.EndpointMessageNotification

/**
 * Adds some simple logging around endpoint notifications to help diagnose activity and performance problems.
 */
@Slf4j
class LoggingEndpointListener implements EndpointMessageNotificationListener<EndpointMessageNotification> {

    @Override
    void onNotification(EndpointMessageNotification notification) {
        log.debug("endpoint={} type={} id={}", notification.endpoint, notification.type, notification.source.correlationId)
    }
}
