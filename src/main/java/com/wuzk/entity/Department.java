package com.wuzk.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDateTime;

@Schema(description = "部门实体")
@Data
public class Department {

    @ToolParam(description = "部门ID")
    @Schema(description = "部门ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @ToolParam(description = "部门名称")
    @NotBlank
    @Schema(description = "部门名称", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deptName;

    @ToolParam(description = "创建时间")
    @Schema(description = "创建时间", example = "2025-07-31 15:43:52")
    private LocalDateTime createTime;

    @ToolParam(description = "更新时间")
    @Schema(description = "更新时间", example = "2025-07-31 15:43:52")
    private LocalDateTime updateTime;
}
