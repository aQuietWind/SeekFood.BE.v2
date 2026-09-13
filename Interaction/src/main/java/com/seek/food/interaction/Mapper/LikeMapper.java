package com.seek.food.interaction.Mapper;

import com.seek.food.dto.Common.SyncStateDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LikeMapper {
    public void syncLike(SyncStateDTO syncStateDTO);
}
