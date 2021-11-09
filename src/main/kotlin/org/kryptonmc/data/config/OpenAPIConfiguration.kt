package org.kryptonmc.data.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OpenApiCustomiser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAPIConfiguration {

    @Bean
    fun openAPI(config: MainConfig) = OpenAPI().apply {
        info(Info().title("KryptonMC API"))
        servers(listOf(Server().url(config.baseUrl.toExternalForm())))
    }

    @Bean
    fun sortSchemasAlphabetically() = OpenApiCustomiser { it.components.schemas(it.components.schemas.toSortedMap()) }
}
