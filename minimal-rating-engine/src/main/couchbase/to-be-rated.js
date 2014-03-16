function (doc, meta) {
  if (meta.type == "base64") {
    var binary = decodeBase64(doc);
    var ending = binary.slice(-4);
    var result=""
    for(var i=0;i<ending.length;++i){
      result+=String.fromCharCode(ending[i]);
    }
    if(result!="DONE"){
      emit(meta.id,result) ;
    }
  }
  
}