package com.demo.v2;

class Pato{
	synchronized void dormir(){ //Obtiene el monitor del objeto Pato
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
		Pato pato = new Pato(); //OBJECTO
		pato.dormir();
		System.out.println("End main");
	}
}
