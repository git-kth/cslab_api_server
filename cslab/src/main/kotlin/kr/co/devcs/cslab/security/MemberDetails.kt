package kr.co.devcs.cslab.security

import kr.co.devcs.cslab.entity.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class MemberDetails(private val member: Member) : UserDetails {
//    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
//        return AuthorityUtils.createAuthorityList()
//    }
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return if (member.isAdmin) {
            listOf(SimpleGrantedAuthority("ADMIN"))
        } else {
            listOf(SimpleGrantedAuthority("USER"))
        }
    }

    override fun getPassword(): String {
        return member.password
    }

    override fun getUsername(): String {
        return member.name    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return member.isEnabled
    }

    fun isAdmin(): Boolean {
        return member.isAdmin
    }
    fun getMember(): Member {
        return member
    }
}