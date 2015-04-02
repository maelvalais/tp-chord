import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;


public class Noeud implements NoeudInterface {
	private int idChord; // L'idChord du noeud-serveur
	private int cleMax; // RAPPEL: cleMin = idChord
	// Ainsi, l'intervalle des clés gérées est [idChord,cleMax]
	private String idRMI; // l'idRMI du noeud-serveur
	private String idRMIPrecedent; // idRMI du noeud précédent
	private String idRMISuivant; // idRMI du noeud suivant
	private final static int CLE_MAX = 1 << 16; // de 0 à 65536-1
	private final static int SERVEUR_PORT = 0;

	@Override
	public boolean supprimerNoeud() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int get(int cle) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean update(int cle, int donnee) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String nouvNoeud_ChercherPredecesseur(int idChord, String idRMIDuNoeudDEntree) 
			throws RemoteException {
		// idChordAAjouter est-il dans mon intervalle ?
		if(this.idChord <= idChord && idChord <= this.cleMax) {
			// Je donne juste mon idRMI
			return this.idRMI;
		} else if (this.idChord != idChord) {
			return null;
		}
		else return nouvNoeud_ChercherPredecesseur(idChord, this.idRMISuivant);
	}

	@Override
	public int nouvNoeud_RecupererIdChord() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Integer> nouvNoeud_RecupererDonneesQuiSuivent(int cleDebut)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String nouvNoeud_RecupererIdRMISuivant() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void nouvNoeud_validerAjoutNoeud(int idChordAjoute,
			String idRMIAjoute) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	
	public Noeud(int idChord, String idRMI) {
		super();
		this.idChord = idChord;
		this.idRMI = idRMI;
	}

	public static void main(String[] args) {
		if (args.length != 3 && args.length != 2) {
			System.err.println("Usage: chord id_chord_nouv_noeud id_RMI_nouv_noeud [id_RMI_point_entree]");
			System.err.println("Note: si id_RMI_point_entree n'est pas donné, c'est le premier noeud");
		}
		int idChordDuNoeud = Integer.parseInt(args[0]);
		String idRMIDuNoeud = args[1];
		String pointEntreeRMI = args[2];
		
		Noeud noeudServeur = new Noeud(idChordDuNoeud,idRMIDuNoeud);

		// Traitement du cas "premier noeud-serveur inséré"
		if(args.length == 2) {
			noeudServeur.idRMI = idRMIDuNoeud;
			noeudServeur.idRMIPrecedent = idRMIDuNoeud;
			noeudServeur.idRMISuivant = idRMIDuNoeud;
			noeudServeur.cleMax = CLE_MAX;
			noeudServeur.idChord = 0;
		}
		
		try {
			Noeud stub = (Noeud) UnicastRemoteObject.exportObject(noeudServeur, SERVEUR_PORT);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(idRMIDuNoeud, stub);
			System.out.println("Le noeud-serveur d'identifiant RMI "+idRMIDuNoeud+" est prêt");
		} catch (RemoteException e) {
			System.err.println("Erreur lors du lien avec rmiregistry pour le remote object Noeud");
			e.printStackTrace();
		}
	}
}
