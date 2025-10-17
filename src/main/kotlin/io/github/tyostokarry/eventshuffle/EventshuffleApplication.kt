package io.github.tyostokarry.eventshuffle

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventshuffleApplication

fun main(args: Array<String>) {
    runApplication<EventshuffleApplication>(*args)
}
