package com.seek.food.employee.Service;

import com.seek.food.dto.Employee.EmployeeDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeService {
    public void insertEmployee(String employeeName);
    public List<EmployeeDTO> getSimpleEmployee(int start,int need);
    public List<EmployeeDTO> getSimpleByResign(boolean resign,int start,int need);
    public List<EmployeeDTO> searchSimpleByName(String employeeName,int start,int need);
    public List<EmployeeDTO> searchSimpleByDep(String depName,int start,int need);
    public EmployeeDTO getDetailEmployee(long employeeId);
    public void updateEmployeeMessage(EmployeeDTO employeeDTO);
    public void updatePersonImage(MultipartFile file,long employeeId);
    public void updateEmployeeResign(long employeeId);
    public void deleteEmployee(long employeeId);
}
