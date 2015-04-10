import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


/**
 * Usage: `java NoeudClient N1 get 5`
 * Usage: `java NoeudClient N1 put 5 1234`
 * Cette classe est un "faux Noeud" qui ne s'insère pas dans l'anneau
 * mais envoie des commandes put/get dans l'anneau par l'intermédiaire
 * d'un noeud dont on connait l'identifiant RMI.
 * @author maelv
 *
 */
public class NoeudClient {
	public static void main(String[] args) {
		if(args.length < 2 || args.length > 4) {
			System.err.println("Usage: chord id_RMI_entrée get une_clé");
			System.err.println("Usage: chord id_RMI_entrée put une_clé une_donnée_int");
			System.err.println("Usage: chord id_RMI_entrée suppr idchord_à_supprimer");
			return;
		}

		String pointEntreeRMI = args[0];
		NoeudInterface pointEntree = null;
		try {
			Registry registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
			pointEntree = (NoeudInterface) registry.lookup(args[0]);			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println("L'id RMI '"+pointEntreeRMI+"' "
					+ "donné comme point d'entrée n'existe pas");
			return;
		}

		int donnee, cle;

		switch (args[1]) {
		case "get":
			if(args.length != 3) {
				System.err.println("Usage: chord id_RMI_entrée get une_clé");
				return;
			}
			try {
				cle = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				System.err.println("Usage: chord id_RMI_entrée get une_clé");
				return;
			}
			System.out.print("get("+cle+"): ");
			Integer resultat = null;
			try {
				resultat = pointEntree.get(cle);
			} catch (RemoteException e) {
				System.out.println(e.getMessage());
			}
			if(resultat != null) {
				System.out.println("client: la clé '"+cle+"' est associée à la donnée '"+resultat+"'");
			} else {
				System.out.println("client: la clé '"+cle+"' n'est associée à aucune donnée");
			}
			break;

			
		case "put":
			if(args.length != 4) {
				System.err.println("Usage: chord id_RMI_entrée put une_clé une_donnée_int");
				return;
			}
			try {
				cle = Integer.parseInt(args[2]);
				donnee = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				System.err.println("Usage: chord id_RMI_entrée put une_clé une_donnée_int");
				return;
			}
			System.out.print("put("+cle+","+donnee+"): ");
			Integer retour = null;
			try {
				retour = pointEntree.put(cle,donnee);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(null == retour) {
				System.out.print("client: la valeur '"+donnee+"' a bien "
						+ "été insérée à la clé '"+cle+"'\n");
			} else {
				System.out.print("client: la valeur '"+retour.toString()+"' "
						+ "a été écrasée par votre nouvelle valeur '"
						+donnee+"' avec la clé '"+cle+"'\n");
			}
			break;
		case "suppr":
			if(args.length != 3) {
				System.err.println("Usage: chord id_RMI_entrée suppr idchord_à_supprimer");
				return;
			}
			try {
				cle = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				System.err.println("Usage: chord id_RMI_entrée suppr idchord_à_supprimer");
				return;
			}
			System.out.print("suppr("+cle+"): ");
			boolean ret = false;
			try {
				ret = pointEntree.supprimerNoeud(cle);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(ret == false) {
				System.out.print("client: l'idchord "+cle+" n'a pas pu être supprimé'\n");
			} else {
				System.out.print("client: l'idchord "+cle+" a été supprimé'\n");
			}
			break;
		default:
			break;
		}
	}
}
