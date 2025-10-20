package io.github.tyostokarry.eventshuffle.config

import io.github.tyostokarry.eventshuffle.exception.EventNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(EventNotFoundException::class)
    fun handleNotFound(ex: EventNotFoundException): ResponseEntity<String> = ResponseEntity(ex.message, HttpStatus.NOT_FOUND)
}
