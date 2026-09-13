package com.seek.food.merchant.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Merchant.MerchantEsTableConfig;
import com.seek.food.config.NacosConfig.Merchant.MerchantRedisKeyConfig;
import com.seek.food.dto.Merchant.MerchantEsDTO;
import com.seek.food.merchant.EsRepository.MerchantRepository;
import com.seek.food.merchant.Mapper.MerchantMapper;
import com.seek.food.util.Redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;



@Component
@Slf4j
public class EsSyncMerchantConsumer {
    private final StringRedisTemplate stringRedisTemplate;
    private final MerchantMapper merchantMapper;
    private final MerchantRedisKeyConfig merchantRedisKeyConfig;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final ElasticsearchOperations elasticsearchOperations;
    private final MerchantEsTableConfig merchantEsTableConfig;
    private final MerchantRepository merchantRepository;

    @Autowired
    public EsSyncMerchantConsumer(StringRedisTemplate stringRedisTemplate
    , MerchantMapper merchantMapper, MerchantRedisKeyConfig merchantRedisKeyConfig, CommonParamRulesConfig commonParamRulesConfig
    , ElasticsearchOperations elasticsearchOperations, MerchantEsTableConfig merchantEsTableConfig, MerchantRepository merchantRepository) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.merchantMapper = merchantMapper;
        this.merchantRedisKeyConfig = merchantRedisKeyConfig;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.elasticsearchOperations = elasticsearchOperations;
        this.merchantEsTableConfig = merchantEsTableConfig;
        this.merchantRepository = merchantRepository;
    }


    @RabbitListener(queues = MQNameKeyEnum.Merchant_Exchange_Es_Sync_Merchant_Queue)
    public void esSyncMerchantQueue(long merchantId){
        MerchantEsDTO merchant=merchantMapper.getEsMerchant(merchantId);
        //校验该商家是否已经删除
        if (merchant==null) {
            ackState(merchantId);
            return;
        }
        //检验是否为空
        if (merchant.getMerchantLocation()==null||merchant.getMerchantLocation().equals(",")) merchant.setMerchantLocation(null);
        //尝试同步
        try {
            updateOnly(merchant);
        }catch (Exception e){
            log.error("merchantId:{},同步es时出现异常:",merchantId,e);
            throw new RuntimeException(e);
        }finally {
            //无论失败成功都更改状态，防止出现永远不更新的问题
            ackState(merchantId);
        }
        merchantRepository.save(merchant);
    }

    //快速修改状态
    private void ackState(long merchantId){
        RedisUtil.oftenSetBit(stringRedisTemplate,merchantRedisKeyConfig.getMerchantEsSyncRecord().getName(),merchantId,
                false,commonParamRulesConfig.getIdCapacity(),commonParamRulesConfig.getIdBitmapAreaNumber());
    }


    public void updateOnly(MerchantEsDTO merchant) {
        // 执行更新，无文档则无任何写入
        elasticsearchOperations.update(merchant, IndexCoordinates.of(merchantEsTableConfig.getIndexName()));
    }










}
