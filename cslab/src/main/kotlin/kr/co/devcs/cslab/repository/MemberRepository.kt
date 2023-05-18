package kr.co.devcs.cslab.repository

import java.util.*
import kr.co.devcs.cslab.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface MemberRepository : JpaRepository<Member, Long> {
    fun existsByEmail(email: String): Boolean
    fun existsBySno(sno: String): Boolean
    fun findBySnoAndBirthDate(sno: String, birthDate: LocalDate): Member?
    fun findPasswordByEmail(email: String): String?
    fun findByEmail(email: String): Optional<Member>
}
