import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * Usage: `java Main`
 * permet de lancer plusieurs noeuds et de les insérer dans l'anneau.
 * Ensuite, on pourra utiliser `java NoeudClient N1 get 5` pour consulter
 * la table de hashage distribuée.
 * @author maelv
 *
 */

public class Main {
	private static Process lancer(int idChord, String idRMI, String idRMIEntree) {
		String command = "java Noeud "+idChord+" "+idRMI+" "+idRMIEntree;
		ProcessBuilder pb = new ProcessBuilder(command.split(" "));
		pb.inheritIO(); // Redirige stdout/stderr vers le processus père
		try {
			return pb.start();
		} catch (IOException e) {
			System.out.println("lancer("+idChord+","+idRMI+","+idRMIEntree+"):"
					+ "erreur");
			e.printStackTrace();
		}
		return null;
	}
	private static void get(int cle, String idRMIEntree) {
		String command = "java NoeudClient "+idRMIEntree+" get "+cle;
		ProcessBuilder pb = new ProcessBuilder(command.split(" "));
		pb.inheritIO(); // Redirige stdout/stderr vers le processus père
		try {
			pb.start();
		} catch (IOException e) {
			System.out.println("put("+idRMIEntree+"):"
					+ "erreur");
			e.printStackTrace();
		}
	}
	private static void put(int cle, int donnee, String idRMIEntree) {
		String command = "java NoeudClient "+idRMIEntree+" put "+cle+" "+donnee;
		ProcessBuilder pb = new ProcessBuilder(command.split(" "));
		pb.inheritIO(); // Redirige stdout/stderr vers le processus père
		try {
			pb.start();
		} catch (IOException e) {
			System.out.println("put("+idRMIEntree+"):"
					+ "erreur");
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		try {
			List<Process> lesprocessus = new Vector<Process>();
			
			Process p = Main.lancer(0,"N1","");
			lesprocessus.add(p);

			List<String> noeudsNoms = new ArrayList<String>();
			Integer[] noeudsIdChord = {6000, 4000, 1, 3000, 2999, 9000};
			for (int i = 0; i < noeudsIdChord.length+1; i++) {
				noeudsNoms.add("N"+(i+1));
			}
			
			for (int i = 0; i < noeudsIdChord.length; i++) {
				p.waitFor(1, TimeUnit.SECONDS);
				lesprocessus.add(Main.lancer(noeudsIdChord[i],noeudsNoms.get(i+1),noeudsNoms.get(i)));
			}
			
			get(1, "N1");

			p.waitFor();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
