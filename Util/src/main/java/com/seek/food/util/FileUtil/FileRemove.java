package com.seek.food.util.FileUtil;

import com.seek.food.util.Exception.BizException;
import com.seek.food.util.Exception.ErrorCodeEnum;
import com.seek.food.util.Function.RunFunction;
import com.seek.food.util.Function.RunWithParam;
import com.seek.food.util.Function.RunWithTwoParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class FileRemove {
    //传输拼接路径下来
    public static void removeFile(String dest,String addr) {
        if (addr==null||addr.isBlank()) return;
        quickRemoveFile(Paths.get(dest,addr));
    }
    //直接完整路径
    public static void removeFileByPath(String path) {
        quickRemoveFile(Paths.get(path));
    }

    //拼接路径
    public static String resolvePath(String dest,String addr) {
        return Paths.get(dest,addr).toString();
    }

    public static void quickRemoveFile(Path path) {
        //检查路径是否存在
        if (!Files.exists(path))return;
        //删除文件
        if(!path.toFile().delete()){
            throw new BizException(ErrorCodeEnum.FILE_DELETE_ERROR);
        }
    }

    //不报错删除
    public static boolean removeFileOutError(String dest,String addr) {
        if (addr==null||addr.isBlank()) return true;
        Path path = Paths.get(dest,addr);
        try {
            quickRemoveFile(path);
            return true;
        }
        catch (Exception e){
            log.error("删除文件:{} ,出现异常:",path,e);
            return false;
        }
    }

    public static void removeFileListOutError(String dest, List<String> addrs, RunWithTwoParams<String,Exception> function) {
        if (addrs==null||addrs.isEmpty())return;
        for (String addr:addrs){
            if (addr==null||addr.isBlank()) continue;
            try {
                removeFile(dest,addr);
            }catch (Exception e){
                function.function(Paths.get(dest,addr).toString(),e);
            }
        }
    }

    public static void removeFileArrayOutError(String dest, String[] addrs,RunWithTwoParams<String,Exception> function) {
        if (addrs == null||addrs.length==0)return;
        for (String addr:addrs){
            if (addr==null||addr.isBlank()) continue;
            try {
                removeFile(dest,addr);
            }catch (Exception e){
                function.function(Paths.get(dest,addr).toString(),e);
            }
        }
    }
}
