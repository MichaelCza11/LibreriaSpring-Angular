package com.distribuida.service;

import com.distribuida.dao.CarritoRepositorio;
import com.distribuida.dao.FacturaDetalleRepositorio;
import com.distribuida.dao.FacturaRepositorio;
import com.distribuida.dao.LibroRepositorio;
import com.distribuida.model.Factura;
import com.distribuida.service.util.CheckoutMapper;
import jakarta.transaction.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GuestCheckoutServiceImpl implements GuestCheckoutService{

    private final CarritoRepositorio carritoRepositorio;
    private final FacturaRepositorio facturaRepositorio;
    private final FacturaDetalleRepositorio facturaDetalleRepositorio;
    private final LibroRepositorio libroRepositorio;

    private static final double IVA = 0.15d;

    public GuestCheckoutServiceImpl(CarritoRepositorio carritoRepositorio,
                                    FacturaRepositorio facturaRepositorio,
                                    FacturaDetalleRepositorio facturaDetalleRepositorio,
                                    LibroRepositorio libroRepositorio
    ){
        this.carritoRepositorio = carritoRepositorio;
        this.facturaRepositorio = facturaRepositorio;
        this.facturaDetalleRepositorio = facturaDetalleRepositorio;
        this.libroRepositorio = libroRepositorio;
    }

    @Override
    @Transactional
    public Factura checkoutByToken(String token) {
        var carrito = carritoRepositorio.findbyToken(token)
                .orElseThrow(() -> new IllegalStateException("No existe carrito para el token"));

        if(carrito.getItem() == null || carrito.getItem().isEmpty()){
            throw new IllegalStateException("El carrito esta vacio");
        }

        for(var item: carrito.getItem()){
            var libro = item.getLibro();
            if(libro.getNumEjemplares() < item.getCantidad()){
                throw new IllegalStateException("Stock insuficiente para: " + libro.getTitulo());
            }
        }

        for(var item: carrito.getItem()){
            var libro = item.getLibro();
            libro.setNumEjemplares(libro.getNumEjemplares() - item.getCantidad());
            libroRepositorio.save(libro);
        }

        String numFactura = "F-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .format(LocalDateTime.now());

        var factura = CheckoutMapper.construirFacturaDesdeCarrito(carrito, numFactura, IVA);

        factura = facturaRepositorio.save(factura);

        for(var item: carrito.getItem()){
            var det = CheckoutMapper.construirDetalle(factura, item);
            facturaDetalleRepositorio.save(det);

        }

        carrito.getItem().clear();
        carritoRepositorio.save(carrito);

        return factura;
    }
}

