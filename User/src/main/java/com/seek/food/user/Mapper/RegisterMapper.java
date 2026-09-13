package com.seek.food.user.Mapper;

import com.seek.food.dto.User.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RegisterMapper {
    public void insertUser(long userId, String phoneNumber, String password);
}
