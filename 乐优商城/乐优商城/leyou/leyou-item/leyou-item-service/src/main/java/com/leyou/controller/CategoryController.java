package com.leyou.controller;

import com.leyou.domain.Category;
import com.leyou.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public ResponseEntity<List<Category>> queryCategoryListByPid(@RequestParam("pid") Long pid){
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.queryCategoryListByPid(pid));
    }

    @GetMapping("/list/ids")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.queryByIds(ids));
    }

    @GetMapping("/all/level/{cid3}")
    public ResponseEntity<List<Category>> queryAllByCid3(@PathVariable("cid3") Long cid3){
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.queryAllByCid3(cid3));
    }
}
