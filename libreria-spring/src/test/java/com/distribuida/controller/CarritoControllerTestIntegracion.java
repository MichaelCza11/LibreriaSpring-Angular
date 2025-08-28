package com.distribuida.controller;

import com.distribuida.model.Carrito;
import com.distribuida.service.CarritoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(CarritoGuestController.class)
public class CarritoControllerTestIntegracion {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CarritoService carritoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testFindAll() throws Exception{
        Carrito carrito = new Carrito();
        Mockito.when(carritoService.findAll()).thenReturn(List.of(carrito));
    }

}
