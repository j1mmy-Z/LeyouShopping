package com.leyou.client;

import com.leyou.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("user-service")
public interface UserClient extends UserApi {
}
