package com.pulkit077.pricetrackerserver.controller;

import com.pulkit077.pricetrackerserver.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/track")
@AllArgsConstructor
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public void getResponseFromBot(@RequestParam("From") String From, @RequestParam("To") String To, @RequestParam("Body") String Body) {
        productService.trackAndSendReply(From, To, Body);
    }
}
