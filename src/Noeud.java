import java.rmi.RemoteException;
import java.util.List;


public class Noeud implements NoeudInterface {
	private int idChord; // L'idChord du noeud-serveur
	private int cleMax; // RAPPEL: cleMin = idChord
	// Ainsi, l'intervalle des clés gérées est [idChord,cleMax]
	private String idRMI; // l'idRMI du noeud-serveur
	private String idRMIPrecedent; // idRMI du noeud précédent
	private String idRMISuivant; // idRMI du noeud suivant


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

}
