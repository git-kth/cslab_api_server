package kr.co.devcs.cslab.repository

import kr.co.devcs.cslab.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*


interface MemberRepository: JpaRepository<Member, Long>{
    fun existsByEmail(email: String): Boolean
    fun existsBySno(sno: String): Boolean
}