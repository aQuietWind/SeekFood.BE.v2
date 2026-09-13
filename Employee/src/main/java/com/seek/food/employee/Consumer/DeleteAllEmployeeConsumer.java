package com.seek.food.employee.Consumer;

import com.seek.food.config.Data.RedisStreamData;
import com.seek.food.config.Enum.MQNameKeyEnum;
import com.seek.food.config.NacosConfig.Employee.EmployeeParamsRulesConfig;
import com.seek.food.config.NacosConfig.Employee.EmployeeRedisStreamConfig;
import com.seek.food.employee.Mapper.EmployeeMapper;
import com.seek.food.employee.Service.EmployeeService;
import com.seek.food.util.FileUtil.FileRemove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class DeleteAllEmployeeConsumer {
    private final StringRedisTemplate stringRedisTemplate;
    private final EmployeeMapper employeeMapper;
    private final EmployeeParamsRulesConfig employeeParamsRulesConfig;
    private final RedisStreamData oldFileStream;
    @Autowired
    public DeleteAllEmployeeConsumer(StringRedisTemplate stringRedisTemplate, EmployeeMapper employeeMapper
    , EmployeeParamsRulesConfig employeeParamsRulesConfig, EmployeeRedisStreamConfig employeeRedisStreamConfig) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.employeeMapper = employeeMapper;
        this.employeeParamsRulesConfig = employeeParamsRulesConfig;
        this.oldFileStream = employeeRedisStreamConfig.getOldFileStream();
    }


    @RabbitListener(queues = MQNameKeyEnum.Merchant_Exchange_Delete_All_Employee_Queue)
    public void deleteAllEmployeeQueue(long merchantId){
        //先获取所有职员的头像地址
        List<String> personImageAddrs=employeeMapper.getAllPersonImageAddr(merchantId);
        //删除所有职员
        employeeMapper.deleteAllEmployee(merchantId);
        //删除头像,并且附上回调函数
        FileRemove.removeFileListOutError(employeeParamsRulesConfig.getPersonImageDest(),personImageAddrs
        ,(path,e)->{
            //不再进行重试，而是留给spring后台线程进行定时处理
            log.error("path:{},在删除时发生错误",path,e);
            stringRedisTemplate.opsForStream().add(oldFileStream.getName(), Map.of(oldFileStream.getKeyName(),path));
        });
    }















}
