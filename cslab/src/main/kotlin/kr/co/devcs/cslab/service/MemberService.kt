package kr.co.devcs.cslab.service

import jakarta.transaction.Transactional
import kr.co.devcs.cslab.entity.Member
import kr.co.devcs.cslab.repository.MemberRepository
import kr.co.devcs.cslab.security.MemberRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class MemberService(
        @Autowired val memberRepository: MemberRepository
) {
    fun checkEmailDuplication(email: String) = memberRepository.existsByEmail(email)

    fun checkSnoDuplication(sno: String) = memberRepository.existsBySno(sno)

    fun checkPassword(password1: String, password2: String) = password1 == password2

    @Transactional
    fun createMember(email: String, password: String, sno: String, name: String, birthDate: String) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        memberRepository.save(Member(
                email = email,
                password = password,
                sno = sno,
                name = name,
                birthDate = LocalDateTime.parse("$birthDate 00:00:00", formatter),
                roles = mutableSetOf(MemberRole.USER),
                isAdmin = true,
                isEnabled = true
        ))
    }
}