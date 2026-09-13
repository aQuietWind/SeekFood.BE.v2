package com.seek.food.merchant.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seek.food.config.Data.RedisStreamData;
import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.Merchant.MerchantParamsRulesConfig;
import com.seek.food.config.NacosConfig.Merchant.MerchantRedisStreamConfig;
import com.seek.food.dto.Merchant.MerchantDTO;
import com.seek.food.merchant.Mapper.MerchantMapper;
import com.seek.food.util.FileUtil.FileRemove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@Slf4j
public class DeleteMerchantConsumer {
    private final MerchantMapper merchantMapper;
    private final MerchantParamsRulesConfig merchantParamsRulesConfig;
    private final RedisStreamData oldFileStream;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public DeleteMerchantConsumer(MerchantMapper merchantMapper, MerchantParamsRulesConfig merchantParamsRulesConfig
    , MerchantRedisStreamConfig merchantRedisStreamConfig, StringRedisTemplate stringRedisTemplate) {
        this.merchantMapper = merchantMapper;
        this.merchantParamsRulesConfig = merchantParamsRulesConfig;
        this.oldFileStream = merchantRedisStreamConfig.getOldFileStream();
        this.stringRedisTemplate = stringRedisTemplate;
    }


    @RabbitListener(queues = MQNameKeyEnum.Merchant_Exchange_Delete_Merchant_Queue)
    public void deleteMerchantQueue(long merchantId){
        MerchantDTO merchant=merchantMapper.getDeleteMerchant(merchantId);
        //删除店主照片
        FileRemove.removeFileOutError(merchantParamsRulesConfig.getMasterImageDest(),merchant.getMerchantMasterImageAddr());
        //删除封面照片
        FileRemove.removeFileOutError(merchantParamsRulesConfig.getHomeImageDest(),merchant.getMerchantHomeImageAddr());
        //删除证明照片
        FileRemove.removeFileArrayOutError(merchantParamsRulesConfig.getProofImageDest(),quickToStringArray(merchant.getMerchantProofImageAddr())
        ,(path,e)->{
            //不再进行重试，而是留给spring后台线程进行定时处理
            log.error("path:{},在删除时发生错误",path,e);
            stringRedisTemplate.opsForStream().add(oldFileStream.getName(), Map.of(oldFileStream.getKeyName(),path));
                });
        //删除展示照片
        FileRemove.removeFileArrayOutError(merchantParamsRulesConfig.getShowImageDest(),quickToStringArray(merchant.getMerchantShowImageAddr())
        ,(path,e)->{
            //不再进行重试，而是留给spring后台线程进行定时处理
            log.error("path:{},在删除时发生错误",path,e);
            stringRedisTemplate.opsForStream().add(oldFileStream.getName(), Map.of(oldFileStream.getKeyName(),path));
                });
    }

    private String[] quickToStringArray(String json){
        if (json == null||json.isBlank())return null;
        return json.substring(1,json.length()-1).replace("\"","").split(",");
    }








}
