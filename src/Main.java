import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;


public class Main {
	public static void main(String[] args) {
		try {
			List<Process> processus = new Vector<Process>();
			List<BufferedReader> stdout = new Vector<BufferedReader>(); 

			String command;
			Process p;
			ProcessBuilder pb; 
			command = "java Noeud 0 N1";
			pb = new ProcessBuilder(command.split(" "));
			pb.inheritIO(); // Redirige stdout/stderr vers le processus père
			p = pb.start();
			stdout.add(new BufferedReader(new InputStreamReader(p.getInputStream())));

			for (int i = 2; i < 5; i++) {
				p.waitFor(1, TimeUnit.SECONDS);
				command = "java Noeud "+i*100+" N"+i +" N1";
				pb = new ProcessBuilder(command.split(" "));
				pb.inheritIO(); // Redirige stdout/stderr vers le processus père
				p = pb.start();
				processus.add(p);
				stdout.add(new BufferedReader(new InputStreamReader(p.getInputStream())));
			}

			p.waitFor();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
