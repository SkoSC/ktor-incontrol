package com.skosc.incontrol.reflect

import com.skosc.incontrol.controller.Controller
import com.skosc.incontrol.handler.ParameterType
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Class wrapping [KParameter] describing [Controller]s handler methods parameter
 *
 * @author a.yakovlev
 * @since indev
 */
internal class ControllerHandlerParameter(val kParameter: KParameter) {
    val kType: KType = kParameter.type
    val type: ParameterType = ParameterTypeResolver.DEFAULT.resolve(kParameter)
    val name: String = ParameterNameResolver(kParameter, type).resolve()

    override fun toString(): String {
        return "$name: $kType"
    }
}