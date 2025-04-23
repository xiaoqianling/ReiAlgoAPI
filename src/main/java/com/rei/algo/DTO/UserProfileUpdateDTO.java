package com.rei.algo.DTO;

import jakarta.validation.constraints.Email;
// Potentially add URL validation if avatarUrl needs stricter validation
// import org.hibernate.validator.constraints.URL;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for updating user profile information (excluding sensitive data like password, username, role).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDTO {

    // Email is optional, but if provided, must be valid
    @Email(message = "邮箱格式不正确")
    private String email;

    // Avatar URL validation can be added if needed (e.g., @URL)
    // @URL(message = "头像 URL 格式不正确")
    private String avatarUrl;

} 