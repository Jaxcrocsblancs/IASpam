package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {
	
	private String[] tabWord;

	private String pathDico = "dictionnaire1000en.txt";
	
	private int[] nbMotTousLesSpam;
	private int[] nbMotTousLesHam;

	private double[] probaMotSpam;
	private double[] probaMotHam;

	private  static int epsilon=1;
	
	public Main(){
		System.out.println("debut");
		tabWord = new String[1000];
		
		charger_dictionnaire();
		int[] nb = lire_message("baseapp/spam/0.txt");
		
		for(int i = 0; i<nb.length; i++){
			System.out.print(nb[i]+" ");
		}
		
		
		System.out.println("fin");
	}
	
	// cette fonction doit pouvoir charger un dictionnaire (parexemple dans un tableau de mots) � partir d�un fichier texte
	public void charger_dictionnaire ( ){
		try{
			InputStream flux=new FileInputStream(new File(pathDico)); 
			InputStreamReader lecture=new InputStreamReader(flux);
			BufferedReader buff=new BufferedReader(lecture);
			String ligne;
			int index = 0;
			while ((ligne=buff.readLine())!=null){
				if(ligne.length()>3){
					tabWord[index]= ligne;
					index++;
				}
			}
			buff.close(); 
			}		
			catch (Exception e){
			System.out.println(e.toString());
			}
		
		System.out.println("Dictionnaire charger");
	}
	
	
	
	/*  cette fonction doit pouvoir lire un message (dans un fichier texte) et le traduire en 
	 une repr�sentation sous forme de vecteur binaire x � partir d�un dictionnaire. */
	private int[] lire_message(String path){
		 int[] nb = new int[tabWord.length];
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
					System.out.println(mot);
					for(int i=0; i<tabWord.length; i++){
						if(mot.equalsIgnoreCase(tabWord[i])){
							nb[i]++;
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

		//On initialise les tableaux avec la taille du dictionnaire
		nbMotTousLesSpam=new int[this.tabWord.length];
		nbMotTousLesHam=new int[this.tabWord.length];

		probaMotSpam=new double[this.tabWord.length];
		probaMotHam=new double[this.tabWord.length];

		//Pour tester
		String rootPath="baseapp/";

		String spamPath=rootPath+="spam/";
		String hamPath=rootPath+="ham/";

		//On parcourt tous les spam
		//On compte pour chaque mot du dictionnaire combien de fois il apparait dans tous les spams
		for (int i=0;i<nbSpam;i++) {
			boolean[] presence=lire_message(spamPath+i+".txt");
			for(int j=0;j<tabWord.length;j++) {
				//Si le mot est dans le dictionnaire alors on augmente le compteur de spam pour ce mot
				if (presence[j]) {
				 	nbMotTousLesSpam[j]++;
				 }
			}
		}

		//On compte pour chaque mot du dictionnaire combien de fois il apparait dans tous les ham
		for (int i=0;i<nbHam;i++) {
			boolean[] presence=lire_message(hamPath+i+".txt");
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
	}
	
	void test(){
		
	}
	
	public static void main(String[] args) {
		new Main();
		

	}
}
