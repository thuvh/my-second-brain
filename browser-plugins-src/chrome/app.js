String.prototype.replaceAll = function(stringToFind,stringToReplace){
	var temp = this;
	var index = temp.indexOf(stringToFind);
	while(index != -1){
		temp = temp.replace(stringToFind,stringToReplace);
		index = temp.indexOf(stringToFind);
	}
	return temp;
};

var postSaveDataByMethodPOST = function(tags) {
	var href = location.href;
	var postUrl = 'http://localhost:10001/linkmarking/save?';
	var metaInfo = Brain2.analytics.pageMetaInfo();
	
	/* 
	// FB parser 
	var text = '';
	var collector = function(){
		text += (jQuery(this).text());
	};
	jQuery('span.messageBody').each(collector);
	*/
	
	var theIframeId = '__brain2_ext_handler' ;
	if(jQuery('#__brain2_ext_handler').length === 0){
		var targetIframe = jQuery("<iframe/>").attr( {'style':'display:none', 'id': theIframeId});
		jQuery('body').append(targetIframe);
	}
	
	var form = jQuery("<form/>").attr( {'method':'POST', 'action': postUrl , 'target': theIframeId } );
	
	var field;
	field = jQuery("<input/>").attr( {'type':'hidden', 'name':'href', 'value':encodeURIComponent(href) });	
	form.append(field);
		
	field = jQuery("<input/>").attr( {'type':'hidden', 'name':'title', 'value': metaInfo['title'] });	
	form.append(field);
	
	field = jQuery("<input/>").attr( {'type':'hidden', 'name':'description', 'value': metaInfo['description'] });	
	form.append(field);
	
	field = jQuery("<input/>").attr( {'type':'hidden', 'name':'tags', 'value': tags });	
	form.append(field);
	
	jQuery('body').append(form);	
	form.submit();	
};

chrome.extension.onRequest.addListener(function(request, sender, sendResponse) {
//  alert('method: '+request.method);
 if(request.method === 'postSaveDataByMethodPOST'){
	  postSaveDataByMethodPOST(request.tags);
	  //Brain2.UI.popupCenter("http://google.com", 500, 450);
	  sendResponse({href: location.href});
  } else {
	  sendResponse({}); // snub them.
  }
});

//postMessage HTML5
window.addEventListener("message", receiveMessage, false);

function receiveMessage(event)
{
  if (event.origin !== "http://localhost:10001")
    return;
  //alert(event.data);
  // ...
}

