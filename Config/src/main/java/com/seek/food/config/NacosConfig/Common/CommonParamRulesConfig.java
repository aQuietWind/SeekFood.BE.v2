package com.seek.food.config.NacosConfig.Common;

import com.seek.food.config.Enum.ConfigKeyEnum;
import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.LocalDate;
import java.util.HashSet;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Common_Param_Rules_Key)
@Data
public class CommonParamRulesConfig {
    private int userIdStart;
    private int merchantIdStart;
    private int riderIdStart;
    private int adminIdStart;
    private int personNameMax;
    private int idBitmapAreaNumber;
    private long idCapacity;
    private String phoneNumberRegex;
    private String passwordRegex;
    private String codeRegex;
    private HashSet<Integer> sexValues;
    private HashSet<String> imageType;
    private long imageSize;
    private int needNumberMin;
    private int needNumberMax;
    private long seedNumberMax;
    private int shouldAmountMax;
    //------------------------
    //校验参数
    public void userIdCheck(long userId) {
        if (!((userId/idCapacity)==userIdStart) ) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void merchantIdCheck(long merchantId) {
        if (! ((merchantId/idCapacity)==merchantIdStart) ) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void userOrMerchantIdCheck(long tokenId) {
        if (! ((tokenId/idCapacity)==merchantIdStart)&& !((tokenId/idCapacity)==userIdStart) ) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void riderIdCheck(long riderId) {
        if (! ((riderId/idCapacity)==riderIdStart) ) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void adminIdCheck(long adminId) {
        if (! ((adminId/idCapacity)==adminIdStart) ) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void passwordCheck(String password) {
        if (password == null || !password.matches(passwordRegex)) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void codeCheck(String code) {
        if (code==null||!code.matches(codeRegex)) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void phoneNumberCheck(String phoneNumber) {
        if (phoneNumber==null||!phoneNumber.matches(phoneNumberRegex)) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void sexCheck(Integer sex){
        if (sex!=null&&!sexValues.contains(sex)) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void birthdayCheck(LocalDate birthday){
        if(birthday!=null&&LocalDate.now().isBefore(birthday)) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void personNameCheck(String personName) {
        if(personName==null||personName.length()>personNameMax||personName.isBlank()) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void lonAndLatCheck(Double lon, Double lat) {
        if (lon!=null&&lat==null) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
        if (lon==null&&lat!=null) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
        if (lon==null)return;
        if (lon>180||lon< -180||lat>90||lat< -90) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void needNumberCheck(int needNumber) {
        if (needNumber>needNumberMax||needNumber<needNumberMin) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void seedNumberCheck(long seedNumber) {
        if (seedNumber>seedNumberMax||seedNumber<=0) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public void shouldAmountCheck(int shouldAmount) {
        if (shouldAmount>shouldAmountMax||shouldAmount<=0) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    //使用100倍idCapacity是为了统一适配多种数字开头的id
    public void commonIdCheck(Long id){
        if (id==null||(id-idCapacity)>100*idCapacity||id<idCapacity) throw new BizException(ErrorCodeEnum.PARAM_ERROR);
    }
    public int getIdStart(long id){
        return Math.toIntExact(id / idCapacity);
    }

}
