package com.seek.food.employee.Mapper;

import com.seek.food.dto.Employee.EmployeeDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EmployeeMapper {
    public void insertEmployee(String employeeName,long employeeId,long merchantId);
    public List<EmployeeDTO> getSimpleEmployee(int start, int need,long merchantId);
    public List<EmployeeDTO> getSimpleByResign(boolean resign,int start, int need,long merchantId);
    public List<EmployeeDTO> getSimpleByName(String employeeName,int start,int need,long merchantId);
    public List<EmployeeDTO> getSimpleByDep(String depName,int start,int need,long merchantId);
    public EmployeeDTO getDetailEmployee(long employeeId,long merchantId);
    public String getPersonImageAddr(long employeeId,long merchantId);
    public List<String> getAllPersonImageAddr(long merchantId);
    public boolean updateEmployeeMessage(EmployeeDTO employeeDTO);
    public boolean updatePersonImage(String addr, String oldAddr, long employeeId, long merchantId);
    public boolean updateEmployeeResign(long employeeId,long merchantId);
    public boolean deleteEmployee(long employeeId,long merchantId);
    public void deleteAllEmployee(long merchantId);
}
