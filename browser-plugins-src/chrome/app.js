String.prototype.replaceAll = function(stringToFind, stringToReplace) {
	var temp = this;
	var index = temp.indexOf(stringToFind);
	while (index != -1) {
		temp = temp.replace(stringToFind, stringToReplace);
		index = temp.indexOf(stringToFind);
	}
	return temp;
};

var postDataLink = function(tags) {
	var href = location.href;
	var postUrl = 'http://localhost:10001/linkmarking/save/json?';
	var metaInfo = Brain2.analytics.pageMetaInfo();
	var data = {
		href : encodeURIComponent(href)
	};
	data['title'] = metaInfo['title'];
	data['description'] = metaInfo['description'];
	data['tags'] = tags;

	jQuery.post(postUrl, data, function(response) {
		console.log(response);
		// chrome.extension.sendRequest({bg_method: "takeScreenshot"},
		// function(response) { console.log(response.message); });
	});
};

chrome.extension.onRequest.addListener(function(request, sender, sendResponse) {
	// alert('method: '+request.method);
	var m = request.method;
	if (m === 'postDataLink') {
		postDataLink(request.tags);
		// Brain2.UI.popupCenter("http://google.com", 500, 450);
		sendResponse({
			href : location.href
		});
	} else if (m === 'getCurrentUrl') {
		sendResponse({
			href : location.href
		});
	} else if (m === 'crawlingMyFacebook') {
		fetchFacebookDataFeed();
	} else {
		sendResponse({}); // snub them.
	}
});

// postMessage HTML5
window.addEventListener("message", function(event) {
	
	if (event.origin !== "http://localhost:10001")
		return;	
	
	// ...
}, false);

// test

var postSaveDataByMethodPOST = function(tags) {
	var href = location.href;
	var postUrl = 'http://localhost:10001/linkmarking/save/html?';
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

/*
jQuery.getScript('http://localhost:10001/resources/js/jquery.min.js', function(){
	jQuery.getScript('http://localhost:10001/resources/js/agent-index.js');	
});
*/