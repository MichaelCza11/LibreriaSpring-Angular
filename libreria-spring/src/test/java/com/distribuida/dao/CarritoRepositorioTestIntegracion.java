package com.distribuida.dao;

import com.distribuida.controller.CarritoGuestController;
import com.distribuida.model.Carrito;
import com.distribuida.model.CarritoItem;
import com.distribuida.service.CarritoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CarritoGuestController.class)
public class CarritoRepositorioTestIntegracion {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CarritoService carritoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testFindAll() throws Exception{
        CarritoItem carritoItem = new CarritoItem();

    }
}
