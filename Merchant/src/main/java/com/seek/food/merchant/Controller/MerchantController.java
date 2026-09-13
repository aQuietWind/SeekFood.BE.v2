package com.seek.food.merchant.Controller;

import com.seek.food.dto.Common.Result;
import com.seek.food.dto.Merchant.MerchantDTO;
import com.seek.food.merchant.Enum.RequestPathEnum;
import com.seek.food.merchant.Service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(RequestPathEnum.Merchant)
public class MerchantController {
    private final MerchantService merchantService;
    @Autowired
    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    //查询单个商家详细信息
    @GetMapping(RequestPathEnum.Merchant_Detail)
    public Result<MerchantDTO> getMerchantDetail(long merchantId) {
        return Result.success(merchantService.getMerchantDetail(merchantId));
    }

    //查询自身的详细信息
    @GetMapping(RequestPathEnum.Merchant_Self)
    public Result<MerchantDTO> getMerchantSelf(){
        return Result.success(merchantService.getMerchantSelf());
    }

    //设置店主的信息
    @PutMapping(RequestPathEnum.Merchant_Update_Master)
    public Result<Void> setMerchantMaster(String merchantMasterName, String merchantMasterCode, @RequestBody MultipartFile merchantMasterImage){
        merchantService.setMerchantMaster(merchantMasterName, merchantMasterCode, merchantMasterImage);
        return Result.success();
    }

    //增加商家的营业证明
    @PostMapping(RequestPathEnum.Merchant_Proof)
    public Result<Void> addMerchantProofImage(@RequestBody MultipartFile file){
        merchantService.addMerchantProofImage(file);
        return Result.success();
    }

    //删除商家的营业证明
    @DeleteMapping(RequestPathEnum.Merchant_Proof)
    public Result<Void> removeMerchantProofImage(int index){
        merchantService.removeMerchantProofImage(index);
        return Result.success();
    }

    //替换商家的营业证明
    @PutMapping(RequestPathEnum.Merchant_Proof)
    public Result<Void> replaceMerchantProofImage(@RequestBody MultipartFile file,int index){
        merchantService.replaceMerchantProofImage(file,index);
        return Result.success();
    }

    //增加商家的展示图
    @PostMapping(RequestPathEnum.Merchant_Show)
    public Result<Void> addShowImage(@RequestBody MultipartFile file){
        merchantService.addShowImage(file);
        return Result.success();
    }

    //删除商家的展示图
    @DeleteMapping(RequestPathEnum.Merchant_Show)
    public Result<Void> removeShowImage(int index){
        merchantService.removeShowImage(index);
        return Result.success();
    }

    //替换商家的展示图
    @PutMapping(RequestPathEnum.Merchant_Show)
    public Result<Void> replaceShowImage(@RequestBody MultipartFile file,int index){
        merchantService.replaceShowImage(file,index);
        return Result.success();
    }

    //更换封面图片
    @PutMapping(RequestPathEnum.Merchant_Update_Home_Image)
    public Result<Void> updateHomeImage(@RequestBody MultipartFile file){
        merchantService.updateHomeImage(file);
        return Result.success();
    }

    //更改商家的信息
    @PutMapping(RequestPathEnum.Merchant_Update_Message)
    public Result<Void> updateMessage(@RequestBody MerchantDTO merchant){
        merchantService.updateMerchantMessage(merchant);
        return Result.success();
    }

    //获取更改商家密码所需的验证码
    @GetMapping(RequestPathEnum.Merchant_Update_Password_Opt)
    public Result<String> getUpdatePasswordOpt(){
        return Result.success(merchantService.getUpdatePasswordOpt());
    }

    //更改商家密码
    @PutMapping(RequestPathEnum.Merchant_Update_Password)
    public Result<Void> updatePassword(String newPassword, String opt){
        merchantService.updatePassword(newPassword,opt);
        return Result.success();
    }

    //获取注销所需的验证码
    @GetMapping(RequestPathEnum.Merchant_Delete_Opt)
    public Result<String> deleteMerchant(){
        return Result.success(merchantService.getDeleteMerchantOpt());
    }

    //注销账户
    @DeleteMapping(RequestPathEnum.Merchant_Delete)
    public Result<Void> deleteMerchant(String opt){
        merchantService.deleteMerchant(opt);
        return Result.success();
    }

    //开业或者停业
    @PutMapping(RequestPathEnum.Merchant_Open)
    public Result<Void> openMerchant(){
        merchantService.updateOpen();
        return Result.success();
    }

    //获取与商家的距离
    @GetMapping(RequestPathEnum.Merchant_Get_Distance)
    public Result<Long> getDistance(double lon,double lat,long merchantId){
        return Result.success(merchantService.getDistance(lon,lat,merchantId));
    }


}
