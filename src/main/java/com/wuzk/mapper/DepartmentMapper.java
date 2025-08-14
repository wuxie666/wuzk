package com.wuzk.mapper;

import com.wuzk.entity.Department;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepartmentMapper {
    Department selectDepartmentById(Long id);
    List<Department> selectAllDepartments();
    int insertDepartment(Department department);
    int updateDepartment(Department department);
    int deleteDepartmentById(Long id);

    Department selectDepartmentByName(String deptName); // 新增方法
}
