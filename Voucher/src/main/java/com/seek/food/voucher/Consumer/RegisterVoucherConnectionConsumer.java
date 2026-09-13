package com.seek.food.voucher.Consumer;

import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.MQ.VoucherExchangeConfig;
import com.seek.food.config.NacosConfig.Voucher.VoucherRedisKeyConfig;
import com.seek.food.dto.Voucher.MerchantVoucherDTO;
import com.seek.food.dto.Voucher.VoucherConnectionDTO;
import com.seek.food.dto.Voucher.VoucherConnectionMQDTO;
import com.seek.food.util.CommonUtil.IdUtil;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.voucher.Mapper.VoucherConnectionMapper;
import com.seek.food.voucher.Service.MerchantVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RegisterVoucherConnectionConsumer {
    private final VoucherConnectionMapper voucherConnectionMapper;
    private final MerchantVoucherService merchantVoucherService;
    private final StringRedisTemplate stringRedisTemplate;
    private final VoucherRedisKeyConfig voucherRedisKeyConfig;
    private final CommonParamRulesConfig commonParamRulesConfig;

    @Autowired
    public RegisterVoucherConnectionConsumer(VoucherConnectionMapper voucherConnectionMapper, MerchantVoucherService merchantVoucherService
            , StringRedisTemplate stringRedisTemplate, VoucherRedisKeyConfig voucherRedisKeyConfig, CommonParamRulesConfig commonParamRulesConfig) {
        this.voucherConnectionMapper = voucherConnectionMapper;
        this.merchantVoucherService = merchantVoucherService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.voucherRedisKeyConfig = voucherRedisKeyConfig;
        this.commonParamRulesConfig = commonParamRulesConfig;
        stringRedisTemplate.opsForValue().setIfAbsent(voucherRedisKeyConfig.getVoucherConnectionIdCount().getName(),""+commonParamRulesConfig.getIdCapacity());
    }


    @RabbitListener(queues = MQNameKeyEnum.Promotion_Exchange_Register_Voucher_Connection_Queue)
    public void registerVoucherConnectionQueue(VoucherConnectionMQDTO connection){
        MerchantVoucherDTO  voucher = merchantVoucherService.getDetail(connection.getVoucherId());
        if (voucher == null)return;
        //新增持有关系
        voucherConnectionMapper.insertConnection(new VoucherConnectionDTO(
                IdUtil.IdGenerateByIncrease(voucherRedisKeyConfig.getVoucherConnectionIdCount().getName(),stringRedisTemplate)
                , connection.getVoucherId(), connection.getUserId(), connection.getPromotionId(), null,voucher.getStartTime()
                ,voucher.getEndTime(),null,null,null));
    }
}
