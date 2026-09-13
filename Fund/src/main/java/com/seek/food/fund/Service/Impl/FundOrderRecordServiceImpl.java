package com.seek.food.fund.Service.Impl;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Fund.FundParamsRulesConfig;
import com.seek.food.config.NacosConfig.Fund.FundRedisKeyConfig;
import com.seek.food.config.NacosConfig.MQ.DeadLetterExchangeConfig;
import com.seek.food.config.NacosConfig.MQ.FundExchangeConfig;
import com.seek.food.dto.Fund.FundOrderRecordDTO;
import com.seek.food.fund.Caffeine.FundOrderRecordCaffeine;
import com.seek.food.fund.Mapper.FundOrderRecordMapper;
import com.seek.food.fund.Service.FundOrderRecordService;
import com.seek.food.fund.Service.FundService;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.util.Redis.RedisUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RefreshScope
public class FundOrderRecordServiceImpl implements FundOrderRecordService {


    private final CommonParamRulesConfig commonParamRulesConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final FundRedisKeyConfig fundRedisKeyConfig;
    private final FundOrderRecordMapper fundOrderRecordMapper;
    private final FundOrderRecordCaffeine fundOrderRecordCaffeine;
    private final FundService fundService;
    private final FundExchangeConfig fundExchangeConfig;
    private final RabbitTemplate rabbitTemplate;
    private final DeadLetterExchangeConfig deadLetterExchangeConfig;

    public FundOrderRecordServiceImpl(CommonParamRulesConfig commonParamRulesConfig, StringRedisTemplate stringRedisTemplate
            , FundRedisKeyConfig fundRedisKeyConfig, FundOrderRecordMapper fundOrderRecordMapper, FundOrderRecordCaffeine fundOrderRecordCaffeine
            , FundService fundService, FundExchangeConfig fundExchangeConfig, RabbitTemplate rabbitTemplate, DeadLetterExchangeConfig deadLetterExchangeConfig) {
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.fundRedisKeyConfig = fundRedisKeyConfig;
        this.fundOrderRecordMapper = fundOrderRecordMapper;
        this.fundOrderRecordCaffeine = fundOrderRecordCaffeine;
        this.fundService = fundService;
        this.fundExchangeConfig = fundExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.deadLetterExchangeConfig = deadLetterExchangeConfig;
    }

    //批量获取预览信息
    @Override
    public List<FundOrderRecordDTO> getSimple(int start, int need){
        //检查需求量
        commonParamRulesConfig.needNumberCheck(need);
        //获取tokenId
        long userId= quickGetUserId();
        //检查冷却期
        quickCooldown(fundRedisKeyConfig.getFundGetSimpleOrderCooldown(),userId);
        //返回查询结果
        return fundOrderRecordMapper.getSimple(start,need,userId);
    }

    //获取详细信息
    @Override
    public FundOrderRecordDTO getDetail(long recordId){
        //检查目标id规范
        commonParamRulesConfig.commonIdCheck(recordId);
        //获取userId,因为只有userId可以下单
        long userId=quickGetUserId();
        return quickGetRecord(userId,recordId);
    }

    //支付订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(long recordId){
        //获取userId
        long userId= quickGetUserId();
        //检查冷却期,通过双id机制，使不同订单享有不同冷却
        quickCooldown(fundRedisKeyConfig.getFundPayOrderRecordCooldown(),userId+""+recordId);
        //获取记录
        FundOrderRecordDTO record=quickGetRecord(userId,recordId);
        if(record==null) throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
        //检测是否已经过了截止期
        if (LocalDateTime.now().isAfter(record.getDeadline())) throw new BizException(ErrorCodeEnum.DATA_IS_EXPIRE);
        //如果失败会报错的，所以不用检验结果
        fundService.decreaseFund(record.getCost(),userId);
        //设置订单记录为已经支付
        if (!fundOrderRecordMapper.ackPay(recordId))throw new BizException(ErrorCodeEnum.DATA_SURVIVE);
        //发送消息
        MQUtil.send(fundExchangeConfig.getExchangeName(),fundExchangeConfig.getUseVoucherQueue().getRoutingKey()
        ,record.getOrderId(),rabbitTemplate);
    }

    //在全局异常回滚死信消息丢失时，可进行全局回滚订单
    @Override
    public void rollback(long recordId){
        //获取userId
        long userId= quickGetUserId();
        //检查冷却期
        quickCooldown(fundRedisKeyConfig.getFundRollbackOrderRecordCooldown(),userId);
        //进行回滚条件判断,获取详细记录信息
        Long orderId=fundOrderRecordMapper.ableRollback(recordId,userId);
        if (orderId==null) throw new BizException(ErrorCodeEnum.CONDITION_NOT_PASS);
        //发送消息进行全局回滚尝试，在MQ的消费者会立即进行判断并且尝试回滚
        MQUtil.send(deadLetterExchangeConfig.getExchangeName(),deadLetterExchangeConfig.getRollbackAllFundImplQueue().getRoutingKey()
        ,orderId ,rabbitTemplate);
    }

    //确认退款
    @Override
    public void ackRefund(long orderId){
        //在订单记录中确认退款,然后让MySQL的触发器进行资金回滚,防止由于消费失败重试引起的重复消费的问题
        fundOrderRecordMapper.refund(orderId);
        //删除缓存
        quickDeleteAllCaffeine(fundOrderRecordMapper.getRecordIdByOrderId(orderId));
    }




    private long quickGetUserId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getUserIdStart(),commonParamRulesConfig.getIdCapacity());
    }

    private void quickCooldown(RedisKeyData keyData, Object id) {
        RedisUtil.checkCooldown(stringRedisTemplate,keyData.getRedisKey(id), keyData.getDuration());
    }

    private FundOrderRecordDTO quickGetRecord(long userId,long recordId){
        return fundOrderRecordCaffeine.getAndAutoLoad(recordId,stringRedisTemplate
                ,fundRedisKeyConfig.getFundOrderRecordCaffeineMessage().getRedisKey(recordId)
                ,fundRedisKeyConfig.getFundOrderRecordCaffeineMessage().getDuration()
                , FundOrderRecordDTO.class, k->fundOrderRecordMapper.getDetail(recordId,userId));
    }

    public void quickDeleteAllCaffeine(long recordId){
        //删除缓存
        fundOrderRecordCaffeine.deleteAllCaffeine(recordId, stringRedisTemplate
                , fundRedisKeyConfig.getFundOrderRecordCaffeineMessage().getRedisKey(recordId));
    }
}
