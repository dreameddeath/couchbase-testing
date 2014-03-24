function (doc, meta) {
  if (meta.type == "base64") {
    var binary = decodeBase64(doc);
    var ending = binary.slice(-4);
    var result=0;
    for(var i=0;i<ending.length;++i){
      result+=(ending[i]<<(i*8));
    }
    if(result!=binary.length){
      emit(meta.id,{result:result,size:binary.length,results:ending}) ;
    }
  }
}