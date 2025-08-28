package com.distribuida.service;

import com.distribuida.dao.CarritoItemRepositorio;
import com.distribuida.dao.CarritoRepositorio;
import com.distribuida.dao.ClienteRepositorio;
import com.distribuida.dao.LibroRepositorio;
import com.distribuida.model.Carrito;
import com.distribuida.model.CarritoItem;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class CarritoServiceImpl implements CarritoService{

    private final CarritoRepositorio carritoRepositorio;
    private final CarritoItemRepositorio carritoItemRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final LibroRepositorio libroRepositorio;

    private static final BigDecimal IVA = new BigDecimal("0.15");
    public CarritoServiceImpl(CarritoRepositorio carritoRepositorio
                              ,CarritoItemRepositorio carritoItemRepositorio
                              ,ClienteRepositorio clienteRepositorio
                              ,LibroRepositorio libroRepositorio
                              ){
        this.carritoRepositorio = carritoRepositorio;
        this.carritoItemRepositorio = carritoItemRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.libroRepositorio = libroRepositorio;


    }

    @Transactional
    @Override
    public Carrito getOrCreateByClienteId(int clienteId, String token) {
        var cliente = clienteRepositorio.findById(clienteId)
                .orElseThrow(()-> new IllegalArgumentException("Cliente no encontrado" + clienteId));

        var carritoOpt = carritoRepositorio.findByCliente(cliente);
        if(carritoOpt.isPresent()) return carritoOpt.get();

        var carrito = new Carrito();
        carrito.setCliente(cliente);
        carrito.setToken(token);
        carrito.recomprobacionTotales();
        return carritoRepositorio.save(carrito);
    }

    @Transactional
    @Override
    public Carrito addItem(int clienteId, int libroId, int cantidad) {
        if(cantidad <= 0) throw new IllegalArgumentException("Cantidad debe ser mayor a 0");

        var carrito = getOrCreateByClienteId(clienteId, null);
        var libro = libroRepositorio.findById(libroId)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado: " + libroId));

        var itemOpt = carritoItemRepositorio.findbyCarritoAndLibro(carrito, libro);
        if(itemOpt.isPresent()){
            var item = itemOpt.get();
            item.setCantidad(item.getCantidad() + cantidad);
            item.setPrecioUnitario(BigDecimal.valueOf(libro.getPrecio()));
            item.calcTotal();
            carritoItemRepositorio.save(item);
        }else {

            var item = new CarritoItem();
            item.setCarrito(carrito);
            item.setLibro(libro);
            item.setCantidad(cantidad);
            item.setPrecioUnitario(BigDecimal.valueOf(libro.getPrecio()));
            item.calcTotal();
            carrito.getItem().add(item);
        }
        carrito.recomputarTotales(IVA);
        return carritoRepositorio.save(carrito);
    }

    @Override
    @Transactional
    public Carrito updateItemCantidad(int clienteId, long carritoItemId, int nuevaCantidad) {
        if(nuevaCantidad < 0) throw new IllegalArgumentException("Cantidad no puede ser negativa");

        var carrito = getByClienteId(clienteId);
        var item = carritoItemRepositorio.findById(carritoItemId)
                .orElseThrow(()-> new IllegalArgumentException("Item no encontrado" +carritoItemId));

        if (!Objects.equals(item.getCarrito().getIdCarrito(), carrito.getIdCarrito())){
            throw new IllegalArgumentException("El item no pertenece al carrito del cliente");
        }
        //va restando libros
        if(nuevaCantidad == 0){
            carrito.getItem().remove(item);
            carritoItemRepositorio.delete(item);
        }else {
            item.setCantidad(nuevaCantidad);
            carritoItemRepositorio.save(item);
        }

        carrito.recomputarTotales(IVA);

        return carritoRepositorio.save(carrito);
    }

    @Override
    @Transactional
    public void removeItem(int clienteId, long carritoItemId) {
    updateItemCantidad(clienteId, carritoItemId, 0);
    }

    @Override
    @Transactional
    public void clear(int clienteId) {
        var carrito  = getByClienteId(clienteId);
        carrito.getItem().clear();
        carrito.recomputarTotales(IVA);
        carritoRepositorio.save(carrito);
    }

    @Override
    @Transactional //(readOnly= true)
    public Carrito getByClienteId(int clienteId) {

        var cliente = clienteRepositorio.findById(clienteId)
                .orElseThrow(()-> new IllegalArgumentException("Cliente no encontrado: " + clienteId));

        return carritoRepositorio.findByCliente(cliente)
                .orElseGet(() -> {
                    var c = new Carrito();
                    c.setCliente(cliente);
                    return c;
                });
    }


    @Override
    @Transactional
    public Carrito getOrCreateByToken(String token) {
        if(token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token de carrito requerido");
        }

        return carritoRepositorio.findbyToken(token).orElseGet(()->{
            var c = new Carrito();
            c.setToken(token);
            c.setSubtotal(BigDecimal.ZERO);
            c.setDescuento(BigDecimal.ZERO);
            c.setImpuesto(BigDecimal.ZERO);
            c.setTotal(BigDecimal.ZERO);
            return carritoRepositorio.save(c);
        });
    }

    @Override
    @Transactional
    public Carrito addItem(String token, int libroId, int cantidad) {

        if(cantidad <= 0) throw new IllegalArgumentException("Cantidad debe ser mayor que 0");
        var carrito = getOrCreateByToken(token);
        var libro = libroRepositorio.findById(libroId)
                .orElseThrow(()-> new IllegalArgumentException("Libro no encontrado: " + libroId));

        var itemOpt = carritoItemRepositorio.findbyCarritoAndLibro(carrito, libro);
        if(itemOpt.isPresent()){
            var item = itemOpt.get();
            item.setCantidad( item.getCantidad() + cantidad);
            item.setPrecioUnitario(BigDecimal.valueOf(libro.getPrecio()));
            item.calcTotal();
            carritoItemRepositorio.save(item);
        }else{
            var item = new CarritoItem();
            item.setCarrito(carrito);
            item.setLibro(libro);
            item.setCantidad(cantidad);
            item.setPrecioUnitario(BigDecimal.valueOf(libro.getPrecio()));
            item.calcTotal();
            carrito.getItem().add(item);
        }
        carrito.recomputarTotales(IVA);
        return carritoRepositorio.save(carrito);
    }

    @Override
    @Transactional
    public Carrito updateItemCantidad(String token, long carritoItemId, int nuevaCantidad) {
        var carrito = getOrCreateByToken(token);
        var item = carritoItemRepositorio.findById(carritoItemId)
                .orElseThrow(()-> new IllegalArgumentException("El item no encontrado: " + carritoItemId));

        if (item.getCarrito() == null
                || item.getCarrito().getIdCarrito() != carrito.getIdCarrito()) {
            // Alternativa segura si son Long:
            // if (!Objects.equals(item.getCarrito().getIdCarrito(), carrito.getIdCarrito())) { ... }
            throw new IllegalArgumentException("El item no pertenece al carrito del token");
        }
        if(nuevaCantidad <= 0){
            carrito.getItem().remove(item);
            carritoItemRepositorio.delete(item);
        }else {
            item.setCantidad(nuevaCantidad);
            item.calcTotal();
            carritoItemRepositorio.save(item);
        }
        carrito.recomputarTotales(IVA);

        return carritoRepositorio.save(carrito);
    }

    @Override
    @Transactional
    public void removeItem(String token, long carritoItemId) {
        updateItemCantidad(token, carritoItemId, 0);
    }

    @Override
    @Transactional
    public void clear(String token) {
        var carrito = getOrCreateByToken(token);
        carrito.getItem().clear();
        carrito.setSubtotal(BigDecimal.ZERO);
        carrito.setDescuento(BigDecimal.ZERO);
        carrito.setImpuesto(BigDecimal.ZERO);
        carrito.setTotal(BigDecimal.ZERO);
        carritoRepositorio.save(carrito);

    }

    @Override
    @Transactional //readOnly = tru
    public Carrito getByToken(String token) {
        return carritoRepositorio.findbyToken(token)
                .orElseGet(()-> {
                    var c = new Carrito();
                    c.setToken(token);
                    c.setSubtotal(BigDecimal.ZERO);
                    c.setDescuento(BigDecimal.ZERO);
                    c.setImpuesto(BigDecimal.ZERO);
                    c.setTotal(BigDecimal.ZERO);
                    return c;
                });
    }
}
