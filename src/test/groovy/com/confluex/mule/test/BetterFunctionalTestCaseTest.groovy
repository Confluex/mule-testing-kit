package com.confluex.mule.test

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mule.api.MuleContext
import org.mule.api.lifecycle.Disposable
import org.mule.api.lifecycle.Initialisable
import org.mule.api.lifecycle.Startable
import org.mule.api.lifecycle.Stoppable

class BetterFunctionalTestCaseTest extends BetterFunctionalTestCase {
    String beforeMulePhase
    String beforePhase
    String expectedAfterPhase
    String expectedAfterMulePhase

    @Override
    protected String getConfigResources() {
        return ''
    }

    @BeforeMule
    void capturePhaseNameBeforeMuleStarts(MuleContext context) {
        beforeMulePhase = context.lifecycleManager.currentPhase
    }

    @Before
    void capturePhaseNameBeforeTestMethod() {
        beforePhase = muleContext.lifecycleManager.currentPhase
    }

    @After
    void capturePhaseNameAfterTestMethod() {
        assert muleContext.lifecycleManager.currentPhase == expectedAfterPhase
    }

    @AfterMule
    void capturePhaseNameAfterMuleStops() {
        assert muleContext.lifecycleManager.currentPhase == expectedAfterMulePhase
    }

    @Test
    void methodsAnnotatedBeforeMuleShouldRunBeforeMuleContextStarts() {
        assert beforeMulePhase == Initialisable.PHASE_NAME
        assert beforePhase == Startable.PHASE_NAME
    }

    @Test
    void methodsAnnotatedAfterMuleShouldRunAfterMuleContextStops() {
        expectedAfterPhase = Startable.PHASE_NAME
        expectedAfterMulePhase = Disposable.PHASE_NAME
    }
}
