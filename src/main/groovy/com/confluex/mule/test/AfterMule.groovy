package com.confluex.mule.test

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Run this method after the Mule context is stopped, but before Mule is disposed.
 *
 * Applies only to methods with no parameters
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterMule {

}