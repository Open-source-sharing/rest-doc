package restdoc.core.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import restdoc.base.auth.AuthContext
import restdoc.base.auth.AuthMetadataImpl
import restdoc.base.auth.AuthenticationInterceptor


/**
 * Web configuration
 * @since 1.0
 */
@Configuration
open class ApplicationConfiguration : WebMvcConfigurer {

    @Autowired
    lateinit var authContext: AuthContext

    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, Any>

    @Bean
    open fun restClient(): RestTemplate = RestTemplate()

    @Bean
    open fun authenticationInterceptor(): AuthenticationInterceptor {
        val authMetadata = AuthMetadataImpl(redisTemplate)
        val authenticationInterceptor = AuthenticationInterceptor(authContext, authMetadata)
        authenticationInterceptor.setPathPatterns(arrayOf("/**"))
        authenticationInterceptor.setOrder(0)
        authenticationInterceptor.setExcludePathPatterns(arrayOf<String>())
        return authenticationInterceptor
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        val authenticationInterceptor = authenticationInterceptor()
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns(authenticationInterceptor.pathPatterns.asList())
                .excludePathPatterns(authenticationInterceptor.excludePathPatterns.asList())
                .order(authenticationInterceptor.order)
    }


    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("POST", "GET", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedOrigins("*")
    }

}