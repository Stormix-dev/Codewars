package io.stormixdev.closestpairofpoints;

import java.util.*;

/*
 * Cosa chiede il problema?
 * Dato un insieme di punti su un piano, vogliamo trovare la coppia di punti più vicina tra loro
 * in termini di distanza euclidea.
 * 
 * L'obiettivo è non fare il confronto brutale O(n^(2)) tra tutte le coppie, ma trovare una soluzione
 * più intelligente in O(n log(n)).
 */
public class Kata {

	/*
	 * La strategia è usare il concetto di "Divide et Impera". 1. Ordiniamo i punti
	 * per X e Y.
	 * 
	 * 2. Dividiamo a metà l'insieme.
	 * 
	 * 3. Risolviamo ricorsivamente per la metà sinistra e destra.
	 * 
	 * 4. Consideriamo una 'striscia centrale' che può contenere la coppia più
	 * vicina (una a sinistra e una a destra).
	 * 
	 * 5. Confrontiamo solo pochi punti vicini nella striscia (entro delta).
	 */

	// Definiamo una semplice classe Point
	static class Point {
		double x, y;

		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}

	// Metodo principale che trova la coppia di punti più vicina
	public static List<Kata.Point> closestPair(List<Kata.Point> points) {
		// Crea una copia della lista dei punti ordinata in base alla coordinata x
		List<Kata.Point> pointsByX = new ArrayList<>(points);
		pointsByX.sort(Comparator.comparingDouble(p -> p.x)); // Ordina i punti in base alla coordinata x

		// Crea una copia della lista dei punti ordinata in base alla coordinata y
		List<Kata.Point> pointsByY = new ArrayList<>(points);
		pointsByY.sort(Comparator.comparingDouble(p -> p.y)); // Ordina i punti in base alla coordinata y

		// Chiama il metodo ricorsivo per trovare la coppia più vicina
		return closestPairRecursive(pointsByX, pointsByY);
	}

	// Metodo ricorsivo che trova la coppia più vicina dividendo i punti in due metà
	private static List<Kata.Point> closestPairRecursive(List<Kata.Point> px, List<Kata.Point> py) {
		int n = px.size(); // Ottiene il numero di punti nella lista

		// Se ci sono al massimo 3 punti, usa l'approccio di bruta force
		if (n <= 3) {
			return bruteForceClosest(px);
		}

		// Trova il punto di divisione a metà
		int mid = n / 2;
		Point midPoint = px.get(mid); // Prende il punto di mezzo in base all'ordinamento sulla coordinata x

		// Divide la lista dei punti in due metà: sinistra e destra
		List<Kata.Point> leftPx = px.subList(0, mid); // Sottolista con i punti a sinistra del punto centrale
		List<Kata.Point> rightPx = px.subList(mid, n); // Sottolista con i punti a destra del punto centrale

		// Divide la lista ordinata per y in due metà
		List<Kata.Point> leftPy = new ArrayList<>();
		List<Kata.Point> rightPy = new ArrayList<>();
		for (Point p : py) { // Per ogni punto nella lista ordinata per y
			if (p.x <= midPoint.x) { // Se il punto è a sinistra del punto centrale
				leftPy.add(p); // Aggiungilo alla metà sinistra
			} else {
				rightPy.add(p); // Altrimenti, aggiungilo alla metà destra
			}
		}

		// Trova ricorsivamente la coppia più vicina nella metà sinistra e nella metà
		// destra
		List<Kata.Point> leftClosest = closestPairRecursive(leftPx, leftPy);
		List<Kata.Point> rightClosest = closestPairRecursive(rightPx, rightPy);

		// Calcola la distanza minima tra le due coppie trovate
		double leftDist = distance(leftClosest.get(0), leftClosest.get(1)); // Distanza tra la coppia di punti a
																			// sinistra
		double rightDist = distance(rightClosest.get(0), rightClosest.get(1)); // Distanza tra la coppia di punti a
																				// destra
		double delta = Math.min(leftDist, rightDist); // La distanza minima tra le due coppie
		List<Kata.Point> bestPair = leftDist <= rightDist ? leftClosest : rightClosest; // La coppia migliore tra le due

		// Crea una lista di punti che sono vicini alla linea di divisione
		List<Kata.Point> strip = new ArrayList<>();
		for (Point p : py) { // Per ogni punto ordinato per y
			if (Math.abs(p.x - midPoint.x) < delta) { // Se la distanza in x dal punto centrale è inferiore alla
														// distanza minima trovata
				strip.add(p); // Aggiungi il punto alla lista strip
			}
		}

		// Per ogni punto nella lista strip, confronta la distanza con i successivi
		for (int i = 0; i < strip.size(); i++) { // Per ogni punto nella lista strip
			for (int j = i + 1; j < strip.size() && (strip.get(j).y - strip.get(i).y) < delta; j++) {

				// Calcola la distanza tra il punto i e il punto j
				double d = distance(strip.get(i), strip.get(j));

				// Se la distanza è inferiore alla distanza minima trovata
				if (d < delta) {
					delta = d; // Aggiorna la distanza minima
					bestPair = List.of(strip.get(i), strip.get(j));
				}
			}
		}
		return bestPair; // Ritorna la coppia di punti più vicina
	}

	// Metodo che trova la coppia più vicina usando il brute force (per 3 o meno
	// punti)
	private static List<Kata.Point> bruteForceClosest(List<Kata.Point> points) {
		double minDist = Double.MAX_VALUE; // Inizializza la distanza minima a un valore molto grande
		List<Kata.Point> bestPair = null; // Variabile per memorizzare la coppia migliore

		for (int i = 0; i < points.size(); i++) { // Cicla su tutti i punti
			for (int j = i + 1; j < points.size(); j++) { // Cicla sui punti successivi al punto i

				// Calcola la distanza tra il punto i e il punto j
				double d = distance(points.get(i), points.get(j));

				// Se la distanza è inferiore alla distanza minima trovata
				if (d < minDist) {

					// Aggiorna la distanza minima
					minDist = d;

					// Aggiorna la coppia migliore
					bestPair = List.of(points.get(i), points.get(j));
				}
			}
		}
		return bestPair; // Ritorna la coppia migliore
	}

	// Metodo per calcolare la distanza euclidea tra due punti
	private static double distance(Point a, Point b) {
		double dx = a.x - b.x; // Calcola la differenza tra le coordinate x dei due punti
		double dy = a.y - b.y; // Calcola la differenza tra le coordinate y dei due punti
		return Math.sqrt(dx * dx + dy * dy); // Usa il teorema di Pitagora per calcolare la distanza
	}

	// Metodo di test
	public static void main(String[] args) {
		List<Kata.Point> input = List.of(new Point(2, 2), new Point(2, 8), new Point(5, 5), new Point(6, 3),
				new Point(6, 7), new Point(7, 4), new Point(7, 9));

		List<Kata.Point> result = closestPair(input);
		System.out.println("Closest pair: (" + result.get(0).x + "," + result.get(0).y + ") and (" + result.get(1).x
				+ "," + result.get(1).y + ")");
	}

}
