package com.demo.v0;

//EXPLICAR EN EL CANAL HELLO WORLD JAVA

class Pato{
	void dormir(){
		System.out.println("Pato duerme 5 segundos");
		try {
			Thread.sleep(5000);
			//Thread.currentThread().sleep(5000);
		} catch (InterruptedException e) {
			System.out.println("Interrumpieron al pato de su sueño");
		}
	}
}

public class Principal {

	public static void main(String[] args) {
		
		System.out.println("Begin main V0");

		Pato pato = new Pato();
		
		pato.dormir();
		
		System.out.println("End main");

	}

}
