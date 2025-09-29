package com.demo.v6;

class Pato implements Runnable {

	Object obj;

	public Pato(Object obj) {
		this.obj = obj;
	}

	void esperar() { 
		synchronized (obj) { // Obtiene el monitor del objeto Object
			System.out.println("Pato espera 5 segundos. Thread: "+Thread.currentThread().getName());
			try {
				obj.wait(5_000); // Libera 5 segundos el monitor
				System.out.println("Termino Thread: "+Thread.currentThread().getName());
			} catch (InterruptedException e) {
				e.printStackTrace();
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
		System.out.println("Begin main V6");
		Object o = new Object(); // OBJECTO
				
		Runnable run1 = new Pato(o); 
		Runnable run2 = new Pato(o); 
		
		Thread t1 = new Thread(run1,"Hilo1");
		Thread t2 = new Thread(run2,"Hilo2");
		Thread t3 = new Thread(run2,"Hilo3");
		
		t1.start();
		t2.start();
		t3.start();
		
		Pato pato = new Pato(o); //Thread main
		pato.esperar();

		t1.join();
		t2.join();
		t3.join();
		
		System.out.println("End main");
	}
}