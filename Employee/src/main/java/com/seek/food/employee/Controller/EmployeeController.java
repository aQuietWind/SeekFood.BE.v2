package com.seek.food.employee.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Employee.EmployeeDTO;
import com.seek.food.employee.Enum.RequestPathEnum;
import com.seek.food.employee.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(RequestPathEnum.Employee)
public class EmployeeController {
    private final EmployeeService employeeService;
    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    //新增职员
    @PostMapping(RequestPathEnum.Employee_Insert)
    public Result<Void> insertEmployee(String employeeName) {
        employeeService.insertEmployee(employeeName);
        return Result.success();
    }

    //分页获取所有职员的简单信息
    @GetMapping(RequestPathEnum.Employee_All_Simple_Message)
    public Result<List<EmployeeDTO>> getAllEmployees(int start,int need) {
        return Result.success(employeeService.getSimpleEmployee(start,need));
    }

    //根据职员姓名获取职员简易信息
    @GetMapping(RequestPathEnum.Employee_Simple_Message_By_Employee_Name)
    public Result<List<EmployeeDTO>> getEmployeesByName(String employeeName,int start,int need){
        return Result.success(employeeService.searchSimpleByName(employeeName,start,need));
    }

    //根据任职情况获取职员简易信息
    @GetMapping(RequestPathEnum.Employee_Simple_Message_By_Resign)
    public Result<List<EmployeeDTO>> getEmployeesByResign(boolean resign,int start,int need){
        return Result.success(employeeService.getSimpleByResign(resign,start,need));
    }

    //根据部门名称获取职员简易信息
    @GetMapping(RequestPathEnum.Employee_Simple_Message_By_Dep)
    public Result<List<EmployeeDTO>> getEmployeesByDep(String depName,int start,int need){
        return Result.success(employeeService.searchSimpleByDep(depName,start,need));
    }

    //获取职员详细信息
    @GetMapping(RequestPathEnum.Employee_Detail_Message)
    public Result<EmployeeDTO> getEmployeesDetail(long employeeId){
        return Result.success(employeeService.getDetailEmployee(employeeId));
    }

    //更新职员信息
    @PutMapping(RequestPathEnum.Employee_Update_Message)
    public Result<Void> updateEmployee(@RequestBody EmployeeDTO employee){
        employeeService.updateEmployeeMessage(employee);
        return Result.success();
    }

    //更改职员照片
    @PutMapping(RequestPathEnum.Employee_Update_Person_Image)
    public Result<Void> updateEmployeesImage(@RequestBody MultipartFile file,long employeeId){
        employeeService.updatePersonImage(file,employeeId);
        return Result.success();
    }

    //更改职员任职状态
    @PutMapping(RequestPathEnum.Employee_Update_Resign)
    public Result<Void> updateEmployeesResign(long employeeId){
        employeeService.updateEmployeeResign(employeeId);
        return Result.success();
    }

    //删除职员
    @DeleteMapping(RequestPathEnum.Employee_Delete)
    public Result<Void> deleteEmployee(long employeeId){
        employeeService.deleteEmployee(employeeId);
        return Result.success();
    }




















}
