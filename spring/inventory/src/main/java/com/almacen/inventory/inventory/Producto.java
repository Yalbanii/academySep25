package com.almacen.inventory.inventory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "productos")
public class Producto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String descripcion;
    
    @Column(nullable = false)
    private String sku;
    
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;
    
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;
    
    @Column(nullable = false)
    private Double precio;
    
    public Producto(String nombre,String descripcion, String sku, int stockActual, int stockMinimo,double precio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.sku = sku;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.precio = precio;
    }
    
    public void reducirStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        if (stockActual < cantidad) {
            throw new IllegalStateException("Stock insuficiente. Disponible: " + stockActual + ", Solicitado: " + cantidad);
        }
        this.stockActual -= cantidad;
    }
    
    public void aumentarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        this.stockActual += cantidad;
    }
    
    public boolean necesitaReposicion() {
        return stockActual <= stockMinimo;
    }
    
    public boolean tieneStockDisponible(int cantidad) {
        return stockActual >= cantidad;
    }
}