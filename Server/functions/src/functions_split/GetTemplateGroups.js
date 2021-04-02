const dbConstants=require('../constants/DbConstants');
const constants=require('../constants/Constants')
const collectionNames=require("../constants/CollectionNames")
const  getTemplateGroups = (db, data, successCallBack, failureCallBack)=>{
    let regionId=data[dbConstants.KEY_REGION_ID]
    let from=data[dbConstants.KEY_FROM]
    let limit = data[dbConstants.KEY_LIMIT]
    let query = db.collection(`${collectionNames.TEMPLATE_GROUPS}`)
    if(regionId){
        query=query.where(dbConstants.KEY_REGION_ID,"==",regionId)
    }
    if(from){
            query=query.where(dbConstants.KEY_CREATEDTIME,">",from)
    }

    limit = Math.min(limit||constants.TEMPLATE_GROUP_LIMIT,constants.TEMPLATE_GROUP_LIMIT)
    query=query.orderBy(dbConstants.KEY_CREATEDTIME,"asc").limit(limit)
    return query.get().then((successRes)=>{
            if(successRes.empty){
                return successCallBack([])
            }
            let templateGroupsArr = successRes.docs.map((doc)=>{
                return doc.data();
            })
            return successCallBack(templateGroupsArr)
        }).catch((err)=>{
            return failureCallBack(err)
        })
}

module.exports={
    getTemplateGroups
}