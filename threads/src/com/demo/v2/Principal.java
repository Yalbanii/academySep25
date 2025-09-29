package com.demo.v2;

//NOTAS:
//- Solo vamos a trabajar con el hilo main

class Pato{
	synchronized void esperar(){ //Obtiene el monitor del objeto Pato
		System.out.println("Pato espera 5 segundos");
		try {
			wait(5000); //Libera 5 segundos el monitor
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

public class Principal {
	public static void main(String[] args) {
		System.out.println("Begin main V2");
		Pato pato = new Pato(); //OBJECTO DEL QUE OBTENEMOS EL MONITOR
		pato.esperar();
		System.out.println("End main");
	}
}
