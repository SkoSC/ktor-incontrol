package com.skosc.incontrol.controller

import com.skosc.incontrol.di.DIContainerWrapper
import com.skosc.incontrol.exeption.InControlErrorCode
import com.skosc.incontrol.exeption.inControlError
import com.skosc.incontrol.handler.ParameterType
import com.skosc.incontrol.reflect.ControllerHandlerParameter
import com.skosc.incontrol.reflect.StaticTypes
import io.ktor.application.*
import io.ktor.request.*
import java.lang.Exception
import kotlin.reflect.full.isSubtypeOf

/**
 * Retrieves parameters for controller from [ApplicationCall]
 *
 * @author a.yakovlev
 * @since indev
 */
internal class ControllerParameterRetriever {

    private val optionalParameterValueMarker = object {}

    suspend fun retrieveParameters(
        expectedParameters: List<ControllerHandlerParameter>,
        diContainerWrapper: DIContainerWrapper,
        call: ApplicationCall
    ): Map<ControllerHandlerParameter, Any?> {
        return expectedParameters.associateWith { parameter ->
            when {
                parameter.type == ParameterType.BODY ->
                    call.receive(parameter.kType)
                parameter.type == ParameterType.QUERY ->
                    normalizeQueryOrPath(parameter, call.request.queryParameters[parameter.name])
                parameter.type == ParameterType.PATH ->
                    normalizeQueryOrPath(parameter, call.parameters[parameter.name])
                parameter.type == ParameterType.DEPENDENCY -> diContainerWrapper.resolve(parameter.name, parameter.kType)
                    ?: throwCantFindDependency(parameter, diContainerWrapper)
                else -> throwCantFindParameter(parameter)
            }
        }.filter { (_, v) -> v != optionalParameterValueMarker }
    }

    private fun normalizeQueryOrPath(parameter: ControllerHandlerParameter, value: String?): Any? {
        return when {
            value != null -> castQueryOrPathToNearestType(parameter, value)
            parameter.isOptional -> optionalParameterValueMarker
            parameter.isNullable -> null
            else -> throwCantFindParameter(parameter)
        }
    }

    private fun castQueryOrPathToNearestType(parameter: ControllerHandlerParameter, value: String): Any {
        try {
            return when {
                parameter.kType.isSubtypeOf(StaticTypes.STRING) -> value
                parameter.kType.isSubtypeOf(StaticTypes.INT) -> value.toInt()
                parameter.kType.isSubtypeOf(StaticTypes.DOUBLE) -> value.toDouble()
                parameter.kType.isSubtypeOf(StaticTypes.BOOLEAN) -> value.toBoolean()
                else -> inControlError(
                    code = InControlErrorCode.OTHER_INTEGRITY,
                    reason = "Integrity error",
                    howToSolve = "Please make Github issue"
                )
            }
        } catch (e: Exception) {
            inControlError(
                cause = e,
                code = InControlErrorCode.HANDLER_PARAMETER_CAST,
                reason = "Can't cast handler parameter $parameter",
                howToSolve = "Please check type annotations"
            )
        }
    }

    private fun throwCantFindParameter(parameter: ControllerHandlerParameter): Nothing = inControlError(
        code = InControlErrorCode.PARAMETER_CANT_FIND_PARAMETER,
        reason = "Can't find required parameter: $parameter",
        howToSolve = "Either pass parameter in request or mark it as nullable in handler"
    )

    private fun throwCantFindDependency(parameter: ControllerHandlerParameter, container: DIContainerWrapper): Nothing =
        inControlError(
            code = InControlErrorCode.PARAMETER_CANT_FIND_PARAMETER,
            reason = "Can't find dependency: $parameter in container: $container",
            howToSolve = "Declare required dependency in your container"
        )
}