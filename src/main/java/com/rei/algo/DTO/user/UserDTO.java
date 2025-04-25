package com.rei.algo.DTO.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rei.algo.model.entity.Role; // Import Role enum
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象 (DTO)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields during serialization
public class UserDTO {

    // Read-only fields, usually set by backend
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String userId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Role role;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    // Fields for input (registration, update) and output
    @NotBlank(message = "用户名不能为空")
    @Size(min = 6, max = 20, message = "用户名长度必须在 6 到 20 之间")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "用户名只能包含字母、数字、下划线和连字符")
    private String username;

    // Password is write-only (for registration/update) and validated
    @NotBlank(message = "密码不能为空") // Password is required for registration
    @Size(min = 6, max = 20, message = "密码长度必须在 6 到 20 之间")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "密码只能包含字母和数字")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Prevent password from being serialized in responses
    private String password;

    // Email is optional but must be valid if provided
    @Email(message = "邮箱格式不正确")
    private String email; // Removed @NotBlank

    // Avatar URL can be updated, but not required for registration
    private String avatarUrl;

    // --- Validation Groups (If needed for more complex scenarios) ---
    // public interface Registration {}
    // public interface PasswordUpdate {}
    // public interface ProfileUpdate {} 
} 