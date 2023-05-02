package kr.co.devcs.cslab.controller

import kr.co.devcs.cslab.dto.MemberDto
import kr.co.devcs.cslab.service.MemberService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/member")
class MemberController(
        @Autowired val memberService: MemberService
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
}