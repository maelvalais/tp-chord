
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * @note Pour faire une requête sur la table de hashage distribuée,
 * il faut être soi-même un noeud ET s'être ajouté à l'anneau logique
 * à partir d'un autre noeud. 
 * 
 * @note Ici, on utilise une "astuce" pour permettre à quelqu'un extérieur
 * à l'anneau d'utiliser la table de hashage : on va instancier Noeud
 * mais sans l'ajouter à l'anneau, et on va utiliser ses fonctions 
 * update et get comme s'il était à l'intérieur
 * 
 * @note Pour s'insérer dans l'anneau, il faut au moins connaitre  
 * l'adresse/identifiant RMI d'un autre noeud.
 * @note La table de hashage distribuée 
 * @author maelv
 *
 */
public interface NoeudInterface extends Remote {
	/* ATTENTION : il faut bien distinguer l'identifiant RMI,
	 * par exemple "N1" (== 192.168.0.78) et l'identifiant au
	 * sens CHORD, qui correspond à un nombre dans l'espace
	 * des clés (par exemple 15 avec un espace de clés de 0 à 127)
	 * 
	 * domaine(idChord) == domaine(cle)
	 */

	/**
	 * 
	 * @return
	 * @throws RemoteException
	 */
	boolean supprimerNoeud(int idChord) throws RemoteException;
	
	/**
	 * On veut récupérer la "donnée" à partir de son "hash" (identifiant) dans
	 * la table de hashage distribuée CHORD.
	 * Dans un cas normal, on devra prendre cet identifiant et lui appliquer
	 * une fonction de hashage pour retomber dans l'espace des clés. Mais ici,
	 * l'espace des clés = l'espace des "hashs"/identifiants.
	 * @note Ici, la donnée correspond à un entier pour simplifier 
	 * les échange RMI
	 * à partir  
	 * @param cle La clé de 
	 * @return -1 si erreur, un idChord sinon
	 * @throws RemoteException
	 */
	Integer get(int cle) throws RemoteException;
	
	/**
	 * On veut modifier la "donnée" à partir de son "hash" (identifiant)
	 * dans la table de hashage distribuée CHORD.
	 * @param cle
	 * @param donnee
	 * @return null si la clé n'existait pas déjà, un Integer sinon
	 * @throws RemoteException
	 */
	Integer put(int cle, int donnee) throws RemoteException;

	/*
	 * Toutes les fonctions suivantes sont à utiliser dans le contexte "pair à pair"
	 * lorsque A veut s'ajouter à l'anneau CHORD et que B est le prédecesseur de A.
	 * Donc A discute directement avec B pour s'ajouter.
	 */
	
	/**
	 * Cette méthode, utilisée par un noeud-client A, est exécutée par un premier
	 * noeud-serveur B que A connait. B vérifie si l'idChord de A appartient à son
	 * propre intervalle de clés (intervalle de clés == intervalle d'idChord). 
	 * - si idChord(A) est dans son intervalle, B renvoit son propre "stub" NoeudInterface.	 
	 * - sinon, B demande à suiv(B) en appelant cette même méthode
	 * @param cle du nouveau noeud à placer
	 * @return le noeud-serveur qui correspond au prédécesseur de idChordAppelant,
	 * ou null si idChordAppelant est déjà utilisé
	 * @throws RemoteException
	 */
	NoeudInterface chercherSuivant(int cle) throws RemoteException;
		
	/**
	 * Récupère les données de l'intervalle [cleDebut, cleFin].
	 * @param cleDebut
	 * @param cleFin
	 * @return
	 * @throws RemoteException
	 */
	HashMap<Integer,Integer> recupererDonneesIntervalle(int cleDebut, int cleFin) throws RemoteException;
	/**
	 * Le noeud-serveur B qui reçoit cette requête depuis le noeud-client A va
	 * valider l'insertion de A dans l'anneau en modifiant son propre intervalle
	 * de clés (qui va diminuer à cause de A, donc) et va supprimer les données en
	 * trop qu'il a déjà donné à A.
	 * @note c'est le suivant de leNoeudAjoute qui s'occupe de tout ça
	 * @throws RemoteException
	 */
	void validerAjoutNoeud(NoeudInterface leNoeudAjoute) throws RemoteException;
	
	int getIdChord() throws RemoteException;
	int getCleDebut() throws RemoteException;
	int getCleFin() throws RemoteException;
	void setNoeudSuivant(NoeudInterface noeudSuivant) throws RemoteException;
	void setNoeudPrecedent(NoeudInterface noeudPrecedent) throws RemoteException;
	NoeudInterface getNoeudSuivant() throws RemoteException;
	NoeudInterface getNoeudPrecedent() throws RemoteException;
	
}
