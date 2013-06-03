package com.confluex.mule.test.event

import org.junit.Before
import org.junit.Test
import org.mule.api.MuleMessage
import org.mule.context.notification.EndpointMessageNotification
import org.slf4j.Logger

import static org.mockito.Mockito.*

class LoggingEndpointListenerTest {
    LoggingEndpointListener listener
    MuleMessage message
    EndpointMessageNotification notification


    @Before
    void createListener() {
        listener = new LoggingEndpointListener(log: mock(Logger))
        notification = mock(EndpointMessageNotification)
        message = mock(MuleMessage)
        when(notification.source).thenReturn(message)
        when(message.uniqueId).thenReturn("testId")
        when(notification.endpoint).thenReturn("testEndpoint")
        when(notification.type).thenReturn("testType")
        when(message.payloadAsString).thenReturn("testPayload")
    }

    @Test
    void shouldLogPayloadIfEnabled() {
        listener.logPayload = true
        listener.onNotification(notification)
        verify(listener.log).debug("endpoint={} id={} type={}", "testEndpoint", "testId", "testType")
        verify(listener.log).debug("endpoint={} id={} payload={}", "testEndpoint", "testId", "testPayload")
    }

    @Test
    void shouldNotLogPayloadIfDisabled() {
        listener.logPayload = false
        listener.onNotification(notification)
        verify(listener.log).debug("endpoint={} id={} type={}", "testEndpoint", "testId", "testType")
        verify(listener.log, never()).debug(eq("endpoint={} id={} payload={}"), anyString(), anyString(), anyString())
    }

    @Test
    void shouldLogIfEndpointNameIsNotConfigured() {
        listener.endpointName = null
        listener.onNotification(notification)
        verify(listener.log, never()).debug(eq("endpoint={} id={} type={}"), anyString(), anyString(), anyString())
    }

    @Test
    void shouldNotLogIfEndpointNameIsNotNotCorrect() {
        listener.endpointName = "notTheCorrectEndpoint"
        listener.onNotification(notification)
        verify(listener.log, never()).debug(eq("endpoint={} id={} type={}"), anyString(), anyString(), anyString())
    }

    @Test
    void shouldLogIfEndpointNameIsCorrect() {
        listener.endpointName = "testEndpoint"
        listener.onNotification(notification)
        verify(listener.log).debug("endpoint={} id={} type={}", "testEndpoint", "testId", "testType")
    }
}
