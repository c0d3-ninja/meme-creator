const replacePath=function (path, data) {
    let resultPath=""
    path=path.split("/")
    path.map((value,index)=>{
        if(value){
            if(value.includes(":")){
                value=value.replace(":","")
                value=data[value]
            }
            if(index===0){
                resultPath+=value
            }else{
                resultPath+="/"+value
            }
        }
    })
    return resultPath
}

module.exports={
    replacePath
}