const dbConstants=require('../constants/DbConstants');
const collectionNames=require("../constants/CollectionNames")
const  getCategories = (db,apiData,successCallBack,failureCallBack)=>{
    return db.collection(collectionNames.TEMPLATE_CATEGORIES).where(dbConstants.KEY_REGION_ID,"==",apiData[dbConstants.KEY_REGION_ID])
        .orderBy(dbConstants.KEY_PRIORITY).get().then((successRes)=>{
            if(successRes.empty){
                return successCallBack([])
            }
            let categoriesArr = successRes.docs.map((doc)=>{
                return doc.data();
            })
            return successCallBack(categoriesArr)
        }).catch((err)=>{
            return failureCallBack(err)
        })
}

module.exports={
    getCategories
}