package com.confluex.mule.test.event

import com.confluex.mule.test.BetterFunctionalTestCase
import groovy.util.logging.Slf4j
import org.junit.Test
import org.mule.tck.junit4.FunctionalTestCase

@Slf4j
class BlockingMessageProcessorListenerFunctionalTest extends BetterFunctionalTestCase {

    @Override
    public String getConfigFile() {
        'example-flow.xml'
    }

    @Test
    void shouldNotifyWhenProcessorFinishes() {
        BlockingMessageProcessorListener listener = listenForMessageProcessor('identityTransformer')

        assert 0 == listener.messages.size()
        assert ! listener.waitForMessages(200)
        muleContext.client.dispatch('inbox', 'thePayload', [:])
        assert listener.waitForMessages(1000)
        assert 1 == listener.messages.size()
        assert 'thePayload' == listener.messages[0].payload
    }

    @Test
    void shouldNotNotifyWhenSomeOtherProcessorRuns() {
        BlockingMessageProcessorListener listener = listenForMessageProcessor('identityTransformer')

        muleContext.client.dispatch('otherInbox', 'thePayload', [:])
        assert ! listener.waitForMessages(1000)
    }
}
