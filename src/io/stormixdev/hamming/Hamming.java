package io.stormixdev.hamming;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/*
 * A Hamming number is a positive integer of the form 2^(i)3^(j)5^(k), for some non-negative integers i, j, and k.
 * Write a function that computes the n-th smallest Hamming number.
 * 
 * Specifically:
 * > The first smallest Hamming number is 1 = 2^(0) * 3^(0) * 5^(0)
 * > The second smallest Hamming number is 2 = 2^(1) * 3^(0) * 5^(0)
 * > The third smallest Hamming number is 3 = 2^(0) * 3^(1) * 5^(0)
 * > The fourth smallest Hamming number is 4 = 2^(2) * 3^(0) * 5^(0)
 * > The fifth smallest Hamming number is 5 = 2^(0) * 3^(0) * 5^(1)
 * > The sixth smallest Hamming number is 6 = 2^(1) * 3^(1) * 5^(0)
 * 
 * The 20 smallest Hamming numbers are given in the Example test fixture.
 * Your code should be able to compute the first 5 000 ( LC: 400, Clojure: 2 000, Haskell: 12 691, NASM, C, D, C++, Go and Rust: 13 282 ) 
 * Hamming numbers without timing out.
*/
public class Hamming {

	public static long hamming(int n) {
		// Step 1: Creiamo un set per evitare duplicati
		Set<Long> seen = new HashSet<Long>();

		// Step 2: Usare una coda di priorità (min-heap) per ottenere sempre il numero
		// più piccolo
		PriorityQueue<Long> pq = new PriorityQueue<Long>();

		// Step 3: Il primo numero di Hamming è 1, quindi lo aggiungiamo alla coda
		pq.offer(1L);
		seen.add(1L);

		// Step 4: Variabili per memorizzare i moltiplicatori 2, 3 e 5
		long[] factors = { 2, 3, 5 };

		// Step 5: Iteriamo n-volte per estrarre l'n-esimo numero di Hamming
		long hammingNumber = 0;

		for (int i = 0; i < n; i++) {
			// Step 6: Estraiamo il numero più piccolo della coda
			hammingNumber = pq.poll();

			// Step 7: Moltiplichiamo il numero estratto per 2, 3 e 5, aggiungendoli nella
			// coda se non previsti
			for (long factor : factors) {
				long next = hammingNumber * factor;

				// Aggiungiamo solo se non è già stato visto
				if (!seen.contains(next)) {
					seen.add(next);
					pq.offer(next);
				}
			}
		}

		// Step 8: Dopo il ciclo, hammingNumber conterrà l'n-esimo numero di Hamming
		return hammingNumber;
	}

	public static void main(String[] args) {
		System.out.println(hamming(95));
	}
}
