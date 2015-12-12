var DOMImplementation = require('./DOMImplementation');
var HTMLParser = require('./HTMLParser');
var Window = require('./Window');

exports.createDOMImplementation = function() {
  return new DOMImplementation();
};

exports.createDocument = function(html,url) {
  if (html) {
    var parser = new HTMLParser(url?url:"about:blank");
    parser.parse(html, true);
    return parser.document();
  }
  return new DOMImplementation().createHTMLDocument("");
};

exports.createWindow = function(html,url) {
  var document = exports.createDocument(html,url);
  return new Window(document,url);
};

exports.impl = require('./impl');
