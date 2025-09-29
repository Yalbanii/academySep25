package com.demo.v8;

class Pato {

	Object obj;

	public Pato(Object obj) {
		this.obj = obj;
	}

	void esperar() { 
		synchronized (obj) { // Obtiene el monitor del objeto Object
			System.out.println("Pato espera 10 segundos. Thread: "+Thread.currentThread().getName());
			try {
				obj.wait(90_000); // Libera 90 segundos el monitor
				System.out.println("Termina wait Thread: "+Thread.currentThread().getName());
			} catch (InterruptedException e) {
				System.out.println("***INTERRUMPIR***");
				e.printStackTrace();
			}
		}
	}

}

public class Principal {
	public static void main(String[] args) throws InterruptedException {
		System.out.println("Begin main V8");
		Object obj = new Object(); // OBJECTO del que usamos el monitor
					
		Thread t1 = new Thread(() -> new Pato(obj).esperar(),"Hilo1");
		t1.start();

		Thread.sleep(5_000); //Duerme el Thread main
		
//		t1.interrupt(); //Interrumpe y lanza una Exception
		synchronized (obj) { 
			obj.notify(); //Avisar al hilo que tenga el monitor obj que deje de esperar (No es una Exception)
		}
		
		System.out.println("End main");
	}
}