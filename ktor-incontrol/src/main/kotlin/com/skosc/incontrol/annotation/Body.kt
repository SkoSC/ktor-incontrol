package com.skosc.incontrol.annotation

/**
 * Marks handler method parameter as [ParameterType.BODY]
 *
 * @author a.yakovlev
 * @since indev
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Body
