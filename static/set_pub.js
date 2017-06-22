$(document).ready(function(){
	$("#pub").on('submit',active_pub);
});

 var canvas = document.getElementById('map');
 var pressed = false;
  
  
  var posX1 = 0;
  var posY1 = 0;
  var rayon = 0;
  var pubs = [];
  
var setpub = false;
function active_pub(){
	if(!setpub){
		$("#pub_button").attr("value", "Terminer");
		$("#pub_button").attr("class", "btn btn-danger");
	}else{
		$("#pub_button").attr("value", "Définir Publicité");
		$("#pub_button").attr("class", "btn btn-primary");
	}
	setpub = !setpub;
}


 

  canvas.onmousemove = function(e) {
   if (canvas.getContext && setpub) {
    var ctx = canvas.getContext('2d');
	var posX = (e.clientX * canvas.width)/$("#map").width() ;
	var posY = (e.clientY * canvas.height)/$("#map").height() - ($("#info_bar").height() * canvas.height)/$("#map").height();
	
	if (!pressed) return;
	
	rayon = Math.sqrt((posX-posX1)*(posX-posX1)+(posY-posY1)*(posY-posY1));
	ctx.beginPath();
	ctx.clearRect(0, 0, canvas.width, canvas.height);
	for(var i = 0 ; i<pubs.length; i++){
		ctx.moveTo(pubs[i].x+pubs[i].rayon, pubs[i].y)
		ctx.arc(pubs[i].x, pubs[i].y, pubs[i].rayon, 0, 2 * Math.PI);
	}
	ctx.moveTo(posX1+rayon, posY1)
	ctx.arc(posX1, posY1, rayon, 0, 2 * Math.PI);
	ctx.fillStyle = "rgba(0, 200, 0, 0.3)";
	ctx.fill();
	ctx.lineWidth = 0.5;
	ctx.stroke();
	

  }
}
  canvas.onmousedown = function(e) {
	if(setpub){
		posX1 = (e.clientX * canvas.width)/$("#map").width() ;
		posY1 = (e.clientY * canvas.height)/$("#map").height() - ($("#info_bar").height() * canvas.height)/$("#map").height();
		pressed = true;
	}	
}

  canvas.onmouseup = function() {
	if(setpub){
		pressed = false;
		
		var publicite = new Object();
		publicite["x"] = posX1;
		publicite["y"] = posY1;
		publicite["rayon"] = rayon;
		
		pubs.push(publicite);
	}
}