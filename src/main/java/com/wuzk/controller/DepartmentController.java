package com.wuzk.controller;

import com.wuzk.entity.Department;
import com.wuzk.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Tag(name = "部门管理", description = "部门的增删改查接口")
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "查询所有部门", description = "获取系统中所有部门列表")
    @Tool(description = "查询所有部门")
    @GetMapping("/getAllDepartments")
    public List<Department> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @Operation(summary = "根据ID查询部门", description = "通过部门ID获取单个部门信息")
    @Tool(description = "通过部门ID获取单个部门信息")
    @GetMapping("/{id}")
    public Department getDepartmentById(@ToolParam(description = "部门ID") @Parameter(description = "部门ID") @PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }

    @Operation(summary = "创建部门", description = "创建一个新的部门")
    @Tool(description = "创建一个新的部门")
    @PostMapping
    public Department createDepartment(@ToolParam(description = "部门实体") @RequestBody Department department) {
        departmentService.createDepartment(department);

        return department;
    }

    @Operation(summary = "更新部门", description = "更新已有部门信息")
    @Tool(description = "更新已有部门信息")
    @PutMapping
    public Department updateDepartment(@ToolParam(description = "部门实体") @RequestBody Department department) {
        departmentService.updateDepartment(department);

        return department;
    }

    @Operation(summary = "删除部门", description = "根据部门ID删除部门")
    @Tool(description = "根据部门ID删除部门")
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteDepartment(@ToolParam(description = "部门ID") @Parameter(description = "部门ID") @PathVariable Long id) {
        departmentService.deleteDepartment(id);
        var response = new HashMap<String, Object>();
        response.put("status", "success");
        response.put("message", "Department deleted");
        response.put("departmentId", id);
        return response;
    }

    @Operation(summary = "根据部门名称查询部门", description = "通过部门名称获取部门信息")
    @Tool(description = "通过部门名称获取部门信息")
    @GetMapping("/name/{deptName}")
    public Department getDepartmentByName(
            @ToolParam(description = "部门名称") @Parameter(description = "部门名称") @PathVariable String deptName) {
        log.info("通过部门名称获取部门信息：{}", deptName);
        return departmentService.getDepartmentByName(deptName);
    }

}
