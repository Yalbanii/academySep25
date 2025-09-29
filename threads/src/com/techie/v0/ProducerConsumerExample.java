package com.techie.v0;

import java.util.LinkedList;
import java.util.Queue;

public class ProducerConsumerExample {
	
	int capacity; //Capacidad cola
	
	Queue<Integer> queue = new LinkedList<>();

	public ProducerConsumerExample(int capacity) {
		this.capacity = capacity;
	}
	
	//Solo un hilo tiene acceso al método
	public synchronized void produce(int value) throws InterruptedException {
		
		while (queue.size()==capacity) {
			System.out.println("LA COLA ESTA A SU CAPACIDAD");
			wait(); //Espera hasta que haya espacio en la cola.
		}
		
		queue.add(value); //Agrega entero a la cola.
		
		System.out.println("Pruduced: "+ value);
		
		notify(); //Notifica al Consumer que un elemento ha sido producido.
		
	}
	
	//Solo un hilo tiene acceso al método
	public synchronized int consume() throws InterruptedException {
		
		while (queue.isEmpty()) {
			System.out.println("LA COLA ESTA VACIA");
			wait(); //Espera hasta que haya elementos en la cola que consumir.
		}
		
		int value = queue.poll();
		
		System.out.println("Consumed: "+ value);

		notify();
		
		return value;
	}
	
	public static void main(String[] args) throws InterruptedException {
		System.out.println("***Begin main***");
		ProducerConsumerExample pc = new ProducerConsumerExample(5);
		
		Thread producerThread = new Thread( () -> {
				try {
					for (int i=0; i<10; i++) {
						pc.produce(i); //Producir del 0 al 9
						Thread.sleep(100);
					}
				}catch(InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});
		
		Thread consumerThread = new Thread( () -> {
			try {
				for (int i=0; i<10; i++) {
					pc.consume(); //Consumir del 0 al 9
					Thread.sleep(200); //Consumer tarda el doble del tiempo que el Producer
				}
			}catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		
		consumerThread.start();
		producerThread.start();
		
		//producerThread.join(); //Thread main espera que termine el hilo producerThread
		//consumerThread.join(); //Thread main espera que termine el hilo consumerThread
		
		System.out.println("***End main***");
		
	}
	
}
