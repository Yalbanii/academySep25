package com.demo.v6;

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
				//Thread.sleep(5000);
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
		System.out.println("Begin main V6");
		Object o = new Object(); // OBJECTO
				
		Runnable run1 = new Pato(o); 
		Runnable run2 = new Pato(o); 
		
		Thread t1 = new Thread(run1,"Hilo1");
		Thread t2 = new Thread(run2,"Hilo2");
		
		t1.start();
		t2.start();
		
		Pato pato = new Pato(o);
		pato.dormir();

//		t1.join();
//		t2.join();
		
		System.out.println("End main");
	}
}