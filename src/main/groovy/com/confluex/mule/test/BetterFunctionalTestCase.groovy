package com.confluex.mule.test

import groovy.util.logging.Slf4j
import org.junit.Before
import org.mule.api.MuleContext
import org.mule.api.context.notification.MuleContextNotificationListener
import org.mule.context.notification.MuleContextNotification
import org.mule.tck.junit4.FunctionalTestCase
import org.mule.util.StringUtils
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.util.ReflectionUtils

import java.lang.annotation.Annotation
import java.lang.reflect.Method

@Slf4j
abstract class BetterFunctionalTestCase extends FunctionalTestCase {

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
                method.invoke(this, [] as Object[])
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
}
