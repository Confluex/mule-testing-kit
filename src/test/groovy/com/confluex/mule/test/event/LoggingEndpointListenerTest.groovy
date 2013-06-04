package com.confluex.mule.test.event

import org.junit.Before
import org.junit.Test
import org.mule.api.MuleMessage
import org.mule.api.construct.FlowConstruct
import org.mule.context.notification.EndpointMessageNotification
import org.slf4j.Logger

import static org.mockito.Mockito.*

class LoggingEndpointListenerTest {
    LoggingEndpointListener listener
    MuleMessage message
    EndpointMessageNotification notification
    FlowConstruct flow


    @Before
    void createListener() {
        listener = new LoggingEndpointListener(log: mock(Logger))
        notification = mock(EndpointMessageNotification)
        message = mock(MuleMessage)
        flow = mock(FlowConstruct)
        when(notification.source).thenReturn(message)
        when(notification.endpoint).thenReturn("testEndpoint")
        when(notification.actionName).thenReturn("testAction")
        when(notification.flowConstruct).thenReturn(flow)
        when(flow.name).thenReturn("testFlowName")
        when(message.uniqueId).thenReturn("testId")
        when(message.payloadAsString).thenReturn("testPayload")
    }

    @Test
    void shouldLogPayloadIfEnabled() {
        listener.logPayload = true
        listener.onNotification(notification)
        verify(listener.log).debug("endpoint={},id={},flow={},action={}", "testEndpoint", "testId", "testFlowName", "testAction")
        verify(listener.log).debug("endpoint={},id={},payload={}", "testEndpoint", "testId", "testPayload")
    }

    @Test
    void shouldNotLogPayloadIfDisabled() {
        listener.logPayload = false
        listener.onNotification(notification)
        verify(listener.log).debug("endpoint={},id={},flow={},action={}", "testEndpoint", "testId", "testFlowName", "testAction")
        verify(listener.log, never()).debug(eq("endpoint={},id={},payload={}"), anyString(), anyString(), anyString())
    }

    @Test
    void shouldLogIfEndpointNameIsNotConfigured() {
        listener.endpointName = null
        listener.onNotification(notification)
        verify(listener.log, never()).debug("endpoint={},id={},flow={},action={}", "testEndpoint", "testId", "testFlowName", "testAction")
    }

    @Test
    void shouldNotLogIfEndpointNameIsNotNotCorrect() {
        listener.endpointName = "notTheCorrectEndpoint"
        listener.onNotification(notification)
        verify(listener.log, never()).debug("endpoint={},id={},flow={},action={}", "testEndpoint", "testId", "testFlowName", "testAction")
    }

    @Test
    void shouldLogIfEndpointNameIsCorrect() {
        listener.endpointName = "testEndpoint"
        listener.onNotification(notification)
        verify(listener.log).debug("endpoint={},id={},flow={},action={}", "testEndpoint", "testId", "testFlowName", "testAction")
    }
}
