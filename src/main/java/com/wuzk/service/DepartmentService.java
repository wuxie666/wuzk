package com.wuzk.service;

import com.wuzk.entity.Department;

import java.util.List;

public interface DepartmentService {
    Department getDepartmentById(Long id);
    List<Department> getAllDepartments();
    int createDepartment(Department department);
    int updateDepartment(Department department);
    int deleteDepartment(Long id);
    Department getDepartmentByName(String deptName); // 新增
}
