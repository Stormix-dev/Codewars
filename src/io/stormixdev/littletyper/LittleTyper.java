package io.stormixdev.littletyper;

import java.util.*;

/* 
 * Questo codice implementa una funzione `inferType` che esegue l'inferenza del tipo 
 * di un'espressione basata su un contesto di dichiarazioni di tipo. Il contesto è una 
 * stringa che definisce i tipi delle variabili e delle funzioni, mentre l'espressione è
 * una sequenza di applicazioni di funzioni e variabili, il cui tipo deve essere dedotto.
 * 
 * La sintassi per i tipi è simile a linguaggi funzionali come Haskell o Idris, dove i
 * tipi possono essere funzioni e possono essere associati a variabili o funzioni dichiarate.
 * La funzione infer_type restituirà il tipo dell'espressione basandosi sul contesto.
 * 
 * Esempio di contesto:
 *   myValue : A
 *   concat : List -> List -> List
 *   append : List -> A -> List
 *   map : (A -> B) -> (List -> List)
 *   pure : A -> List
 *   
 *   Più nel dettaglio, 
 *   myValue : A
 *   pure : A -> List
 *   Con l'espressione pure myValue il sistema capisce che myValue è di tipo A, e che pure 
 *   prende un A e restituisce un List.
 *   Quindi pure myValue ha tipo List. 
 *   
 * Esempio di espressione:
 *   "append (concat (pure myValue) (pure myValue)) myValue" -> "List"
 *   
 * Le funzioni di applicazione sono left-associative e il tipo è inferito ricorsivamente.
 * 
 * Inoltre, vengono gestiti errori come:
 *   - Variabili non dichiarate nel contesto.
 *   - Funzione applicata a un valore che non è una funzione.
 *   - Funzione invocata con un argomento di tipo errato.
 */
public class LittleTyper {

	// Classe che rappresenta il risultato del parsing del tipo
	private static class TypeParseResult {
		public Type result; // Tipo inferito
		public int endIndex; // Indice finale dell'analisi

		// Costruttore per inizializzare il risultato e l'indice
		public TypeParseResult(Type t, int i) {
			result = t;
			endIndex = i;
		}
	}

	// Classe astratta che rappresenta un tipo
	private static abstract class Type {

		// Metodo per "unire" una lista di tipi in un tipo composto (funzione)
		private static Type merge(List<Type> list, int index) {
			if (index == list.size() - 1)
				return list.get(index); // Restituisce l'ultimo tipo della lista
			return new FunctionType(list.get(index), merge(list, index + 1)); // Combinazione di tipi funzione
		}

		// Metodo per fare il parsing di una stringa di tipo (ad esempio A -> B -> C)
		public static TypeParseResult parse(String expr, int index) {
			List<Type> list = new ArrayList<>();
			StringBuilder current = new StringBuilder();

			// Scansiona il tipo in input carattere per carattere
			for (int i = index; i < expr.length(); i++) {
				if (expr.charAt(i) == '(') {
					TypeParseResult r = parse(expr, i + 1); // Se trova una parentesi aperta, entra nel sotto-tipo
					list.add(r.result); // Aggiunge il risultato del sotto-parsing
					i = r.endIndex; // Aggiorna l'indice per saltare al termine del sotto-tipo
				} else if (expr.charAt(i) == ')') {
					// Se chiude una parentesi, aggiunge il tipo e ritorna il risultato
					if (current.length() > 0) {
						list.add(new NamedType(current.toString())); // Aggiunge il tipo finale
						current.setLength(0); // Reset del costruttore di stringhe
					}
					return new TypeParseResult(merge(list, 0), i); // Unisce i tipi e ritorna il risultato
				} else if (expr.charAt(i) == '-') {
					// Gestisce il caso in cui trova il simbolo '-'
					if (current.length() > 0) {
						list.add(new NamedType(current.toString())); // Aggiunge il tipo precedentemente trovato
						current.setLength(0); // Reset
					}
					i = i + 1; // Passa al prossimo carattere
				} else {
					current.append(expr.charAt(i)); // Aggiunge il carattere al tipo corrente
				}
			}

			// Alla fine del parsing, aggiunge l'ultimo tipo trovato
			if (current.length() > 0)
				list.add(new NamedType(current.toString()));

			return new TypeParseResult(merge(list, 0), expr.length() - 1); // Restituisce il tipo completo
		}
	}

	// Classe che rappresenta un tipo nominato (ad esempio "A", "B")
	private static class NamedType extends Type {

		public String name; // Nome del tipo

		// Costruttore per inizializzare il tipo
		public NamedType(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name; // Restituisce il nome del tipo come stringa
		}

		// Override del metodo equals per confrontare due tipi nominati
		@Override
		public boolean equals(Object other) {
			if (other instanceof NamedType) {
				NamedType t = (NamedType) other;
				return t.name.equals(name); // Confronta i nomi dei tipi
			}
			return false;
		}
	}

	// Classe che rappresenta un tipo funzione (ad esempio A -> B)
	private static class FunctionType extends Type {

		public Type from; // Tipo di input della funzione
		public Type to; // Tipo di output della funzione

		// Costruttore per inizializzare una funzione di tipo
		public FunctionType(Type from, Type to) {
			this.from = from;
			this.to = to;
		}

		// Override del metodo equals per confrontare due funzioni
		@Override
		public boolean equals(Object other) {
			if (other instanceof FunctionType) {
				FunctionType t = (FunctionType) other;
				return from.equals(t.from) && to.equals(t.to); // Confronta i tipi di input e output
			}
			return false;
		}

		// Metodo toString per restituire la rappresentazione della funzione come
		// stringa
		@Override
		public String toString() {
			String a = from.toString();
			String b = to.toString();
			if (from instanceof FunctionType)
				return "(" + a + ")->" + b; // Aggiunge parentesi per funzioni nidificate
			else
				return a + "->" + b; // Restituisce la funzione nel formato "A -> B"
		}
	}

	// Classe astratta per rappresentare una espressione
	private static abstract class Expr {

		// Metodo astratto per valutare il tipo di un'espressione
		public abstract Type evaluate(Map<String, Type> context);
	}

	// Classe per espressioni singole (variabili)
	private static class SingleExpr extends Expr {
		public String name; // Nome della variabile

		// Costruttore
		public SingleExpr(String name) {
			this.name = name;
		}

		// Metodo per valutare il tipo di una variabile (dalla mappa di contesto)
		@Override
		public Type evaluate(Map<String, Type> context) {
			if (!context.containsKey(name))
				throw new IllegalArgumentException(); // Lancia eccezione se la variabile non è nel contesto
			return context.get(name); // Restituisce il tipo dalla mappa
		}

		@Override
		public String toString() {
			return name; // Restituisce il nome dell'espressione
		}
	}

	// Classe per espressioni multiple (applicazione di funzioni)
	private static class MultiExpr extends Expr {
		public List<Expr> subExpr; // Lista di sotto-espressioni

		public MultiExpr(List<Expr> subExpr) {
			this.subExpr = subExpr;
		}

		// Metodo per applicare una funzione a un argomento
		private static Type applyFunction(Type function, Type arg) {
			if (function == null)
				return arg; // Se non ci sono funzioni, restituisce l'argomento

			if (function instanceof FunctionType) {
				FunctionType func = (FunctionType) function;
				if (func.from.equals(arg))
					return func.to; // Se il tipo dell'argomento è quello aspettato, restituisce il tipo di ritorno
				else
					throw new IllegalArgumentException(); // Altrimenti lancia un errore
			} else {
				throw new IllegalArgumentException(); // Se il tipo non è una funzione, lancia errore
			}
		}

		// Metodo ricorsivo per applicare le funzioni in sequenza
		private Type eval(Type function, int index, Map<String, Type> context) {
			Type type = subExpr.get(index).evaluate(context);
			if (index == subExpr.size() - 1)
				return applyFunction(function, type); // Applicazione finale della funzione
			return eval(applyFunction(function, type), index + 1, context); // Continua applicando la funzione
		}

		// Metodo per valutare il tipo dell'espressione
		@Override
		public Type evaluate(Map<String, Type> context) {
			return eval(null, 0, context); // Inizia la valutazione
		}

		@Override
		public String toString() {
			return "(" + String.join(" ", subExpr.stream().map(x -> x.toString()).toList()) + ")"; // Restituisce
																									// l'espressione
																									// come stringa
		}
	}

	// Classe che rappresenta il risultato del parsing di un'espressione
	private static class ExprParseResult {
		public Expr result;
		public int endIndex; // Indice finale dell'espressione

		public ExprParseResult(Expr r, int i) {
			result = r;
			endIndex = i;
		}
	}

	// Metodo per fare il parsing di un'espressione (gestisce variabili e
	// applicazioni di funzioni)
	private static ExprParseResult parseExpr(String expr, int index) {
		List<Expr> result = new ArrayList<>();
		StringBuilder current = new StringBuilder();

		// Analizza carattere per carattere
		for (int i = index; i < expr.length(); i++) {
			if (expr.charAt(i) == '(') {
				if (current.length() > 0) {
					result.add(new SingleExpr(current.toString())); // Aggiunge la variabile trovata
					current.setLength(0); // Reset
				}
				ExprParseResult r = parseExpr(expr, i + 1); // Analizza la sotto-espressione
				result.add(r.result); // Aggiunge il risultato
				i = r.endIndex; // Aggiorna l'indice
			} else if (expr.charAt(i) == ')') {
				if (current.length() > 0) {
					result.add(new SingleExpr(current.toString())); // Aggiunge l'ultimo tipo trovato
					current.setLength(0); // Reset
				}
				return new ExprParseResult(new MultiExpr(result), i); // Restituisce il risultato dell'espressione
			} else if (expr.charAt(i) == ' ') {
				if (current.length() > 0) {
					result.add(new SingleExpr(current.toString())); // Aggiunge una variabile
					current.setLength(0); // Reset
				}
			} else {
				current.append(expr.charAt(i)); // Aggiunge il carattere corrente all'espressione
			}
		}

		// Aggiunge l'ultima variabile se esiste
		if (current.length() > 0) {
			result.add(new SingleExpr(current.toString()));
			current.setLength(0);
		}

		return new ExprParseResult(new MultiExpr(result), expr.length() - 1); // Restituisce il risultato finale
	}

	// Metodo per analizzare il contesto (dove ogni variabile è associata a un tipo)
	private static Map<String, Type> parseContext(String context) {
		Map<String, Type> result = new HashMap<>();
		String[] parts = context.replaceAll(" ", "").split("\n"); // Pulisce il contesto e lo divide per linee
		for (String part : parts) {
			String[] temp = part.split(":"); // Divide nome e tipo
			result.put(temp[0], Type.parse(temp[1], 0).result); // Aggiunge al contesto
		}
		return result;
	}

	// Metodo principale per inferire il tipo dato un contesto e un'espressione
	public static String inferType(String context, String expression) {
		return parseExpr(expression, 0).result.evaluate(parseContext(context)).toString(); // Analizza e ritorna il tipo
	}

}