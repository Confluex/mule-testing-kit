package com.confluex.mule.test

import groovy.util.logging.Slf4j
import org.mule.api.processor.MessageProcessor
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor

@Slf4j
class ConfigurationDetector implements BeanPostProcessor {
    def beans = [:]

    @Override
    Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean
    }

    @Override
    Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof MessageProcessor) {
            def instances = (beans[beanName] = beans[beanName] ?: new HashSet())
            log.debug("Found bean $beanName ($bean): ${bean.getClass().getName()}")
            instances << bean
        }
        return bean
    }
}
