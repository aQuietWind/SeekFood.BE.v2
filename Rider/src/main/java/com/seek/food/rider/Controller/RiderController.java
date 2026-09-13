package com.seek.food.rider.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Rider.RiderDTO;
import com.seek.food.rider.Enum.RequestPathEnum;
import com.seek.food.rider.Service.RiderService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(RequestPathEnum.Rider)
public class RiderController {


    private final RiderService riderService;

    public RiderController(RiderService riderService) {
        this.riderService = riderService;
    }

    //获取详细信息
    @GetMapping(RequestPathEnum.Rider_Get_Detail)
    public Result<RiderDTO> getDetail(long riderId){
        return Result.success(riderService.getDetail(riderId));
    }

    //获取自身详细信息
    @GetMapping(RequestPathEnum.Rider_Get_Self)
    public Result<RiderDTO> getSelf(){
        return Result.success(riderService.getSelf());
    }

    //更新人物照片
    @PutMapping(RequestPathEnum.Rider_Update_Person_Image)
    public Result<Void> updatePersonImage(MultipartFile file){
        riderService.updatePersonImage(file);
        return Result.success();
    }

    //获取更新密码的验证码
    @GetMapping(RequestPathEnum.Rider_Get_Update_Password_Opt)
    public Result<String> getUpdatePasswordOpt(){
        return Result.success(riderService.getUpdatePasswordOpt());
    }

    //更新密码
    @PutMapping(RequestPathEnum.Rider_Update_Password)
    public Result<Void> updatePassword(String password, String opt){
        riderService.updatePassword(password, opt);
        return Result.success();
    }

    //获取注销自身的验证码
    @GetMapping(RequestPathEnum.Rider_Get_Delete_Opt)
    public Result<String> getDeleteOpt(){
        return Result.success(riderService.getDeleteOpt());
    }

    //注销自身
    @DeleteMapping(RequestPathEnum.Rider_Delete)
    public Result<Void> delete(String opt){
        riderService.delete(opt);
        return Result.success();
    }










}
