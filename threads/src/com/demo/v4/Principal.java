package com.demo.v4;

class Pato implements Runnable {

	Object obj;

	public Pato(Object obj) {
		this.obj = obj;
	}

	void dormir() { 
		synchronized (obj) { // Obtiene el monitor del objeto Object
			System.out.println("Pato espera 5 segundos. Thread: "+Thread.currentThread().getName());
			try {
				obj.wait(5000); // Libera 5 segundos el monitor
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		dormir();
	}
}

public class Principal {
	public static void main(String[] args) throws InterruptedException {
		System.out.println("Begin main V4");
		Object o = new Object(); // OBJECTO
		Runnable run = new Pato(o); 
				
		new Thread(run,"Hilo1").start();
		
		((Pato)run).dormir();
		
		System.out.println("End main");
	}
}