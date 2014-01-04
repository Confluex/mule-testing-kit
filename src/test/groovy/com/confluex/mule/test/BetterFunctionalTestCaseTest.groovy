package com.confluex.mule.test

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mule.api.MuleContext
import org.mule.api.lifecycle.Disposable
import org.mule.api.lifecycle.Initialisable
import org.mule.api.lifecycle.Startable

class BetterFunctionalTestCaseTest extends BetterFunctionalTestCase {
    String beforeMulePhase
    String beforePhase
    Closure afterPhaseAssertion
    Closure afterMulePhaseAssertion

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
        afterPhaseAssertion?.call()
    }

    @AfterMule
    void capturePhaseNameAfterMuleStops() {
        afterMulePhaseAssertion?.call()
    }

    @Test
    void methodsAnnotatedBeforeMuleShouldRunBeforeMuleContextStarts() {
        assert beforeMulePhase == Initialisable.PHASE_NAME
        assert beforePhase == Startable.PHASE_NAME
    }

    @Test
    void methodsAnnotatedAfterMuleShouldRunAfterMuleContextStops() {
        afterPhaseAssertion = {
            assert muleContext.lifecycleManager.currentPhase == Startable.PHASE_NAME
        }
        afterMulePhaseAssertion = {
            assert muleContext.lifecycleManager.currentPhase == Disposable.PHASE_NAME
        }
    }
}
