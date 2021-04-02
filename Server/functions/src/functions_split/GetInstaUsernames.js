const dbConstants=require('../constants/DbConstants');
const collectionNames=require("../constants/CollectionNames")
const  getInstaUsernames = (db,userIds=[],successCallBack,failureCallBack)=>{
    let instaUsernameMap={}
    let createdByPromise =[]
    userIds.map((createdById)=>{
        if(!instaUsernameMap.hasOwnProperty(createdById)){
            createdByPromise.push(db.doc(`${collectionNames.USERS}/${createdById}`).get())
            instaUsernameMap[createdById]=null
        }
    })
    return Promise.all(createdByPromise).then((successRes)=>{
        successRes.map((successResDoc)=>{
            if(!successResDoc.exists){
                return;
            }
            let data = successResDoc.data()
            instaUsernameMap[data[dbConstants.KEY_ID]]=data[dbConstants.KEY_INSTA_USERNAME]
        })
        return successCallBack && successCallBack(instaUsernameMap)
    }).catch((err)=>{
        return failureCallBack && failureCallBack(err)
    })

}

module.exports={
    getInstaUsernames
}