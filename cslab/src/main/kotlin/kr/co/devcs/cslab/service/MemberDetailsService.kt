package kr.co.devcs.cslab.service

import kr.co.devcs.cslab.entity.Member
import kr.co.devcs.cslab.repository.MemberRepository
import kr.co.devcs.cslab.security.MemberDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.util.*

@Service
class MemberDetailsService(@Autowired private val memberRepository: MemberRepository) : UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        val member: Member = memberRepository.findByEmail(email).get()
        return MemberDetails(member.email, member.password, member.isEnabled)
    }
}