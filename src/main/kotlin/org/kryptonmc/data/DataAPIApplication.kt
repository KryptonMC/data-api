package org.kryptonmc.data

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class DataAPIApplication

fun main(args: Array<String>) {
    runApplication<DataAPIApplication>(*args)
}
