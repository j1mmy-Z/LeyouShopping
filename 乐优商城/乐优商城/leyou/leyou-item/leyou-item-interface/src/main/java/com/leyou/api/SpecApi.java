package com.leyou.api;

import com.leyou.domain.SpecGroup;
import com.leyou.domain.SpecParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SpecApi {
    @GetMapping("/spec/params")
    List<SpecParam> querySpecParam(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "searching",required = false) Boolean searching
    );

    @GetMapping("/spec/group")
    List<SpecGroup> queryListByCid(@RequestParam("cid") Long cid);
}
