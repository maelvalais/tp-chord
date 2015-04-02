import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;


public class Noeud implements NoeudInterface {
	private int cleDebut; // L'idChord (= cleDebut) du noeud-serveur
	private int cleFin; // RAPPEL: cleMin = idChord
	// Ainsi, l'intervalle des clés gérées est [idChord,cleMax]
	private String idRMI; // l'idRMI du noeud-serveur
	private Noeud suivant;
	private Noeud precedent;
	private static int CLE_MAX = 1 << 16; // de 0 à 65536-1
	private static int SERVEUR_PORT = 0;
	private List<Integer> donnees = new ArrayList<Integer>(); 
	// NOTE: donnees[uneCleChord - this.cleMin]
	private static int RMI_REGISTRY_HOST = 0; 

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
	public Noeud nouvNoeud_ChercherPredecesseur(int idChord) 
			throws RemoteException {
		// idChordAAjouter est-il dans mon intervalle ?
		if(this.cleDebut <= idChord && idChord <= this.cleFin) {
			// Je donne juste mon idRMI
			return this;
		} else if (this.cleDebut == idChord) {
			return null;
		}
		else return suivant.nouvNoeud_ChercherPredecesseur(idChord);
	}

	@Override
	public ArrayList<Integer> nouvNoeud_RecupererDonneesQuiSuivent(int cleDebut)
			throws RemoteException {
		ArrayList<Integer> sous_liste = new ArrayList<Integer>();
		for (int cle = cleDebut; cle%CLE_MAX != (this.cleFin+1)%CLE_MAX; cle++) {
			sous_liste.add(donnees.get(cle%CLE_MAX - this.cleDebut));
		}
		return sous_liste;
	}

	@Override
	public void nouvNoeud_validerAjoutNoeud(Noeud leNoeudAjoute) throws RemoteException {
		// On supprime les données dans l'intervalle qui est 
		// maintenant pris en charge par idChordAjoute.
		// L'intervalle est [idChordAjoute;this.cleMax]
		for (int cle = leNoeudAjoute.getIdChord(); cle%CLE_MAX != (this.cleFin+1)%CLE_MAX; cle++) {
			donnees.remove(cle%CLE_MAX - this.cleDebut);
		}
		suivant.setNoeudPrecedent(leNoeudAjoute);
		this.setNoeudSuivant(leNoeudAjoute);
		System.out.println("Noeud-serveur "+this.idRMI+" "
				+ "a ajouté le noeud-serveur "+leNoeudAjoute);
	}

	
	public Noeud(int idChord, String idRMI) {
		super();
		this.cleDebut = idChord;
		this.idRMI = idRMI;
	}
	
	/**
	 * Méthode réservée au noeud-client
	 * @param pointEntreeRMI
	 */
	public void ajoutChord(String pointEntreeRMI) {
		try {
			// On récupère le noeud-serveur en question
			Registry registry = LocateRegistry.getRegistry(RMI_REGISTRY_HOST);
			Noeud pointEntree = (Noeud) registry.lookup(pointEntreeRMI);
			Noeud pred = pointEntree.nouvNoeud_ChercherPredecesseur(this.cleDebut);
			if(pred == null) {
				System.err.println("Impossible d'insérer");
				return;
			}
			this.precedent = pred;
			this.suivant = pred.getNoeudSuivant();
			this.cleFin = pred.getCleFin();
			this.donnees = pred.nouvNoeud_RecupererDonneesQuiSuivent(this.cleDebut);
			pred.nouvNoeud_validerAjoutNoeud(this);
			//this.idChord
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			noeudServeur.suivant = noeudServeur.precedent = noeudServeur;
			noeudServeur.cleFin = (idChordDuNoeud-1)%CLE_MAX;
			noeudServeur.cleDebut = idChordDuNoeud;
		} else {
			noeudServeur.ajoutChord(pointEntreeRMI);
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

	@Override
	public int getIdChord() throws RemoteException {
		return this.cleDebut;
	}

	@Override
	public int getCleDebut() throws RemoteException {
		return this.cleDebut;
	}

	@Override
	public void setNoeudSuivant(Noeud noeudSuivant) throws RemoteException {
		this.suivant = noeudSuivant;
	}
	
	@Override
	public int getCleFin() throws RemoteException {
		return this.cleFin;
	}

	@Override
	public void setNoeudPrecedent(Noeud noeudPrecedent) throws RemoteException {
		this.precedent = noeudPrecedent;
	}

	@Override
	public Noeud getNoeudSuivant() throws RemoteException {
		return this.suivant;
	}

	@Override
	public Noeud getNoeudPrecedent() throws RemoteException {
		return this.precedent;
	}
}
