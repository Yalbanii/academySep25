package com.almacen.inventory.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StockBajo {
	
	Long id;
	String nombre;
	Integer stockActual;
	Integer stockMinimo;

}
