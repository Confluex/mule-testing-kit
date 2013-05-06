package com.confluex.mule.test.event

import org.junit.Before
import org.junit.Test
import org.mule.api.MuleMessage
import org.mule.api.endpoint.ImmutableEndpoint
import org.mule.context.notification.EndpointMessageNotification

import static org.mockito.Mockito.*

class EndpointListenerTest {
    EndpointListener listener

    @Before
    void createListener() {
        listener = new EndpointListener("foo")
    }

    @Test
    void shouldCountDownOnMatchingEvents() {
        EndpointMessageNotification notification = createMockAction("foo", EndpointMessageNotification.MESSAGE_DISPATCHED)
        assert listener.latch.count == 1
        listener.onNotification(notification)
        assert listener.latch.count == 0
    }

    @Test
    void shouldNotCountDownOnUnknownEvents() {
        EndpointMessageNotification notification = createMockAction("foo", EndpointMessageNotification.MESSAGE_REQUEST_BEGIN)
        assert listener.latch.count == 1
        listener.onNotification(notification)
        assert listener.latch.count == 1
    }

    @Test
    void shouldRetainMessagesOnKnownEvents() {
        EndpointMessageNotification notification = createMockAction("foo", EndpointMessageNotification.MESSAGE_DISPATCHED)
        def message = mock(MuleMessage)
        when(notification.source).thenReturn(message)

        listener.onNotification(notification)
        listener.onNotification(notification)
        listener.onNotification(notification)
        listener.onNotification(notification)
        listener.onNotification(notification)

        assert listener.messages == [
                message, message, message,message, message
        ]
    }

    protected EndpointMessageNotification createMockAction(String endpointName, Integer action) {
        def notification = mock(EndpointMessageNotification)
        def endpoint = mock(ImmutableEndpoint)
        when(notification.immutableEndpoint).thenReturn(endpoint)
        when(notification.action).thenReturn(action)
        when(endpoint.name).thenReturn(endpointName)
        return notification
    }

}
