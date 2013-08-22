package com.confluex.mule.test.event;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.mule.api.MuleEvent;
import org.mule.api.NamedObject;
import org.mule.api.processor.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class LoggingMessageProcessorAspect {
    Integer threshHold = 20;
    Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Produces a tab delimited log of component execution time and other useful information. It will only
     * log if the execution time (ms) is greater than or equal to the configured threshHold.
     *
     * Fields: componentName, flowName, rootMessageId, executionTime (ms)
     */
    @Around("call(public * org.mule.api.processor.MessageProcessor+.process(..)) && target(processor) && args(event)")
    public MuleEvent logComponentEvent(ProceedingJoinPoint pjp, MuleEvent event, MessageProcessor processor) throws Throwable {
        String componentName = processor instanceof NamedObject ? ((NamedObject) processor).getName() : processor.toString();
        Long started = System.currentTimeMillis();
        MuleEvent proceed = (MuleEvent) pjp.proceed();
        Long executionTime = System.currentTimeMillis() - started;
        if (executionTime >= threshHold) {
            String[] params = {componentName, event.getFlowConstruct().getName(), event.getMessage().getMessageRootId(), executionTime.toString()};
            log.info("componentExecution\t{}\t{}\t{}\t{}", params);
        }
        return proceed;
    }


}
