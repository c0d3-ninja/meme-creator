let deleteCollection=(db,collectionPath,batchSize)=>{
    let collectionRef = db.collection(collectionPath);
    let query = collectionRef.orderBy('__name__').limit(batchSize);
    return new Promise((resolve,reject)=>{
        deleteQueryBatch(db,query,resolve,reject)
    })
}

function deleteQueryBatch(db, query, resolve, reject,collectionPath) {
    query.get()
        .then((snapshot) => {
            // When there are no documents left, we are done
            if (snapshot.size === 0) {
                return 0;
            }

            // Delete documents in a batch
            let batch = db.batch();
            snapshot.docs.forEach((doc) => {
                batch.delete(doc.ref);
            });

            return batch.commit().then(() => {
                return snapshot.size;
            });
        }).then((numDeleted) => {
        if (numDeleted === 0) {
            resolve();
            return 1;
        }

        // Recurse on the next process tick, to avoid
        // exploding the stack.
        return process.nextTick(() => {
             deleteQueryBatch(db, query, resolve, reject);
        });
    })
        .catch((err)=>{
            console.log("exception while deleting collection===>"+collectionPath,err)
            reject()
            return -1;
        });
}

module.exports={
    deleteCollection
}