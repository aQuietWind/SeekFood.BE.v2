package com.seek.food.user.Mapper;

import com.seek.food.dto.User.UserDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    public UserDTO getUserDetailMessage(long userId);
    public boolean updateUserPassword(String phoneNumber,String newPassword);
    public boolean updateUserHeader(long userId, String addr,String oldAddr);
    public boolean updateUserMessage(UserDTO userDTO);
    public boolean increaseOrderAmount(long userId);
    public List<UserDTO> getUsersSimpleMessage(List<Long> userIds);
    public String getPhoneNumber(long userId);
    public boolean deleteUser(long userId);
    public String getHeaderPath(long userId);
    public String getDeleteHeaderPath(long userId);
    public boolean updateOrderAmount(long userId,int changeNumber);
}
