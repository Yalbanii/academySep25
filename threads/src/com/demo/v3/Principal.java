package com.demo.v3;

//NOTAS:
//- Solo vamos a trabajar con el hilo main

class Pato {

	Object obj;

	public Pato(Object obj) {
		this.obj = obj;
	}

	void esperar() { 
		synchronized (obj) { // Obtiene el monitor del objeto Object
			System.out.println("Pato espera 5 segundos");
			try {
				obj.wait(5000); // Libera 5 segundos el monitor
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

public class Principal {
	public static void main(String[] args) {
		System.out.println("Begin main V3");
		Object o = new Object(); //OBJECTO DEL QUE OBTENEMOS EL MONITOR

		Pato pato = new Pato(o); 
		pato.esperar();

		System.out.println("End main");
	}
}
