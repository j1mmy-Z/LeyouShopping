package com.leyou.search.service;

import com.leyou.common.vo.PageResult;
import com.leyou.domain.Spu;
import com.leyou.search.domain.Goods;
import com.leyou.search.domain.SearchRequest;

public interface SearchService {
    Goods buildGoods(Spu spu);

    PageResult<Goods> search(SearchRequest searchRequest);

    void creatOrUpdateIndex(Long spuId);

    void deleteIndex(Long spuId);
}
