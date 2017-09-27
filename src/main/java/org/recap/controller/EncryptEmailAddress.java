package org.recap.controller;

import org.recap.service.EncryptEmailAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by akulak on 20/9/17.
 */
@RestController
@RequestMapping("/encryptEmailAddress")
public class EncryptEmailAddress {

    @Autowired
    private EncryptEmailAddressService encryptEmailAddressService;

    @GetMapping(value = "/startEncryptEmailAddress")
    public String startEncryptEmailAddress(){
       return encryptEmailAddressService.encryptEmailAddress();

    }

}
