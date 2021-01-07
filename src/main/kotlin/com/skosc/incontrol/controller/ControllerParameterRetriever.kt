package com.skosc.incontrol.controller

import com.skosc.incontrol.di.DIContainerWrapper
import com.skosc.incontrol.exeption.InControlErrorCode
import com.skosc.incontrol.exeption.inControlError
import com.skosc.incontrol.handler.ParameterType
import com.skosc.incontrol.reflect.ControllerHandlerParameter
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.util.*

/**
 * Retrieves parameters for controller from [ApplicationCall]
 *
 * @author a.yakovlev
 * @since indev
 */
internal class ControllerParameterRetriever() {

    suspend fun retrieveParameters(
        expectedParameters: List<ControllerHandlerParameter>,
        diContainerWrapper: DIContainerWrapper,
        call: ApplicationCall
    ): Map<ControllerHandlerParameter, Any> {
        return expectedParameters.associateWith { parameter ->
            when (parameter.type) {
                ParameterType.BODY -> call.receive(parameter.kType)
                ParameterType.QUERY -> call.request.queryParameters[parameter.name]
                    ?: throwCantFindParameter(parameter)
                ParameterType.PATH -> call.parameters[parameter.name]
                    ?: throwCantFindParameter(parameter)
                ParameterType.DEPENDENCY -> diContainerWrapper.resolve(parameter.name, parameter.kType)
                    ?: throwCantFindDependency(parameter, diContainerWrapper)
            }
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