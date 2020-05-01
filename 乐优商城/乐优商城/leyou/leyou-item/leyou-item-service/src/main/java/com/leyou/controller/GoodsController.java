package com.leyou.controller;


import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.dao.SpuDetailDao;
import com.leyou.domain.Sku;
import com.leyou.domain.Spu;
import com.leyou.domain.SpuDetail;
import com.leyou.service.GoodsService;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "key",required = false) String key
    ){
       PageResult<Spu> pageResult = goodsService.querySpuByPage(page,rows,saleable,key);
       return ResponseEntity.status(HttpStatus.OK).body(pageResult);
    }

    @PostMapping("/goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu){
        goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu){
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailById(@PathVariable("spuId") Long spuId){
        SpuDetail spuDetail=goodsService.querySpuDetailById(spuId);
        return ResponseEntity.status(HttpStatus.OK).body(spuDetail);
    }

    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id")Long spuId){
       List<Sku> skus =  goodsService.querySkuBySpuId(spuId);
       return ResponseEntity.status(HttpStatus.OK).body(skus);
    }

    @GetMapping("/spu/{spuId}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("spuId") Long spuId){
        Spu spu = goodsService.querySpuByID(spuId);
        return ResponseEntity.status(HttpStatus.OK).body(spu);
    }

    @PostMapping("/stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> carts){
        goodsService.decreaseStock(carts);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
