package com.seek.food.config.NacosConfig.Gateway;

import com.seek.food.config.Enum.ConfigKeyEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(ConfigKeyEnum.Gateway_Redis_Key_Config)
public class GatewayRedisKeyConfig {
    private String idCheck;
    private String ipCheck;
    private String idBlack;
    private String ipBlack;

    public GatewayRedisKeyConfig() {
    }

    public GatewayRedisKeyConfig(String idCheck, String ipCheck, String idBlack, String ipBlack) {
        this.idCheck = idCheck;
        this.ipCheck = ipCheck;
        this.idBlack = idBlack;
        this.ipBlack = ipBlack;
    }

    /**
     * 获取
     * @return idCheck
     */
    public String getIdCheck() {
        return idCheck;
    }

    /**
     * 设置
     * @param idCheck
     */
    public void setIdCheck(String idCheck) {
        this.idCheck = idCheck;
    }

    /**
     * 获取
     * @return ipCheck
     */
    public String getIpCheck() {
        return ipCheck;
    }

    /**
     * 设置
     * @param ipCheck
     */
    public void setIpCheck(String ipCheck) {
        this.ipCheck = ipCheck;
    }

    /**
     * 获取
     * @return idBlack
     */
    public String getIdBlack() {
        return idBlack;
    }

    /**
     * 设置
     * @param idBlack
     */
    public void setIdBlack(String idBlack) {
        this.idBlack = idBlack;
    }

    /**
     * 获取
     * @return ipBlack
     */
    public String getIpBlack() {
        return ipBlack;
    }

    /**
     * 设置
     * @param ipBlack
     */
    public void setIpBlack(String ipBlack) {
        this.ipBlack = ipBlack;
    }

    public String toString() {
        return "RedisKeyConfig{idCheck = " + idCheck + ", ipCheck = " + ipCheck + ", idBlack = " + idBlack + ", ipBlack = " + ipBlack + "}";
    }
}
