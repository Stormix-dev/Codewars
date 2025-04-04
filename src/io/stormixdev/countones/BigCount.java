package io.stormixdev.countones;

import java.math.BigInteger;

/*
 * Given two numbers: 'left' and 'right' (1 <= 'left' <= 'right' <= 200.000.000.000.000) 
 * return sum of all '1' occurencies in binary representations of numbers between 'left' and 'right' (including both)
 * 
 * Example:
	countOnes 4 7 should return 8, because:
	4(dec) = 100(bin), which adds 1 to the result.
	5(dec) = 101(bin), which adds 2 to the result.
	6(dec) = 110(bin), which adds 2 to the result.
	7(dec) = 111(bin), which adds 3 to the result.
	So finally result equals 8.
	
	WARNING: Segment may contain billion elements, to pass this kata, your solution cannot iterate through all numbers in the segment!
 */

public class BigCount {
	
	/*
	 *  Strategia: useremp una formula ricorsiva, usando un pattern molto potente.
	 *  Per un numero n, vogliamo contare quanti 1 ci sono da 0 a n nella loro rappresentazione binaria.
	 *  
	 *  Supponiamo che: 
	 *  p = la posizione del bit più significativo (il più alto)
	 *  power = 2^p
	 *  
	 *  allora 
	 *  countOnesUpTo(n) = (p) * (power/2) 		// tutti gli 1 nei numeri da 0 a power - 1
	 *  					+ (n - power + 1)
	 *  					+ countOnesUpTo(n - power)	
	 *  
	 *  ESEMPIO
	 *  Per n = (7)_10 che in binario è (111)_2
	 *  
	 *  countOnesUpTo(7) = 4(base) + 4(alto) + countOnesUpTo(3)
	 *  
	 *  Time complexity: O(log n) -> solo fino a 64 ricorsioni
	 *  
	 */
	
	public static BigInteger countOnes(long left, long right) {
		return countUpTo(right).subtract(countUpTo(left - 1));
	}
	
	private static BigInteger countUpTo(long n) {
		if (n <= 0) return BigInteger.ZERO;
		
		// Troviamo il bit più alto
		long p = 63 - Long.numberOfLeadingZeros(n);	// posizione MSB
		long power = 1L << p;
		
		// base: quanti 1 da 0 a 2^p - 1
		BigInteger base = BigInteger.valueOf(p).multiply(BigInteger.valueOf(power >> 1));
		
		// alto: quanti numeri da 2^p a n -> hanno tutti 1 nel MSB
		BigInteger high = BigInteger.valueOf(n - power + 1);
		
		// Ricorsione sulla parte rimanente
		BigInteger rest = countUpTo(n - power);
		
		return base.add(high).add(rest);
	}
	
	public static void main(String[] args) {
		System.out.println(countOnes(3, 434234234));
	}

}
