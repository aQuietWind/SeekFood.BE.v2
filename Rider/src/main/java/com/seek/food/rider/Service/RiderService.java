package com.seek.food.rider.Service;

import com.seek.food.dto.Rider.RiderDTO;
import org.springframework.web.multipart.MultipartFile;

public interface RiderService {
    public RiderDTO getDetail(long riderId);
    public RiderDTO getSelf();
    public void updatePersonImage(MultipartFile file);
    public String getUpdatePasswordOpt();
    public void updatePassword(String password, String opt);
    public String getDeleteOpt();
    public void delete(String opt);
}
