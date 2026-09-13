package com.seek.food.interaction.Mapper;

import com.seek.food.dto.Common.SyncStateDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CollectMapper {
    public void syncCollect(SyncStateDTO syncStateDTO);
    public List<Long> getCollectList(int start, int need, long accountId,int type);
}
