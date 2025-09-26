package com.demo.v7;

class Pato {

	Object obj;

	public Pato(Object obj) {
		this.obj = obj;
	}

	void dormir() { 
		synchronized (obj) { // Obtiene el monitor del objeto Object
			System.out.println("Pato espera 5 segundos. Thread: "+Thread.currentThread().getName());
			try {
				obj.wait(5000); // Libera 5 segundos el monitor
				System.out.println("Empieza a dormir Thread: "+Thread.currentThread().getName());
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}

public class Principal {
	public static void main(String[] args) throws InterruptedException {
		System.out.println("Begin main V7");
		Object o = new Object(); // OBJECTO
					
		Thread t1 = new Thread(() -> new Pato(o).dormir(),"Hilo1");
		Thread t2 = new Thread(() -> new Pato(o).dormir(),"Hilo2");
		
		t1.start();
		t2.start();
		
		Pato pato = new Pato(o);
		pato.dormir();
		
		System.out.println("End main");
	}
}