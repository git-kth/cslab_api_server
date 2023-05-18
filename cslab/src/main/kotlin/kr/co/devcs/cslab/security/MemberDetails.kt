package kr.co.devcs.cslab.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class MemberDetails(private val email: String, private val password: String, private val isEnabled: Boolean, private val isAdmin: Boolean) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return if (isAdmin) {
            listOf(SimpleGrantedAuthority("ADMIN"))
        } else {
            listOf(SimpleGrantedAuthority("USER"))
        }
    }    override fun getPassword() = password
    override fun getUsername() = email
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = isEnabled
    fun isAdmin() = isAdmin
}