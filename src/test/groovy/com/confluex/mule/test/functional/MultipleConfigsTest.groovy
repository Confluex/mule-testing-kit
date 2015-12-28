package com.confluex.mule.test.functional;

import com.confluex.mule.test.BetterFunctionalTestCase
import org.junit.Test;

public class MultipleConfigsTest extends BetterFunctionalTestCase {

    @Override
    String[] getConfigFiles() {
        ['mule-config-1.xml', 'mule-config-2.xml', 'mule-config-3.xml']
    }

    @Test
    void flowsInAllConfigurationFilesShouldRun() {
        def listener = listenForEndpoint('outbox')
        muleContext.client.dispatch('vm://multiple.test.inbox', 'anything', [:])
        assert listener.waitForMessages()
    }
}
