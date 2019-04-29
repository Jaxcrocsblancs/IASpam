package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Main {
	
	private String[] tabWord;

	private String pathDico = "dictionnaire1000en.txt";
	private String pathSpam = "baseapp/ham/";
	private String pathHam  = "baseapp/spam/";

	
	private int[] nbMotTousLesSpam;
	private int[] nbMotTousLesHam;

	private double[] probaMotSpam;
	private double[] probaMotHam;
	private double probaSpam;
	private double probaHam;
	
	private  static int epsilon=1;
	
	public Main(){
		System.out.println("debut");
		charger_dictionnaire();
		apprentissage(499, 499);
		test(499,499);
		
		
		
		
		//boolean[] nb = lire_message("baseapp/ham/0.txt");
		/*System.out.println("fin1");
		System.out.println(probaMotSpam.length);
		for(int i = 0; i<probaMotSpam.length; i++){
			System.out.print(probaMotSpam[i]+" ");
			System.out.println(probaMotHam[i]+" ");
		}*/
		System.out.println("fin");
	}
	
	// cette fonction doit pouvoir charger un dictionnaire (parexemple dans un tableau de mots) à partir d’un fichier texte
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
		
		System.out.println("Dictionnaire charger");
	}
	
	
	
	/*  cette fonction doit pouvoir lire un message (dans un fichier texte) et le traduire en 
	 une représentation sous forme de vecteur binaire x à partir d’un dictionnaire. */
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
	
	boolean identification(boolean[] tabPresence){
		// Calcul des probabilités a posteriori
		//Calcul P(Y=SPAM|X=x) 
		double PSpam=0;
		//1/P(X=x)
		
		
		PSpam = Math.log(probaSpam) ;
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
		PHam=Math.log(probaHam);
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
	
	
	void test(int nbSpam, int nbHam){
		System.out.println("TEST :");
		//TEST des SPAM
		int erreurSpam=0;
		boolean[] tabPresence;
		for(int i=0;i<nbSpam;i++){
			tabPresence = lire_message(pathSpam+i+".txt");
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
			tabPresence = lire_message(pathHam+i+".txt");
			if(identification(tabPresence)){
				System.out.println("HAM numéro "+i+" identifié comme un SPAM ***erreur***");
				erreurHam++;
			}else{
				System.out.println("HAM numéro "+i+" identifié comme un HAM");
			}
		}
		int nbSpamHamTotal=nbSpam+nbHam;
		int errTotal=erreurHam+erreurSpam;
		
		System.out.println("Erreur de test sur les "+nbSpam+" SPAM : "+((double)erreurSpam/nbSpam)*100);
		System.out.println("Erreur de test sur les "+nbHam+" HAM : "+((double)erreurHam/nbHam)*100);
		System.out.println("Erreur de test globals sur "+nbSpamHamTotal+" mails : "+((double)errTotal/nbSpamHamTotal)*100);
		
	}

	public static void main(String[] args) {
		new Main();
		

	}
}
