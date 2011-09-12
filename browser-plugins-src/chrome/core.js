/**
 * The core js for loading all dependent modules
 */
var Brain2 = {};
Brain2._time = (new Date()).getTime();
Brain2._config = {};
Brain2._cachePool = {};

// --------- Module Dom -------------------
Brain2.Dom = {
    _readyCallbacks : [],
    _domReady : false,
    _unbindEventFunc: []
};

Brain2.Dom.addListen = function(evnt, elem, func) {    
    if (elem.addEventListener)  // W3C DOM
        elem.addEventListener(evnt,func,false);
    else if (elem.attachEvent) { // IE DOM
        elem.attachEvent("on"+evnt, func);     
    }
    var clean = function(){
        if (elem.removeEventListener)  // W3C DOM
            elem.removeEventListener(evnt,func,false);
        else if (elem.detachEvent) { // IE DOM
            elem.detachEvent("on"+evnt, func);     
        }
    };
    Brain2.Dom._unbindEventFunc.push(clean);    
};

(function() {
    try {        
        Brain2.Dom._headNode = document.getElementsByTagName('head')[0];
        Brain2.Dom._bodyNode = document.getElementsByTagName('body')[0];		
    } catch (e) { }
}());
/**
 * Get browser type.
 * 
 * @return string 'ie' | 'mozilla' |'safari' | 'other'
 */
Brain2.Dom.getBrowserType = function() {
    if (!Brain2.Dom._browserType) {
        var userAgent = window.navigator.userAgent.toLowerCase(), keys = ['msie', 'firefox', 'safari', 'gecko'];
        var names = ['ie', 'mozilla', 'safari', 'mozilla'];
        for ( var i = 0; i < keys.length; i++) {
            if (userAgent.indexOf(keys[i]) >= 0) {
                Brain2.Dom._browserType = names[i];
                break;
            }
        }
    }
    return Brain2.Dom._browserType;
};



Brain2.Dom.loadScript = function(path, callback, async) {
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = path;
    if(async === true){
        script.async = true;
    }
    if (callback instanceof Function) {
        script.onreadystatechange = function() {
            if (this.readyState === 'loaded') {
                callback.apply({}, []);
            }
        };
        script.onload = callback;
    }
    Brain2.Dom._headNode.appendChild(script);
};

Brain2.Dom.ready = function(f) {
    if (f instanceof Function) {
        if (Brain2.Dom._domReady) {
            f.apply({}, []);
        } else {
            Brain2.Dom._readyCallbacks.push(f);
        }
    } else {
        throw ('Parameter for Brain2.Dom.ready is not a function!');
    }
};

Brain2.Dom.processReadyCallbacks = function() {
    Brain2.Dom._headNode = document.getElementsByTagName('head')[0];
    Brain2.Dom._bodyNode = document.getElementsByTagName('body')[0];
    var arr = Brain2.Dom._readyCallbacks;
    for ( var i in arr) {
        var f = arr[i];
        f.apply({}, []);
    }
    Brain2.Dom._domReady = true;
};

Brain2.Dom.bindDomReadyEvent = function() {
    if (document.addEventListener) {
        document.addEventListener("DOMContentLoaded", function() {
            Brain2.Dom.processReadyCallbacks();
        }, false);
    } else if (document.all && !window.opera) {
        var scriptStr = '<script type="text/javascript" id="contentloadtag" defer="defer" src="javascript:void(0)"><\/script>';
        document.write(scriptStr);
        var contentloadtag = document.getElementById("contentloadtag");
        contentloadtag.onreadystatechange = function() {
            if (this.readyState === 'complete') {
                Brain2.Dom.processReadyCallbacks();
            }
        };
    }
};

Brain2.Dom.getAttributesNode = function(elm) {
    if (typeof Node === 'object') {
        if (typeof Node.prototype.getAllAttributes != 'function') {
            Node.prototype.getAllAttributes = function() {
                var rv = {};
                for ( var i = 0; i < this.attributes.length; i++) {
                    rv[this.attributes.item(i).nodeName] = this.attributes.item(i).nodeValue;
                }
                return rv;
            };
        }
        return elm.getAllAttributes();
    } else {
        var attributes = {};
        var i = 0;
        size = elm.attributes.length;
        for (; i < size; i++) {
            var attr = elm.attributes[i];
            attributes[attr.name] = attr.value;
        }
        return attributes;
    }
};

Brain2.Dom.find = function(selector){
    selector = selector + '';
    var result = false;
    if(selector.length > 1){		
        var h = selector.substring(0,1);
        var k = selector.substring(1);
        if(h === "#"){
            result = document.getElementById(k);
            if(result === null){
                result = false;
            }
        } else {
            result = document.getElementsByTagName(k);
            if(result.length === 0){
                result = false;
            }
        }
    }
    return result;
};

// add handler to Dom
Brain2.Dom.bindDomReadyEvent();
Brain2.Dom.addListen("load", window, function() { 
    if (!Brain2.Dom._domReady) {
        Brain2.Dom.processReadyCallbacks();
    }
});
Brain2.Dom.addListen("unload", window, function() {
    var arr = Brain2.Dom._unbindEventFunc;
    for(var i=0; i<arr.length; i++){
        var f = arr[i];
        f.apply({},[]);
    }
});

Brain2.Dom.makeNode = function(name,attrs){
    var node = document.createElement(name);
    if(typeof attrs === 'object'){
        for(var k in attrs){
            node.setAttribute(k, attrs[k]+'');
        }
    }
    if(name === 'iframe'){        
        node.setAttribute('noResize', 'noResize');
        node.setAttribute('frameBorder', '0');
        node.setAttribute('border', '0');
        node.setAttribute('cellSpacing', '0');
        node.setAttribute('marginHeight', '0');
        node.setAttribute('marginWidth', '0');        
        node.setAttribute('scrolling', 'no'); 
        node.setAttribute('allowTransparency', 'true');
    }
    return node;
};


// --------- Module cookie -------------------
Brain2.cookie = function(key, value, options) {
    if (arguments.length > 1 && String(value) !== "[object Object]") {
        options = (typeof options === 'object') ? options : {};
        if (value === null || value === undefined) {
            options.expires = -1;
        }
        if (typeof options.expires === 'number') {
            var days = options.expires, t = options.expires = new Date();
            t.setDate(t.getDate() + days);
        }
        value = String(value);
        return (document.cookie = [encodeURIComponent(key), '=', options.raw ? value : encodeURIComponent(value),
            options.expires ? '; expires=' + options.expires.toUTCString() : '',
            options.path ? '; path=' + options.path : '', options.domain ? '; domain=' + options.domain : '',
            options.secure ? '; secure' : ''].join(''));
    }
    options = value || {};
    var result, decode = options.raw ? function(s) {
        return s;
    } : decodeURIComponent;
    return (result = new RegExp('(?:^|; )' + encodeURIComponent(key) + '=([^;]*)').exec(document.cookie)) ? decode(result[1]) : null;
};

// --------- Module Log -------------------
Brain2.Log = {
    _logContainer : false,
    _showLog : false
};
Brain2.Log.getContainer = function() {
    if (typeof Brain2.Log._logContainer != 'object') {
        var n = document.createElement('ol');
        n.setAttribute('id', 'fosp-log-test');
        document.getElementsByTagName('body')[0].appendChild(n);
        Brain2.Log._logContainer = n;
    }
    return Brain2.Log._logContainer;
};
Brain2.Log.test = function(o) {
    if (Brain2.Log._showLog) {
        Brain2.Dom.ready(function() {
            var logNode = Brain2.Log.getContainer();
            logNode.innerHTML = logNode.innerHTML + '<li>Test: ' + o + ' passed! </li>';
        });
    }
};
Brain2.Log.info = function(o) {
    if (Brain2.Log._showLog) {
        Brain2.Dom.ready(function() {
            var logNode = Brain2.Log.getContainer();
            logNode.innerHTML = logNode.innerHTML + '<li>Info: ' + o + '</li>';
        });
    }
};

//--------- Load Module api -------------------

Brain2.api = {
    callbackQueue:{}
};

Brain2.api.shareLinkDirect = function(url, message ,title ,description ,src_image , cb){    
    var param = ("href="+ encodeURIComponent(url) + "&message=" + encodeURIComponent(message));
    param += ('&title=' + encodeURIComponent(title));
    param += ('&description=' + encodeURIComponent(description));
    param += ('&src_image=' + encodeURIComponent(src_image));
    param += ('&callback=' + encodeURIComponent(cb));
    param += ('&_t='+(new Date().getTime()));
    var shareUrl = Brain2._config.baseUrl + "/share/directshare?"+param;
    Brain2.Dom.loadScript(shareUrl);
};

//--------- Load Module util -------------------
Brain2.util = {};
Brain2.util.trim = function(str)
{    
    return typeof str === 'string' ? str.replace(/^\s\s*/, '').replace(/\s\s*$/, '') : '';
};
Brain2.util.replaceAll = function(temp, stringToFind,stringToReplace){    
    var index = temp.indexOf(stringToFind);
    while(index != -1){
        temp = temp.replace(stringToFind,stringToReplace);
        index = temp.indexOf(stringToFind);
    }
    return temp;
};
Brain2.util.safeString = function(s, length)
{    
    if(typeof length === 'undefined') length = 200;
    s = s.substring(0,length);	
    s = Brain2.util.replaceAll(s, 'â€œ','');
    s = Brain2.util.replaceAll(s, 'â€','');
    s = Brain2.util.replaceAll(s, '"','');	
    s = Brain2.util.replaceAll(s, "'",'');	
    return s;
};

Brain2.analytics = {
    pageMetaInfo: function(){        
        if(typeof Brain2._cachePool['meta_description'] !== 'string'){
            Brain2._cachePool['meta_description'] = '';
            var list = document.getElementsByTagName('meta');
            var size = list.length;
            for ( var i = 0; i < size; i++) {
                var meta = list[i];
                var metaname = meta.name;
                if (metaname) {
                    if (metaname === 'description') {                   
                        if (meta.content) {
                            Brain2._cachePool['meta_description'] = (meta.content);
                        }
                    }
                }
            }
        }
        if(typeof Brain2._cachePool['all_url_images'] !== 'object'){
            Brain2._cachePool['all_url_images'] = [];
            var imgs = document.getElementsByTagName('img');        
            var imgsSize = imgs.length;       
            for(var i=0; i< imgsSize; i++) {
                var src = imgs[i].src;
                var img = new Image(); 
                img.src = src;
                if((img.width > 60 && img.height > 60) && (img.width < 888 && img.height < 888) ) {
                    if(src.length === (src.lastIndexOf('.jpg')+4)){                   
                        Brain2._cachePool['all_url_images'].push(encodeURIComponent(src));                    
                    }
                }
            }
        }
        var title = document.title;
        var description = Brain2._cachePool['meta_description'];
        return {
            'title':title,
            'description':description,
            'all_url_images':Brain2._cachePool['all_url_images']
        };
    }
};

Brain2.UI = {
    _win : false
    ,
    popupCenter : function(pageURL, w, h) {
        
        if(Brain2.UI._win !== false){
            if(typeof Brain2.UI._win.focus === 'function') {
                Brain2.UI._win.focus();
                return false;
            }
        }
        
        var left = (screen.width/2)-(w/2);
        var top = (screen.height/2)-(h/2);
        if(top > 150 ) top -= 70;
        var params = 'menubar=0,resizable=1,toolbar=no,location=no,directories=no,status=yes,menubar=no,scrollbars=no,resizable=yes,copyhistory=no,'; 
        params = params + ('width='+w+', height='+h+', top='+top+', left='+left);
        var win = window.open (pageURL, "_blank", params);
        if(typeof win.focus === 'function') {
            win.focus();           
        }
        Brain2.UI._win = win;
        return win;
    }
};