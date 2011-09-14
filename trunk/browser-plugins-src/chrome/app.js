String.prototype.replaceAll = function(stringToFind,stringToReplace){
	var temp = this;
	var index = temp.indexOf(stringToFind);
	while(index != -1){
		temp = temp.replace(stringToFind,stringToReplace);
		index = temp.indexOf(stringToFind);
	}
	return temp;
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
	tpl += '<a href="javascript:;" title="Save this link" ><img src="http://dl.dropbox.com/u/4074962/icons/bigfolder.png" /></a>';
		
	var node = jQuery( tpl );
	node.click(postSaveData);

	var leftPos = jQuery(window).width() - 90;
	var topPos = jQuery(window).height() - 90;
	node.attr( {'style' : "position:fixed !important; left:"+leftPos+"px; top: "+topPos+"px;z-index:9999;width:100px!important;padding:0px!important" });	
	jQuery('body').append(node);
	
};

appendPlugins();		


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

