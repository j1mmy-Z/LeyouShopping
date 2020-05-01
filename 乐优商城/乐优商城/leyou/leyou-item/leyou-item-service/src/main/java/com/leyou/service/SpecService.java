package com.leyou.service;

import com.leyou.domain.SpecGroup;
import com.leyou.domain.SpecParam;

import java.util.List;

public interface SpecService {
    List<SpecGroup> querySpecGroupByCid(Long cid);

    List<SpecParam> querySpecParam(Long gid,Long cid,Boolean searching);

    List<SpecGroup> queryListByCid(Long cid);
}
