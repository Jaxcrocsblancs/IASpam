package model;

import java.io.*;
import java.util.ArrayList;

/**
 * Classe principale qui va gérer l'apprentissage et le test
 */
public class Main implements Serializable {


	/** Le dictionnaire (tableau de String contenant 10000 mot anglais) **/
	private String[] tabWord;

	/** Le chemin du dictionnaire contenant **/
	private String pathDico = "dictionnaire1000en.txt";

	/** Le chemin des spams de la base d'apprentissage **/
	private String pathSpam = "baseapp/spam/";

	/** Le chemin des hams de la base d'apprentissage **/
	private String pathHam  = "baseapp/ham/";

	/** Le chemin des spams de la base de test **/
	private String pathSpamT = "basetest/spam/";

	/** Le chemin des hams de la base de test **/
	private String pathHamT  = "basetest/ham/";

	/** Le compteur utilisé par un mail pour chaque mot du dictionnaire dans les spams **/
	private int[] nbMotTousLesSpam;

	/** Le compteur utilisé par un mail pour chaque mot du dictionnaire dans les hams **/
	private int[] nbMotTousLesHam;

	/** La probabilité que le mot en question soit un mot plutot utilisé dans les spams **/
	private double[] probaMotSpam;

	/** La probabilité que le mot en question soit un mot plutot utilisé dans les hams **/
	private double[] probaMotHam;

	/** La probabilité initiale que un mail donné soit un spam (à priori tel un classifieur simpliste (comme ZeroR)) **/
	private double probaSpam;

	/** La probabilité initiale que un mail donné soit un ham (à priori tel un classifieur simpliste (comme ZeroR)) **/
	private double probaHam;

	/** Hyper paramètre du classifieur **/
	private  static int epsilon=1;

	/**
	 * Fonction principale faisant un apprentissage et un test tout de suite après
	 */
	public Main(){
		charger_dictionnaire();
		/*apprentissage(nbSpam, 2499);

		if (testNb == -1) {
			test(nbSpam,nbHam);
		}*/





		//boolean[] nb = lire_message("baseapp/ham/0.txt");
		/*System.out.println("fin1");
		System.out.println(probaMotSpam.length);
		for(int i = 0; i<probaMotSpam.length; i++){
			System.out.print(probaMotSpam[i]+" ");
			System.out.println(probaMotHam[i]+" ");
		}*/
	}
	//

	/**
	 * Cette fonction charge un dictionnaire dans tabword
	 */
	public void charger_dictionnaire ( ){
		try{
			ArrayList<String> listWord = new ArrayList<String>(); 
			InputStream flux=new FileInputStream(new File(pathDico)); 
			InputStreamReader lecture=new InputStreamReader(flux);
			BufferedReader buff=new BufferedReader(lecture);
			String ligne;
			while ((ligne=buff.readLine())!=null){
				if(ligne.length()>3){
					listWord.add(ligne);
				}
			}
			tabWord = new String[listWord.size()];
			listWord.toArray(tabWord);
			
			buff.close(); 
			}		
			catch (Exception e){
			System.out.println(e.toString());
			}
		
		System.out.println("Dictionnaire chargé");
	}

	/**
	 * Cette fonction lit un message et retourne un tableau correspondant à la présence des mots dans le dictionnaire
	 * @param path Un string qui contient le chemin d'accès vers le message
	 * @return Un tableau booléen de la meme taille du dictionnaire corrsepondant à la présence des mots dans le message
	 */
	private boolean[] lire_message(String path){
		boolean[] nb = new boolean[tabWord.length];
		try{
			InputStream flux=new FileInputStream(new File(path)); 
			InputStreamReader lecture=new InputStreamReader(flux);
			BufferedReader buff=new BufferedReader(lecture);
			String ligne;	
			
			while ((ligne=buff.readLine())!=null){
				
				int index = 1;
				int MemIndex = 0;
				//lecture de toute la ligne
				while(ligne.length()>index){
					//arret si ce n'est pas une lettre (chiffre ponctuation espace symbole autre)
					while(ligne.length()>index && ((ligne.charAt(index)>=65 && ligne.charAt(index)<=90) || (ligne.charAt(index)>=97 && ligne.charAt(index)<=122))){
						index++;
					}
					//Test si mots dans dico
					String mot = ligne.substring(MemIndex, index);
					//System.out.println(mot);
					for(int i=0; i<tabWord.length; i++){
						if(mot.equalsIgnoreCase(tabWord[i])){
							nb[i]=true;
						}
					}
					MemIndex = index+1;
					index++;
				}				
			}
			buff.close(); 
			}		
			catch (Exception e){
			System.out.println(e.toString());
			}
		return nb;
	}


	/**
	 * Cette fonction réalise l'apprentissage pour un nombre de spam et de ham défini
	 * @param nbSpam Le nombre de spam
	 * @param nbHam Le nombre de ham
	 */
	void apprentissage(int nbSpam, int nbHam){
		System.out.println("Apprentissage...");
		
		//On initialise les tableaux avec la taille du dictionnaire
		nbMotTousLesSpam=new int[this.tabWord.length];
		nbMotTousLesHam=new int[this.tabWord.length];

		probaMotSpam=new double[this.tabWord.length];
		probaMotHam=new double[this.tabWord.length];


		//On parcourt tous les spam
		//On compte pour chaque mot du dictionnaire combien de fois il apparait dans tous les spams
		for (int i=0;i<nbSpam;i++) {
			boolean[] presence=lire_message(pathSpam+i+".txt");
			for(int j=0;j<tabWord.length;j++) {
				//Si le mot est dans le dictionnaire alors on augmente le compteur de spam pour ce mot
				if (presence[j]) {
				 	nbMotTousLesSpam[j]++;
				 }
			}
		}

		//On compte pour chaque mot du dictionnaire combien de fois il apparait dans tous les ham
		for (int i=0;i<nbHam;i++) {
			
			boolean[] presence=lire_message(pathHam+i+".txt");
			for(int j=0;j<tabWord.length;j++) {
				//Si le mot est dans le dictionnaire alors on augmente le compteur de ham pour ce mot
				if (presence[j]) {
					nbMotTousLesHam[j]++;
				}
			}
		}

		//On calcule les probabilités pour chaque mot spam
		for (int i=0; i<tabWord.length; i++) {
			probaMotSpam[i] = (1.0*nbMotTousLesSpam[i]+epsilon)/(nbSpam+2*epsilon);
		}

		//On calcule les probabilités pour chaque mot ham
		for (int i=0; i<tabWord.length; i++) {
			probaMotHam[i] = (1.0*nbMotTousLesHam[i]+epsilon)/(nbHam+2*epsilon);
		}
		this.probaSpam = ((double)nbSpam) / ((double)(nbHam + nbSpam));
		this.probaHam = 1. - probaSpam;
		System.out.println("fin apprentissage");
	}

	/**
	 * Fonction qui va permmettre grace aux probabilités conditionnelles de déterminer si un mail est probablement un spam ou non
	 * @param tabPresence Un tableau booléen indiquant la présence de chaque mot
	 * @return Un booléen retournant vrai si c'est on identifie le message comme un spam et faux sinon
	 */
	boolean identification(boolean[] tabPresence){
		// Calcul des probabilités a posteriori
		//Calcul P(Y=SPAM|X=x) 
		double PSpam=0;
		//1/P(X=x)
		PSpam = Math.log(1/probaSpam) ;
		for(int i=0;i<tabPresence.length;i++){
			//P(Y=SPAM)∏dj=1(bjSPAM)xj(1−bjSPAM)1−xj
			if(tabPresence[i]){
				PSpam+=Math.log(probaMotSpam[i]);
			}else{
				PSpam+=Math.log(1.-probaMotSpam[i]);
			}
		}
		
		
		//Calcul P(Y=HAM|X=x) 
		double PHam=0;
		//1/P(X=x)
		PHam=Math.log(1/probaHam);
		for(int i=0;i<tabPresence.length;i++){
			//P(Y=HAM)∏dj=1(bjHAM)xj(1−bjHAM)1−xj
			if(tabPresence[i]){
				PHam+=Math.log(probaMotHam[i]);
			}else{
				PHam+=Math.log(1.-probaMotHam[i]);
			}
		}
		if(PSpam<PHam){
			return false;
		}else{
			return true;
		}
	}

	/**
	 * Fonction qui réalise et affiche le test sur un nombre de ham et spam déterminé
	 * @param nbSpam Le nombre de spam
	 * @param nbHam Le nombre de ham
	 */
	void test(int nbSpam, int nbHam){
		System.out.println("TEST :");
		//TEST des SPAM
		int erreurSpam=0;
		boolean[] tabPresence;
		for(int i=0;i<nbSpam;i++){
			tabPresence = lire_message(pathSpamT+i+".txt");
			if(identification(tabPresence)){
				System.out.println("SPAM numéro "+i+" identifié comme un SPAM");
			}else{
				System.out.println("SPAM numéro "+i+" identifié comme un HAM  ***erreur***");
				erreurSpam++;
			}
		}
		//TEST des HAM
		int erreurHam=0;
		
		for(int i=0;i<nbHam;i++){
			tabPresence = lire_message(pathHamT+i+".txt");
			if(identification(tabPresence)){
				System.out.println("HAM numéro "+i+" identifié comme un SPAM ***erreur***");
				erreurHam++;
			}else{
				System.out.println("HAM numéro "+i+" identifié comme un HAM");
			}
		}
		int nbSpamHamTotal=nbSpam+1+nbHam+1;
		int errTotal=erreurHam+erreurSpam;
		
		System.out.println("Erreurs de test sur les "+(nbSpam+1)+" SPAM : "+((double)erreurSpam/nbSpam)*100);
		System.out.println("Erreurs de test sur les "+(nbHam+1)+" HAM : "+((double)erreurHam/nbHam)*100);
		System.out.println("Erreurs de test globals sur "+nbSpamHamTotal+" mails : "+((double)errTotal/nbSpamHamTotal)*100);
		
	}

	/**
	 * Fonction pricncipale qui crée un Main (effectue apprentissage + test)
	 * @param args Un tableau de String correspondant aux paramètres de la ligne de commande
	 */
	public static void main(String[] args) {
		System.out.println("debut");

		boolean sauvegarde=false;
		boolean chargement=false;

		String nomClassifieurSauvegarde="";
		String nomClassifieurChargement="";

		//499 par défaut
		int nbSpam=499;
		int nbHam=499;

		//int testNumber=-1;

		Main classifieur=null;

		for (int i=0;i<args.length;i++) {
			if (args[i].equals("-save") && (args.length > i+1)) {
				sauvegarde=true;
				nomClassifieurSauvegarde=args[i+1];
			} else if (args[i].equals("-load") && (args.length > i+1)) {
				chargement=true;
				nomClassifieurChargement=args[i+1];
			} else if (args[i].equals("-nbSpam") && (args.length > i+1)) {
				nbSpam=Integer.parseInt(args[i+1]);
			} else if (args[i].equals("-nbHam") && (args.length > i+1)) {
				nbHam=Integer.parseInt(args[i+1]);
			} /*else if (args[i].equals("-testNb") && (args.length > i+1)) {
				testNumber=Integer.parseInt(args[i+1]);
			}*/
		}

		if (sauvegarde) {
			classifieur=new Main();
			classifieur.apprentissage(nbSpam, nbHam);
			sauvegarde(classifieur,nomClassifieurSauvegarde);
			//classifieur.test(nbSpam,nbHam);
		} else if (chargement) {
			classifieur=chargement(nomClassifieurChargement);
			classifieur.test(nbSpam,nbHam);
		}

		System.out.println("fin");
	}

	/**
	 * Sauvegarde un classifieur dans un fichier
	 * @param classifieur Le classifieur à sauvegarder
	 * @param nomClassifieur Le nom du fichier utilisé pour la sauvegarde
	 */
	public static void sauvegarde(Main classifieur, String nomClassifieur) {
		try {
			FileOutputStream fichierSortie = new FileOutputStream(nomClassifieur);
			ObjectOutputStream oos = new ObjectOutputStream(fichierSortie);
			oos.writeObject(classifieur);
			oos.close();
			fichierSortie.close();
			System.out.println("Le classififeur a été sauvegardé");
		} catch (IOException i) {
			i.printStackTrace();
			System.out.println("Il y a eu un problème lors de la sauvegarde");
		}
	}

	/**
	 * Charge un classifieur dans un fichier
	 * @param nomClassifieur Le classifieur à sauvegarder
	 * @return Le classifieur de type Main
	 */
	public  static Main chargement(String nomClassifieur) {
		Main classifieur = null;
		try {
			FileInputStream fichierEntree = new FileInputStream(nomClassifieur);
			ObjectInputStream ois = new ObjectInputStream(fichierEntree);
			classifieur = (Main) ois.readObject();
			ois.close();
			fichierEntree.close();
		} catch (Exception e) {
			System.out.println("Erreur lors du chargement");
			return null;
		}
		return classifieur;
	}
}
