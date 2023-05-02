package kr.co.devcs.cslab.entity

import jakarta.persistence.*
import kr.co.devcs.cslab.security.MemberRole
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
class Member (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @Column(unique = true)
        val email: String,

        val password: String,

        @Column(unique = true)
        val sno: String,

        val name: String,

        val birthDate: LocalDateTime,

        @Enumerated(EnumType.STRING)
        @ElementCollection(fetch = FetchType.EAGER)
        val roles: MutableSet<MemberRole>,

        @CreationTimestamp
        val createdDate: LocalDateTime = LocalDateTime.now(),

        @Column(nullable = false)
        val isAdmin: Boolean = false,

        @Column(nullable = false)
        val isEnabled: Boolean = false
)