const collectionNames=require("../constants/CollectionNames")
const dbConstants=require("../constants/DbConstants")
const getUserDetails = (db,options,successCallBack,failureCallBack)=>{
    let query = db.doc(`${collectionNames.USERS}/${options[dbConstants.KEY_ID]}`).get()
    if(options[dbConstants.KEY_RETURN_PROMISE]){
        return  query
    }
    return query.then((userDoc)=>{
        if(!userDoc.exists){
            return successCallBack(null)
        }
        return successCallBack(userDoc.data())
    }).catch((err)=>{
        return failureCallBack(err)
    })

}

module.exports={
    getUserDetails
}