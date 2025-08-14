package com.wuzk.service.impl;

import com.wuzk.entity.Department;
import com.wuzk.mapper.DepartmentMapper;
import com.wuzk.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    public Department getDepartmentById(Long id) {
        return departmentMapper.selectDepartmentById(id);
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentMapper.selectAllDepartments();
    }

    @Override
    public int createDepartment(Department department) {
        return departmentMapper.insertDepartment(department);
    }

    @Override
    public int updateDepartment(Department department) {
        return departmentMapper.updateDepartment(department);
    }

    @Override
    public int deleteDepartment(Long id) {
        return departmentMapper.deleteDepartmentById(id);
    }

    @Override
    public Department getDepartmentByName(String deptName) {
        return departmentMapper.selectDepartmentByName(deptName);
    }
}
