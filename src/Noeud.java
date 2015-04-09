import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;


/**
 * @note 9/04/2015 
 * Pour régler l'exception java.lang.ClassNotFoundException: NoeudInterface :
 * vérifier qu'on lance bien `rmiregistry &` depuis le dossier bin
 * 
 * @author maelv
 *
 */
public class Noeud implements NoeudInterface {
	private int cleDebut; // L'idChord (= cleDebut) du noeud-serveur
	private int cleFin; // RAPPEL: cleMin = idChord
	private NoeudInterface moi;
	private NoeudInterface suivant;
	private NoeudInterface precedent;
	private static int CLE_MAX = 1 << 16; // de 0 à 65536-1
	private HashMap<Integer,Integer> donnees = new HashMap<Integer, Integer>(); 
	// NOTE: donnees[uneCleChord - this.cleMin] 

	@Override
	public boolean supprimerNoeud() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer get(int cle) throws RemoteException {
		if(!dansIntervalle(cle)) {
			System.out.println("get(): "+cle+" n'est pas dans mon intervalle "
					+this.intervalle()+", je passe au suivant");
			return this.suivant.get(cle);
		}
		System.out.println("get(): "+cle+" est dans mon intervalle "+this.intervalle());
		return donnees.get(cle);
	}

	@Override
	public Integer put(int cle, int donnee) throws RemoteException {
		if(!dansIntervalle(cle)) {
			System.out.println("put(): "+cle+" n'est pas dans mon intervalle ("
					+this.intervalle()+"), je passe au suivant");
			return this.suivant.put(cle,donnee);
		}
		System.out.println("put(): "+cle+" est dans mon intervalle ("+this.intervalle()+")");
		return this.donnees.put(cle, donnee);
	}
	
	private boolean dansIntervalle(int cle) {
		if(this.cleDebut <= this.cleFin) {
			return this.cleDebut <= cle && cle <= this.cleFin;
		} else {
			return (this.cleDebut <= cle && cle <= Noeud.CLE_MAX)
				|| (0 <= cle && cle <= this.cleFin);
		}
	}
	
	@Override
	public NoeudInterface chercherPredecesseur(int idChord) 
			throws RemoteException {
		System.out.println("chercherPred(idChord="+Integer.toString(idChord)+"): "
				+ "mon intervalle est" + this.intervalle());

		// idChord est-il dans mon intervalle ?
		if(dansIntervalle(idChord)) {
			// Je donne juste mon idRMI
			System.out.println("chercherPred(idChord="+Integer.toString(idChord)+"): "
					+ "cet idChord est dans mon intervalle ("+this.intervalle()+")");
			return this;
		} else if (this.cleDebut == idChord) {
			return null;
		}
		else {
			System.out.println("chercherPred(idChord="+Integer.toString(idChord)+"): "
					+ "cet idChord n'est pas dans mon intervalle"
					+ " ("+this.intervalle()+"). Je demande à mon suivant.");
			return suivant.chercherPredecesseur(idChord);
		}
	}

	@Override
	public HashMap<Integer,Integer> recupererDonneesIntervalle(int cleDebut, int cleFin)
			throws RemoteException {
		if(!dansIntervalle(cleDebut) || !dansIntervalle(cleFin)) {
			System.err.println("recupererDonneesIntervalle(): en dehors de mon intervalle"
					+" ("+this.intervalle()+")");
			return null;
		}
		HashMap<Integer,Integer> sous_ensemble_table = new HashMap<Integer,Integer>();
		for (int cle = cleDebut; cle != (cleFin+1)%CLE_MAX; cle=(cle+1)%CLE_MAX) {
			sous_ensemble_table.put(cle, this.donnees.get(cle));
		}
		return sous_ensemble_table;
	}

	@Override
	public void validerAjoutNoeud(NoeudInterface noeud) throws RemoteException {
		// On supprime les données dans l'intervalle qui est 
		// maintenant pris en charge par idChordAjoute.
		// L'intervalle est [idChordAjoute;this.cleMax]
		for (int cle = noeud.getIdChord(); cle%CLE_MAX != (this.cleFin+1)%CLE_MAX; cle++) {
			donnees.remove(cle);
		}
		
		this.cleFin = (noeud.getCleDebut()-1)%CLE_MAX;
		
		suivant.setNoeudPrecedent(noeud);
		this.setNoeudSuivant(noeud);
		
		System.out.println("validerAjoutNoeud(): "
			+"le noeud d'idChord "+noeud.getIdChord()+" a bien été ajouté");
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
	public int getCleFin() throws RemoteException {
		return this.cleFin;
	}
	
	@Override
	public void setNoeudSuivant(NoeudInterface noeudSuivant) throws RemoteException {
		this.suivant = noeudSuivant;
	}

	@Override
	public void setNoeudPrecedent(NoeudInterface noeudPrecedent) throws RemoteException {
		this.precedent = noeudPrecedent;
	}

	@Override
	public NoeudInterface getNoeudSuivant() throws RemoteException {
		return this.suivant;
	}

	@Override
	public NoeudInterface getNoeudPrecedent() throws RemoteException {
		return this.precedent;
	}

	
	public Noeud(int idChord, String idRMI) {
		super();
		this.cleDebut = idChord;
	}
	
	/**
	 * Méthode réservée au noeud-client
	 * @param pointEntreeRMI
	 */
	public boolean ajoutChord(String pointEntreeRMI) {
		try {
			// On récupère le noeud-serveur en question
			Registry registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
			NoeudInterface pointEntree = (NoeudInterface) registry.lookup(pointEntreeRMI);
			NoeudInterface pred = pointEntree.chercherPredecesseur(this.cleDebut);
			if(pred == null) {
				System.err.println("ajoutChord(): l'identifiant CHORD (= clé) "
						+Integer.toString(this.cleDebut)+" est déjà utilisé");
				return false;
			}
			this.precedent = pred;
			this.suivant = pred.getNoeudSuivant();
			this.cleFin = pred.getCleFin();
			this.donnees = pred.recupererDonneesIntervalle(this.cleDebut, this.cleFin);
			System.out.println("ajoutChord(): la table récupérée est de taille "
					+Integer.toString(this.donnees.size()));
			if(this.donnees != null) {
				pred.validerAjoutNoeud(this.moi);
				System.out.println("ajoutChord(): noeud bien ajouté"
						+" avec l'intervalle "+this.intervalle()+"");
			} else {
				System.err.println("ajoutChord(): noeud non ajouté");
			}
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println("ajoutChord(): l'id RMI '"+
				pointEntreeRMI+"' donné comme point d'entrée n'existe pas");
			return false;
		}
		return true;
	}
	
	private String intervalle() {
		return "["+Integer.toString(this.cleDebut)+","+Integer.toString(this.cleFin)+"]";
	}
	

	public static void main(String[] args) {
		if (args.length != 3 && args.length != 2) {
			System.err.println("Usage: chord id_chord_nouv_noeud id_RMI_nouv_noeud [id_RMI_point_entree]");
			System.err.println("Note: si id_RMI_point_entree n'est pas donné, c'est le premier noeud");
		}
		int idChordDuNoeud = Integer.parseInt(args[0]);
		String idRMIDuNoeud = args[1];
		
		Noeud noeud = new Noeud(idChordDuNoeud,idRMIDuNoeud);
		try {
			NoeudInterface stub = (NoeudInterface) UnicastRemoteObject.exportObject(noeud, noeud.cleDebut+400); // XXX Il faudrait fixer ça !
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(idRMIDuNoeud, stub);
			noeud.moi = stub;
			System.out.println("Le noeud d'identifiant RMI '"+idRMIDuNoeud+"'"
					+" a été ajouté au rmiregistry");
		} catch (RemoteException e) {
			System.err.println("Erreur lors du lien avec rmiregistry pour le remote object Noeud");
			e.printStackTrace();
		}

		noeud.cleDebut = idChordDuNoeud;
		if(args.length == 2) {
			// Cas où c'est le premier oeud inséré
			noeud.suivant = noeud.precedent = noeud;
			noeud.cleFin = (idChordDuNoeud-1)%CLE_MAX;
			int cle = noeud.cleDebut;
			for (int i = 0; i < CLE_MAX; i++) {
				noeud.donnees.put(cle,cle);
				cle=(cle+1)%CLE_MAX;
			}
		} else {
			// Cas où ce noeud s'insère à d'autres noeuds
			String pointEntreeRMI = args[2];
			if(!noeud.ajoutChord(pointEntreeRMI)) {
				// Si on a pas réussi à insérer le noeud dans l'anneau
				return;
			}
		}
	}
}
