/*jQuery.fn.center = function () {
	this.css("position", "absolute");
	this.css("top", ($(window).height()-this.height())/2 + $(window).scrollTop() + "px");
	this.css("left", ($(window).width()-this.width())/2 + $(window).scrollLeft() + "px");
}*/
var milisegundos=1000;
var tiempoRestante=0;
var centinela=true;
var scene =$('.scene').parallax(); 
var temporizador,altoUL;	


$(document).ready(function() {
	
	 redimensionarCapas();
	
	 centrar($(".container"));
	 
	 setInterval("comprobarPosiciones()",milisegundos);
	 
	 colocarEtiquetas();
 });

function redimensionarCapas(){
	$(".scene:last-child li:nth-child(3)").css("min-width",$("#fondo").width()*1.2+"px");
	$(".scene:last-child li:nth-child(2)").css("width",$(".scene:last-child li:nth-child(3)").width()+"px");
}

function centrar(obj) {
	altoUL = -600 - 1.2*($(window).width()-$(window).height());
	
	obj.css("left", ($(window).width()-obj.width())/2 + $(window).scrollLeft() + "px");
	obj.css("top", altoUL + "px");
}

function comprobarPosiciones(){
	var difAlto=(Math.ceil($(".scene:last-child li:nth-child(2)").position().top)-Math.ceil($(".scene:last-child li:nth-child(3)").position().top));
	var difAncho=Math.ceil($(".scene:last-child li:nth-child(2)").position().left)-Math.ceil($(".scene:last-child li:nth-child(3)").position().left);
	//console.log("alto : "+ difAlto + " top : "+ altoUL + "px");
	var factor=difAlto / $(window).width();
	
	if(((factor <= -0.002 && -0.007 < factor) || (factor <= -0.140 && -0.145 < factor) || (factor <= 0.135 && 0.130 < factor)) && difAncho < 5 && difAncho > -6){
			//scene.parallax('friction',1,1);
		
			if(centinela){
				//temporizador=setTimeout("parpadeo()",200);
				temporizador=setInterval("parpadeo()",100);
			}
			$("#powered").css("color","black");
			
	}else{
			$(".scene:last-child li:nth-child(1) img").attr("src","images/capa4_50.png");
			//clearTimeout(temporizador);
			clearInterval(temporizador);
			centinela=true;
			$("#powered").css("color","orange");
	}
			
	//$("#powered").html("Capa 2 : X="+Math.ceil($(".scene:last-child li:nth-child(2)").position().left)+" Y="+Math.ceil($(".scene:last-child li:nth-child(2)").position().top)+" <\br> Capa 3 : X="+Math.ceil($(".scene:last-child li:nth-child(3)").position().left)+" Y="+Math.ceil($(".scene:last-child li:nth-child(3)").position().top));
	//setTimeout("comprobarPosiciones()",milisegundos);
}

function colocarEtiquetas(){
	$("#powered img").height($(window).height()>$(window).width()?$(window).height()/20:$(window).height()/13);
	$("#powered img").width(9*$("#powered img").height());
	 $("#powered").css("top",$(window).height()-$("#powered img").height()+"px");
	 $("#powered").css("left",($(window).width()-$("#powered img").width())/2+"px");
}

function parpadeo(){
	
	if(tiempoRestante>0 && !centinela){
		if($(".scene:last-child li:nth-child(1) img").attr("src")=="images/capa3_50.png")
			$(".scene:last-child li:nth-child(1) img").attr("src","images/capa4_50.png");
		else
			$(".scene:last-child li:nth-child(1) img").attr("src","images/capa3_50.png");
		tiempoRestante=tiempoRestante-199;
	}else if(centinela){
		centinela=false;
		tiempoRestante=2000;
	}else{
		tiempoRestante=0;
	}
}
