package com.distribuida.controller;

import com.distribuida.model.Factura;
import com.distribuida.service.GuestCheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guest/checkout")
public class GuestCheoutController {

    private final GuestCheckoutService service;

    public GuestCheoutController (GuestCheckoutService service){
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<Factura> checkout(@RequestParam String token){
        return ResponseEntity.ok(service.checkoutByToken(token));
    }


}
