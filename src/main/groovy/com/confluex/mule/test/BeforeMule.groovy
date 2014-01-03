package com.confluex.mule.test

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Run this method after the Mule context is initialized, but before Mule is started.
 *
 * Applies only to methods with no parameters and methods with a single parameter of type MuleContext.
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeMule {

}