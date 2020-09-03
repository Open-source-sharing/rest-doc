package restdoc.web.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import restdoc.web.util.TemplateUtil
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class EchoController {

    @GetMapping("/echo")
    fun index(request: HttpServletRequest): String {
        val headers = request.headerNames.toList()
        for (header in headers) {
            println("${header} : ${request.getHeader(header)}")
        }
        return "login"
    }

    // http://732200850369286144.tformal.onepushing.com/p?nodeId=732200866127286272


    // http://732200850369286144.tformal.onepushing.com/js/chunk-vendors.68048db9.js
    // http://740101574658887680.tformal.onepushing.com/js/chunk-vendors.c06c8233.js

    @GetMapping("/render")
    @ResponseBody
    fun render(response: HttpServletResponse,request: HttpServletRequest): String {

        val headers = request.headerNames.toList()
        for (header in headers) {
            println("${header} : ${request.getHeader(header)}")
        }

        val keywordLine = "<a href='https://www.baidu.com'>SEO关键字<a>"
        return TemplateUtil.gen(mutableMapOf<String, Any>("keywordLine" to keywordLine))
    }
}