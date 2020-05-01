package com.leyou.service.Impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.JimmyException;
import com.leyou.dao.SpecGroupDao;
import com.leyou.dao.SpecParamDao;
import com.leyou.domain.SpecGroup;
import com.leyou.domain.SpecParam;
import com.leyou.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import sun.security.krb5.internal.PAData;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecServiceImpl implements SpecService {

    @Autowired
    private SpecGroupDao specGroupDao;

    @Autowired
    private SpecParamDao specParamDao;

    @Override
    public List<SpecGroup> querySpecGroupByCid(Long cid) {
        SpecGroup specGroup=new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> groups = specGroupDao.select(specGroup);
        if (CollectionUtils.isEmpty(groups)){
            throw new JimmyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return groups;
    }

    @Override
    public List<SpecParam> querySpecParam(Long gid,Long cid,Boolean searching) {
       // SpecParam specParam=new SpecParam();
        Example example=new Example(SpecParam.class);
        Example.Criteria criteria = example.createCriteria();
        if (gid!=null){
            criteria.andEqualTo("group_id",gid);
        }
        if (cid!=null) {
            criteria.andEqualTo("cid",cid);
        }
        if (searching!=null){
            criteria.andEqualTo("searching",searching);
        }
        //List<SpecParam> params = specParamDao.select(specParam);
        List<SpecParam> params = specParamDao.selectByExample(example);
        if (CollectionUtils.isEmpty(params)){
            throw new JimmyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return params;
    }

    @Override
    public List<SpecGroup> queryListByCid(Long cid) {
        List<SpecGroup> specGroups = querySpecGroupByCid(cid);
        List<SpecParam> specParams = querySpecParam(null, cid, null);
        Map<Long,List<SpecParam>> map=new HashMap<>();
        for (SpecParam specParam : specParams) {
            if (!map.containsKey(specParam.getGroupId())){
                map.put(specParam.getGroupId(),new ArrayList<>());
            }
            map.get(specParam.getGroupId()).add(specParam);
        }
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;
    }
}
