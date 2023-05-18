package kr.co.devcs.cslab.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.http.HttpServletRequest
import kr.co.devcs.cslab.service.MemberDetailsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtils(@Autowired private val memberDetailsService: MemberDetailsService) {
    val jwtExpTime = 1000L * 60 * 3
    val key: SecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512)

    fun generateJwtToken(username: String): String {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + jwtExpTime))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getUserNameFromJwtToken(token: String): String {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body.subject
    }

    fun getClaims(token: String): Claims {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
    }

    fun validateJwtToken(authToken: String): Boolean {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken)
            return true
        } catch (e: SignatureException) {
            println("Invalid JWT signature: ${e.message}")
        } catch (e: MalformedJwtException) {
            println("Invalid JWT token: ${e.message}")
        } catch (e: ExpiredJwtException) {
            println("JWT token is expired: ${e.message}")
        } catch (e: UnsupportedJwtException) {
            println("JWT token is unsupported: ${e.message}")
        } catch (e: IllegalArgumentException) {
            println("JWT claims string is empty: ${e.message}")
        }
        return false
    }

    fun getAuthentication(token: String): UsernamePasswordAuthenticationToken {
        val memberDetails = memberDetailsService.loadUserByUsername(getUserNameFromJwtToken(token))
        val res = UsernamePasswordAuthenticationToken(memberDetails, "", memberDetails.authorities)
        if (!memberDetails.isEnabled) res.isAuthenticated = false
        return res
    }

    fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        return null
    }
    fun isAdminToken(token: String): Boolean {
        val claims = getClaims(token)
        val username = getUserNameFromJwtToken(token)
        val memberDetails = memberDetailsService.loadUserByUsername(username)
        return memberDetails.authorities.contains(SimpleGrantedAuthority("ADMIN")) && !claims.expiration.before(Date())
    }
}