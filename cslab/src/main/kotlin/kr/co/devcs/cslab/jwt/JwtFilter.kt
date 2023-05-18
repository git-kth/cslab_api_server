package kr.co.devcs.cslab.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtFilter(@Autowired private val jwtUtils: JwtUtils) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = jwtUtils.resolveToken(request)
        println(token)
        if (token != null && jwtUtils.validateJwtToken(token)) {
            val authentication = jwtUtils.getAuthentication(token)
            println(authentication.isAuthenticated)
            if(authentication.isAuthenticated) SecurityContextHolder.getContext().authentication = authentication
        }
        filterChain.doFilter(request, response)
    }
}