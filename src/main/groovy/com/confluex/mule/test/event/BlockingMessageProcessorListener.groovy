package com.confluex.mule.test.event

import com.confluex.mule.test.ConfigurationDetector
import groovy.util.logging.Slf4j
import org.mule.api.MuleMessage
import org.mule.api.context.notification.MessageProcessorNotificationListener
import org.mule.context.notification.MessageProcessorNotification

/**
 * This can be useful for testing events without having to modify your config files or mock out
 * fake message processors. You can attach this listener to a named message processor and block until
 * the required number of messages have been processed.
 */
@Slf4j
class BlockingMessageProcessorListener extends BaseBlockingMessageListener<MessageProcessorNotification> implements MessageProcessorNotificationListener<MessageProcessorNotification> {
    final String name
    ConfigurationDetector configurationDetector

    /**
     * Creates a new listener and count down latch.
     *
     * @param name the name of the message processor
     * @param expectedCount the number of expected messages (default = 1)
     */
    public BlockingMessageProcessorListener(String name, Integer expectedCount = 1) {
        super(expectedCount)
        this.name = name;
    }

    @Override
    protected boolean matches(MessageProcessorNotification notification) {
        if (notification.action == MessageProcessorNotification.MESSAGE_PROCESSOR_POST_INVOKE) {
            if (matchesExpectedProcessorName(notification)) {
                log.debug "Matched MessageProcessorNotification for ${notification.processor.class.simpleName} $name"
                return true
            }
        }
        logUnmatchedNotification(notification)
        return false
    }

    @Override
    protected MuleMessage getMessage(MessageProcessorNotification notification) {
        return notification.source.message
    }

    protected boolean matchesExpectedProcessorName(MessageProcessorNotification notification) {
        try {
            return configurationDetector.beans.containsKey(this.name) &&
                    configurationDetector.beans[this.name].contains(notification.processor)
        } catch (MissingPropertyException e) {
            return false
        }
    }

    /**
     * At TRACE level, log every ignored message processor
     * At DEBUG level, log only the non-mule message processors we ignored because we can't determine the name
     */
    private void logUnmatchedNotification(MessageProcessorNotification notification) {
        try {
            if (log.debugEnabled) { // intentionally checking for debug and logging at trace, we want to trip the exception if debug or higher
                log.trace "Ignored MessageProcessorNotification for ${notification.processor.class.simpleName}: ${notification.processor.name}"
            }
        } catch (MissingPropertyException e) {
            if (log.debugEnabled &&
                    ! notification.processor.class.package.name ==~ /(org\/mule|com.mulesoft)\..+/) {
                    log.debug "Ignored MessageProcessorNotification, unable to determine name of MessageProcessor ${notification.processor.class.name}"
            } else if (log.traceEnabled) {
                log.trace "Ignored MessageProcessorNotification, unable to determine processor name for ${notification.processor.class.name}"
            }
        }
    }

    public void setConfigurationDetector(ConfigurationDetector configurationDetector) {
        this.configurationDetector = configurationDetector
    }
}
