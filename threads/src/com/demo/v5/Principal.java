package com.demo.v5;

class Pato implements Runnable {

	Object obj;

	public Pato(Object obj) {
		this.obj = obj;
	}

	void esperar() { 
		synchronized (obj) { // Obtiene el monitor del objeto Object
			System.out.println("Pato espera 5 segundos. Thread: "+Thread.currentThread().getName());
			try {
				obj.wait(5000); // Libera 5 segundos el monitor
			} catch (InterruptedException e) {
				System.out.println("Se interrumpio el Thread: "+Thread.currentThread().getName());
				//e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		esperar();
	}
}

public class Principal {
	public static void main(String[] args) throws InterruptedException {
		System.out.println("Begin main V5");
		Object o = new Object(); // OBJECTO
		
		Runnable run1 = new Pato(o); 
		Runnable run2 = new Pato(o); 
		
		Thread t1 = new Thread(run1,"Hilo1");
		Thread t2 = new Thread(run2,"Hilo2");
		
		t1.start();
		t2.start();
		
		//Hilo main interrumpe
		t1.interrupt();
		t2.interrupt();
		
		((Pato)run1).esperar();
		//Thread.currentThread().interrupt();
		
		System.out.println("End main");
	}
}