package piat.opendatasearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Adrián Mayo Barrios - 54241386A
 */
public class ManejadorXML extends DefaultHandler implements ParserCatalogo {
	
	private String sCodigoConcept = null;	// Codigo de la categoría (Concept) buscada (Query)
	private String sNombreConcept = null;	// Nombre de la categoría (Concept) buscada (Label)
		
	private List <String> lConcepts = new ArrayList<String>(); // Lista con los uris de los elementos <concept> que pertenecen a la categoría
	private Map <String, HashMap<String,String>> hDatasets;	// Mapa con información de los dataset que pertenecen a la categoría

	private StringBuilder buffer = new StringBuilder();

    private boolean isConcepts;
    private boolean isDatasets;
    private boolean isConcept;
    private boolean isCode;
    private boolean isLabel;
    private boolean isDatasetAGuardarMapa;
    private boolean isConceptAGuardarLista;
    private boolean guardarNombreCategoria;
    private boolean isDataset;
    private boolean isTitle;
    private boolean isDescription;
    private boolean isTheme;
    
    private int nConcept; //Para controlar en que nivel estamos
    private int nConceptNoGuardar;
    
    private String idConcept;
    private String idDataset;
    private String title;
    private String description;
    private String theme;
    

	/**  
	 * @param sCodigoConcepto código de la categoría a procesar
	 * @throws ParserConfigurationException 
	 */
	public ManejadorXML (String sCodigoConcepto) throws SAXException, ParserConfigurationException {
		
		sCodigoConcept = sCodigoConcepto;
	}

	
	
	 //===========================================================
	 // Métodos a implementar de la interfaz ParserCatalogo
	 //===========================================================

	/**
	 * <code><b>getLabel</b></code> 
	 * @return Valor de la cadena del elemento <code>label</code> del <code>concept</code> cuyo 
	 * elemento <code><b>code</b></code> sea <b>igual</b> al criterio a búsqueda.
	 * <br>
	 * null si no se ha encontrado el concept pertinente o no se dispone de esta información  
	 */
	@Override
	public String getLabel() {
		
		return sNombreConcept;
	}

	/**
	 * <code><b>getConcepts</b></code>
	 *	Devuelve una lista con información de los <code><b>concepts</b></code> resultantes de la búsqueda. 
	 * <br> Cada uno de los elementos de la lista contiene la <code><em>URI</em></code> del <code>concept</code>
	 * 
	 * <br>Se considerarán pertinentes el <code><b>concept</b></code> cuyo código
	 *  sea igual al criterio de búsqueda y todos sus <code>concept</code> descendientes.
	 *  
	 * @return
	 * - List  con la <em>URI</em> de los concepts pertinentes.
	 * <br>
	 * - null  si no hay concepts pertinentes.
	 * 
	 */
	@Override	
	public List<String> getConcepts() {
		
		return lConcepts;
	}

	/**
	 * <code><b>getDatasets</b></code>
	 * 
	 * @return Mapa con información de los <code>dataset</code> resultantes de la búsqueda.
	 * <br> Si no se ha realizado ninguna  búsqueda o no hay dataset pertinentes devolverá el valor <code>null</code>
	 * <br> Estructura de cada elemento del map:
	 * 		<br> . <b>key</b>: valor del atributo ID del elemento <code>dataset</code>con la cadena de la <code><em>URI</em></code>  
	 * 		<br> . <b>value</b>: Mapa con la información a extraer del <code>dataset</code>. Cada <code>key</code> tomará los valores <em>title</em>, <em>description</em> o <em>theme</em>, y <code>value</code> sus correspondientes valores.

	 * @return
	 *  - Map con información de los <code>dataset</code> resultantes de la búsqueda.
	 *  <br>
	 *  - null si no hay datasets pertinentes.  
	 */	
	@Override
	public Map<String, HashMap<String, String>> getDatasets() {
		
		return hDatasets;
	}
	
	
	

	
	//===========================================================
	// Métodos a implementar de SAX DocumentHandler
	//===========================================================
	
	@Override
	/**
	 * Empieza el documento e inicializa todas las variables a false, las cuales serán puestas a true cuando se vayan leyendo (en el end element) 
	 */
	public void startDocument() throws SAXException {

		super.startDocument();
		System.out.println("Comeienza el documento catalogo.xml del que optenemos la información");

		//Pongo todos las variables a false
		
	    isConcepts = false;
	    isConcept = false;
	    isCode = false;
	    isLabel = false;
	    isDatasets = false;
	    isDataset = false;
	    isDatasetAGuardarMapa = false;
	    isConceptAGuardarLista = false;
	    isTitle = false;
	    isDescription = false;
	    isTheme = false;
	    guardarNombreCategoria = false;
	    
	    nConcept = 0;
		
	}//cierra startDocument()

	
	@Override
	/*
	 * Finaliza el documento
	 */
	public void endDocument() throws SAXException {
		
		super.endDocument();
		System.out.println("Finaliza el documento catalog.xml");
					
	}//cierra endDocument()


	@Override
	/**
	 * @param uri uri del espacio de nombres (NameSpace)
	 * @param localName Nombre sin espacio de nombres del elemento (Sin el prefijo que yo le haya puesto al espacio de nombres)
	 * @param qName Nombre del elemento con el espacio de nombres (ejemplo: klm:coordenada)
	 * @param attributes lista de atributos
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		super.startElement(uri, localName, qName, attributes);
		
		//Forma de controlar el estado en el que estoy en cada momento es mediante variables booleanas, cuando me llega el elemento titulo pongo titulo = true (En end element pongo esa variable a false)

		switch (qName) {
		
			case "concepts": //El evento fue start concepts
				isConcepts = true;
				break;
			case "concept": //El evento fue start concept
				idConcept = null;
				isConcept = true;
				nConcept ++;
				
				//Si estoy en un concept que NO está dentro de un dataset
				if (!isDataset) {
					idConcept = attributes.getValue("id"); //Obtenemos el valor del atributo id del Concepto en el que evaluamos

				}
				
				//Si estoy en un concept que está dentro de un dataset
				if(isConcept && isDataset) {
					idConcept = attributes.getValue("id");
					if(lConcepts.contains(idConcept)) {
						isDatasetAGuardarMapa = true; //Si el codigo coincide con el codigo que nos interesa lo marcamos para almacenarse en el mapa de dataset mas adelante
					}
				}
				
			break;
				
			case "label": //El evento fue start label
				isLabel = true;
			break;
			
			case "code": //El evento fue start code
				isCode = true;
			break;
			
			case "datasets": //El evento fue start datasets
				isDatasets = true;
				hDatasets = new HashMap<String,HashMap<String,String>>(); //Preparamos el mapa por si tenemos que guardar algún dataset
			break;
			
			case "dataset": //El evento fue start dataset
				isDataset = true;
				idDataset = null;
				
				for(int i=0; i < attributes.getLength(); i++) {
					idDataset = attributes.getValue(i);
				}
			break;
			
			case "title": // El evento fue start title
				isTitle = true;
			break;
			
			case "description": //El evento fue start description
				isDescription = true;
			break;
			
			case "theme": //El evento fue start theme
				isTheme = true;
			break;
			
		}// cierra el switch
		 	
	}//cierra startElement

	
	@Override
	/**
	 * En el end element es donde hago la mayoria de las cosas, por que cuando se acaba un elemento es cuando yo tengo que hacer las cosas.
	 * Esperamos a que llegue el end element y entonces es cuando ya podemos manejar la información almacenada en nuestro string builder (En nuestro buffer)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {

		super.endElement(uri, localName, qName);
		
		
		//Analizamos dentro del concept
		if(isCode) {

			if(buffer.toString().trim().equals(sCodigoConcept)) { //Obtener el codigo que quiero y el del concept en el que estoy y ver si son iguales 
				isConceptAGuardarLista = true;
				guardarNombreCategoria = true;
				nConceptNoGuardar = nConcept;
				lConcepts.add(idConcept);
			}
			
		}else if(isLabel && isConceptAGuardarLista && guardarNombreCategoria) {
			sNombreConcept = buffer.toString().trim();
			guardarNombreCategoria = false;
		}
		
		
		//Analizamos dentro del dataset
		if(isDataset && !isConcept) {
			if(isTitle) {
				title = buffer.toString().trim();
			}
			else if(isDescription) {
				description = buffer.toString().trim();
			}else if(isTheme) {
				theme = buffer.toString().trim();
			}
		}

		
		switch (qName) {
		
			case "concepts":
				isConcepts = false;
			break;
		
			case "concept":
				isConcept = false;
				
				if(isConceptAGuardarLista) {
					if( !lConcepts.contains(idConcept) && !isDataset){
						lConcepts.add(idConcept);
						
					}
					if(nConcept <= nConceptNoGuardar) {
						isConceptAGuardarLista=false;
					}
				}
				nConcept--;
			break;
			
			case "code":
				isCode = false;
			break;
			
			case "label":
				isLabel = false;
			break;
			
			case "datasets":
				isDatasets = false;
			break;
			
			case "dataset":
				isDataset = false;
				if(isDatasetAGuardarMapa) {
					hDatasets.put(idDataset, new HashMap<String, String>());	
					isDatasetAGuardarMapa = false;
					
					if(description != null) {
						hDatasets.get(idDataset).put("description", description);	
					}
					
					if(theme != null) {
						hDatasets.get(idDataset).put("theme", theme);
					}
					
					hDatasets.get(idDataset).put("title", title);
					
				}
				
			break;
			
			case "title":
				isTitle = false;
			break;
			
			case "description":
				isDescription = false;
			break;
			
			case "theme":
				isTheme = false;
			break;
		
		} //cierra el switch(qName)
		
		buffer.setLength(0); //vacio el buffer
					
	}//cierra endElement()
	
	
	@Override
	/**
	 * @param ch cadena de caracteres
	 * @param start número de caracter en el que se inicia esa cadena de caraceres dentro del documento xml
	 * @param length longitud de la cadena de caracteres
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {

		super.characters(ch, start, length);
		
		buffer.append(ch,start, length); //Voy cogiendo todo lo que me llegue y lo voy encadenando		
	
	}//cierra characters
	

}//cierra la clase
