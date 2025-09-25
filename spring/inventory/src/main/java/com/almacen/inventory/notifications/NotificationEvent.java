package com.almacen.inventory.notifications;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import com.almacen.inventory.shared.StockBajo;

@Component
public class NotificationEvent {
	
	@ApplicationModuleListener
	void avisarStockBajo(StockBajo stockBajo){
		System.out.println("⚠️ ALERTA: Stock bajo detectado!");
        System.out.println("   Producto: " + stockBajo.getNombre());
        System.out.println("   Stock actual: " + stockBajo.getStockActual());
        System.out.println("   Stock mínimo: " + stockBajo.getStockMinimo());
	}

}
