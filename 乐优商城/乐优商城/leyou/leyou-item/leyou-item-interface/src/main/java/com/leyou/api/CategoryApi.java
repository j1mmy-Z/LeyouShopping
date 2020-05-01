package com.leyou.api;

import com.leyou.domain.Category;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;


public interface CategoryApi {

    @GetMapping("/category/list/ids")
     List<Category> queryCategoryByIds(@RequestParam("ids") List<Long> ids);


    @GetMapping("/category/all/level/{cid3}")
    List<Category> queryAllByCid3(@PathVariable("cid3") Long cid3);
}
