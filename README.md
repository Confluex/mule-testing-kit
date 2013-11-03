# Confluex Test Notifications

Since the MuleFunctionalTest runs in a separate thread from the server, it's often
difficult to determine when the messages are finished and when to start your assertions (usually by waiting or
sending the messages to a mock endpoint).

**BlockingEndpointListener**

This event listener will attach to an endpoint and allow your test to wait and collect the messages sent to the endpoint.
The following example configuration and test case demonstrate the basic usage:

_Mule Configuration_


```xml
<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns="http://www.mulesoft.org/schema/mule/core"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:vm="http://www.mulesoft.org/schema/mule/vm"
      xsi:schemaLocation="
        http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
        http://www.mulesoft.org/schema/mule/vm http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd
        ">

    <notifications dynamic="true">
        <notification event="ENDPOINT-MESSAGE" />
    </notifications>


    <vm:endpoint name="in" path="in" />
    <vm:endpoint name="out" path="out" />

    <flow name="Test Endpoint Listener">
        <inbound-endpoint ref="in"/>
        <logger level="INFO" message="Message content:  #[payload]"/>
        <outbound-endpoint ref="out"/>
    </flow>
</mule>
```

_Functional Test Case_

```groovy
class BlockingEndpointListenerFunctionalTest extends FunctionalTestCase {

    @Test
    void shouldWaitForSingleMessage() {
        def listener = new BlockingEndpointListener("out")
        muleContext.registerListener(listener)
        muleContext.client.dispatch("in", "Bacon", [:])
        assert listener.waitForMessages()
        assert listener.messages.size() == 1
        assert listener.messages.first().payloadAsString == 'Bacon'
    }

    @Test
    void shouldWaitForMultipleMessages() {
        def listener = new BlockingEndpointListener("out", 10)
        muleContext.registerListener(listener)
        10.times {
            muleContext.client.dispatch("in", "Bacon", [:])
        }

        assert listener.waitForMessages()
        assert listener.messages.size() == 10
        assert listener.messages.first().payloadAsString == 'Bacon'
    }

    @Test
    void shouldTimeOutIfNumberOfMessagesIsLessThanExpected() {
        def listener = new BlockingEndpointListener("out", 2)
        muleContext.registerListener(listener)
        muleContext.client.dispatch("in", "Bacon", [:])
        assert !listener.waitForMessages(2000)
        assert listener.messages.first().payloadAsString == 'Bacon'
    }
}
```