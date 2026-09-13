package com.seek.food.interaction.Service;

public interface LikeService {
    public Boolean likeMerchant(long merchantId,boolean value);
    public Boolean likeFirstComment(long commentId,boolean value);
    public Boolean likeSecondComment(long commentId,boolean value);
    public Boolean getLikeMerchant(long merchantId);
    public Boolean getLikeFirstComment(long commentId);
    public Boolean getLikeSecondComment(long commentId);
}
