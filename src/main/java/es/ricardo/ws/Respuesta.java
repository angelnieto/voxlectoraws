package es.ricardo.ws;

public class Respuesta {
	
	private int errorID;
	private String texto;
	private int veces;
	private String mensajeError;
	
	public Respuesta(int error,String texto){
		this.errorID=error;
		this.texto=texto;
	}

	public int getErrorID() {
		return errorID;
	}
	public void setErrorID(int errorID) {
		this.errorID = errorID;
	}
	public String getTexto() {
		return texto;
	}
	public void setTexto(String texto) {
		this.texto = texto;
	}
	public int getVeces() {
		return veces;
	}

	public void setVeces(int veces) {
		this.veces = veces;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public void setMensajeError(String mensajeError) {
		this.mensajeError = mensajeError;
	}

}
