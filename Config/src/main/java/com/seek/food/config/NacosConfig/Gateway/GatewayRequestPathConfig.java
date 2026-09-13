package com.seek.food.config.NacosConfig.Gateway;


import com.seek.food.config.Enum.ConfigKeyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.HashSet;

@RefreshScope
@ConfigurationProperties(prefix = ConfigKeyEnum.Gateway_Request_Path_Config)
@Data
public class GatewayRequestPathConfig {
    private HashSet<String> allowPath;
    private HashSet<String> rejectPath;

    public boolean checkAllowPath(String path){
        return allowPath.contains(path);
    }
    public boolean checkRejectPath(String path){
        return rejectPath.contains(path);
    }

}






