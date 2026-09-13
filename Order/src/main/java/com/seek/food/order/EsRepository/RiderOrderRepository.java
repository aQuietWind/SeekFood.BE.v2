package com.seek.food.order.EsRepository;

import com.seek.food.dto.Order.RiderOrderEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiderOrderRepository extends ElasticsearchRepository<RiderOrderEsDTO, Long> {

}