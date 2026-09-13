package com.seek.food.rider.Mapper;

import com.seek.food.dto.Rider.RiderDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RiderMapper {
    public RiderDTO getDetail(long riderId);
    public boolean updatePersonImage(String addr,String oldAddr,long riderId);
    public String getPersonImageAddr(long riderId);
    public String getPersonImageAddrAfterDelete(long riderId);
    public String getPhoneByRiderId(long riderId);
    public void updatePassword(String password,long riderId);
    public boolean delete(long riderId);
}
