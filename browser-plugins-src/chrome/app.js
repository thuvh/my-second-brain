String.prototype.replaceAll = function(stringToFind,stringToReplace){
	var temp = this;
	var index = temp.indexOf(stringToFind);
	while(index != -1){
		temp = temp.replace(stringToFind,stringToReplace);
		index = temp.indexOf(stringToFind);
	}
	return temp;
};

var postSaveDataByMethodPOST = function() {
	var href = location.href;
	var link = 'http://localhost:10001/linkmarking/save?';
	var tags = '';
	var data = {'description':'link hay', 'href':encodeURIComponent(href), 'tags':tags, 'title':document.title};
	var callback = function(rs){
		alert(rs);
	};
	
	var theIframeId = '__brain2_ext_handler' ;
	if(jQuery('#__brain2_ext_handler').length === 0){
		var targetIframe = jQuery("<iframe/>").attr( {'style':'display:none', 'id': theIframeId});
		jQuery('body').append(targetIframe);
	}
	
	var form = jQuery("<form/>").attr( {'method':'POST', 'action': link , 'target': theIframeId });
	
	var field = jQuery("<input/>").attr( {'type':'hidden', 'name':'href', 'value':encodeURIComponent(href) });	
	form.append(field);
	
	var text = '';
	var collector = function(){
		text += (jQuery(this).text());
	};
	jQuery('span.messageBody').each(collector);
	
	var field = jQuery("<input/>").attr( {'type':'hidden', 'name':'description', 'value': text });	
	form.append(field);
	
	jQuery('body').append(form);
	
	form.submit();
	
};

var postSaveData = function() {
	var href = location.href;
	var link = 'http://localhost:10001/linkmarking/save?';
	var tags = '';
	var data = {'description':'link hay', 'href':encodeURIComponent(href), 'tags':tags, 'title':document.title};
	var callback = function(rs){
		alert(rs);
	};
	
	jQuery.ajax({
	  url: link,
	  dataType: 'json',
	  data: data,
	  success: callback
	});
};

function appendPlugins() {
		
	var tpl = '';
	tpl += '<div><a href="javascript:;" title="Save this link" ><img src="http://dl.dropbox.com/u/4074962/icons/bigfolder.png" /></a></div>';
		
	var node = jQuery( tpl );
	node.click(postSaveDataByMethodPOST);

	var leftPos = jQuery(window).width() - 90;
	var topPos = jQuery(window).height() - 90;
	node.attr( {'style' : "width:100px!important;padding-left:0px!important; clear: both;z-index:999999" });	
	jQuery('body').prepend(node);
	
};

//appendPlugins();		


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

window.addEventListener("message", receiveMessage, false);

function receiveMessage(event)
{
  if (event.origin !== "http://localhost:10001")
    return;
  alert(event.data);
  // ...
}

