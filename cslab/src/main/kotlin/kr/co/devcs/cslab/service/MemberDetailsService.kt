package kr.co.devcs.cslab.service

import kr.co.devcs.cslab.entity.Member
import kr.co.devcs.cslab.repository.MemberRepository
import kr.co.devcs.cslab.security.MemberDetails
import kr.co.devcs.cslab.security.MemberRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
class MemberDetailsService(@Autowired private val memberRepository: MemberRepository) : UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        val member: Optional<Member> = memberRepository.findByEmail(email)
        return MemberDetails(member.get())
    }
}