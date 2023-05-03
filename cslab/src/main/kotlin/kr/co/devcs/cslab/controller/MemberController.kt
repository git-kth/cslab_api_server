package kr.co.devcs.cslab.controller

import jakarta.servlet.http.HttpSession
import kr.co.devcs.cslab.data.ChangePasswordRequest
import kr.co.devcs.cslab.data.ChangePasswordResponse
import kr.co.devcs.cslab.dto.MemberDto
import kr.co.devcs.cslab.service.MemberService
import kr.co.devcs.cslab.util.EmailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.Timer
import java.util.TimerTask
import java.util.UUID


@RestController
@RequestMapping("/api/member")
class MemberController(
        @Autowired val memberService: MemberService,
        @Autowired val emailService: EmailService

) {
    @PostMapping("/signup")
    fun signup(@RequestBody @Validated memberDto: MemberDto, bindingResult: BindingResult): ResponseEntity<String> {
        if(bindingResult.hasErrors()){
            val msg = StringBuilder()
            bindingResult.allErrors.forEach{
                val field = it as FieldError
                val message = it.defaultMessage
                msg.append("${field.field} : $message\n")
            }
            return ResponseEntity.badRequest().body(msg.toString())
        }
        if(memberService.checkSnoDuplication(memberDto.sno!!)) {
            val msg = StringBuilder()
            msg.append("이미 있는 학번입니다.")
            return ResponseEntity.badRequest().body(msg.toString())
        }

        if(memberService.checkEmailDuplication(memberDto.email!!)) {
            val msg = StringBuilder()
            msg.append("이미 있는 이메일입니다.")
            return ResponseEntity.badRequest().body(msg.toString())
        }

        if(!memberService.checkPassword(memberDto.password1!!, memberDto.password2!!)) {
            val msg = StringBuilder()
            msg.append("패스워드가 일치하지 않습니다.")
            return ResponseEntity.badRequest().body(msg.toString())
        }
        memberService.createMember(memberDto.email, memberDto.password1, memberDto.sno, memberDto.name!!, memberDto.birthDate!!)
        return ResponseEntity.ok("signup success")
    }


    @PostMapping("/find-email")
    fun findId(
        @RequestBody memberDto: MemberDto
    ): ResponseEntity<String> {
        val sno = memberDto.sno ?: return ResponseEntity.badRequest().body("학번을 입력해주세요.")
        val birthDate = memberDto.birthDate ?: return ResponseEntity.badRequest().body("생년월일을 입력해주세요.")
        val email = memberService.findEmailBySnoAndBirthDate(sno, birthDate) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(email)
    }


    @PostMapping("/find-password")
    fun findPassword(@RequestBody memberDto: MemberDto,session: HttpSession): ResponseEntity<String> {
        val email = memberDto.email ?: return ResponseEntity.badRequest().body("이메일을 입력해 주세요.")
        val member = memberService.findByEmail(email) ?: return ResponseEntity.badRequest().body("가입된 회원이 없습니다.")
        val authCode = emailService.sendEmailForm(email, member.name)
        session.setAttribute("authNum", authCode)
        session.setAttribute("email",email)
        return ResponseEntity.ok(authCode)
    }


    @PostMapping("/change-password")
    fun changePassword(@RequestBody changePasswordRequest: ChangePasswordRequest, session: HttpSession): ResponseEntity<ChangePasswordResponse> {
        val email = changePasswordRequest.email ?: return ResponseEntity.badRequest().body(ChangePasswordResponse(false, "이메일을 입력해 주세요."))
        val authNum = changePasswordRequest.authNum ?: return ResponseEntity.badRequest().body(ChangePasswordResponse(false, "인증번호를 입력해 주세요."))
        val newPassword = changePasswordRequest.newPassword ?: return ResponseEntity.badRequest().body(ChangePasswordResponse(false, "새로운 비밀번호를 입력해 주세요."))
        val confirmPassword = changePasswordRequest.confirmPassword ?: return ResponseEntity.badRequest().body(ChangePasswordResponse(false, "새로운 비밀번호 확인을 입력해 주세요."))

        val response = memberService.changePassword(email, authNum, newPassword, confirmPassword, session)
        if (response.success) {
//            session.removeAttribute("authNum")
            session.invalidate()
            return ResponseEntity.ok(response)
        } else {
            return ResponseEntity.badRequest().body(response)
        }
    }
}

