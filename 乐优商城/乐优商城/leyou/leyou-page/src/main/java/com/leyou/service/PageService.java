package com.leyou.service;

import java.util.Map;

public interface PageService {
    Map<String, Object> loadModel(Long spuId);

    void createHtml(Long spuId);

    void deleteHtml(Long spuId);
}
