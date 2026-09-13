package com.seek.food.admin.Service.Impl;

import com.seek.food.admin.Mapper.SuggestionMapper;
import com.seek.food.admin.Service.SuggestionService;
import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Admin.AdminParamsRulesConfig;
import com.seek.food.config.NacosConfig.Admin.AdminRedisKeyConfig;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.MQ.AdminExchangeConfig;
import com.seek.food.dto.Admin.SuggestionDTO;
import com.seek.food.util.CommonUtil.IdUtil;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.FileUtil.FileSave;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.util.Redis.RedisUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
@RefreshScope
public class SuggestionServiceImpl implements SuggestionService {


    private final AdminRedisKeyConfig adminRedisKeyConfig;
    private final AdminExchangeConfig adminExchangeConfig;
    private final RabbitTemplate rabbitTemplate;
    private final AdminParamsRulesConfig adminParamsRulesConfig;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final SuggestionMapper suggestionMapper;

    public SuggestionServiceImpl(AdminRedisKeyConfig adminRedisKeyConfig, AdminExchangeConfig adminExchangeConfig, RabbitTemplate rabbitTemplate, AdminParamsRulesConfig adminParamsRulesConfig, CommonParamRulesConfig commonParamRulesConfig, StringRedisTemplate stringRedisTemplate, SuggestionMapper suggestionMapper) {
        this.adminRedisKeyConfig = adminRedisKeyConfig;
        this.adminExchangeConfig = adminExchangeConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.adminParamsRulesConfig = adminParamsRulesConfig;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.suggestionMapper = suggestionMapper;
    }

    @PostConstruct
    public void init() {
        stringRedisTemplate.opsForValue().setIfAbsent(adminRedisKeyConfig.getAdminSuggestionIdCount().getName(), ""+commonParamRulesConfig.getIdCapacity());
        FileSave.createDestDir(adminParamsRulesConfig.getSuggestionImageDest());
    }

    //插入建议
    @Override
    public void insertSuggestion(String description, MultipartFile file){
        //检查参数格式
        adminParamsRulesConfig.suggestionDescriptionCheck(description);
        //检查冷却并且获取id
        long tokenId= quickGetIdAndCheckCooldown(adminRedisKeyConfig.getAdminInsertSuggestionCooldown(),TokenIdContext.get());
        String addr=null;
        //先保存文件
        if (file!=null&&!file.isEmpty())addr=quickSave(file);
        //视为无效插入，直接返回
        if (addr==null&&(description==null||description.isBlank())) return;
        try {
        suggestionMapper.insert(new SuggestionDTO(
                IdUtil.IdGenerateByIncrease(adminRedisKeyConfig.getAdminSuggestionIdCount().getName(),stringRedisTemplate),
                tokenId,
                addr,
                description,
                null,
                null
        ));
        }catch (Exception e){
            log.error("accountId:{},在插入建议时,发生异常",tokenId,e);
            //删除可能保存的文件
            quickDeleteFile(addr);
        }
    }

    //批量获取建议
    @Override
    public List<SuggestionDTO> getSuggestionList(int start, int need){
        //检查参数
        commonParamRulesConfig.needNumberCheck(need);
        //只需要检查是否是管理员Id即可
        quickCheckAdminIdAndCheckCooldown(adminRedisKeyConfig.getAdminGetSuggestionListCooldown());
        //获取数据
        return suggestionMapper.getList(start, need);
    }

    //确认建议
    @Override
    public void ackSuggestion(long suggestionId){
        //检查参数
        commonParamRulesConfig.commonIdCheck(suggestionId);
        //只需要检查是否是管理员Id即可
        quickCheckAdminIdAndCheckCooldown(adminRedisKeyConfig.getAdminGetSuggestionListCooldown());
        //确认建议并且删除文件
        if (suggestionMapper.ack(suggestionId)) quickDeleteFile(suggestionMapper.getImageAddrAfterAck(suggestionId));
        else throw new BizException(ErrorCodeEnum.DATA_NOT_FOUND);
    }


    private void quickCooldown(RedisKeyData key, Object id){
        RedisUtil.checkCooldown(stringRedisTemplate,key.getRedisKey(id),key.getDuration());
    }

    private long quickGetIdAndCheckCooldown(RedisKeyData key, String id){
        quickCooldown(key,id);
        return Long.parseLong(id);
    }

    private void quickCheckAdminIdAndCheckCooldown(RedisKeyData key){
        String tokenId=TokenIdContext.get();
        quickCooldown(key,tokenId);
        commonParamRulesConfig.adminIdCheck(Long.parseLong(tokenId));
    }

    private String quickSave(MultipartFile file){
        return FileSave.quickCheckAndSaveFile(
                file
                ,adminParamsRulesConfig.getSuggestionImageDest()
                ,commonParamRulesConfig.getImageSize()
                ,commonParamRulesConfig.getImageType());
    }
    private void quickDeleteFile(String addr){
        if (addr==null||addr.isEmpty()) return;
        MQUtil.send(adminExchangeConfig.getExchangeName(),adminExchangeConfig.getDeleteFileAdminQueue().getRoutingKey()
                , Paths.get(adminParamsRulesConfig.getSuggestionImageDest(),addr).toString(),rabbitTemplate);
    }
}
