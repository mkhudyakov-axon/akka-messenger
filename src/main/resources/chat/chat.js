function guid() {
  function s4() {
    return Math.floor((1 + Math.random()) * 0x10000)
      .toString(16)
      .substring(1);
  }
  return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
    s4() + '-' + s4() + s4() + s4();
}

$(document).ready(function() {
	/* Open a WebSocket */
	var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    var mapSocket = new WS("ws"+location.protocol.substring(4)+"//"+window.location.hostname+":6696/chat/ws/" + guid())
	mapSocket.onopen = function(event) {
        console.log("WebSocket connection established")
		var msg = "Hey, how are you?"
		mapSocket.send(msg)
	}
	/* If errors on websocket */
	var onalert = function(event) {
        console.log("websocket connection closed or lost")
    }
	mapSocket.onerror = onalert
	mapSocket.onclose = onalert
	
	mapSocket.onmessage = function(event) {
		console.log(event.data)
    }
})
