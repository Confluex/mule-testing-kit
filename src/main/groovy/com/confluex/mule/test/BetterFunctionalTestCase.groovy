package com.confluex.mule.test

import com.confluex.mule.test.event.BlockingEndpointListener
import com.confluex.mule.test.event.BlockingMessageProcessorListener
import com.confluex.mule.test.event.BlockingTransactionListener
import groovy.util.logging.Slf4j
import org.junit.Before
import org.mule.api.MuleContext
import org.mule.api.config.ConfigurationBuilder
import org.mule.api.context.notification.MuleContextNotificationListener
import org.mule.config.spring.SpringXmlConfigurationBuilder
import org.mule.context.notification.MuleContextNotification
import org.mule.tck.junit4.FunctionalTestCase
import org.mule.util.StringUtils
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.util.ReflectionUtils

import java.lang.annotation.Annotation
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@Slf4j
abstract class BetterFunctionalTestCase extends FunctionalTestCase {

    @Override
    protected ConfigurationBuilder getBuilder() throws Exception
    {
        String configResources = getConfigResources();
        if (configResources != null)
        {
            return new SpringXmlConfigurationBuilder(configResources + ",testing-kit-config.xml");
        }
        configResources = getConfigFile();
        if (configResources != null)
        {
            if (configResources.contains(","))
            {
                throw new RuntimeException("Do not use this method when the config is composed of several files. Use getConfigFiles method instead.");
            }
            return new SpringXmlConfigurationBuilder([configResources, "testing-kit-config.xml"] as String[]);
        }
        def multipleConfigResources = (getConfigFiles() as List) << "testing-kit-config.xml";
        return new SpringXmlConfigurationBuilder(multipleConfigResources as String[]);
    }

    /** Runs before Mule starts */
    @Override
    protected MuleContext createMuleContext() throws Exception {
        MuleContext context = super.createMuleContext();

        findAnnotatedMethods(BeforeMule.class).each { method ->
            final List<Class<?>> parameterTypes = method.parameterTypes.toList()
            if (parameterTypes.empty) {
                method.invoke(this, [] as Object[])
            } else if (parameterTypes.size() == 1 && parameterTypes[0].isAssignableFrom(MuleContext)) {
                method.invoke(this, [context] as Object[]);
            } else {
                log.error "BeforeMule annotation not applicable to method with parameters ${StringUtils.join(parameterTypes, ',')}";
            }
        }

        return context;
    }

    @Before
    void registerNotificationListenerForAfterMuleMethods() {
        muleContext.registerListener(new MuleContextNotificationListener<MuleContextNotification>() {
            public void onNotification(final MuleContextNotification notification) {
                if (MuleContextNotification.CONTEXT_DISPOSED == notification.getAction()) {
                    invokeAfterMuleMethods()
                }
            }
        });
    }

    void invokeAfterMuleMethods() {
        findAnnotatedMethods(AfterMule.class).each { method ->
            final List<Class<?>> parameterTypes = method.parameterTypes.toList()
            if (parameterTypes.empty) {
                try {
                    method.invoke(this, [] as Object[])
                } catch (InvocationTargetException e) {
                    throw e.cause
                }
            } else {
                log.error "AfterMule annotation not application to method with parameters ${StringUtils.join(parameterTypes, ',')}"
            }
        }
    }

    private <A extends Annotation> Set<Method> findAnnotatedMethods(Class<A> annotationClass) {
        ReflectionUtils.getUniqueDeclaredMethods(this.getClass()).findAll { method ->
            null != AnnotationUtils.findAnnotation(method, annotationClass)
        }
    }

    BlockingEndpointListener listenForEndpoint(String endpoint, int expectedCount = 1) {
        def listener = new BlockingEndpointListener(endpoint, expectedCount)
        muleContext.registerListener(listener)
        return listener
    }

    BlockingMessageProcessorListener listenForMessageProcessor(String messageProcessor, int expectedCount = 1) {
        def listener = new BlockingMessageProcessorListener(messageProcessor, expectedCount)
        listener.setConfigurationDetector(muleContext.registry['_configDetector'])
        muleContext.registerListener(listener)
        return listener
    }

    BlockingTransactionListener listenForTransaction() {
        def listener = new BlockingTransactionListener()
        muleContext.registerListener(listener)
        return listener
    }
}
