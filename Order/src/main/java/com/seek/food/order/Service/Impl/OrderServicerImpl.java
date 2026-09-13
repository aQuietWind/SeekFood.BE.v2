package com.seek.food.order.Service.Impl;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.MQ.OrderExchangeConfig;
import com.seek.food.config.NacosConfig.Order.OrderParamsRulesConfig;
import com.seek.food.config.NacosConfig.Order.OrderRedisKeyConfig;
import com.seek.food.config.NacosConfig.Order.OrderRiderOrderEsTableConfig;
import com.seek.food.dto.Chat.ChatRoomDTO;
import com.seek.food.dto.Comment.FirstCommentDTO;
import com.seek.food.dto.Common.ChangeAmountDTO;
import com.seek.food.dto.Fund.FundOrderRecordDTO;
import com.seek.food.dto.Fund.FundRechargeRecordDTO;
import com.seek.food.dto.Meal.MealDTO;
import com.seek.food.dto.Order.OrderDTO;
import com.seek.food.dto.Order.RiderOrderEsDTO;
import com.seek.food.order.Caffeine.OrderCaffeine;
import com.seek.food.order.EsRepository.RiderOrderRepository;
import com.seek.food.order.Feign.MealClient;
import com.seek.food.order.Feign.MerchantClient;
import com.seek.food.order.Feign.VoucherClient;
import com.seek.food.order.Mapper.OrderMapper;
import com.seek.food.order.Mapper.SearchMapper;
import com.seek.food.order.Service.OrderService;
import com.seek.food.util.CommonUtil.DoubleUtil;
import com.seek.food.util.CommonUtil.IdUtil;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.util.Redis.RedisUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RefreshScope
@Slf4j
public class OrderServicerImpl implements OrderService {


    private final CommonParamRulesConfig commonParamRulesConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final OrderRedisKeyConfig orderRedisKeyConfig;
    private final MealClient mealClient;
    private final VoucherClient voucherClient;
    private final MerchantClient merchantClient;
    private final RedissonClient redissonClient;
    private final OrderParamsRulesConfig orderParamsRulesConfig;
    private final OrderMapper orderMapper;
    private final OrderExchangeConfig orderExchangeConfig;
    private final RabbitTemplate rabbitTemplate;
    private final OrderCaffeine orderCaffeine;
    private final RiderOrderRepository riderOrderRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final OrderRiderOrderEsTableConfig orderRiderOrderEsTableConfig;

    public OrderServicerImpl(CommonParamRulesConfig commonParamRulesConfig, StringRedisTemplate stringRedisTemplate
            , OrderRedisKeyConfig orderRedisKeyConfig, MealClient mealClient, VoucherClient voucherClient, MerchantClient merchantClient
            , RedissonClient redissonClient, OrderParamsRulesConfig orderParamsRulesConfig, OrderMapper orderMapper
            , OrderExchangeConfig orderExchangeConfig, RabbitTemplate rabbitTemplate, OrderCaffeine orderCaffeine, RiderOrderRepository riderOrderRepository, ElasticsearchOperations elasticsearchOperations, OrderRiderOrderEsTableConfig orderRiderOrderEsTableConfig) {
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.orderRedisKeyConfig = orderRedisKeyConfig;
        this.mealClient = mealClient;
        stringRedisTemplate.opsForValue().setIfAbsent(orderRedisKeyConfig.getOrderIdCount().getName(),""+commonParamRulesConfig.getIdCapacity());
        this.voucherClient = voucherClient;
        this.merchantClient = merchantClient;
        this.redissonClient = redissonClient;
        this.orderParamsRulesConfig = orderParamsRulesConfig;
        this.orderMapper = orderMapper;
        this.orderExchangeConfig = orderExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.orderCaffeine = orderCaffeine;
        this.riderOrderRepository = riderOrderRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.orderRiderOrderEsTableConfig = orderRiderOrderEsTableConfig;
    }

    //新增订单
    @Override
    public void insertOrder(long mealId,Long connectionId,int number,double lon,double lat,String deliveryAddress){
        commonParamRulesConfig.commonIdCheck(mealId);
        if (connectionId!=null)commonParamRulesConfig.commonIdCheck(connectionId);
        //获取用户id
        long userId=quickGetUserId();
        //上锁,没成功就直接返回
        RLock lock=redissonClient.getLock(orderRedisKeyConfig.getOrderInsertLock().getRedisKey(userId));
        if (!lock.tryLock()) throw new BizException(ErrorCodeEnum.REQUEST_IN_COOLDOWN);
        try {
            //获取餐品信息
            MealDTO meal = mealClient.mealGetDetail(mealId).getData();
            if (meal == null) throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
            //通过运送地址，发到商家模块去获取运送距离
            Long distance = merchantClient.merchantGetDistance(lon, lat, meal.getMerchantId()).getData();
            if (distance == null) throw new BizException(ErrorCodeEnum.DATA_NOT_RIGHT);
            //检查优惠券是否合规，不用担心并发问题，因为上了redis锁，除非redis故障了
            Double discountCost=0.0;
            if (connectionId!=null) {
                discountCost = voucherClient.connectionCheck(connectionId, connectionId).getData();
                if (discountCost == null) throw new BizException(ErrorCodeEnum.CONDITION_NOT_PASS);
            }
            //生成order实体类用于DB插入
            OrderDTO order=OrderDTO.quickGet(
                    IdUtil.IdGenerateByIncrease(orderRedisKeyConfig.getOrderIdCount().getName(),stringRedisTemplate)
                    ,userId,meal,connectionId,number,lon,lat,deliveryAddress,discountCost,orderParamsRulesConfig.distanceCost(distance));
            //锁定优惠券
            if (connectionId!=null)voucherClient.connectionLock(connectionId, order.getOrderId());
            //新增该订单
            orderMapper.insertOrder(order);
            //删除可能存在的空缓存
            quickDeleteCaffeine(order.getOrderId());
            //发送至Fund处新增订单记录，然后等待支付
            MQUtil.send(orderExchangeConfig.getExchangeName(),orderExchangeConfig.getRegisterFundOrderRecordQueue().getRoutingKey()
            , new FundOrderRecordDTO(userId, order.getOrderId(), "正常下单",order.getTotalCost()),rabbitTemplate );
        }finally {
            //解锁
            lock.unlock();
        }
    }

    //获取订单的简易信息
    @Override
    @Transactional
    public List<OrderDTO> getSimple(int start, int need){
        //检测参数
        commonParamRulesConfig.needNumberCheck(need);
        //检测冷却
        long tokenId=quickGetIdAndCheckCooldown(orderRedisKeyConfig.getOrderGetSimpleCooldown(),quickGetTokenId());
        //从DB返回
        return orderMapper.getSimple(start,need,tokenId);
    }

    //根据所处阶段获取订单的简易信息
    @Override
    @Transactional
    public List<OrderDTO> getSimpleState(int start, int need, int state){
        //检测参数
        commonParamRulesConfig.needNumberCheck(need);
        orderParamsRulesConfig.stateNumberCheck(state);
        //检测冷却
        long tokenId=quickGetIdAndCheckCooldown(orderRedisKeyConfig.getOrderGetSimpleByStateCooldown(),quickGetTokenId());
        //如果目标阶段为0，则检测是不是用户要去获取订单
        if (state==0)commonParamRulesConfig.userIdCheck(tokenId);
        //从DB返回
        return orderMapper.getSimpleState(start,need,state,tokenId);
    }

    //获取订单详细信息
    @Override
    @Transactional
    public OrderDTO  getDetail(long orderId){
        //检测参数
        commonParamRulesConfig.commonIdCheck(orderId);
        //获取tokenId
        long tokenId=quickGetTokenId();
        //获取订单信息
        OrderDTO order=quickGetOrder(orderId);
        //鉴别该tokenId是否有资格获取该订单信息
        checkIdWithOrder(order,tokenId);
        return order;
    }

    //用户订单退款
    @Override
    @Transactional
    public void refund(long orderId){
        //检查订单id
        commonParamRulesConfig.commonIdCheck(orderId);
        //检查冷却
        long userId=quickGetIdAndCheckCooldown(orderRedisKeyConfig.getOrderRefundCooldown(),quickGetUserId());
        //退款尝试
        quickUpdate(orderId,k->orderMapper.refund(orderId,userId));
        //回滚资金
        quickRollback(orderId);
    }

    //商家拒绝执行订单
    @Override
    @Transactional
    public void merchantReject(long orderId){
        //检查订单id
        commonParamRulesConfig.commonIdCheck(orderId);
        //检查冷却
        long merchantId=quickGetIdAndCheckCooldown(orderRedisKeyConfig.getOrderMerchantRejectCooldown(),quickGetMerchantId());
        //退款尝试
        quickUpdate(orderId,k->orderMapper.merchantReject(orderId,merchantId));
        //回滚资金
        quickRollback(orderId);
    }

    //商家确定订单
    @Override
    @Transactional
    public void merchantAck(long orderId){
        //检查订单id
        commonParamRulesConfig.commonIdCheck(orderId);
        //检查冷却
        long merchantId=quickGetIdAndCheckCooldown(orderRedisKeyConfig.getOrderMerchantAckCooldown(),quickGetMerchantId());
        //更新尝试
        quickUpdate(orderId,k->orderMapper.merchantAck(orderId,merchantId));
        //插入至es，使骑手可以获取订单信息来决定是否抢单
        riderOrderRepository.save(new RiderOrderEsDTO(quickGetOrder(orderId)));
    }

    //商家确认制作完订单
    @Override
    @Transactional
    public void merchantMake(long orderId){
        //检查订单id
        commonParamRulesConfig.commonIdCheck(orderId);
        //检查冷却
        long merchantId=quickGetIdAndCheckCooldown(orderRedisKeyConfig.getOrderMerchantMakeCooldown(),quickGetMerchantId());
        //更新尝试
        quickUpdate(orderId,k->orderMapper.merchantMake(orderId,merchantId));
        //打款到商家端
        OrderDTO order=quickGetOrder(orderId);
        double amount= DoubleUtil.onlyXAfterPoint(2,order.getTotalCost()-order.getRiderCost());
        quickTransfer(merchantId,"商家完成订单制作,获得打款。  订单Id为:"+orderId,amount);
        //发送至MQ增加商家订单数
        quickSend(orderExchangeConfig.getChangeMerchantOrderAmountQueue().getRoutingKey(),new ChangeAmountDTO(merchantId,1));
        //发送至MQ增加餐品的销售量
        quickSend(orderExchangeConfig.getChangeMealSalesVolumeQueue().getRoutingKey(),new ChangeAmountDTO(order.getMealId(),order.getNumber()));
    }

    //骑手接受订单
    @Override
    @Transactional
    public void riderAccept(long orderId){
        //检查订单id
        commonParamRulesConfig.commonIdCheck(orderId);
        //检查冷却
        long riderId=quickGetIdAndCheckCooldown(orderRedisKeyConfig.getOrderRiderAcceptCooldown(),quickGetRiderId());
        //更新尝试
        quickUpdate(orderId,k->orderMapper.riderAccept(orderId,riderId));
        //es同步,执行局部更新
        elasticsearchOperations.update(UpdateQuery.builder(String.valueOf(orderId))
                        .withDocument(Document.from(Map.of(orderRiderOrderEsTableConfig.getAccept(),true)))
                        .build()
                , IndexCoordinates.of(orderRiderOrderEsTableConfig.getIndexName()));
        //发送到Chat模块消费者进行聊天室初始化
        OrderDTO order=quickGetOrder(orderId);
        quickSend(orderExchangeConfig.getChatRoomInitQueue().getRoutingKey(),new ChatRoomDTO(
                order.getOrderId(),order.getUserId(),order.getMerchantId(),order.getRiderId()
        ));
    }

    //骑手确认已经拿到订单餐品
    @Override
    @Transactional
    public void riderAck(long orderId){
        //检查订单id
        commonParamRulesConfig.commonIdCheck(orderId);
        //检查冷却
        long riderId=quickGetIdAndCheckCooldown(orderRedisKeyConfig.getOrderRiderAckCooldown(),quickGetRiderId());
        //更新尝试
        quickUpdate(orderId,k->orderMapper.riderAck(orderId,riderId));
    }

    //骑手确认已经运送餐品到目标地点
    @Override
    @Transactional
    public void riderDelivery(long orderId){
        //检查订单id
        commonParamRulesConfig.commonIdCheck(orderId);
        //检查冷却
        long riderId=quickGetIdAndCheckCooldown(orderRedisKeyConfig.getOrderRiderDeliveryCooldown(),quickGetRiderId());
        //更新尝试
        quickUpdate(orderId,k->orderMapper.riderDelivery(orderId,riderId));
        //打款到骑手端
        quickTransfer(riderId,"骑手完成订单,获得打款。  订单Id为:"+orderId,quickGetOrder(orderId).getRiderCost());
    }

    //用户确认接收到订单餐品
    @Override
    @Transactional
    public void userReceive(long orderId){
        //检查订单id
        commonParamRulesConfig.commonIdCheck(orderId);
        //检查冷却
        long userId=quickGetIdAndCheckCooldown(orderRedisKeyConfig.getOrderUserReceiveCooldown(),quickGetUserId());
        //更新尝试
        quickUpdate(orderId,k->orderMapper.userReceive(orderId,userId));
        //发送增加用户订单数
        quickSend(orderExchangeConfig.getChangeUserOrderAmountQueue().getRoutingKey(),new ChangeAmountDTO(userId,1));
        //发送更改聊天室状态
        quickSend(orderExchangeConfig.getChatRoomCompleteQueue().getRoutingKey(),orderId);
    }

    //用户插入评论时查询订单获取一定信息
    @Override
    public FirstCommentDTO commentSelect(long orderId){
        //检测订单id
        commonParamRulesConfig.commonIdCheck(orderId);
        //获取用户id
        long userId=quickGetIdAndCheckCooldown(orderRedisKeyConfig.getOrderCommentSelectCooldown(),quickGetUserId());
        //获取信息
        return orderMapper.commentSelect(orderId,userId);
    }






    private void quickCooldown(RedisKeyData key, Object id){
        RedisUtil.checkCooldown(stringRedisTemplate,key.getRedisKey(id),key.getDuration());
    }

    private long quickGetMerchantId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
    }

    private long quickGetUserId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getUserIdStart(),commonParamRulesConfig.getIdCapacity());
    }

    private long quickGetRiderId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getRiderIdStart(),commonParamRulesConfig.getIdCapacity());
    }

    private long quickGetTokenId(){
        return TokenIdContext.getAndToLong();
    }

    private long quickGetIdAndCheckCooldown(RedisKeyData key, long id){
        quickCooldown(key,id);
        return id;
    }

    private void checkIdWithOrder(OrderDTO order,long tokenId){
        if (order.getUserId()!=tokenId&&order.getRiderId()!=tokenId&&order.getMerchantId()!=tokenId){
            throw new BizException(ErrorCodeEnum.REQUEST_NOT_QUALIFIED);
        }
    }

    private void quickUpdate(long orderId, Function<Long,Boolean> function){
        orderCaffeine.updateAndRemoveCaffeine(orderId,stringRedisTemplate
                ,orderRedisKeyConfig.getOrderMessageCaffeine().getRedisKey(orderId),function);
    }

    private void quickRollback(long orderId){
        MQUtil.send(orderExchangeConfig.getExchangeName(),orderExchangeConfig.getRollbackFundQueue().getRoutingKey()
                , orderId,rabbitTemplate);
    }

    private OrderDTO quickGetOrder(long orderId){
        return orderCaffeine.getAndAutoLoad(orderId,stringRedisTemplate,orderRedisKeyConfig.getOrderMessageCaffeine().getRedisKey(orderId)
            ,orderRedisKeyConfig.getOrderMessageCaffeine().getDuration(),OrderDTO.class,k->orderMapper.getDetail(orderId));
    }

    private void quickTransfer(long accountId,String description,double amount){
        MQUtil.send(orderExchangeConfig.getExchangeName(),orderExchangeConfig.getTransferFundQueue().getRoutingKey()
                , new FundRechargeRecordDTO(null,accountId,description,amount,null),rabbitTemplate);
    }

    private void quickDeleteCaffeine(long orderId){
        orderCaffeine.deleteAllCaffeine(orderId,stringRedisTemplate,orderRedisKeyConfig.getOrderMessageCaffeine().getRedisKey(orderId));
    }

    private void quickSend(String routingKey,Object message){
        MQUtil.send(orderExchangeConfig.getExchangeName(),routingKey,message,rabbitTemplate);
    }

}
