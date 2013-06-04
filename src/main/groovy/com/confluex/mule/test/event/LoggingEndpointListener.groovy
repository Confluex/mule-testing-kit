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
    Map<String, Date> timeTracker = [:]

    @Override
    void onNotification(EndpointMessageNotification notification) {
        if (!endpointName || notification.endpoint == endpointName) {
            def id = notification.source.uniqueId
            def flow = notification.flowConstruct.name
            def action = notification.actionName
            def endpoint = notification.endpoint
            def lastSeen = findLastSeenInSecs(id)
            log.debug("endpoint={},id={},flow={},lastSeen={},action={}", endpoint, id, flow, lastSeen, action)
            //don't invoke payloadAsString
            if (logPayload) {
                log.debug("endpoint={},id={},payload={}", endpoint, id, notification.source?.payloadAsString)
            }
        }

    }

    protected Long findLastSeenInSecs(String id) {
        def now = new Date()
        doWithLock {
            def lastSeen = timeTracker[id]
            timeTracker[id] = now
            if (!lastSeen) {
                return 0
            }
            return now.time - lastSeen.time
        }

    }

    def doWithLock = { closure ->
        try {
            lock.lock()
            closure()
        }
        finally {
            lock.unlock()
        }
    }
}
