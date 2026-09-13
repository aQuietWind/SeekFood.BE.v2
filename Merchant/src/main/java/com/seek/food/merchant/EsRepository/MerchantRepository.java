package com.seek.food.merchant.EsRepository;

import com.seek.food.dto.Merchant.MerchantEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends ElasticsearchRepository<MerchantEsDTO, Long> {

}