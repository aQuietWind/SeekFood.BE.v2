package com.seek.food.interaction.Service;

import java.util.List;

public interface CollectService {
    public Boolean collectMerchant(long merchantId,boolean value);
    public Boolean getCollectMerchant(long merchantId);
    public List<Long> getCollectMerchantList(int start, int need);
}
