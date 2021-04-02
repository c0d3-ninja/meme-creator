const dbConstants=require("../constants/DbConstants")
const {getSearchTagsAr,getSearchTagsForServer} = require("../utils/CommonUtils")
let triggerSearchTagsArrMigration=(db,collectionPath,batchSize)=>{
    let collectionRef = db.collection(collectionPath);
    let query = collectionRef.orderBy('__name__').where(dbConstants.KEY_SEARCH_TAGS_ARRAY,"array-contains","Thavasi Movie").limit(batchSize);
    return new Promise((resolve,reject)=>{
        updateQueryBatch(db,query,resolve,reject)
    })
}

async function updateQueryBatch(db, query, resolve, reject,collectionPath) {
    query.get()
        .then((snapshot) => {
            console.log("migration started...")
            // When there are no documents left, we are done
            if (snapshot.size === 0) {
                return 0;
            }

            let batchArr = []
            let batchIndex=0;
            let thresholdLen=0;

            // Delete documents in a batch
            batchArr.push(db.batch());
            snapshot.docs.forEach((doc) => {
                thresholdLen++;
                if(thresholdLen===499){
                    batchArr.push(db.batch())
                    thresholdLen=0;
                }
                let data=doc.data();
                let searchTags = data[dbConstants.KEY_SEARCHTAGS]
                searchTags = searchTags.replace("Kgf,","Kgf Movie,")
                searchTags=getSearchTagsForServer(searchTags)
                let searchTagsArr = searchTags.split(",")
                data[dbConstants.KEY_SEARCHTAGS]=searchTags
                data[dbConstants.KEY_SEARCH_TAGS_ARRAY]=searchTagsArr
                // data=Object.assign(data,{[dbConstants.KEY_SEARCH_TAGS_ARRAY]:getSearchTagsArr(data[dbConstants.KEY_SEARCHTAGS])})
                // batchArr[batchArr.length-1].update(doc.ref,data);
                batchArr[batchArr.length-1].delete(doc.ref);
            });

           batchArr.forEach(async batch => await batch.commit());
           console.log("migration completed")

           return 0 ;
        })
        .catch((err)=>{
            console.log("exception while updating collection===>"+collectionPath,err)
            reject()
            return -1;
        });
}

module.exports={
    triggerSearchTagsArrMigration
}