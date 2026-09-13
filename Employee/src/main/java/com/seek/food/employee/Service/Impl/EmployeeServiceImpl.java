package com.seek.food.employee.Service.Impl;

import com.seek.food.config.Data.RedisKeyData;
import com.seek.food.config.NacosConfig.Common.CommonParamRulesConfig;
import com.seek.food.config.NacosConfig.Employee.EmployeeParamsRulesConfig;
import com.seek.food.config.NacosConfig.Employee.EmployeeRedisKeyConfig;
import com.seek.food.config.NacosConfig.MQ.EmployeeExchangeConfig;
import com.seek.food.dto.Common.ChangeAmountDTO;
import com.seek.food.dto.Employee.EmployeeDTO;
import com.seek.food.employee.Caffeine.EmployeeCaffeine;
import com.seek.food.employee.Mapper.EmployeeMapper;
import com.seek.food.employee.Service.EmployeeService;
import com.seek.food.util.CommonUtil.IdUtil;
import com.seek.food.util.Context.TokenIdContext;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.FileUtil.FileSave;
import com.seek.food.util.Function.RunWithParam;
import com.seek.food.util.MQ.MQUtil;
import com.seek.food.util.Redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

@Service
@RefreshScope
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeMapper employeeMapper;
    private final EmployeeExchangeConfig employeeExchangeConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final EmployeeParamsRulesConfig employeeParamsRulesConfig;
    private final EmployeeCaffeine employeeCaffeine;
    private final EmployeeRedisKeyConfig employeeRedisKeyConfig;
    private final CommonParamRulesConfig commonParamRulesConfig;
    private final RabbitTemplate rabbitTemplate;
    @Autowired
    public EmployeeServiceImpl(EmployeeMapper employeeMapper,EmployeeExchangeConfig employeeExchangeConfig
    , StringRedisTemplate stringRedisTemplate, EmployeeParamsRulesConfig employeeParamsRulesConfig, EmployeeCaffeine employeeCaffeine
    , EmployeeRedisKeyConfig employeeRedisKeyConfig, CommonParamRulesConfig commonParamRulesConfig, RabbitTemplate rabbitTemplate) {
        this.employeeMapper = employeeMapper;
        this.employeeExchangeConfig = employeeExchangeConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.employeeParamsRulesConfig = employeeParamsRulesConfig;
        this.employeeCaffeine = employeeCaffeine;
        this.employeeRedisKeyConfig = employeeRedisKeyConfig;
        this.commonParamRulesConfig = commonParamRulesConfig;
        this.rabbitTemplate = rabbitTemplate;
        stringRedisTemplate.opsForValue().setIfAbsent(employeeRedisKeyConfig.getEmployeeIdCount().getName(),""+commonParamRulesConfig.getIdCapacity());
        FileSave.createDestDir(employeeParamsRulesConfig.getPersonImageDest());
    }

    //新增职员
    @Override
    public void insertEmployee(String employeeName){
        //检查名字格式
        commonParamRulesConfig.personNameCheck(employeeName);
        //获取商家Id
        long merchantId= quickGetMerchantId();
        //检查冷却期
        quickCheckCooldown(employeeRedisKeyConfig.getEmployeeInsertCooldown(),merchantId);
        //新增
        employeeMapper.insertEmployee(employeeName
                , IdUtil.IdGenerateByIncrease(employeeRedisKeyConfig.getEmployeeIdCount().getName(),stringRedisTemplate),merchantId);
        //MQ转发
        quickAmountSync(merchantId,1);
    }

    //获取职员简易信息
    @Override
    public List<EmployeeDTO> getSimpleEmployee(int start, int need){
        //检查格式
        commonParamRulesConfig.needNumberCheck(need);
        //获取商家Id
        long merchantId=quickGetMerchantId();
        //检查冷却
        quickCheckCooldown(employeeRedisKeyConfig.getEmployeeGetSimpleCooldown(),merchantId);
        return employeeMapper.getSimpleEmployee(start,need,merchantId);
    }

    //根据任职情况获取职员简易信息
    @Override
    public List<EmployeeDTO> getSimpleByResign(boolean resign,int start,int need){
        //检查格式
        commonParamRulesConfig.needNumberCheck(need);
        //获取商家Id
        long merchantId=quickGetMerchantId();
        //检查冷却
        quickCheckCooldown(employeeRedisKeyConfig.getEmployeeGetSimpleCooldown(),merchantId);
        return employeeMapper.getSimpleByResign(resign,start,need,merchantId);
    }

    //根据职员姓名获取职员简易信息
    @Override
    public List<EmployeeDTO> searchSimpleByName(String employeeName,int start,int need){
        //检查格式
        commonParamRulesConfig.needNumberCheck(need);
        commonParamRulesConfig.personNameCheck(employeeName);
        //获取商家id
        long merchantId=quickGetMerchantId();
        //检查冷却
        quickCheckCooldown(employeeRedisKeyConfig.getEmployeeGetSimpleCooldown(),merchantId);
        return employeeMapper.getSimpleByName(employeeName,start,need,merchantId);
    }

    //根据部门名称获取职员简易信息
    @Override
    public List<EmployeeDTO> searchSimpleByDep(String depName,int start,int need){
        //检查参数
        commonParamRulesConfig.needNumberCheck(need);
        employeeParamsRulesConfig.depNameCheck(depName);
        //获取商家Id
        long merchantId=quickGetMerchantId();
        //检查冷却
        quickCheckCooldown(employeeRedisKeyConfig.getEmployeeGetSimpleCooldown(),merchantId);
        return employeeMapper.getSimpleByDep(depName,start,need,merchantId);
    }

    //获取职员详细信息
    @Override
    public EmployeeDTO getDetailEmployee(long employeeId){
        //检查格式
        commonParamRulesConfig.commonIdCheck(employeeId);
        //获取商家id
        long merchantId=quickGetMerchantId();
        //缓存获取
        return employeeCaffeine.getAndAutoLoad(employeeId,stringRedisTemplate,employeeRedisKeyConfig.getEmployeeMessageCaffeine().getRedisKey(employeeId)
        ,employeeRedisKeyConfig.getEmployeeMessageCaffeine().getDuration(), EmployeeDTO.class
        ,key->employeeMapper.getDetailEmployee(employeeId,merchantId));
    }

    //更新职员信息
    @Override
    public void updateEmployeeMessage(EmployeeDTO employee){
        //检查格式
        commonParamRulesConfig.commonIdCheck(employee.getEmployeeId());
        if (employee.getEmployeeName()!=null) commonParamRulesConfig.personNameCheck(employee.getEmployeeName());
        if (employee.getEmployeeCode()!=null) commonParamRulesConfig.codeCheck(employee.getEmployeeCode());
        if (employee.getEmployeePhoneNumber()!=null) commonParamRulesConfig.phoneNumberCheck(employee.getEmployeePhoneNumber());
        employeeParamsRulesConfig.employeeCheck(employee);
        //获取商家id
        long merchantId=quickGetMerchantId();
        //放入其中
        employee.setMerchantId(merchantId);
        //检查冷却
        quickCheckCooldown(employeeRedisKeyConfig.getEmployeeUpdateMessageCooldown(),merchantId);
        //写入MySQL
        quickUpdateAndRemoveCaffeine(employee.getEmployeeId(),k->employeeMapper.updateEmployeeMessage(employee));
    }

    //更改职员照片
    @Override
    public void updatePersonImage(MultipartFile file,long employeeId){
        //检查格式
        commonParamRulesConfig.commonIdCheck(employeeId);
        //获取商家id
        long merchantId=quickGetMerchantId();
        //检查冷却
        quickCheckCooldown(employeeRedisKeyConfig.getEmployeeUpdatePersonImageCooldown(),merchantId);
        //先保存文件
        String addr=FileSave.quickCheckAndSaveFile(file,employeeParamsRulesConfig.getPersonImageDest()
        ,commonParamRulesConfig.getImageSize(),commonParamRulesConfig.getImageType());
        //先获取旧文件地址
        String oldAddr=employeeMapper.getPersonImageAddr(employeeId,merchantId);
        //写入DB,并且清除缓存
        quickUpdateAndRemoveCaffeine(employeeId,k->{
            if (!employeeMapper.updatePersonImage(addr,oldAddr,employeeId,merchantId)) {
                //删除刚刚保存的文件
                quickDeletePersonImage(addr);
                return false;
            }
            return true;
        });
        //将旧文件放入MQ后台销毁
        quickDeletePersonImage(oldAddr);
    }

    //更改职员任职状态
    @Override
    public void updateEmployeeResign(long employeeId){
        //检查格式
        commonParamRulesConfig.commonIdCheck(employeeId);
        //获取商家id
        long merchantId=quickGetMerchantId();
        //检查冷却
        quickCheckCooldown(employeeRedisKeyConfig.getEmployeeUpdateResignCooldown(),merchantId);
        //更改雇佣状态
        quickUpdateAndRemoveCaffeine(employeeId,k->employeeMapper.updateEmployeeResign(employeeId,merchantId));
    }

    //删除职员
    @Override
    public void deleteEmployee(long employeeId){
        //检查格式
        commonParamRulesConfig.commonIdCheck(employeeId);
        //获取商家id
        long merchantId=quickGetMerchantId();
        //检查冷却
        quickCheckCooldown(employeeRedisKeyConfig.getEmployeeDeleteCooldown(),merchantId);
        //删除职员
        quickUpdateAndRemoveCaffeine(employeeId,k->employeeMapper.deleteEmployee(employeeId,merchantId));
        //发送MQ同步
        quickAmountSync(merchantId,-1);
    }

    private long quickGetMerchantId(){
        return TokenIdContext.getAndCheck(commonParamRulesConfig.getMerchantIdStart(),commonParamRulesConfig.getIdCapacity());
    }

    private void quickCheckCooldown(RedisKeyData key,long id){
        RedisUtil.checkCooldown(stringRedisTemplate,key.getRedisKey(id),key.getDuration());
    }

    private void quickDeletePersonImage(String addr){
        if (addr==null||addr.isBlank()) return;
        MQUtil.send(employeeExchangeConfig.getExchangeName(), employeeExchangeConfig.getDeleteFileEmployeeQueue().getRoutingKey()
                , Paths.get(employeeParamsRulesConfig.getPersonImageDest(),addr).toString(),rabbitTemplate);
    }

    private void quickAmountSync(long merchantId,int changeNumber){
        MQUtil.send(employeeExchangeConfig.getExchangeName(),employeeExchangeConfig.getChangeEmployeeAmountQueue().getRoutingKey()
                , new ChangeAmountDTO(merchantId,changeNumber),rabbitTemplate);
    }

    private void quickUpdateAndRemoveCaffeine(long id, Function<Long,Boolean> fn){
        employeeCaffeine.updateAndRemoveCaffeine(id,stringRedisTemplate,employeeRedisKeyConfig.getEmployeeMessageCaffeine().getRedisKey(id)
        ,fn);
    }














}
