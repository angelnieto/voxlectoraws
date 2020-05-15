package es.ricardo.ws.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.ParentReference;
import com.google.gson.Gson;

import es.ricardo.ws.Base64;
import es.ricardo.ws.ExampleHandler;
import es.ricardo.ws.ParsedExampleDataSet;
import es.ricardo.ws.Respuesta;
import es.ricardo.ws.XmlSerializer;
import es.ricardo.ws.config.VoxlectoraConfig;

@RestController
@RequestMapping("/autenticador")
public class AutenticadorController {

	private static Logger log = LoggerFactory.getLogger(AutenticadorController.class);
	
	private String serverFolder;
	 
    @Autowired 
    public AutenticadorController(VoxlectoraConfig config) {
    	if(config != null) {
    		this.serverFolder = config.getTempFolder();
    	}
    }
    
//	private String serverFolder= System.getProperty("jboss.server.base.dir")+"/tmp/vfs/imagenes";


	 
	@PostMapping( produces = {"application/json; charset=UTF-8"})
	 public ResponseEntity<String> getDescription(@RequestBody String json) {
		 Respuesta respuesta=null;
		 //int error=-1;
		 int veces=-1;
		 String texto="";
		 
		 try {
		      //verifico que existe la propiedad server.tempFolder
			 	log.info("serverFolder :" + serverFolder);
			 
			  if(serverFolder == null) {
	        	   if(("MAC OS X").equalsIgnoreCase(System.getProperty("os.name"))) {
	        		   serverFolder = "/Users/ricardomolinacuesta/Documents/voxlectora/tmp/vfs/imagenes";
	        	   }
	           }
			 
			 
	    	JsonParser parser=new JsonParser();
			JsonObject jsonObject=parser.parse(json.trim()).getAsJsonObject();	    	
	    	//guardo la imagen en el servidor
			guardarImagen(jsonObject);
	    	//identifico al usuario
		   	Drive service=selectServiceAccount();
		   			    	
			if(Boolean.parseBoolean(jsonObject.get("esSAD").getAsString()) || (veces=esDeSegunda(jsonObject,service))<=15){        
				
				//Creamos el directorio para nuestra aplicación en Google Drive
    			File directorioRaiz=getDirectorioRaiz(jsonObject,service);
	         	
	         	//Creo el directorio reservado para el dispositivo
	         	File directorioDispositivo=getDirectorio(jsonObject.get("cacharroID").getAsString());
	         	List<ParentReference> directoriosPadre=new ArrayList<ParentReference>();
		        ParentReference pr=new ParentReference();
		        pr.setId(directorioRaiz.getId());
		        directoriosPadre.add(pr);
		        directorioDispositivo.setParents(directoriosPadre);
		        
		        List<File> directorios=searchFileInDrive(directorioDispositivo.getTitle(),directorioRaiz.getId(),service);
	         	if(directorios.isEmpty()){
	           		 directorioDispositivo = service.files().insert(directorioDispositivo).execute();
	         		 directorios.add(directorioDispositivo);
	         	 }else{
	         		directorioDispositivo.setId(directorios.get(0).getId());
	         	 }
	         	 
         	   // Inserto el archivo con la imagen
	           java.io.File fileContent = new java.io.File(serverFolder + java.io.File.separator + jsonObject.get("cacharroID").getAsString()+".jpeg");
	           FileContent mediaContent = new FileContent("image/jpeg", fileContent);
	           
	           // File's metadata.
	           File body = new File();
	           body.setTitle(fileContent.getName());
	           body.setMimeType("image/jpeg");
	           
	           List<ParentReference> directoriosFichero=new ArrayList<ParentReference>();
	           pr=new ParentReference();
	           pr.setId(directorioDispositivo.getId());
	           directoriosFichero.add(pr);
	           body.setParents(directoriosFichero);
	           
	           List<File> fotos=searchFileInDrive("image.jpeg",directorioDispositivo.getId(),service);
	           File file=null;
	           if(fotos.isEmpty()){
		         	  file= service.files().insert(body, mediaContent).execute();
	           }else{
		         	  file = service.files().update(fotos.get(0).getId(), body, mediaContent).execute();
	           }
           
	     	  //Indago sobre la existencia de una copia anterior de la imagen ya escaneada
	     	  List<File> copias=searchFileInDrive("copia",directorioDispositivo.getId(),service);
	     	  //si existe, la borro
	     	  if(!copias.isEmpty())
		     		  for(int i=0;i<copias.size();i++)
		     			  service.files().delete(copias.get(0).getId()).execute();
		     	  
 	  			//y ahora me creo una nueva copia
	           File copiedFile = new File();
	           copiedFile.setTitle("copia");
	           copiedFile.setParents(directoriosFichero);
	         		   
	           copiedFile= service.files().copy(file.getId(), copiedFile).setConvert(true).setOcr(true).setOcrLanguage("es").execute();
	           
	           texto=recuperarTexto(copiedFile,service);
	           
	           if(Boolean.parseBoolean(jsonObject.get("esSAD").getAsString())){
	        	   service.files().delete(file.getId()).execute();
	        	   service.files().delete(copiedFile.getId()).execute();
	           }else{
	            	service.files().delete(directorioDispositivo.getId()).execute();
	           }
	           //borro el archivo temporal con la imagen
	           fileContent.delete();
			}else{
				 texto="Limite de consultas superado.";
			}
		    
			respuesta=new Respuesta(-1,texto);
			respuesta.setVeces(veces);
			respuesta.setMensajeError("");
				 	
		} catch (Exception e) {
			respuesta=new Respuesta(1,texto);
			respuesta.setVeces(veces);
			if("reloj".equals(e.getMessage()))
				respuesta.setMensajeError("reloj");
			else
				respuesta.setMensajeError(e.getMessage());
		}

		if(respuesta!=null)
		   	return new ResponseEntity<String>(new Gson().toJson(respuesta), HttpStatus.CREATED);
		else
		   	return null;
	 }

	 private Drive selectServiceAccount() throws Exception{
	    try{	
	    	return getDriveService();
	    }catch(Exception e){
	    	throw new Exception("reloj");
	    }
	 }
	  
	/**
	 * Build and returns a Drive service object authorized with the service accounts.
	 *
	 * @return Drive service object that is ready to make requests.
	 */
	 public Drive getDriveService() throws GeneralSecurityException,IOException, URISyntaxException {
	      
	    List<String> scopes=new ArrayList<String>();
	    scopes.add(DriveScopes.DRIVE);
	    	
	    HttpTransport httpTransport = new NetHttpTransport();
	    JacksonFactory jsonFactory = new JacksonFactory();
	    GoogleCredential credential = new GoogleCredential.Builder()
	    	.setTransport(httpTransport)
	    	.setJsonFactory(jsonFactory)
	    	.setServiceAccountId("466180996139-ba9tuu9oq02acnbvrkgkpsl8tfersrt8@developer.gserviceaccount.com")
	    	.setServiceAccountScopes(scopes)
	    	.setServiceAccountPrivateKeyFromP12File(getTempPkc12File())
	    	.build();
      
	    	Drive service = new Drive.Builder(httpTransport, jsonFactory,null).setHttpRequestInitializer(credential).build();
	           
	      return service;
	    }

	private java.io.File getTempPkc12File() throws IOException {
//	    InputStream pkc12Stream = sContext.getResourceAsStream("/assets/98010a5ea85c050f3883584897d9f2585f8c375a-privatekey.p12");
	    InputStream pkc12Stream = AutenticadorController.class.getResourceAsStream("/assets/98010a5ea85c050f3883584897d9f2585f8c375a-privatekey.p12");
	    java.io.File tempPkc12File = java.io.File.createTempFile("certificado", "p12");
	    OutputStream tempFileStream = new FileOutputStream(tempPkc12File);
	
	    int read = 0;
	    byte[] bytes = new byte[1024];
	    while ((read = pkc12Stream.read(bytes)) != -1) {
	        tempFileStream.write(bytes, 0, read);
	    }
	    return tempPkc12File;
	}
	
	private void guardarImagen(JsonObject objetoJSON) throws IOException{
    	JsonElement jsonElement=objetoJSON.get("imagen");
    	
        byte[] imagenDescodificada=null;
        BufferedImage bi=null;
        imagenDescodificada=Base64.decode(jsonElement.getAsString());
        if(imagenDescodificada!=null){
        	ByteArrayInputStream bis=new ByteArrayInputStream(imagenDescodificada);
        	bi=ImageIO.read(bis);
        	
        	java.io.File mediaStorageDir = new java.io.File(serverFolder);
		    // Crear el directorio de almacenamiento si no existe
		    if (! mediaStorageDir.exists())
		        if (! mediaStorageDir.mkdirs())
		            throw new IOException("No se pudo crear el directorio de almacenaje de imágenes");
		    
		    //Crear un archivo con la imagen
		    java.io.File mediaFile = new java.io.File(mediaStorageDir.getPath() + java.io.File.separator + objetoJSON.get("cacharroID").getAsString()+".jpeg");
		    ImageIO.write(bi, "jpeg", mediaFile);
    
        }
	}
	
	private int esDeSegunda(JsonObject objetoJSON,Drive service) throws IOException, ParserConfigurationException, SAXException{
		  
	   	//Creamos el directorio para nuestra aplicación en Google Drive
    	 File directorioRaiz=getDirectorioRaiz(objetoJSON,service);
	 
    	 // File's metadata.
		 File body = new File();
		 body.setTitle(objetoJSON.get("cacharroID").getAsString()+".xml");
		 body.setMimeType("text/xml");
       
		 List<ParentReference> directoriosFichero=new ArrayList<ParentReference>();
		 ParentReference pr=new ParentReference();
		 pr.setId(directorioRaiz.getId());
		 directoriosFichero.add(pr);
		 body.setParents(directoriosFichero);
       
		 //Indago si el usuario nunca usó la aplicación
		 List<File> ficheroXML=searchFileInDrive(body.getTitle(),directorioRaiz.getId(),service);
		 File file=null;
		 int veces=0;
		 
		 if(ficheroXML.isEmpty()){
			 veces=1;
			 subirFichero(veces,directorioRaiz,objetoJSON,service);
		 }else{
			file = service.files().get(ficheroXML.get(0).getId()).execute();
			veces=getVeces(file,service);
			if(veces<=15)
				//Comentar para probar la barra de intentos
				subirFichero(++veces, directorioRaiz,objetoJSON,service);
				//veces=15;
			else 
				return veces;
		}
		 return veces;
	}
      
	private File getDirectorioRaiz(JsonObject objetoJSON,Drive service) throws IOException{
		File directorioRaiz=null;
		if(Boolean.parseBoolean(objetoJSON.get("esSAD").getAsString()))
			directorioRaiz=getDirectorio("Lector_1_1");
		else
			directorioRaiz=getDirectorio("Lector_1");

		 List<File> directorios=searchFileInDrive(directorioRaiz.getTitle(),null,service);
		 if(directorios.isEmpty()){
			 
			directorioRaiz = service.files().insert(directorioRaiz).execute();
		
			directorios.add(directorioRaiz);
		 }else{
			 directorioRaiz.setId(directorios.get(0).getId());
		 }
			 
		return directorioRaiz;
	}
	
    private List<File> searchFileInDrive(String nombre,String parentId,Drive service) throws IOException{
	  	  List<File> result = new ArrayList<File>();
	  	  String q="title ='"+nombre+"'";
	  	  if(parentId!=null)
	  		  q+=" and '"+parentId+"' in parents";
	  	  
	  	  try{
	  		  FileList files = service.files().list().setQ(q).execute();
	  		
	  		if(files!=null)
	  			result.addAll(files.getItems());
	  	  }catch(IOException e){
	  		  throw new IOException("reloj");
	  	  }
	  	  
	    return result;
    }
    
    private File getDirectorio(String nombre){
     	File folder = new File();
     	folder.setTitle(nombre);
     	folder.setMimeType("application/vnd.google-apps.folder");
     	return folder;
    }
    
	private void subirFichero(int veces,File directorioRaiz,JsonObject objetoJSON,Drive service) throws IOException{

		//Escribo el fichero en el directorio Files
        FileOutputStream fo=new FileOutputStream(getXMLPath(objetoJSON));
		PrintWriter pw=new PrintWriter(fo);
		//Escribo el archivo xml temporal en JBOSS
		pw.write(escribirXML(veces));
						
		pw.close();
		
		java.io.File fileContent = new java.io.File(getXMLPath(objetoJSON));
        FileContent mediaContent = new FileContent("text/xml", fileContent);

        // File's metadata.
        File body = new File();
        body.setTitle(fileContent.getName());
        body.setMimeType("text/xml");
           
        List<ParentReference> directoriosFichero=new ArrayList<ParentReference>();
        ParentReference pr=new ParentReference();
        pr.setId(directorioRaiz.getId());
        directoriosFichero.add(pr);
        body.setParents(directoriosFichero);
           
        List<File> ficherosXML=searchFileInDrive(body.getTitle(),directorioRaiz.getId(),service);
        if(ficherosXML.isEmpty()){
         	   service.files().insert(body, mediaContent).execute();
        }else{
         	  service.files().update(ficherosXML.get(0).getId(), body, mediaContent).execute();
        }
        
        /*if (file != null) 
     		  showToast("XML inserted: " + file.getTitle());*/
        
        fileContent.delete();
	}
	
	private String getXMLPath(JsonObject objetoJSON){
		//Ruta en la que colocaré el archivo XML temporal dentro de JBOSS
		java.io.File mediaStorageDir = new java.io.File(serverFolder);
		return mediaStorageDir.getPath() + java.io.File.separator + objetoJSON.get("cacharroID").getAsString()+".xml";
	}
	
	private String escribirXML(int veces) throws IllegalArgumentException, IllegalStateException, IOException{
		XmlSerializer serializer = new es.ricardo.ws.XmlSerializer();
		StringWriter writer = new StringWriter();
				
		serializer.setOutput(writer);
		serializer.startDocument("UTF-8", false);
	    serializer.startTag("map");
	    serializer.startTag("esDeSegunda");
	    serializer.attribute("value", Integer.toString(veces));
	    serializer.endTag("esDeSegunda");
	    serializer.endTag("map");
	    serializer.endDocument();
	
		return writer.toString(); 
	}
	
	private int getVeces(File xml,Drive service) throws ParserConfigurationException, SAXException, IOException{

		 if (xml.getDownloadUrl() != null && xml.getDownloadUrl().length() > 0) {
			 ParsedExampleDataSet parsedExampleDataSet=null;
			/* Get a SAXParser from the SAXPArserFactory. */
              SAXParserFactory spf = SAXParserFactory.newInstance();
              SAXParser sp = spf.newSAXParser();
           /* Get the XMLReader of the SAXParser we created. */
             XMLReader xr = sp.getXMLReader();
           /* Create a new ContentHandler and apply it to the XML-Reader*/
            ExampleHandler myExampleHandler = new ExampleHandler();
            xr.setContentHandler(myExampleHandler);
           /* Parse the xml-data from our URL. */
            HttpResponse resp = service.getRequestFactory().buildGetRequest(new GenericUrl(xml.getDownloadUrl())).execute();
             xr.parse(new InputSource(resp.getContent()));
           /* Parsing has finished. */
            
             parsedExampleDataSet =myExampleHandler.getParsedData();
	              
			 return parsedExampleDataSet.getExtractedInt();
		 }
		 return 0;
	}
	
    private String recuperarTexto(File copiedFile,Drive service) throws IOException  {
		 HttpResponse response;
		 BufferedReader in = null;
		 StringBuffer sb =null ;
		try {
			response = service.getRequestFactory().buildGetRequest(new GenericUrl(copiedFile.getExportLinks().get("text/plain"))).execute();
		     		
			sb = new StringBuffer();

            in = new BufferedReader(new InputStreamReader(response.getContent(),"UTF-8"));

			String str;
			while ((str = in.readLine()) != null) {
				if(!str.contains("___") && !"".equals(str))
            		sb.append(str).append(System.getProperty("line.separator"));
            }
		}finally {
			  if (in != null)
              	in.close();
	    }
   
		 return sb.toString();
    }
 	
}
