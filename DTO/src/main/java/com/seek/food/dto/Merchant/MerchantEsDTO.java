package com.seek.food.dto.Merchant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
//注解属性只能硬编码，没办法
@Document(indexName = "merchant")
public class MerchantEsDTO {
    @Id
    private Long merchantId;
    private String merchantName;
    private Integer merchantCollectAmount;
    private Integer merchantOrderAmount;
    private String merchantHomeImageAddr;
    private String merchantLocation;
    @Field("is_open")
    private Boolean open;
    @Field("is_delete")
    private Boolean delete;
}
