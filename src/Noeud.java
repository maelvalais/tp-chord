import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @note 9/04/2015 
 * Pour régler l'exception java.lang.ClassNotFoundException: NoeudInterface :
 * vérifier qu'on lance bien `rmiregistry &` depuis le dossier bin
 * 
 * Un noeud a l'intervalle de clés ]pred.clé, clé]
 * 
 * @author maelv
 *
 */
public class Noeud implements NoeudInterface {
	private int cle; // L'idChord (= cleDebut) du noeud-serveur
	private NoeudInterface moi;
	private NoeudInterface suivant;
	private NoeudInterface precedent;
	private List<NoeudInterface> finger;
	private String idRMI; // Juste pour affichage
	private final static int PUISSANCE_2 = 8; // définit NB_CLES et CLE_MAX
	private final static int NB_CLES = 1 << PUISSANCE_2;
	private final static int CLE_MAX = NB_CLES - 1; // de 0 à NB_CLES-1
	private final int TAILLE_FINGER_TABLE = PUISSANCE_2 - 1;
	private HashMap<Integer,Integer> donnees = new HashMap<Integer, Integer>(); 
	// NOTE: donnees[uneCleChord - this.cleMin] 

	private void print(String s) {
		System.out.println(this.idRMI+" ("+this.cle+"): "+s);
	}

	@Override
	public boolean supprimerNoeud(int cle) throws RemoteException {
		// Si (c'est pas dans mon intervalle OU que clé = ma clé)
		// ET que mon précédent != clé
		if((!dansIntervalle(cle) || cle == this.getIdChord())
				&& cle!=this.precedent.getIdChord()) {
			print("supprimerNoeud("+cle+"): pas dans mon intervalle "
					+this.intervalle()+" -> suivant");
			return this.suivant.supprimerNoeud(cle);
		} else if(this.precedent.getIdChord()==cle){ // Ce noeud s'occupe de la suppression du noeud précédent "cle" 
			print("supprimerNoeud("+cle+"): dans mon intervalle "+this.intervalle());
			HashMap<Integer,Integer> sous_ensemble_table = new HashMap<Integer,Integer>();
			sous_ensemble_table =  
					this.precedent.recupererDonneesIntervalle(this.precedent.getCleDebut(), 
							this.precedent.getCleFin());
			this.donnees.putAll(sous_ensemble_table);

			this.precedent.getNoeudPrecedent().setNoeudSuivant(moi);
			this.precedent = this.precedent.getNoeudPrecedent();
			demanderTousLesNoeudsMajFingerTable(this.moi);
			return true;
		} else {
			print("supprimerNoeud("+cle+"): ce noeud n'existe pas");
			return false;
		}
	}

	@Override
	public Integer getSimple(int cle) throws RemoteException {
		if(!dansIntervalle(cle)) {
			print("get(): "+cle+" pas dans mon intervalle "
					+this.intervalle()+" -> suivant");
			return this.suivant.getSimple(cle);
		}
		print("get(): "+cle+" dans mon intervalle "+this.intervalle());
		Integer res = donnees.get(cle);
		if(res == null) {
			print("get(): pourtant, "+cle+" n'est pas dans ma hashtable!");
			print("get(): ma table est " + donnees.toString());
		}
		return donnees.get(cle);
	}
	
	@Override
	public Integer getAvecFingerTable(int cle) throws RemoteException {
		return this.chercherSuivantAvecFingerTable(cle).getSimple(cle);
	}

	@Override
	public Integer putSimple(int cle, int donnee) throws RemoteException {
		if(!dansIntervalle(cle)) {
			print("put(): "+cle+" n'est pas dans mon intervalle ("
					+this.intervalle()+"), je passe au suivant");
			return this.suivant.putSimple(cle,donnee);
		}
		print("put(): "+cle+" est dans mon intervalle "+this.intervalle());
		return this.donnees.put(cle, donnee);
	}
	
	@Override
	public Integer putAvecFingerTable(int cle, int donnee) throws RemoteException {
		return this.chercherSuivantAvecFingerTable(cle).putSimple(cle, donnee);
	}

	/**
	 * Lorsqu'on dit qu'une clé est dans l'intervalle du noeud,
	 * c'est que la clé appartient à ]clé-prédécesseur-fin, ma-clé-début]
	 * @param cle
	 * @return
	 * @throws RemoteException 
	 */
	private boolean dansIntervalle(int cle) throws RemoteException  {
		// ]clé-prédécesseur-fin, ma-clé-début]
		if (cle<0 || cle > CLE_MAX) {
			throw new RemoteException("ATTENTION: clé "+cle+" pas dans [0,"+CLE_MAX+"]");
		}
		// intervalle de la forme ]4,500]
		if(this.getCleDebut() <= this.getCleFin()) {
			return this.getCleDebut() <= cle && cle <= this.getCleFin();
		} else { // intervalle de la forme [500,4[ == ]4,max] union [0,500]
			return (this.getCleDebut() <= cle && cle <= Noeud.CLE_MAX)
					|| (0 <= cle && cle <= this.getCleFin());
		}
	}

	private String intervalle() throws RemoteException {
		if(this.getCleDebut() <= this.getCleFin()) {
			return "["+this.getCleDebut()+","+this.getCleFin()+"]";
		} else { // intervalle de la forme [500,4[ == ]4,max] ou [0,500]
			return "["+this.getCleDebut()+","+(Noeud.CLE_MAX)+"]"
			+ "∪[0,"+this.getCleFin()+"]";
		}
	}
	private String fingerTable() throws RemoteException {
		String res = "";
		for (int i = 1; i <= TAILLE_FINGER_TABLE; i++) {
			res += "("+this.finger_start(i)+","
					+this.finger_node(i).getIdChord()+"), ";
		}
		return res;
	}
	
	@Override
	public NoeudInterface chercherSuivantAvecFingerTable(int cle) 
			throws RemoteException {
		// Si dans mon intervalle
		if(dansIntervalle(cle)) {
			return(this.moi);
		}
		for (int i = 1; i < TAILLE_FINGER_TABLE; i++) {
			if(dansIntervalle(cle, finger_start(i), finger_start(i+1))
					&& cle != finger_start(i+1))
				return finger_node(i);
		}
		return finger_node(TAILLE_FINGER_TABLE);
	}

	@Override
	public NoeudInterface chercherSuivantSimple(int cle) 
			throws RemoteException {
		// clé est-elle dans mon intervalle ?
		if(this.dansIntervalle(cle)) {
			print("chercherSuivant("+Integer.toString(cle)+"): "
					+ "dans mon intervalle "+this.intervalle()+"");
			return this;
		} else {
			print("chercherSuivant("+Integer.toString(cle)+"): "
					+ "pas dans mon intervalle"
					+ this.intervalle()+" -> suivant");
			return suivant.chercherSuivantSimple(cle);
		}
	}

	@Override
	public HashMap<Integer,Integer> recupererDonneesIntervalle(int cleDebut, int cleFin)
			throws RemoteException {
		if(!dansIntervalle(cleDebut) || !dansIntervalle(cleFin)) {
			System.err.println("recupererDonneesIntervalle("+cleDebut+","+cleFin+"): "
					+ "en dehors de mon intervalle "+this.intervalle());
			return null;
		}
		HashMap<Integer,Integer> sous_ensemble_table = new HashMap<Integer,Integer>();
		for (int cle = cleDebut; cle != modulo(cleFin+1); cle=modulo(cle+1)) {
			sous_ensemble_table.put(cle, this.donnees.get(cle));
		}
		return sous_ensemble_table;
	}

	@Override
	/**
	 * C'est le suivant du noeud qui valide tout ça
	 */
	public void validerAjoutNoeud(NoeudInterface noeud) throws RemoteException {
		// On supprime les données dans l'intervalle qui est 
		// maintenant pris en charge par idChordAjoute 
		// càd sur l'intervalle ]this.pred.cle, noeud.cle]
		for (int cle = this.getCleDebut(); cle != modulo(noeud.getCleFin()+1); cle=modulo(cle+1)) {
			donnees.remove(cle);
		}
		print("validerAjoutNoeud(): Clés de "+this.getCleDebut()+" à "+noeud.getCleFin()+" retirées");
		precedent.setNoeudSuivant(noeud);
		precedent = noeud;
		print("validerAjoutNoeud(): noeud "+noeud.getIdChord()+" bien ajouté");
		demanderTousLesNoeudsMajFingerTable(this.moi);
	}
	
	@Override
	public void demanderTousLesNoeudsMajFingerTable(NoeudInterface depart) throws RemoteException {
		if(this.moi != depart) {
			this.suivant.demanderTousLesNoeudsMajFingerTable(depart);
		} else {
			
		}
		mettreAJourMaFingerTable();
	}

	@Override
	public int getIdChord() throws RemoteException {
		return this.cle;
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
		this.cle = idChord;
		this.idRMI = idRMI;
	}
	private void ajouterAuRMIRegistry() throws RemoteException {
		int port = (int)(Math.random()*(1<<16))%(1<<16 - 5000) + 4000; // Port dans [61535,65535]
		NoeudInterface stub = (NoeudInterface) UnicastRemoteObject.exportObject(this, port);
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind(this.idRMI, stub);
		System.out.println("Le noeud d'identifiant RMI '"+this.idRMI+"'"
				+" a été ajouté au rmiregistry");
		this.moi = stub;
	}
	
	@Override
	public int getCleDebut() throws RemoteException {
		return (this.precedent.getIdChord()+1)%NB_CLES;
	}

	@Override
	public int getCleFin() throws RemoteException {
		return this.cle;
	}

	/**
	 * Méthode réservée au noeud-client
	 * @param pointEntreeRMI (si null, c'est le premier noeud inséré)
	 */
	private boolean ajoutChord(String pointEntreeRMI) {
		try {
			if(pointEntreeRMI == null) { // Cas à un seul noeud
				this.suivant = this.precedent = this.moi;
				initialiserFingerTable();
			} else { // Cas à >= 2 noeuds
				// On récupère le noeud-serveur en question
				Registry registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
				NoeudInterface pointEntree = (NoeudInterface) registry.lookup(pointEntreeRMI);
				NoeudInterface suiv = pointEntree.chercherSuivantAvecFingerTable(this.cle);
				if(suiv == pointEntree) {
					System.err.println("ajoutChord(): l'identifiant CHORD (= clé) "
							+Integer.toString(this.cle)+" est déjà utilisé");
					return false;
				}
				this.suivant = suiv;
				this.precedent = suiv.getNoeudPrecedent();
				this.donnees = suiv.recupererDonneesIntervalle(this.getCleDebut(),this.getCleFin());
				if(this.donnees != null) {
					print("ajoutChord(): la table récupérée est de taille "+this.donnees.size());
					initialiserFingerTable();
					suiv.validerAjoutNoeud(this.moi);
					print("ajoutChord(): noeud "+this.cle+" bien ajouté"
							+" avec intervalle "+this.intervalle()+"");
				} else {
					System.err.println("ajoutChord(): noeud non ajouté");
				}
			}
		} catch (RemoteException e) {
			System.err.println("ajoutChord(): erreur de type remote exception");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println("ajoutChord(): l'id RMI '"+
					pointEntreeRMI+"' donné comme point d'entrée n'existe pas");
			return false;
		}

		return true;
	}

	private static int modulo(int val) {
		return (val%NB_CLES+NB_CLES)%NB_CLES;
	}
	
	/**
	 * Note : je me suis inspiré de l'article "Chord: A Scalable 
	 * Peer-to-peer Lookup Service for Internet Applications" 
	 * (Ion Stoica et al.) pour écrire les algorithmes de la 
	 * finger table.
	 * ATTENTION : ma finger table est dans [1, PUISSANCE_2]
	 */
	private int finger_start(int i) {
		return modulo(this.cle + (1<<(i-1))); 
	}
	private NoeudInterface finger_node(int i) {
		return finger.get(i-1); // XXX
	}
	private void set_finger_node(int i, NoeudInterface n) {
		finger.set(i-1, n);
	}
	/**
	 * Vérfie si val appartient à [debut,fin]
	 * @param val
	 * @param debut
	 * @param fin
	 * @return
	 */
	private static boolean dansIntervalle(int val, int debut, int fin) {
		if(debut < fin)
			return debut <= val && val <= fin;
		else
			return (debut <= val && val <= CLE_MAX) 
					|| (0 <= val && val <= fin);
	}

	private void mettreAJourMaFingerTable() throws RemoteException {
		if(this.moi == this.suivant || this.moi == this.precedent) {
			
		} else {
			for (int i = 1; i <= TAILLE_FINGER_TABLE; i++) {
				set_finger_node(i, this.suivant.chercherSuivantSimple(finger_start(i)));
			}
		}
		print("mettreAJourMaFingerTable(): ma nouv table est \n"+fingerTable());
	}

	private void initialiserFingerTable() {
		finger = new ArrayList<NoeudInterface>();
		for (int i = 1; i <= TAILLE_FINGER_TABLE; i++) {
			finger.add(this.moi);
		}
	}

	public static void main(String[] args) {
		if (args.length != 3 && args.length != 2) {
			System.err.println("Usage: chord id_chord_nouv_noeud id_RMI_nouv_noeud [id_RMI_point_entree]");
			System.err.println("Note: si id_RMI_point_entree n'est pas donné, c'est le premier noeud");
		}
		try {
			Noeud noeud = new Noeud(Integer.parseInt(args[0]),args[1]);
			noeud.ajouterAuRMIRegistry();
			if(args.length == 2) { // Cas où c'est le premier noeud inséré
				noeud.ajoutChord(null);
				for (int i = 0, cle = noeud.cle; i < NB_CLES; i++,cle=modulo(cle+1)) {
					noeud.donnees.put(cle,cle); // données arbitraires
				}
				System.out.println("ajoutChord(): premier noeud "+noeud.cle 
						+" ajouté. Intervalle: "+noeud.intervalle()+"");
			} else { // Cas où ce noeud s'insère à d'autres noeuds
				if(!noeud.ajoutChord(args[2])) {
					return; // Si on a pas réussi à insérer le noeud dans l'anneau
				}
			}
		} catch (RemoteException e) {
			System.err.println("Erreur lors du lien avec rmiregistry pour le remote object Noeud");
			e.printStackTrace();
		}
	}


}
