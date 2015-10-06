package com.confluex.mule.test.event

import com.confluex.mule.test.BetterFunctionalTestCase
import groovy.util.logging.Slf4j
import org.junit.Before
import org.junit.Test
import org.mule.tck.junit4.FunctionalTestCase

@Slf4j
class BlockingTransactionListenerFunctionalTest extends BetterFunctionalTestCase {

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
    void shouldNotifyOnCommit() {
        String txId = publishToFlowAndCaptureTxId('thePayload')
        assert listener.waitForCommit(txId, WAIT_TIME)
    }

    @Test
    void shouldNotNotifyCommitWhenRolledBack() {
        String txId = publishToFlowAndCaptureTxId('POISON')
        assert ! listener.waitForCommit(txId, WAIT_TIME)
    }

    @Test
    void shouldNotifyOnRollback() {
        String txId = publishToFlowAndCaptureTxId('POISON')

        assert listener.waitForRollback(txId, WAIT_TIME)
    }

    @Test
    void shouldNotNotifyRollbackWhenCommitted() {
        String txId = publishToFlowAndCaptureTxId('thePayload')
        assert ! listener.waitForRollback(txId, WAIT_TIME)
    }

    private String publishToFlowAndCaptureTxId(String payload) {
        BlockingMessageProcessorListener messageListener = listenForMessageProcessor('txidCapture')

        muleContext.client.dispatch('txInbox', payload, [:])
        messageListener.waitForMessages()
        def transactionId = messageListener.messages[0].getOutboundProperty('txid')
        log.debug "TXID: ${transactionId}"
        return transactionId
    }
}
