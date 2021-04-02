const collectionNames=require("../constants/CollectionNames")
const dbConstants=require("../constants/DbConstants")
const  getTemplate = (db,options,successCallBack,failureCallBack)=>{
    let query = db.doc(`${collectionNames.TEMPLATES}/${options[dbConstants.KEY_ID]}`).get()
    if(options[dbConstants.KEY_RETURN_PROMISE]){
        return query
    }
    return query.then((docRes)=>{
        if(!docRes.exists){
            return successCallBack(null)
        }
        return successCallBack(docRes.data)
    }).catch((err)=>{
        return failureCallBack(err)
    })
}

module.exports={
    getTemplate
}