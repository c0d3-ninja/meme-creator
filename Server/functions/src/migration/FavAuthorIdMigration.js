const dbConstants=require("../constants/DbConstants")
const {getTemplateIdFromFavId} = require("../utils/CommonUtils")
const collectionNames=require("../constants/CollectionNames")
let triggerAuthorIdMigration=(db)=>{
    return new Promise((resolve,reject)=>{
        return  updateQueryBatch(db,resolve,reject)
    })
}
async function updateQueryBatch(db, resolve, reject) {
    let query=db.collection(`${collectionNames.FAVORITES}`).orderBy('__name__')
    return query.get()
        .then((snapshot) => {
            console.log("migration started...")
            // When there are no documents left, we are done
            if (snapshot.size === 0) {
                return 0;
            }
            let favIdDocRefMap={}
            let templateIdFavIdMap = {}
            console.log("Total fav documents===>",snapshot.docs.length)
            snapshot.docs.forEach((doc)=>{
                if(doc.exists) {
                    let data = doc.data()
                    let favId = data[dbConstants.KEY_ID]
                    if (!data.hasOwnProperty(dbConstants.KEY_AUTHOR_ID)) {
                    templateIdFavIdMap[getTemplateIdFromFavId(favId)] = favId
                    favIdDocRefMap[favId] = doc.ref
                }
                }
            })
            let templatesPromiseArr = Object.keys(templateIdFavIdMap).map((templateId)=>{
                return db.doc(`${collectionNames.TEMPLATES}/${templateId}`).get()
            })

            return Promise.all(templatesPromiseArr).then((successRes)=>{
                let templateIdAuthorIdMap = {}
                successRes.map((doc)=>{
                    if(doc && doc.exists){
                        let data = doc.data()
                        templateIdAuthorIdMap[data[dbConstants.KEY_ID]]=data[dbConstants.KEY_CREATEDBY]
                    }
                })
                let favIdAuthorIdMap = {}
                Object.keys(templateIdAuthorIdMap).map((templateId)=>{
                    favIdAuthorIdMap[templateIdFavIdMap[templateId]] = templateIdAuthorIdMap[templateId]
                })

                let batchArr = []
                let batchIndex=0;
                let thresholdLen=0;
                // Delete documents in a batch
                batchArr.push(db.batch());
                Object.keys(favIdDocRefMap).map((favId)=>{
                    let currentRef = favIdDocRefMap[favId]
                    thresholdLen++;
                    if(thresholdLen===499){
                        batchArr.push(db.batch())
                        thresholdLen=0;
                    }
                    let authorId = favIdAuthorIdMap[favId]|| null
                    batchArr[batchArr.length-1].update(currentRef,{[dbConstants.KEY_AUTHOR_ID]:authorId});
                })
                batchArr.forEach(async batch => await batch.commit());
                console.log("migration completed")
                return resolve(0) ;

            }).catch((err)=>{
                console.log("Migration unsuccessful",err)
                return reject()
            })
        })
        .catch((err)=>{
            console.log("exception while updating collection===>",err)
            return reject();
        });
}

module.exports={
    triggerAuthorIdMigration
}