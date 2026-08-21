package com.voc.service.config.web;

import com.voc.service.common.response.Result;
import com.voc.service.config.PBEStringEncryptor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Tag(name = "获取加密串工具")
@RestController
@RequestMapping("/tools/config")
public class ConfigController {

    @Operation(summary = "PBE加密")
    @PostMapping(value = "/pbe/e")
    Result<?> encrypt(@RequestBody List<String> values) {
        Map<String, String> map = new HashMap<>();
        values.stream().forEach(e -> {
            map.put(e, PBEStringEncryptor.getInstance().encrypt(e));
        });

        return Result.OK(map);
    }
    @Operation(summary = "PBE加密")
    @PostMapping(value = "/pbe/d")
    Result<?> decrypt(@RequestBody List<String> values) {
        Map<String, String> map = new HashMap<>();
        values.stream().forEach(e -> {
            map.put(e, PBEStringEncryptor.getInstance().decrypt(e));
        });

        return Result.OK(map);
    }

    public static void main(String[] args) {
        System.out.println(PBEStringEncryptor.getInstance().encrypt("20X0srHDD@HbxMWQ"));
    }

}
