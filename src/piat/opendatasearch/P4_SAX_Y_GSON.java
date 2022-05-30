package piat.opendatasearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/**
 * @author Adrián Mayo Barrios - 54241386A
 */
public class P4_SAX_Y_GSON {
	
	static File fileIn;
	static String sCodigoConcept = null;	//Codigo de la categoría (Del concepto)
	static File fileOut;
	
	static String sNombreConcept = null;	//Nombre de la categoría (Del concepto)
	
	static List<String> listaConcepts = new ArrayList<String>();
	static Map <String, HashMap<String,String>> mapaDatasets = null;
	static Map<String, List<Map<String,String>>> mapaDatasetConcepts = null;

	/**
	 * Clase principal de la aplicación de extracción de información del 
	 * Portal de Datos Abiertos del Ayuntamiento de Madrid
	 *
	 *	ARGUMENTOS
	 *
	 * 	args[0] => Ruta al documento catalogo.xml
	 * 	args[1] => Código de la categoría de la que se desea información
	 * 	args[2] => Ruta al documento XML de salida en el que se almacenará el resultado de la búsqueda
	 */
	public static void main(String[] args) {
		
		
		//PASO 1. Verificación y validación de los argumentos de entrada
		vericacionValidacionArgumentos(args);
		
		//PASO 2. Extracción de concepts y datasets
		parserSAX();
		
		//PASO 3. Extracción de resources
		parserJSON_API_GSON();
		
		//PASO 4. Transformación de información y generación del documento XML con los resultados
		generacionXMLSalida();
		
		System.exit(0);
		
	}//cierra main()
	
	
	
	
	/**
	 * Métdodo que verifica y valida los argumentos de entrada al programa
	 * 
	 * @param args Argumentos a verificar y validar
	 */
	private static void vericacionValidacionArgumentos(String[] args) {
		
		Pattern patronExtensionXML = Pattern.compile(".*.xml$");
		Pattern patronCodigoCategoria1 = Pattern.compile("^[\\d]{3,4}-[-0-9A-Z]{3,8}");
		Pattern patronCodigoCategoria2 = Pattern.compile("[\\d]{3,4}");
		
		
		//Verificar si tenemos 3 argumentos de entrada
		if (args.length!=3){
			String mensaje="ERROR: Argumentos incorrectos "+ args.length;
			if (args.length>0)
				mensaje+="\nHe recibido estos argumentos: " + Arrays.asList(args).toString()+"\n";
			mostrarUso(mensaje);
			System.exit(0);
		}		
		
		//Si la entrada o la salida no son ficheros .xml no me interesa
		if ((!patronExtensionXML.matcher(args[0]).find()) || (!patronExtensionXML.matcher(args[2]).find())) {	
			String mensaje="El fichero de entrada o de salida no tienen la extension .xml";
			mostrarUso(mensaje);
			System.exit(0);
		}
		
		//Comprobar el formato de la Categoría que queremos buscar
		if( (!patronCodigoCategoria1.matcher(args[1]).find() ) && (!patronCodigoCategoria2.matcher(args[1]).find()) ){
			String mensaje = "ERROR: el segundo argumento no es un código válido";
			mostrarUso(mensaje);
			System.exit(0);
		}
		
		//Comprobar que el fichero de entrada es un path de fichero que tiene permiso de lectura
		System.out.println("Trato de abrir el archivo " + args[0]);
		File ficheroEntrada = new File(args[0]);
		
		if ( !ficheroEntrada.exists() || !ficheroEntrada.canRead()) {
			
			String mensaje="El fichero de entrada no existe o no se puede leer";
			mostrarUso(mensaje);
			System.exit(0);
		}
		
		//Comprobar que el fichero de salida es un path de fichero que tiene permiso de escritura
		System.out.println("Trato de abrir el archivo " + args[2]);
		File ficheroSalida = new File(args[2]);
		
		if ( !ficheroSalida.exists() || !ficheroSalida.canRead()) {
			
			String mensaje="El fichero de salida no existe o no se puede leer";
			mostrarUso(mensaje);
			System.exit(0);
		}
		
		
		fileIn = new File(args[0]);
		sCodigoConcept = args[1];
		fileOut = new File(args[2]);
		
	}//Cierra vericacion_Y_Validacion()
	
	
	
	
	private static void parserSAX(){
		
		 try {
			 
			//Paso 2.1 - Instanciar un objeto ManejadorXML pasando como parámetro el código de la categoría recibido en el segundo argumento de main()
			ManejadorXML manejador = new ManejadorXML(sCodigoConcept); // Es nuestro manejador. Clase donde voy redefinir mis métodos startElement(), endElement()...

			
			//Paso 2.2 - Instanciar un objeto SAXParser e invocar a su método parse() pasando como parámetro un descriptor de fichero, cuyo nombre se recibió en el primer argumento de main(), y la instancia del objeto ManejadorXML 
			// Creo y configuro la factoría
	    	SAXParserFactory factory = SAXParserFactory.newInstance(); //Invoco a la factoria de parser con el metodo newInstance. NO es una clase, no hago el new, por eso uso el metodo
	    	factory.setNamespaceAware(true); // Configuro la factoria para que trabaje con espacios de nombres (NameSpace)

	   	 	SAXParser saxParser = factory.newSAXParser(); //El metodo newSAXParser de la factoria me genera un objeto de tipo SAXParser (esto me ha evitado hacer aqui el new de SAXParser) 											  
	   	 	saxParser.parse(fileIn, manejador); // Le paso el documento xml original de entrada y el manejador. En el momento en el que instancio el parser van a empezar a llegar los eventos
		
			
			//Paso 2.3 - Invocar al método getConcepts() del objeto ManejadorXML para obtener un List<String> con las uris de los elementos <concept> cuyo elemento <code> contiene el código de la categoría buscado
	   	 	listaConcepts = manejador.getConcepts();
			
	   	 	
	   	 	//Paso 2.4 - Invocar al método getLabel() del objeto ManejadorXML para obtener el nombre de la categoría buscada
	   	 	sNombreConcept = manejador.getLabel();
	   	 	
	   	 	
	   	 	//Paso 2.5 - Invocar al método getDatasets() del objeto ManejadorXML para obtener un mapa con los datasets de la categoría buscada 
	   	 	mapaDatasets = manejador.getDatasets();
	   	 	
	   	 	
	   	 	//Imprimo a ver si está sacando la información correctamente
	   	 	System.out.println(listaConcepts.toString());
	   	 	System.out.println(sNombreConcept);
	   	 	System.out.println(mapaDatasets.toString());
	   	 

		 } catch (SAXException e) {
	            System.out.println("SAXException : El xml no se ha formado correctamente");
	            e.printStackTrace();
	     } catch (IOException e) {
	            System.out.println("IOException: Error entrada-salida");
	            e.printStackTrace();
	     } catch (ParserConfigurationException e) {
	    	 	e.printStackTrace();
	     }
		
	}//cierra extraccionInformacionDocumentoEntrada()
	
	
	
	/**	
	 * Extracción de información del array graph, contenido en el JSON apuntado por cada id de los dataset contenidos en el fichero XML que obtubimos de salida en el paso anterior
	 * Parser JSON que utiliza el API GSON Streaming
	 */
	private static void parserJSON_API_GSON() {
		
		mapaDatasetConcepts = P4_SAX_Y_GSON.getDatasetConcepts(listaConcepts, mapaDatasets);
				
        JSONDatasetParser json = new JSONDatasetParser("fichero", listaConcepts, mapaDatasetConcepts);
	}
	
	
	
	
	private static void generacionXMLSalida() {
		
		
		try {
			
			//Paso 3.1 - Crear el fichero de salida con el nombre recibido en el tercer argumento de main()
			PrintWriter escritorFicheroSalida;
			escritorFicheroSalida = new PrintWriter (new FileWriter( fileOut ));
			
			//Paso 3.2 - Volcar al fichero de salida los datos en el formato XML especificado por ResultadosBusquedaP3.xsd
		    salidaXML salidaXML = new salidaXML();
	        
	        escritorFicheroSalida.write(salidaXML.XMLOutputString(listaConcepts, mapaDatasets,sNombreConcept));
	        
	        escritorFicheroSalida.close();
	        
			System.out.println("Fin del análisis.");
			
			
		} catch (IOException e) {
			System.out.println("IOException: Error entrada-salida");
			e.printStackTrace();
		}
		
	}//cierra transformacionDeInformacionYGeneracionXMLSalida()
	
	
	
	
	private static Map<String, List<Map<String,String>>> getDatasetConcepts(List<String> lConcepts,	Map<String, HashMap<String, String>> mDatasets){
		
		Map<String, List<Map<String,String>>> datasetConcepts = null;
		
		return datasetConcepts;
		
	}
	
	

	
	/**
	 * Muestra mensaje de los argumentos esperados por la aplicación.
	 * Deberá invocase en la fase de validación ante la detección de algún fallo
	 *
	 * @param mensaje  Mensaje adicional informativo (null si no se desea)
	 */
	private static void mostrarUso(String mensaje){
		Class<? extends Object> thisClass = new Object(){}.getClass();
		
		if (mensaje != null)
			System.err.println(mensaje+"\n");
		System.err.println(
				"Uso: " + thisClass.getEnclosingClass().getCanonicalName() + " <ficheroCatalogo> <códigoCategoría> <ficheroSalida>\n" +
				"donde:\n"+
				"\t ficheroCatalogo:\t path al fichero XML con el catálogo de datos\n" +
				"\t códigoCategoría:\t código de la categoría de la que se desea obtener datos\n" +
				"\t ficheroSalida:\t\t nombre del fichero XML de salida\n"	
				);				
	}		

}//cierra la clase
