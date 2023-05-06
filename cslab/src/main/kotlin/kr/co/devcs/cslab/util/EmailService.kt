package kr.co.devcs.cslab.util

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.Response
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.*


@Service
class EmailService {
    data class AuthCode(val code: String, val createdAt: LocalDateTime = LocalDateTime.now())

    private val authCodes = mutableMapOf<String, AuthCode>()

    @Value("\${SENDGRID_API_KEY}")
    lateinit var api_key: String

    fun createEmailCode(email: String): String {
        val random = Random()
        val key = StringBuffer()
        for (i in 0..7) {
            when (random.nextInt(3)) {
                0 -> key.append((random.nextInt(26) + 97).toChar())
                1 -> key.append((random.nextInt(26) + 65).toChar())
                2 -> key.append((random.nextInt(9)))
            }
        }
        val authCode = key.toString()
        println("code: $authCode")
        authCodes[email] = AuthCode(authCode)
        return authCode
    }

    fun sendEmailForm(email: String, name: String): String {
        val from = Email("admin@em9806.devcs.co.kr", "CSLAB 관리자")
        val subject = "CSLAB APP 인증코드 발송"
        val to = Email(email)
        val authCode = createEmailCode(email)
        val content = Content("text/plain", "and easy to do anywhere, even with Java [$authCode]")
        val mail = Mail(from, subject, to, content)
        val sg = SendGrid(api_key)
        val request = Request()
        request.method = Method.POST
        request.endpoint = "mail/send"
        request.body = mail.build()
        val response: Response = sg.api(request)
        println(response.statusCode)
        println(response.body)
        println(response.headers)
        return authCode
    }

    fun isValidEmailCode(email: String, emailCode: String): Boolean {
        val authCode = authCodes[email] ?: return false
        val createdAt = authCode.createdAt
        val now = LocalDateTime.now()
        val duration = Duration.between(createdAt, now)
        return authCode.code == emailCode && duration.toMinutes() < 5
    }
}