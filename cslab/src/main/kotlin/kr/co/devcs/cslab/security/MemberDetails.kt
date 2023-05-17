package kr.co.devcs.cslab.security

import kr.co.devcs.cslab.entity.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails

class MemberDetails(private val email: String, private val password: String, private val isEnabled : Boolean) : UserDetails {
    override fun getAuthorities(): MutableList<GrantedAuthority> = AuthorityUtils.createAuthorityList()
    override fun getPassword() = password
    override fun getUsername() = email
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = isEnabled
}