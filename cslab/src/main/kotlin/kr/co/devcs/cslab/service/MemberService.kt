package kr.co.devcs.cslab.service

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import kr.co.devcs.cslab.data.ChangePasswordResponse
import kr.co.devcs.cslab.dto.LoginDto
import kr.co.devcs.cslab.entity.Member
import kr.co.devcs.cslab.jwt.JwtUtils
import kr.co.devcs.cslab.repository.MemberRepository
import kr.co.devcs.cslab.security.MemberRole
import kr.co.devcs.cslab.util.EmailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.servlet.function.EntityResponse
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

    fun checkEnabled(email: String) = memberRepository.findByEmail(email).get().isEnabled

    fun checkLoginPassword(email: String, password: String) = passwordEncoder.matches(password, findByEmail(email).get().password)

    fun findEmailBySnoAndBirthDate(sno: String, birthDate: String): String? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val member = memberRepository.findBySnoAndBirthDate(sno, LocalDate.parse(birthDate, formatter))
        return member?.email
    }

    fun findByEmail(email: String) = memberRepository.findByEmail(email)

fun changePassword(email: String, authNum: String, newPassword: String, confirmPassword: String, session: HttpSession): ChangePasswordResponse {
    val member = memberRepository.findByEmail(email).get() ?: return ChangePasswordResponse(false, "해당 이메일의 회원이 존재하지 않습니다.")
    val sessionAuthNum = session.getAttribute("authNum") as? String ?: return ChangePasswordResponse(false, "인증번호를 먼저 입력해주세요.")
    if (!emailService.isValidEmailCode(email,authNum)){
        return ChangePasswordResponse(false, "인증번호가 일치하지 않습니다.")
    }
    if (newPassword != confirmPassword) {
        return ChangePasswordResponse(false, "비밀번호가 서로 다릅니다. 다시 확인 해주세요.")
    }
    if (passwordEncoder.matches(newPassword, member.password)) {
        return ChangePasswordResponse(false, "이전 비밀번호와 동일한 비밀번호로는 변경할 수 없습니다.")
    }
    member.password = passwordEncoder.encode(newPassword)
    memberRepository.save(member)
    return ChangePasswordResponse(true, "비밀번호가 성공적으로 변경되었습니다.")
}


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
                isAdmin = false,
                isEnabled = false
            )
        )
    }

    fun login(loginDto: LoginDto) = jwtUtils.generateJwtToken(loginDto.email!!)
}