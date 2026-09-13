package com.seek.food.user.Service;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.User.UserDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    public UserDTO getUserDetailMessage(long userId);
    public UserDTO getUserSelfMessage();
    public String updateUserPasswordGetOpt(String phoneNumber);
    public void updateUserPassword(String phoneNumber, String newPassword,String opt);
    public void updateUserHeader(MultipartFile file);
    public void updateUserMessage(UserDTO userDTO);
    public List<UserDTO> getUsersSimpleMessage(List<Long> userIds);
    public String getUserDeleteOpt();
    public void deleteUser(String opt);
}
