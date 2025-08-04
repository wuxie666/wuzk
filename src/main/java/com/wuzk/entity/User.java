package com.wuzk.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDateTime;

@Schema(description = "用户实体")
@Data
public class User {

    @ToolParam(description = "用户ID")
    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @ToolParam(description = "用户名")
    @NotBlank
    @Schema(description = "用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @ToolParam(description = "密码")
    @NotBlank
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @ToolParam(description = "邮箱，可选")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @ToolParam(description = "创建时间")
    @Schema(description = "创建时间", example = "2025-07-31 15:43:52")
    private LocalDateTime createTime;
}
