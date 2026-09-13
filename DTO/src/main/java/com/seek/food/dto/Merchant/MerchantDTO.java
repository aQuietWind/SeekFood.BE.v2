package com.seek.food.dto.Merchant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDTO {
    private Long merchantId;
    private String merchantName;
    private String merchantMasterName;
    private String merchantMasterCode;
    private String merchantMasterImageAddr;
    private String merchantMasterPhoneNumber;
    private String merchantHomeImageAddr;
    private String merchantProofImageAddr;
    private String merchantShowImageAddr;
    private String merchantShowDescription;
    private String merchantAddr;
    private Double merchantLon;
    private Double merchantLat;
    private Integer merchantOrderAmount;
    private Integer merchantFirstCommentAmount;
    private Integer merchantLikeAmount;
    private Integer merchantCollectAmount;
    private Integer merchantEmployeeAmount;
    private String merchantPassword;
    private String createTime;
    private Boolean qualified;
    private Boolean open;
    private Boolean delete;
}