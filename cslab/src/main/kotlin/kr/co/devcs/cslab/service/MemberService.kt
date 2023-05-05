package kr.co.devcs.cslab.service

import jakarta.transaction.Transactional
import kr.co.devcs.cslab.dto.LoginDto
import kr.co.devcs.cslab.entity.Member
import kr.co.devcs.cslab.jwt.JwtUtils
import kr.co.devcs.cslab.repository.MemberRepository
import kr.co.devcs.cslab.security.MemberRole
import kr.co.devcs.cslab.util.EmailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class MemberService(
    @Autowired val memberRepository: MemberRepository,
    @Autowired val emailService: EmailService,
    @Autowired val passwordEncoder: PasswordEncoder,
    @Autowired val jwtUtils: JwtUtils
) {
    fun checkEmailDuplication(email: String) = memberRepository.existsByEmail(email)

    fun checkSnoDuplication(sno: String) = memberRepository.existsBySno(sno)

    fun checkPassword(password1: String, password2: String) = password1 == password2

    @Transactional
    fun createMember(email: String, password: String, sno: String, name: String, birthDate: String) {
        emailService.sendEmailForm(email, name)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        memberRepository.save(
            Member(
                email = email,
                password = passwordEncoder.encode(password),
                sno = sno,
                name = name,
                birthDate = LocalDate.parse(birthDate, formatter),
                roles = mutableSetOf(MemberRole.ROLE_USER),
                isAdmin = true,
                isEnabled = false
            )
        )
    }

    fun login(loginDto: LoginDto): String {
        val member: Optional<Member> =
            loginDto.email?.let { memberRepository.findByEmail(it) } ?: throw Exception("존재하지 않는 이메일입니다.")
        if (!passwordEncoder.matches(loginDto.password, member.get().password)) throw Exception("비밀번호가 일치하지 않습니다.")
        return jwtUtils.generateJwtToken(loginDto.email)
    }
}