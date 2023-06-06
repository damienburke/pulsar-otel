package pulsarotel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class PulsarOtelApplication

fun main(args: Array<String>) {
    runApplication<PulsarOtelApplication>(*args)
}