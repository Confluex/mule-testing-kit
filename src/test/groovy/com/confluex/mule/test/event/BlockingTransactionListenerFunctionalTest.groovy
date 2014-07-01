package com.confluex.mule.test.event

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mule.tck.junit4.FunctionalTestCase

class BlockingTransactionListenerFunctionalTest extends FunctionalTestCase {

    public static final int WAIT_TIME = 1500
    BlockingTransactionListener listener

    @Override
    String getConfigFile() {
        'example-flow.xml'
    }

    @Before
    void registerListener() {
        listener = new BlockingTransactionListener()
        muleContext.registerListener listener
    }

    @Test
    void shouldNotifyOnTransaction() {
        muleContext.client.dispatch('txInbox', 'thePayload', [:])

        assert listener.waitForTransaction(WAIT_TIME)
        assert null != listener.transactionId
    }

    @Test
    void shouldNotifyOnCommit() {
        muleContext.client.dispatch('txInbox', 'thePayload', [:])
        assert listener.waitForCommit(WAIT_TIME)
    }

    @Test
    void shouldNotNotifyCommitWhenRolledBack() {
        muleContext.client.dispatch('txInbox', 'POISON', [:])
        assert ! listener.waitForCommit(WAIT_TIME)
    }

    @Test
    void shouldNotifyOnRollback() {
        muleContext.client.dispatch('txInbox', 'POISON', [:])
        assert listener.waitForRollback(WAIT_TIME)
    }

    @Test
    void shouldNotNotifyRollbackWhenCommitted() {
        muleContext.client.dispatch('txInbox', 'thePayload', [:])
        assert ! listener.waitForRollback(WAIT_TIME)
    }
}
