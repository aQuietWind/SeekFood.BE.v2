package com.seek.food.dto.Employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
    private Long employeeId;
    private Long merchantId;
    private String employeeName;
    private String employeeCode;
    private String employeePhoneNumber;
    private String employeeAddr;
    private String employeePersonImageAddr;
    private Integer employeeMonthSalary;
    private Integer employeeYearSalary;
    private String employeeDescription;
    private String employeePositionName;
    private String employeeDepName;
    private LocalDateTime createTime;
    private Boolean resign;
    private Boolean delete;
}
