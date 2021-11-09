package org.kryptonmc.data.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI

@Controller
class RootController {

    @GetMapping("/", "/docs")
    fun redirectToDocs() = ResponseEntity.status(HttpStatus.FOUND).location(URI("docs/")).build<Any>()
}
