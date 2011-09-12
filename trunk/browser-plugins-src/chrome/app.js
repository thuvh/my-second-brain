String.prototype.replaceAll = function(stringToFind,stringToReplace){
	var temp = this;
	var index = temp.indexOf(stringToFind);
	while(index != -1){
		temp = temp.replace(stringToFind,stringToReplace);
		index = temp.indexOf(stringToFind);
	}
	return temp;
};

function appendPlugins() {
	var href = location.href;
	//alert(href);
	if(href.indexOf('banbe.net')<0){		
		var tpl = '';
		tpl += '<span class="bb_like_holder" ><a href="[link]" >Post</a></span>';		
		tpl = '<div >' + tpl + '</div>';
		var node = jQuery( tpl.replaceAll('[link]',href) );
	
		var leftPos = jQuery(window).width() - 90;
		var topPos = jQuery(window).height() - 90;
		node.attr( {'style' : "position:fixed !important; left:"+leftPos+"px; top: "+topPos+"px;z-index:9999;width:100px!important;padding:0px!important" });	
		jQuery('body').append(node);	
	}
};
jQuery(document).ready(function(){
	appendPlugins();
});

	

chrome.extension.onRequest.addListener(function(request, sender, sendResponse) {
  alert('method: '+request.method);
  if (request.method == "fromPopup") {
    // Send JSON data back to Popup.
    sendResponse({data: "from Content Script to Popup"});
  } else if(request.method === 'whatShareLink'){
	 sendResponse({href: location.href});
  } else {
    sendResponse({}); // snub them.
  }
});

