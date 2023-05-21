package kr.co.devcs.cslab.controller

import jakarta.servlet.http.HttpSession
import kr.co.devcs.cslab.data.ChangePasswordRequest
import kr.co.devcs.cslab.dto.LoginDto
import kr.co.devcs.cslab.dto.MemberDto
import kr.co.devcs.cslab.service.MemberService
import kr.co.devcs.cslab.util.EmailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

data class MemberResponse(val data: MutableMap<String, String>, val errors: MutableList<String>)
@RestController
@RequestMapping("/api/member")
class MemberController(
    @Autowired val memberService: MemberService,
    @Autowired val emailService: EmailService,
) {
    @PostMapping("/signup")
    fun signup(
        @RequestBody @Validated memberDto: MemberDto,
        bindingResult: BindingResult
    ): ResponseEntity<String> {
        if (bindingResult.hasErrors()) {
            val msg = StringBuilder()
            bindingResult.allErrors.forEach {
                val field = it as FieldError
                val message = it.defaultMessage
                msg.append("${field.field} : $message\n")
            }
            return ResponseEntity.badRequest().body(msg.toString())
        }

        if (memberService.checkSnoDuplication(memberDto.sno!!)) {
            val msg = StringBuilder()
            msg.append("이미 있는 학번입니다.")
            return ResponseEntity.badRequest().body(msg.toString())
        }

        if (memberService.checkEmailDuplication(memberDto.email!!)) {
            val msg = StringBuilder()
            msg.append("이미 있는 이메일입니다.")
            return ResponseEntity.badRequest().body(msg.toString())
        }

        if (!memberService.checkPassword(memberDto.password1!!, memberDto.password2!!)) {
            val msg = StringBuilder()
            msg.append("패스워드가 일치하지 않습니다.")
            return ResponseEntity.badRequest().body(msg.toString())
        }
        memberService.createMember(
            memberDto.email,
            memberDto.password1,
            memberDto.sno,
            memberDto.name!!,
            memberDto.birthDate!!
        )
        return ResponseEntity.ok("signup success")
    }

    @PostMapping("/find-email")
    fun findId(@RequestBody memberDto: MemberDto): ResponseEntity<MemberResponse> {
        val email =
            memberService.findEmailBySnoAndBirthDate(memberDto.sno!!, memberDto.birthDate!!)
                ?: return ResponseEntity.badRequest().body(MemberResponse(mutableMapOf(), mutableListOf("가입된 회원이 없습니다.")))
        return ResponseEntity.ok().body(MemberResponse(mutableMapOf("email" to email), mutableListOf()))
    }

    @PostMapping("/find-password")
    fun findPassword(
        @RequestBody memberDto: MemberDto,
        session: HttpSession
    ): ResponseEntity<MemberResponse> {
        val memberOptional = memberService.findByEmail(memberDto.email!!)
        if (memberOptional.isPresent) {
            val member = memberOptional.get()
            val authCode = emailService.sendEmailForm(memberDto.email, member.name)
            session.setAttribute("authNum", authCode)
            session.setAttribute("email", memberDto.email)
            return ResponseEntity.ok().body(MemberResponse(mutableMapOf("success" to "true"), mutableListOf()))
        } else {
            return ResponseEntity.badRequest().body(MemberResponse(mutableMapOf(), mutableListOf("가입된 회원이 없습니다.")))
        }
    }

    @PostMapping("/change-password")
    fun changePassword(
        @RequestBody changePasswordRequest: ChangePasswordRequest,
        session: HttpSession
    ): ResponseEntity<MemberResponse> {
        val response =
            memberService.changePassword(changePasswordRequest.email!!,
                changePasswordRequest.authNum!!, changePasswordRequest.newPassword!!, changePasswordRequest.confirmPassword!!, session)
        return if (response.success) {
            session.invalidate()
            ResponseEntity.ok().body(MemberResponse(mutableMapOf("success" to "true"), mutableListOf()))
        } else {
            ResponseEntity.badRequest().body(MemberResponse(mutableMapOf(), mutableListOf(response.message)))
        }
    }

    @PostMapping("/login")
    fun login(
        @RequestBody @Validated loginDto: LoginDto,
        bindingResult: BindingResult
    ): ResponseEntity<MemberResponse> {
        if (bindingResult.hasErrors()) {
            val msg = StringBuilder()
            bindingResult.allErrors.forEach {
                val field = it as FieldError
                val message = it.defaultMessage
                msg.append("${field.field} : $message\n")
            }
            return ResponseEntity.badRequest().body(MemberResponse(mutableMapOf(), mutableListOf(msg.toString())))
        }
        if (!memberService.checkEmailDuplication(loginDto.email!!)) {
            return ResponseEntity.badRequest().body(MemberResponse(mutableMapOf(), mutableListOf("가입된 회원이 없습니다.")))
        }
        if (!memberService.checkPassword(loginDto.password!!, loginDto.password)) {
            return ResponseEntity.badRequest().body(MemberResponse(mutableMapOf(), mutableListOf("패스워드가 일치하지 않습니다.")))
        }
        if (!memberService.checkEnabled(loginDto.email)) {
            return ResponseEntity.badRequest().body(MemberResponse(mutableMapOf(), mutableListOf("이메일 인증을 완료해주세요.")))
        }
        return ResponseEntity.ok(MemberResponse(mutableMapOf("token" to memberService.login(loginDto)), mutableListOf()))
    }

    @GetMapping("/auth")
    fun security() = "success"

    @GetMapping("/admin/page")
    fun adminPage(authentication: Authentication): ResponseEntity<String> {
        val isAdmin = authentication.authorities.any { it.authority == "ADMIN" }
        return if (isAdmin) {
            ResponseEntity.ok("Welcome to the admin page!")
        } else {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()        }
    }

    @GetMapping("/admin/confirm")
    fun adminconfirm(): String{
        return "HI"
    }
}