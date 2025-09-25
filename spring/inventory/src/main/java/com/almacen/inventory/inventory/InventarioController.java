package com.almacen.inventory.inventory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    
    private final InventarioService inventarioService;
    
    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }
    
    @PostMapping("/productos")
    public ResponseEntity<Producto> crearProducto(
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam String sku,
            @RequestParam int stockActual,
            @RequestParam int stockMinimo,
            @RequestParam double precio) {
        
        Producto producto = inventarioService.crearProducto(nombre,descripcion, sku, stockActual, stockMinimo, precio);
        return ResponseEntity.ok(producto);
    }
    
    @PostMapping("/productos/{productoId}/reducir-stock")
    public ResponseEntity<Void> reducirStock(
            @PathVariable Long productoId,
            @RequestParam int cantidad) {
        
        inventarioService.reducirStock(productoId, cantidad);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/productos/{productoId}/aumentar-stock")
    public ResponseEntity<Void> aumentarStock(
            @PathVariable Long productoId,
            @RequestParam int cantidad) {
        
        inventarioService.aumentarStock(productoId, cantidad);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/productos/{productoId}")
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Long productoId) {
        Producto producto = inventarioService.obtenerProducto(productoId);
        return ResponseEntity.ok(producto);
    }
    
    @GetMapping("/productos/stock-bajo")
    public ResponseEntity<List<Producto>> obtenerProductosConStockBajo() {
        List<Producto> productos = inventarioService.obtenerProductosConStockBajo();
        return ResponseEntity.ok(productos);
    }
    
    @GetMapping("/productos/{productoId}/disponibilidad")
    public ResponseEntity<Boolean> verificarDisponibilidad(
            @PathVariable Long productoId,
            @RequestParam int cantidad) {
        
        boolean disponible = inventarioService.verificarDisponibilidad(productoId, cantidad);
        return ResponseEntity.ok(disponible);
    }
}