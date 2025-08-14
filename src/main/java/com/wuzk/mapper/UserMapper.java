package com.wuzk.mapper;

import com.wuzk.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User selectUserById(Long id);
    List<User> selectAllUsers();
    int insertUser(User user);
    int updateUser(User user);
    int deleteUserById(Long id);

    User selectUserByUsername(@Param("username") String username);
}