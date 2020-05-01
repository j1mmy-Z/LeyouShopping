package com.leyou.controller;

import com.leyou.domain.SpecGroup;
import com.leyou.domain.SpecParam;
import com.leyou.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/spec")
public class SpecController {

    @Autowired
    private SpecService specService;

    @GetMapping("/groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroupByCid(@PathVariable("cid") Long cid){
       List<SpecGroup> groups = specService.querySpecGroupByCid(cid);
       return ResponseEntity.status(HttpStatus.OK).body(groups);
    }

    @GetMapping("/params")
    public ResponseEntity<List<SpecParam>> querySpecParam(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "searching",required = false) Boolean searching
    ){
        List<SpecParam> params = specService.querySpecParam(gid,cid,searching);
        return ResponseEntity.status(HttpStatus.OK).body(params);
    }


    @GetMapping("/group")
    public ResponseEntity<List<SpecGroup>> queryListByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.status(HttpStatus.OK).body(specService.queryListByCid(cid));
    }
}
