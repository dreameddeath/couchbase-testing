function (doc, meta) {
  if(meta.id.indexOf("job/")==0){
    if(doc.state=="DONE"){
  	emit([meta.id,doc.state], doc["@t"]);
    }
  }
}