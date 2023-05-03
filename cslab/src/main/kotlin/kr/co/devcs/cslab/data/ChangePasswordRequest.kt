package kr.co.devcs.cslab.data

data class ChangePasswordRequest(
    val email: String?,
    val authNum: String?,
    val newPassword: String?,
    val confirmPassword: String?
)