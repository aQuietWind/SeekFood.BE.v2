package com.seek.food.merchant.Mapper;

import com.seek.food.dto.Common.SimplePoint;
import com.seek.food.dto.Merchant.MerchantDTO;
import com.seek.food.dto.Merchant.MerchantEsDTO;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MerchantMapper {
    public MerchantDTO getMerchantById(long merchantId);
    public boolean setMerchantMaster(long merchantId,String merchantMasterName, String merchantMasterCode,String merchantMasterImageAddr);
    public boolean addMerchantProofImage(long merchantId,String addr, int max);
    public String getMerchantProofImageByIndex(long merchantId, int index);
    public boolean removeMerchantProofImage(long merchantId,String oldAddr,int index);
    public boolean replaceMerchantProofImage(long merchantId,String addr,String oldAddr,int index);
    public boolean addShowImage(long merchantId,String addr, int max);
    public String getShowImageByIndex(long merchantId, int index);
    public boolean removeShowImage(long merchantId,String oldAddr,int index);
    public boolean replaceShowImage(long merchantId,String addr,String oldAddr,int index);
    public String getHomeImage(long merchantId);
    public boolean updateHomeImage(long merchantId,String addr,String oldAddr);
    public boolean updateMessage(MerchantDTO merchantDTO);
    public String getPhoneNumber(long merchantId);
    public boolean updatePassword(long merchantId,String password);
    public boolean deleteMerchant(long merchantId);
    public boolean updateOpen(long merchantId);
    public MerchantEsDTO getEsMerchant(long merchantId);
    public MerchantDTO getDeleteMerchant(long merchantId);
    public boolean updateEmployeeAmount(long merchantId,int changeNumber);
    public boolean updateCollectAmount(long merchantId,int changeNumber);
    public boolean updateFirstCommentAmount(long merchantId,int changeNumber);
    public boolean updateLikeAmount(long merchantId,int changeNumber);
    public boolean updateOrderAmount(long merchantId,int changeNumber);
    public SimplePoint getLonLat(long merchantId);















}
