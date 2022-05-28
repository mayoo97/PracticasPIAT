package piat.opendatasearch;

/**
 * @author Adrián Mayo Barrios
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class salidaXML {

	//Cadena de caracteres que representa el inicio del documento XML, incluido el summary
	private String sXMLPatternIntro = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"
			+ "<searchResults" + "\t" + "xmlns=\"http://www.piat.dte.upm.es/ResultadosBusquedaP3.xsd\" \r\n"
			+ "\t\t\t\t" + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \r\n"
			+ "\t\t\t\t" + "xsi:schemaLocation=\"http://www.piat.dte.upm.es/practica3 ResultadosBusquedaP3.xsd \" >" + "\n" 
			+ "<summary>" + "\n" + "\t"  
			+ "<query>" + "#QUERY#" + "</query>" + "\n" +"\t" 
			+ "<numConcepts>" + "#NUMCONCEPTS#" + "</numConcepts>" + "\n" + "\t" 
			+ "<numDatasets>" + "#NUMDATASETS#" + "</numDatasets>" + "\n" 
			+ "</summary>" + "\n";
	
	//Cadena de caracteres que representa a los results (va justo a continuación de la anterior) include los concepts, lo datasets y los resources
	private String sXMLPatternResults =  "<results>" + "\n" + "\t" 
			+ "<concepts>" + "\n" + "#CONCEPTS#" + "\t"
			+ "</concepts>" + "\n" + "\t"
			+ "<datasets>" + "\n" + "#DATASETS#" + "\t"
			+ "</datasets>" + "\n"
			+ "</results>" + "\n"
			+ "</searchResults>";
	
	//Patrón para el concept, el cual tiene id pero no contenido
	private String sXMLPatternConcept= "\t" + "<concept id  = \"" + "#IDCONCEPT#" + "\"/>" + "\n";
	
	//Patrón para los datasets, los cuales tienen id pero y contenido
	private String sXMLPatternDataset= "\t\t" + "<dataset id = \"" + "#IDDATASET#" + "\">" + "\n"
			+ "#CONTENIDODATASET#" + "\t\t" 
			+ "</dataset>" + "\n";
	
	//Patrón para un elemento cualquiera que no tiene id, solo contenido
	private String sXMLPaternElemento = "\t<#ELEMENTO#>" + "#CONTENIDOELEMENTO#" + "</#ELEMENTO#>\n";
	
	
	/** conceptsOutput()
	 * pasa la información de los concepts de formato lista a formato texto (xml) 
	 * @param lConcepts	lista de concepts a imprimir
	 * @return devuelve la lista de concepts pasada a xml en una String
	 */
	public String conceptsOutput (List<String> lConcepts) {

		StringBuilder sb = new StringBuilder();
	
		for (String unConcepto : lConcepts){
			sb.append ("\t" + sXMLPatternConcept.replace("#IDCONCEPT#", unConcepto)); //Con el append voy concatenando información
		}
	
		String conceptsStringSalida = sb.toString();
	
		return conceptsStringSalida;
	
	}
	
	
	/** datasetsOutput()
	 * pasa la información de los datasets de formato mapa a formato texto (xml) 
	 * @param mapaDatasets	mapas con la información del datasets
	 * @return devuelve información de los datasets pasada a xml en una String
	 */
	public String datasetsOutput (Map <String, HashMap<String,String>> mapaDatasets) {

		StringBuilder sbTotal= new StringBuilder();
		StringBuilder sbUnDataset = new StringBuilder();
		String contenidoDataset;
		
		for (String idDataset : mapaDatasets.keySet()){// por cada dataset
				
			/////  TITLE  ////
			if (mapaDatasets.get(idDataset).containsKey("title")) {

				sbUnDataset.append("\t\t" + sXMLPaternElemento.replace("#ELEMENTO#", "title").replace ("#CONTENIDOELEMENTO#", mapaDatasets.get(idDataset).get("title")));
					
			}else { //Como es un campo obligatorio si no viene le pongo espacio
					
				sbUnDataset.append( "\t\t" + sXMLPaternElemento.replace("#ELEMENTO#", "title").replace ("#CONTENIDOELEMENTO#", " "));
					
			}
				
			/////  DESCRIPTION  ////
			if (mapaDatasets.get(idDataset).containsKey("description")) {
				
				sbUnDataset.append( "\t\t" + sXMLPaternElemento.replace("#ELEMENTO#", "description").replace ("#CONTENIDOELEMENTO#", mapaDatasets.get(idDataset).get("description")));
					
			}
				
			/////  THEME  ////
			if (mapaDatasets.get(idDataset).containsKey("theme")) {
		
				sbUnDataset.append("\t\t" + sXMLPaternElemento.replace("#ELEMENTO#", "theme").replace ("#CONTENIDOELEMENTO#", mapaDatasets.get(idDataset).get("theme")));
								
			}
				
			contenidoDataset = sbUnDataset.toString();
			
			sbUnDataset.setLength(0); //Reseteo cada vez que recorro un dataset, para pasar al siguiente

			sbTotal.append(sXMLPatternDataset.replace("#IDDATASET#", idDataset).replace("#CONTENIDODATASET#", contenidoDataset)); //aqui voy concatenando toda la información de todos los datasets.
			
		}
	
		String  datasetsStringSalida = sbTotal.toString();
	
		return datasetsStringSalida;
	
	}
	
	
	/** XMLOutputString()
	 * pasa la información de todo excepto los resources concatenada a formato texto (xml) 
	 * @param mapaDatasets	mapas con la información del datasets
	 * @param lConcepts	lista de concepts a imprimir
	 * @param codigo código de la categoría de la que se está sacando la información
	 * @param mapaResources	mapas con la información de los resources
	 * @return devuelve información de los resurces pasada a xml en una String
	 */
	public String XMLOutputString (Map <String, HashMap<String,String>> mapaDatasets, List<String> lConcepts, String codigo, String contenidoResources) {

		StringBuilder sbSalida = new StringBuilder();
		
		sbSalida.append ( sXMLPatternIntro.replace( "#QUERY#", codigo ).replace( "#NUMCONCEPTS#", String.valueOf(lConcepts.size()) ).replace("#NUMDATASETS#", String.valueOf(mapaDatasets.size())) );
		sbSalida.append ( sXMLPatternResults.replace( "#CONCEPTS#", conceptsOutput (lConcepts) ).replace( "#DATASETS#", datasetsOutput (mapaDatasets)).replace( "#RESOURCES#", contenidoResources));
		
		String salida = sbSalida.toString();
		
		return salida;
	
	}

}
