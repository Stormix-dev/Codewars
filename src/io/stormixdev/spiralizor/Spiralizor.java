package io.stormixdev.spiralizor;

/*
 * Abbiamo un numero intero size >= 5. 
 * Il problema chiede di creare una matrice NxN (size righe e size colonne), che contenga un "serpente" fatto di 1 che:
 * 
 * 1. Parte dal bordo in alto a sinistra e si muove in spirale verso il centro.
 * 2. Si muove sempre in una direzinoe (destra, giù, sinistra, su) e cambia quando colpisce un ostacolo.
 * 3. Non può mai toccarsi, nemmeno negli angoli.
 * 4. L'output deve essere una int[][] dove 1 rappresenta una cella occupata dalla spirale e 0 una cella vuota.
 */
public class Spiralizor {

	public static int[][] spiralize(int size) {

		// Non ha senso creare una spirale
		if (size <= 0)
			return null;

		// Inizializza una matrice quadrata di zeri
		int[][] spiral = new int[size][size];

		// Definisce i limiti iniziali per colonne e righe
		int minCol = 0;
		int maxCol = size - 1;
		int minRow = 0;
		int maxRow = size - 1;

		// La matrice inizialmente è riempita con tutti 0

		// Costruiamo la spirale finché i confini non si incrociano
		while (minRow <= maxRow) {

			// Va da sx a dx sulla riga in alto
			for (int i = minCol; i <= maxCol; i++)	spiral[minRow][i] = 1;

			// Va dall'alto in basso sull'ultima colonna (dx)
			for (int i = minRow; i <= maxRow; i++)	spiral[i][maxCol] = 1;

			// Aumenta la colonna sx solo se non siamo all'inizio (per evitare chiusura della spirale)
			if (minCol != 0)	minCol += 1;
			
			// Caso di fine spirale: se le righe sono troppo vicine, si esce
			if (maxRow - 1 == minRow)	break;

			// Va da dx a sx sulla riga in basso
			for (int i = maxCol - 1; i >= minCol; i--)	spiral[maxRow][i] = 1;

			// Sale sulla prima colonna (sx), lasciando uno spazio di margine in alto
			for (int i = maxRow - 1; i >= minRow + 2; i--)	spiral[i][minCol] = 1;

			// Restringe i limiti per passare al "livello" più interno della spirale
			minCol += 1;
			minRow += 2;
			maxCol -= 2;
			maxRow -= 2;
		}
		return spiral;
	}

	// Metodo di test per stampare la matrice
	public static void print(int[][] mat) {
		for (int[] row : mat) {
			for (int el : row) {
				System.out.print(el == 1 ? "0" : "."); // per visualizzarlo come nel problema
			}
			System.out.println();
		}
	}

	// METODO MAIN
	public static void main(String[] args) {
		print(spiralize(5));
		System.out.println();
		print(spiralize(10));
	}

}
