package org.kryptonmc.data.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.net.URL

@ConfigurationProperties("main")
@ConstructorBinding
data class MainConfig(
    val token: String,
    val baseUrl: URL = URL("http://localhost")
)
