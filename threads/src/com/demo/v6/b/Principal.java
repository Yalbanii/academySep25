package com.demo.v6.b;

class Pato implements Runnable {

	synchronized void dormir() {
		System.out.println("Pato duerme 5 segundos. Thread: " + Thread.currentThread().getName());
		try {
			Thread.sleep(5_000); // Duerme 5 segundos y bloquea
			System.out.println("Termino Thread: " + Thread.currentThread().getName());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		dormir();
	}
}

public class Principal {
	public static void main(String[] args) throws InterruptedException {
		System.out.println("Begin main V6a");

		Runnable run = new Pato();

		Thread t1 = new Thread(run, "Hilo1");
		Thread t2 = new Thread(run, "Hilo2");
		Thread t3 = new Thread(run, "Hilo3");

		t1.start();
		t2.start();
		t3.start();

		Pato pato = new Pato(); // Thread main
		pato.dormir();

		System.out.println("End main");
	}
}