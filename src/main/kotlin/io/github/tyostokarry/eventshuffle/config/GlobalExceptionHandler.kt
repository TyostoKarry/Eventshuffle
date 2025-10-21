package io.github.tyostokarry.eventshuffle.config

import io.github.tyostokarry.eventshuffle.exception.EventNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(EventNotFoundException::class)
    fun handleNotFound(ex: EventNotFoundException): ResponseEntity<String> = ResponseEntity(ex.message, HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<String> = ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseError(ex: HttpMessageNotReadableException): ResponseEntity<String> =
        ResponseEntity(ex.cause?.message ?: ex.message, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(ex: MethodArgumentNotValidException): ResponseEntity<String> {
        val errorMessages =
            ex.bindingResult.fieldErrors.joinToString("; ") {
                "${it.defaultMessage}"
            }
        return ResponseEntity(errorMessages, HttpStatus.BAD_REQUEST)
    }
}
