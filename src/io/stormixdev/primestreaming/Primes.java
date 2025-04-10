package io.stormixdev.primestreaming;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/*
 * Quello che vogliamo è creare un IntStream infinito che generi numeri primi, e che sia anche molto veloce.
 * Ad esempio, deve riuscire a generare anche un milione di numeri primi in pochi secondi.
 */
public class Primes {

	public static IntStream stream() {

		// Unisce due stream:
		// 1. uno contenente solo il primo numero primo (2)
		// 2. uno che genera infiniti numeri dispari a partire da 3 e filtra solo i
		// primi
		return IntStream.concat(

				// Stream che contiene solo il numero 2 (il primo numero primo)
				IntStream.of(2),

				// Stream infinito di numeri dispari, partendo da 3 (tutti i numeri>2 devono
				// essere dispari per essere primi
				IntStream.iterate(3, i -> i + 2)

						// Applica un filtro per conservare solo i numeri che sono primi
						.filter(new java.util.function.IntPredicate() {

							// Lista che memorizza i numeri primi già trovati
							// Serve per evitare di ricontrollare da capo ogni volta e velocizzare il test
							final List<Integer> primes = new ArrayList<>();

							// Metodo chiamato per ogni numero del flusso per decidere se è primo o no
							@Override
							public boolean test(int candidate) {

								// Calcola la radice quadrata del numero: non serve controllare oltre questo
								// valore
								int sqrt = (int) Math.sqrt(candidate);

								// Per ogni primo già trovato:
								for (int prime : primes) {
									// Se il primo attuale è maggiore della radice, abbiamo finito: è primo
									if (prime > sqrt)
										break;

									// Se è divisile per un primo, allora non è primo
									if (candidate % prime == 0)
										return false;
								}

								// Se è arrivato fin qui, il numero è primo: lo aggiungiamo alla lista per i
								// prossimi test
								primes.add(candidate);

								// Confermiamo che tale numero è primo.
								return true;
							}
						}));
	}

	// METODO MAIN
	public static void main(String[] args) {
		Primes.stream()
		.limit(5000)
		.forEach(System.out::println);
	}

}

