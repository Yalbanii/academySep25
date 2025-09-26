package com.demo.v1;

class Pato{
	void dormir(){
		System.out.println("Pato espera 5 segundos");
		try {
			wait(5000);
			//Exception in thread "main" 
			//java.lang.IllegalMonitorStateException: current thread is not owner
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

public class Principal {

	public static void main(String[] args) {
		
		System.out.println("Begin main V1");

		Pato pato = new Pato();
		
		pato.dormir();
		
		System.out.println("End main");

	}

}
