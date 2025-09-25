package com.almacen.inventory.inventory;

import com.almacen.inventory.shared.StockBajo;
//import com.ejemplo.shared.StockRepuesto;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioService {
    
    private final ProductoRepository productoRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public InventarioService(ProductoRepository productoRepository, ApplicationEventPublisher eventPublisher) {
        this.productoRepository = productoRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public Producto crearProducto(String nombre, String descripcion, String sku, int stockActual, int stockMinimo, double precio) {
        Producto producto = new Producto(nombre, descripcion, sku, stockActual, stockMinimo, precio);
        return productoRepository.save(producto);
    }
    
    @Transactional
    public void reducirStock(Long productoId, int cantidad) {
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
        
        producto.reducirStock(cantidad);
        productoRepository.save(producto); //update
        
        // Verificar si necesita reposición después de la reducción
        if (producto.necesitaReposicion()) {
        	StockBajo sb = new StockBajo(producto.getId(), producto.getNombre(), 
                    producto.getStockActual(), producto.getStockMinimo());
            eventPublisher.publishEvent(sb);
        }
    }
    
    @Transactional
    public void aumentarStock(Long productoId, int cantidad) {
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
        
        producto.aumentarStock(cantidad);
        productoRepository.save(producto);
        
        //eventPublisher.publishEvent(new StockRepuesto(producto.getId(), producto.getNombre(), cantidad));
    }
    
    public Producto obtenerProducto(Long productoId) {
        return productoRepository.findById(productoId)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
    }
    
    public List<Producto> obtenerProductosConStockBajo() {
        return productoRepository.findAll().stream()
            .filter(Producto::necesitaReposicion)
            .toList();
    }
    
    public boolean verificarDisponibilidad(Long productoId, int cantidad) {
        return productoRepository.findById(productoId)
            .map(producto -> producto.tieneStockDisponible(cantidad))
            .orElse(false);
    }
}