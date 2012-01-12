String.prototype.replaceAll = function(stringToFind, stringToReplace) {
	var temp = this;
	var index = temp.indexOf(stringToFind);
	while (index != -1) {
		temp = temp.replace(stringToFind, stringToReplace);
		index = temp.indexOf(stringToFind);
	}
	return temp;
};

var port = chrome.extension.connect();
var baseAgentUrl = 'http://localhost:10001';
var agentStatus = false;

if( location.host.indexOf("http://localhost") != 0 ){	
	var img = jQuery('<img/>').attr('src', baseAgentUrl + '/resources/images/empty.gif');
	img.on("load", function(){
		agentStatus = true;
		localStorage.setItem('agent-status',agentStatus);
		jQuery.getScript(baseAgentUrl + '/resources/js/jquery.min.js', function(){
			jQuery.getScript(baseAgentUrl + '/resources/js/prototype-functors/F_Page.js');	
		});	
	}).on("error", function(){
		agentStatus = false;
		//TODO here
		localStorage.setItem('agent-status',agentStatus);
		jQuery(this).remove();
	});	
	jQuery('body').append(img);
}

var postDataLink = function(tags) {	
	var postUrl = baseAgentUrl + '/infocrawler/addNewEntryToDropbox/json?';
	var data = {'functors':{}};
	
	//alert(jQuery.data(document.body,"functors") );
	data.functors = JSON.parse(jQuery("body").attr('functors'));
	
	//pre-process here
	data.functors.F_Page.tags = tags.split(',');

	//now prepare for sending
	data.functors = JSON.stringify(data.functors);	

	jQuery.post(postUrl, data , function(response) {
		console.log(response);
		// chrome.extension.sendRequest({bg_method: "takeScreenshot"},
		// function(response) { console.log(response.message); });
	});
	return true;
};

chrome.extension.onRequest.addListener(function(request, sender, sendResponse) {
	// alert('method: '+request.method);
	var m = request.method;
	if (m === 'postDataLink') {
		if(postDataLink(request.tags)){
			sendResponse({href : location.href});
		}
		// Brain2.UI.popupCenter("http://google.com", 500, 450);		
	} else if (m === 'getCurrentUrl') {
		sendResponse({
			href : location.href
		});
	} else if (m === 'getAgentStatus') {
		sendResponse({
			agentStatus : agentStatus
		});	
	} else if (m === 'getPageMetaInfo') {
		sendResponse({
			pageMetaInfo : Brain2.analytics.pageMetaInfo()
		});	
	} else if (m === 'crawlingMyFacebook') {
		fetchFacebookDataFeed();
	} else {
		sendResponse({}); // snub them.
	}
});

// postMessage HTML5
window.addEventListener("message", function(event) {	
	if (event.origin !== baseAgentUrl )
		return;		
	// ...
}, false);



var postSaveDataByMethodPOST = function(tags) {
	var href = location.href;
	var postUrl = baseAgentUrl + '/linkmarking/save/html?';
	var metaInfo = Brain2.analytics.pageMetaInfo();

	/*
	 * // FB parser var text = ''; var collector = function(){ text +=
	 * (jQuery(this).text()); }; jQuery('span.messageBody').each(collector);
	 */

	var theIframeId = '__brain2_ext_handler';
	if (jQuery('#__brain2_ext_handler').length === 0) {
		var targetIframe = jQuery("<iframe/>").attr({
			'style' : 'display:none',
			'id' : theIframeId
		});
		jQuery('body').append(targetIframe);
	}

	var form = jQuery("<form/>").attr({
		'method' : 'POST',
		'action' : postUrl,
		'target' : theIframeId
	});

	var field;
	field = jQuery("<input/>").attr({
		'type' : 'hidden',
		'name' : 'href',
		'value' : encodeURIComponent(href)
	});
	form.append(field);

	field = jQuery("<input/>").attr({
		'type' : 'hidden',
		'name' : 'title',
		'value' : metaInfo['title']
	});
	form.append(field);

	field = jQuery("<input/>").attr({
		'type' : 'hidden',
		'name' : 'description',
		'value' : metaInfo['description']
	});
	form.append(field);

	field = jQuery("<input/>").attr({
		'type' : 'hidden',
		'name' : 'tags',
		'value' : tags
	});
	form.append(field);

	jQuery('body').append(form);
	form.submit();
};

var fetchFacebookDataFeed = function() {
	var feeds = jQuery('#profile_minifeed').find('> li');
	feeds.each(function() {
		var href = jQuery(this).find('a.external').attr('href');
		if (href) {
			console.log("-------------------------------");
			console.log(href);
			console.log(jQuery(this).find('div.uiAttachmentTitle a').text());
			console.log(jQuery(this).find('div.uiAttachmentDesc').text());

			var msg = jQuery(this).find('span.messageBody').text();
			console.log(msg);
		}
	});
	jQuery('#profile_pager').find('a.uiMorePagerPrimary').click();
};



