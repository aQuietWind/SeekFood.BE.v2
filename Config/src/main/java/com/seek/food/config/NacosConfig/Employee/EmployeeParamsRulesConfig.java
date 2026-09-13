package com.seek.food.config.NacosConfig.Employee;

import com.seek.food.config.Enum.ConfigKeyEnum;
import com.seek.food.dto.Employee.EmployeeDTO;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Slf4j
@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Employee_Params_Rules_Config)
@Data
public class EmployeeParamsRulesConfig {
    private int employeePositionMax;
    private int employeeDepNameMax;
    private int employeeAddrMax;
    private int employeeDescriptionMax;
    private String personImageDest;

    public void positionNameCheck(String employeePositionName) {
        if (employeePositionName != null&& employeePositionName.length()>employeePositionMax) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void depNameCheck(String employeeDepName) {
        if (employeeDepName != null&& employeeDepName.length()>employeeDepNameMax) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void addrCheck(String employeeAddr) {
        if (employeeAddr!=null&&employeeAddr.length()>employeeAddrMax) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void descriptionCheck(String employeeDescription) {
        if (employeeDescription!=null&&employeeDescription.length()>employeeDescriptionMax) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }

    public void employeeCheck(EmployeeDTO employee) {
        addrCheck(employee.getEmployeeAddr());
        descriptionCheck(employee.getEmployeeDescription());
        positionNameCheck(employee.getEmployeePositionName());
        depNameCheck(employee.getEmployeeDepName());
    }
}
