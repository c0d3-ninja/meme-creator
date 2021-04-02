const templateValidation =  require("./src/constants/validation/TemplateValidation") ;

const functions = require('firebase-functions');

const admin = require('firebase-admin');
const httpCodes = require("./src/constants/HttpConstants");
const dbConstants=require('./src/constants/DbConstants');
const collectionNames=require("./src/constants/CollectionNames")
const constants = require("./src/constants/Constants")
const {getContextUser,printException,checkNull,getCurrentTimeMillis,daysToMillis,generateRandomId,getSearchTagsForServer,getSearchTagsForClient,
    getSearchTagsArr,isAdminAccount,sanitizeUsername,getTemplateIdFromFavId} = require('./src/utils/CommonUtils')
const {isValidRegion} = require("./src/constants/validation/UserValidation")
const {isValidCategory,generateCategoryId,generateCategoryName} = require("./src/constants/validation/AdminAddCategoryValidation")
const {isValidSendFeedbackRequest} = require("./src/constants/validation/SendFeedbackValidation")
const {isValidTemplateGroupRequest} = require("./src/constants/validation/TemplateGroupValidation")
const algoliaSearch = require('algoliasearch');
const adminConstants = require("./src/constants/AdminConstants")
const {deleteCollection} = require("./src/functions_split/DeleteCollection")
const {getCategories} = require("./src/functions_split/GetCategories")
const {getTemplateGroups} = require("./src/functions_split/GetTemplateGroups")
const {getInstaUsernames} = require("./src/functions_split/GetInstaUsernames")
const {getTemplate} = require("./src/functions_split/GetTemplate")
const {getUserDetails} = require("./src/functions_split/Users")
const BuildConfig = require("./BuildConfig")
const {isValidNotification} = require("./src/constants/validation/NotificationValidation")
const {isValidMemeRequest} = require("./src/constants/validation/MemeValidation")
const {triggerSearchTagsArrMigration} = require("./src/migration/SearchStrArrMigration")
const {triggerAuthorIdMigration} = require("./src/migration/FavAuthorIdMigration")


if(BuildConfig.DEBUG){
    let  serviceAccount
    if(BuildConfig.ISPRODUCTION){
         serviceAccount = require("./prod_serviceaccount")
    }else{
        serviceAccount = require("./serviceaccount")
    }
    const adminConfig = JSON.parse(process.env.FIREBASE_CONFIG);
    adminConfig.credential = admin.credential.cert(serviceAccount);
    admin.initializeApp(adminConfig);
}else{
    admin.initializeApp(functions.config().firebase);
}




const searchClient = algoliaSearch("test","test");
const feedTemplateFeedIndex = searchClient.initIndex(BuildConfig.VALUE_FEED_SEARCH_INDEX);
const favoriteTemplateIndex = searchClient.initIndex(BuildConfig.VALUE_FAV_SEARCH_INDEX);



let db = admin.firestore();
const UNAUTHORIZED = {[dbConstants.KEY_STATUSCODE]:httpCodes.UNAUTHORIZED}
const BADREQUEST = {[dbConstants.KEY_STATUSCODE]:httpCodes.BADREQUEST}
const INTERNALSERVERERROR={[dbConstants.KEY_STATUSCODE]:httpCodes.INTERNALSERVERERROR}
const NOCONTENT = {[dbConstants.KEY_STATUSCODE]:httpCodes.NOCONTENT}
const NOTFOUND = {[dbConstants.KEY_STATUSCODE]:httpCodes.NOTFOUND}
const CONFLICT = {[dbConstants.KEY_STATUSCODE]:httpCodes.CONFLICT}
const BLOCKED={[dbConstants.KEY_STATUSCODE]:httpCodes.BLOCKED}
const MESSAGE={[dbConstants.KEY_STATUSCODE]:httpCodes.MESSAGE,
    [dbConstants.KEY_DATA]:{[dbConstants.KEY_MESSAGE]:constants.UPDATE_MESSAGE}}



exports.triggerCreateTemplate = functions.firestore.document(`${collectionNames.TEMPLATES}/{templateId}`).onCreate((snapshot,context)=>{
    let data = snapshot.data();
    let docId =data[dbConstants.KEY_ID]
    let searchTags = data[dbConstants.KEY_SEARCHTAGS]
    data[dbConstants.VALUE_FACETS]=searchTags.split(",")
    data[dbConstants.KEY_ALGOLIA_OBJECTID]=docId
    return feedTemplateFeedIndex.saveObject(data).catch((exception)=>{
        printException("triggerCreateTemplate.indexSearch","",exception)
        data[dbConstants.KEY_FAILURE_DETAILS] = {
            exception,
            [dbConstants.KEY_OPERATION]:dbConstants.VALUE_ONCREATE,
            [dbConstants.KEY_DOC_PATH]:`${collectionNames.TEMPLATES}/${context.params.templateId}`,
            [dbConstants.KEY_SEARCH_INDEX]:dbConstants.VALUE_FEED_SEARCH_INDEX
        }
        return db.doc(`${collectionNames.FAILED_SEARCH_INDEX}/${docId}`).set(data)
    })
})

exports.triggerUpdateTemplate = functions.firestore.document(`${collectionNames.TEMPLATES}/{templateId}`).onUpdate((snapshot,context)=>{
    let data = snapshot.after.data();
    let docId =data[dbConstants.KEY_ID]
    data[dbConstants.KEY_ALGOLIA_OBJECTID]=docId
    let searchTags = data[dbConstants.KEY_SEARCHTAGS]
    data[dbConstants.VALUE_FACETS]=searchTags.split(",")
    return feedTemplateFeedIndex.saveObject(data).then(()=>{
        return db.doc(`${collectionNames.FAILED_SEARCH_INDEX}/${docId}`).delete()
    }).catch((exception)=>{
        printException("triggerCreateTemplate.indexSearch","",exception)
        data[dbConstants.KEY_FAILURE_DETAILS] = {
            exception,
            [dbConstants.KEY_OPERATION]:dbConstants.VALUE_ONUPDATE,
            [dbConstants.KEY_DOC_PATH]:`${collectionNames.TEMPLATES}/${context.params.templateId}`,
            [dbConstants.KEY_SEARCH_INDEX]:dbConstants.VALUE_FEED_SEARCH_INDEX
        }
        return db.doc(`${collectionNames.FAILED_SEARCH_INDEX}/${docId}`).set(data)
    })
})

exports.triggerDeleteTemplate = functions.firestore.document(`${collectionNames.TEMPLATES}/{templateId}`).onDelete((snapshot,context)=>{
    let data = snapshot.data();
    let docId =data[dbConstants.KEY_ID]
    data[dbConstants.KEY_ALGOLIA_OBJECTID]=docId
    return feedTemplateFeedIndex.deleteObject(docId).then(()=>{
        return db.doc(`${collectionNames.FAILED_SEARCH_INDEX}/${docId}`).delete()
    }).catch((exception)=>{
        printException("triggerDeleteTemplate.indexSearch","",exception)
        data[dbConstants.KEY_FAILURE_DETAILS] = {
            exception,
            [dbConstants.KEY_OPERATION]:dbConstants.VALUE_ONDELETE,
            [dbConstants.KEY_DOC_PATH]:`${collectionNames.TEMPLATES}/${context.params.templateId}`,
            [dbConstants.KEY_SEARCH_INDEX]:dbConstants.VALUE_FEED_SEARCH_INDEX
        }
        return db.doc(`${collectionNames.FAILED_SEARCH_INDEX}/${docId}`).set(data)
    })
})

exports.triggerTemplateUploadNotifyToAdmin = functions.firestore.document(`${collectionNames.UNVERIFIED_TEMPLATES}/{templateId}`).onCreate((snapshot)=>{
    let data = snapshot.data();
    return db.doc(`${collectionNames.USERS}/${data[dbConstants.KEY_CREATEDBY]}`).get().then((userRes)=>{
        if(!userRes.exists){
            return;
        }
        let userDocData = userRes.data()
        let name = userDocData[dbConstants.KEY_EMAIL].replace("@gmail.com","")
        let messagingObj = {
            data:{
                [dbConstants.KEY_FCM_TITLE]:`Template upload`,
                [dbConstants.KEY_FCM_DESC]:`${name} uploaded a new template`,
                [dbConstants.KEY_FCM_ID]:dbConstants.VALUE_FCM_TEMPLATES_CHANNEL,
                [dbConstants.KEY_IMAGEURL]:data[dbConstants.KEY_IMAGEURL]
            },
            topic:constants.ADMIN_FCM_TOPIC_ID
        }

        return admin.messaging().send(messagingObj).then(()=>{
            return true
        }).catch((err)=>{
            printException("triggerTemplateUploadNotifyToAdmin","AdminEmail@",err)
            return INTERNALSERVERERROR
        })
    })

})

exports.triggerMemeUploadNotifyToAdmin = functions.firestore.document(`${collectionNames.UNVERIFIED_MEMES}/{templateId}`).onCreate((snapshot)=>{
    let data = snapshot.data();
    return db.doc(`${collectionNames.USERS}/${data[dbConstants.KEY_CREATEDBY]}`).get().then((userRes)=>{
        if(!userRes.exists){
            return;
        }
        let userDocData = userRes.data()
        let name = userDocData[dbConstants.KEY_EMAIL].replace("@gmail.com","")
        let messagingObj = {
            data:{
                [dbConstants.KEY_FCM_TITLE]:`Meme upload`,
                [dbConstants.KEY_FCM_DESC]:`${name} uploaded a new meme`,
                [dbConstants.KEY_FCM_ID]:dbConstants.VALUE_FCM_MEMES_CHANNEL,
                [dbConstants.KEY_IMAGEURL]:data[dbConstants.KEY_IMAGEURL]
            },
            topic:constants.ADMIN_FCM_TOPIC_ID
        }

        return admin.messaging().send(messagingObj).then(()=>{
            return true
        }).catch((err)=>{
            printException("triggerMemeUploadNotifyToAdmin","AdminEmail@",err)
            return INTERNALSERVERERROR
        })
    })

})

exports.triggerDeleteUnVerifiedTemplate = functions.firestore.document(`${collectionNames.UNVERIFIED_TEMPLATES}/{templateId}`).onDelete((snapshot)=>{
    let data = snapshot.data();
    let isSpam = data[dbConstants.KEY_IS_SPAM]
    return db.doc(`${collectionNames.USERS}/${data[dbConstants.KEY_CREATEDBY]}`).get().then((userRes)=>{
        if(!userRes.exists){
            return;
        }
        let userDocData = userRes.data()
        let notificationTokens = userDocData[dbConstants.KEY_NOTIFICATION_TOKEN] ||[]
        if(notificationTokens.length===0){
            return;
        }
        let messagingObj = {
            data:{
                [dbConstants.KEY_FCM_TITLE]:isSpam?"Template verification failed ❌":"Template verified ✅",
                [dbConstants.KEY_FCM_DESC]:isSpam?"It seems the uploaded template is invalid,Kindly upload a valid template":"Congrats, Other users can use your template now.Thanks for your contribution",
                [dbConstants.KEY_FCM_ID]:dbConstants.VALUE_FCM_VERIFICATION_CHANNEL,
                [dbConstants.KEY_IMAGEURL]:data[dbConstants.KEY_IMAGEURL]
            }
        }

        return admin.messaging().sendToDevice(notificationTokens,messagingObj)
    })

})


exports.triggerDeleteUnVerifiedMeme = functions.firestore.document(`${collectionNames.UNVERIFIED_MEMES}/{memeId}`).onDelete((snapshot)=>{
    let data = snapshot.data();
    let isSpam = data[dbConstants.KEY_IS_SPAM]
    if(isSpam){
        return;
    }
    return db.doc(`${collectionNames.USERS}/${data[dbConstants.KEY_CREATEDBY]}`).get().then((userRes)=>{
        if(!userRes.exists){
            return;
        }
        let userDocData = userRes.data()
        let notificationTokens = userDocData[dbConstants.KEY_NOTIFICATION_TOKEN] ||[]
        if(notificationTokens.length===0){
            return;
        }
        let messagingObj = {
            data:{
                [dbConstants.KEY_FCM_TITLE]:"Meme verified ✅",
                [dbConstants.KEY_FCM_DESC]:"Congrats, Other users can share your meme now.Thanks for your contribution",
                [dbConstants.KEY_FCM_ID]:dbConstants.VALUE_FCM_VERIFICATION_CHANNEL,
                [dbConstants.KEY_IMAGEURL]:data[dbConstants.KEY_IMAGEURL]
            }
        }
        return admin.messaging().sendToDevice(notificationTokens,messagingObj)
    })

})

exports.triggerReportsUserCollectionDelete = functions.firestore.document(`${collectionNames.REPORTS}/{templateId}`).onDelete((snapshot)=>{
    let data = snapshot.data();
    let docId =data[dbConstants.KEY_ID]
    return deleteCollection(db,`${collectionNames.REPORTS}/${docId}/${collectionNames.USERS}`,constants.REPORT_USERS_COLLECTION_BATCH_SIZE)
})

exports.triggerMemeReportsUserCollectionDelete = functions.firestore.document(`${collectionNames.REPORTS_MEME}/{templateId}`).onDelete((snapshot)=>{
    let data = snapshot.data();
    let docId =data[dbConstants.KEY_ID]
    return deleteCollection(db,`${collectionNames.REPORTS_MEME}/${docId}/${collectionNames.USERS}`,constants.REPORT_USERS_COLLECTION_BATCH_SIZE)
})

exports.triggerCreateFavoriteTemplate = functions.firestore.document(`${collectionNames.FAVORITES}/{templateId}`).onCreate((snapshot,context)=>{
    let data = snapshot.data();
    let docId =data[dbConstants.KEY_ID]
    data[dbConstants.KEY_ALGOLIA_OBJECTID]=docId
    let searchTags = data[dbConstants.KEY_SEARCHTAGS]
    data[dbConstants.VALUE_FACETS]=searchTags.split(",")
    return favoriteTemplateIndex.saveObject(data).catch((exception)=>{
        printException("triggerCreateFavoriteTemplate.indexSearch","",exception)
        data[dbConstants.KEY_FAILURE_DETAILS] = {
            exception,
            [dbConstants.KEY_OPERATION]:dbConstants.VALUE_ONCREATE,
            [dbConstants.KEY_DOC_PATH]:`${collectionNames.FAVORITES}/${context.params.templateId}`,
            [dbConstants.KEY_SEARCH_INDEX]:dbConstants.VALUE_FAV_SEARCH_INDEX
        }
        return db.doc(`${collectionNames.FAILED_SEARCH_INDEX}/${docId}`).set(data)
    })
})

exports.triggerUpdateFavoriteTemplate = functions.firestore.document(`${collectionNames.FAVORITES}/{templateId}`).onUpdate((snapshot,context)=>{
    let data = snapshot.after.data();
    let docId =data[dbConstants.KEY_ID]
    data[dbConstants.KEY_ALGOLIA_OBJECTID]=docId
    let searchTags = data[dbConstants.KEY_SEARCHTAGS]
    data[dbConstants.VALUE_FACETS]=searchTags.split(",")
    return favoriteTemplateIndex.saveObject(data).then(()=>{
        return db.doc(`${collectionNames.FAILED_SEARCH_INDEX}/${docId}`).delete()
    }).catch((exception)=>{
        printException("triggerUpdateFavoriteTemplate.indexSearch","",exception)
        data[dbConstants.KEY_FAILURE_DETAILS] = {
            exception,
            [dbConstants.KEY_OPERATION]:dbConstants.VALUE_ONUPDATE,
            [dbConstants.KEY_DOC_PATH]:`${collectionNames.FAVORITES}/${context.params.templateId}`,
            [dbConstants.KEY_SEARCH_INDEX]:dbConstants.VALUE_FAV_SEARCH_INDEX
        }
        return db.doc(`${collectionNames.FAILED_SEARCH_INDEX}/${docId}`).set(data)
    })
})

exports.triggerDeleteFavoriteTemplate = functions.firestore.document(`${collectionNames.FAVORITES}/{templateId}`).onDelete((snapshot,context)=>{
    let data = snapshot.data();
    let docId =data[dbConstants.KEY_ID]
    data[dbConstants.KEY_ALGOLIA_OBJECTID]=docId
    return favoriteTemplateIndex.deleteObject(docId).then(()=>{
        return db.doc(`${collectionNames.FAILED_SEARCH_INDEX}/${docId}`).delete()
    }).catch((exception)=>{
        printException("triggerDeleteFavoriteTemplate.indexSearch","",exception)
        data[dbConstants.KEY_FAILURE_DETAILS] = {
            exception,
            [dbConstants.KEY_OPERATION]:dbConstants.VALUE_ONCREATE,
            [dbConstants.KEY_DOC_PATH]:`${collectionNames.FAVORITES}/${context.params.templateId}`,
            [dbConstants.KEY_SEARCH_INDEX]:dbConstants.VALUE_FAV_SEARCH_INDEX
        }
        return db.doc(`${collectionNames.FAILED_SEARCH_INDEX}/${docId}`).set(data)
    })
})


/*get operations start*/


exports.getRegions_v1 = functions.https.onCall((data, context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    return db.collection(collectionNames.REGION).orderBy(dbConstants.KEY_PRIORITY).get().then((successRes)=>{
        if(successRes.empty){
            return NOCONTENT
        }
        let regionsArr = successRes.docs.map((doc)=>{ return doc.data()})
        return {[dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
            [dbConstants.KEY_DATA]:regionsArr
        }
    }).catch((e)=>{
        printException("getRegions_v1",currentUser.email,e)
        return INTERNALSERVERERROR
    })

})
exports.getCategories_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let regionId = data[dbConstants.KEY_REGION_ID];
    if(checkNull(regionId)){
        return  BADREQUEST
    }
    return getCategories(db,data,(categoriesArr)=>{
        if(categoriesArr.length===0){
            return NOCONTENT
        }
        return{
            [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
            [dbConstants.KEY_DATA]:categoriesArr
        }
    },(error)=>{
        printException("getCategories_v1",currentUser.email,error)
        return INTERNALSERVERERROR
    })

})
exports.getTemplates_v1=functions.https.onCall((data, context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let regionId=data[dbConstants.KEY_REGION_ID]
    let categoryId=data[dbConstants.KEY_CATEGORY_ID]
    let from=data[dbConstants.KEY_FROM]
    let createdBy=data[dbConstants.KEY_CREATEDBY];
    let limit = data[dbConstants.KEY_LIMIT]
    let searchStr = data[dbConstants.KEY_SEARCHSTR]
    limit = Math.min(limit||constants.TEMPLATE_LIST_LIMIT,constants.TEMPLATE_LIST_LIMIT)
    let query = db.collection(`${collectionNames.TEMPLATES}`)
    if(regionId){
        query=query.where(dbConstants.KEY_REGION_ID,"==",regionId)
    }
    if(searchStr){
        query=query.where(dbConstants.KEY_SEARCH_TAGS_ARRAY,"array-contains",searchStr)
    }

    if(categoryId && categoryId!==`${constants.PREFIX_ALL}_${regionId}`){
        query=query.where(dbConstants.KEY_CATEGORY_ID,"==",categoryId)
    }
    if(from){
        if(data[dbConstants.KEY_QUERY_TYPE]===dbConstants.VALUE_QUERY_TYPE_BACKWARD){
            query=query.where(dbConstants.KEY_CREATEDTIME_ID,">",from)
        }else{
            query=query.where(dbConstants.KEY_CREATEDTIME_ID,"<",from)
        }
    }
    if(createdBy){
        query=query.where(dbConstants.KEY_CREATEDBY,"==",createdBy)
    }

    if(data[dbConstants.KEY_QUERY_TYPE]===dbConstants.VALUE_QUERY_TYPE_BACKWARD){
        query=query.orderBy(dbConstants.KEY_CREATEDTIME_ID,"asc").limit(limit)
    }else{
        query=query.orderBy(dbConstants.KEY_CREATEDTIME_ID,"desc").limit(limit)
    }

    return  query.get().then((successRes)=>{
        if(successRes.empty){
            return NOCONTENT
        }
        let favPromiseArr=[]
        let responseData = successRes.docs.map((doc)=>{
            let docData=doc.data()
            favPromiseArr.push(db.doc(`${collectionNames.FAVORITES}/${docData.id}_${userId}`).get())
            return docData
        })

        return Promise.all(favPromiseArr).then((favSuccessRes)=>{
            let favDocResult = favSuccessRes.map((favDoc)=>{
                if(favDoc.exists){
                    return true
                }
                return false
            })
            for(let i=0;i<responseData.length;i++){
                let currData =responseData[i]
                currData[dbConstants.KEY_ISFAVORITE]=favDocResult[i]
                currData[dbConstants.KEY_SEARCHTAGS]=currData[dbConstants.KEY_SEARCHTAGS]?getSearchTagsForClient(currData[dbConstants.KEY_SEARCHTAGS]):""
                currData[dbConstants.KEY_AUTHOR_ID]=currData[dbConstants.KEY_CREATEDBY]||null
            }
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                [dbConstants.KEY_DATA]:responseData
            })
        }).catch((err)=>{
            printException("getTemplates_v1.getFavDocs",currentUser.email,err)
            return INTERNALSERVERERROR
        })

    }).catch((err)=>{
        printException("getTemplates_v1",currentUser.email,err)
        return INTERNALSERVERERROR
    })
})
exports.getFavTemplates_v1=functions.https.onCall((data, context) => {
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let limit = data[dbConstants.KEY_LIMIT]
    limit = (limit && limit<constants.TEMPLATE_LIST_LIMIT)?limit:constants.TEMPLATE_LIST_LIMIT
    let query = db.collection(`${collectionNames.FAVORITES}`)
    let from = data[dbConstants.KEY_FROM]
    if(from){
        if(data[dbConstants.KEY_QUERY_TYPE]===dbConstants.VALUE_QUERY_TYPE_BACKWARD){
            query=query.where(dbConstants.KEY_CREATEDTIME_ID,">",from)
        }else{
            query=query.where(dbConstants.KEY_CREATEDTIME_ID,"<",from)
        }
    }
    if(data[dbConstants.KEY_QUERY_TYPE]===dbConstants.VALUE_QUERY_TYPE_BACKWARD){
        query=query.where(dbConstants.KEY_CREATEDBY,"==",userId).orderBy(dbConstants.KEY_CREATEDTIME_ID,"asc").limit(limit)
    }else{
        query=query.where(dbConstants.KEY_CREATEDBY,"==",userId).orderBy(dbConstants.KEY_CREATEDTIME_ID,"desc").limit(limit)
    }

    return query.get().then((successRes)=>{
        if(successRes.empty){
            return NOCONTENT
        }
        let responseData=successRes.docs.map((doc)=>{
            let docData=doc.data()
            docData[dbConstants.KEY_ISFAVORITE]=true
            docData[dbConstants.KEY_SEARCHTAGS]=docData[dbConstants.KEY_SEARCHTAGS]?getSearchTagsForClient(docData[dbConstants.KEY_SEARCHTAGS]):""
            return docData
        })
        return ({
            [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
            [dbConstants.KEY_DATA]:responseData
        })
    }).catch((err)=>{
        printException("getFavTemplates_v1",currentUser.email,err)
        return INTERNALSERVERERROR
    })
})

exports.searchTemplates_v1 = functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }

    let userId = currentUser.userId
    let limit = data[dbConstants.KEY_LIMIT]
    let searchOptions = {
        [dbConstants.KEY_HITS_PER_PAGE]:(limit && limit<constants.TEMPLATE_LIST_LIMIT)?limit:constants.TEMPLATE_LIST_LIMIT
    }
    let createdBy = data[dbConstants.KEY_CREATEDBY]
    if(createdBy){
        if(createdBy!==userId){
            return BADREQUEST
        }
        searchOptions[dbConstants.KEY_FILTERS]=`${dbConstants.KEY_CREATEDBY}:${createdBy}`
    }
    let from =data[dbConstants.KEY_FROM]
    if(from){
        let oldFilter = searchOptions[dbConstants.KEY_FILTERS]
        if(oldFilter){
            searchOptions[dbConstants.KEY_FILTERS]+=` AND ${dbConstants.KEY_CREATEDTIME} < ${from}`
        }else{
            searchOptions[dbConstants.KEY_FILTERS]=`${dbConstants.KEY_CREATEDTIME} < ${from}`
        }
    }

    let regionId = data[dbConstants.KEY_REGION_ID]
    if(regionId){
        let oldFilter = searchOptions[dbConstants.KEY_FILTERS]
        if(oldFilter){
            searchOptions[dbConstants.KEY_FILTERS]+=` AND ${dbConstants.KEY_REGION_ID}:${regionId}`
        }else{
            searchOptions[dbConstants.KEY_FILTERS]=`${dbConstants.KEY_REGION_ID}:${regionId}`
        }
    }

    let categoryId = data[dbConstants.KEY_CATEGORY_ID]
    if(categoryId && categoryId!==`${constants.PREFIX_ALL}_${regionId}`){
        let oldFilter = searchOptions[dbConstants.KEY_FILTERS]
        if(oldFilter){
            searchOptions[dbConstants.KEY_FILTERS]+=` AND ${dbConstants.KEY_CATEGORY_ID}:${categoryId}`
        }else{
            searchOptions[dbConstants.KEY_FILTERS]=`${dbConstants.KEY_CATEGORY_ID}:${categoryId}`
        }
    }
    let searchStr = data[dbConstants.KEY_SEARCHSTR]

    return feedTemplateFeedIndex.search(searchStr,searchOptions).then((successRes)=>{
        let hits = successRes[dbConstants.KEY_HITS]
        if(hits.length===0){
            return NOCONTENT
        }
        let favPromises= hits.map((object)=>{
            return db.doc(`${collectionNames.FAVORITES}/${object[dbConstants.KEY_ID]}_${userId}`).get()
        })
        return Promise.all(favPromises).then((favPromiseRes)=>{
            let favArr = favPromiseRes.map((promiseDoc)=>{
                return promiseDoc.exists
            })
            for(let i=0;i<hits.length;i++){
                let currentData=hits[i]
                currentData[dbConstants.KEY_ISFAVORITE]=favArr[i]||false
                currentData[dbConstants.KEY_AUTHOR_ID]=currentData[dbConstants.KEY_CREATEDBY]||null
            }
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                [dbConstants.KEY_DATA]:hits
            })
        }).catch((err)=>{
            printException("searchTemplates_v1",currentUser.email,err)
            for(let i=0;i<hits.length;i++){
                hits[i][dbConstants.KEY_ISFAVORITE]=false
            }
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                [dbConstants.KEY_DATA]:hits
            })
        })

    }).catch((err)=>{
        printException("searchTemplates_v1",currentUser.email,err)
        return INTERNALSERVERERROR
    })
})
exports.searchFavTemplates_v1 = functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let limit = data[dbConstants.KEY_LIMIT]
    let searchOptions = {
        [dbConstants.KEY_HITS_PER_PAGE]:(limit && limit<constants.TEMPLATE_LIST_LIMIT)?limit:constants.TEMPLATE_LIST_LIMIT
    }
    searchOptions[dbConstants.KEY_FILTERS]=`${dbConstants.KEY_CREATEDBY}:${currentUser.userId}`
    let from =data[dbConstants.KEY_FROM]
    if(from){
        let oldFilter = searchOptions[dbConstants.KEY_FILTERS]
        if(oldFilter){
            searchOptions[dbConstants.KEY_FILTERS]+=` AND ${dbConstants.KEY_CREATEDTIME} < ${from}`
        }else{
            searchOptions[dbConstants.KEY_FILTERS]=`${dbConstants.KEY_CREATEDTIME} < ${from}`
        }
    }
    let searchStr = data[dbConstants.KEY_SEARCHSTR]
    return favoriteTemplateIndex.search(searchStr,searchOptions).then((successRes)=>{
        let hits = successRes[dbConstants.KEY_HITS]
        if(hits.length===0){
            return NOCONTENT
        }

        for(let i=0;i<hits.length;i++){
            hits[i][dbConstants.KEY_ISFAVORITE]=true
        }
        return ({
            [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
            [dbConstants.KEY_DATA]:hits
        })
    }).catch((err)=>{
        printException("searchTemplates_v1",currentUser.email,err)
        return INTERNALSERVERERROR
    })
})
exports.getFeedSearchSuggestion_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let region = data[dbConstants.KEY_REGION_ID]
    let searchStr = data[dbConstants.KEY_SEARCHSTR]
    let createdBy=data[dbConstants.KEY_CREATEDBY];
    let filterObj={}
    if(region){
        filterObj = {
            [dbConstants.KEY_FILTERS]:`${dbConstants.KEY_REGION_ID}:${region}`
        }
    }
    let categoryId = data[dbConstants.KEY_CATEGORY_ID]
    if(categoryId && categoryId!==`${constants.PREFIX_ALL}_${region}`){
        if(filterObj[dbConstants.KEY_FILTERS]){
            filterObj[dbConstants.KEY_FILTERS] = filterObj[dbConstants.KEY_FILTERS]+` AND ${dbConstants.KEY_CATEGORY_ID}:${categoryId}`
        }else{
            filterObj = {
                [dbConstants.KEY_FILTERS]:`${dbConstants.KEY_CATEGORY_ID}:${categoryId}`
            }
        }
    }

    if(createdBy){
        if(createdBy!==currentUser.userId){
            return UNAUTHORIZED
        }
        if(filterObj[dbConstants.KEY_FILTERS]){
            filterObj[dbConstants.KEY_FILTERS] = filterObj[dbConstants.KEY_FILTERS]+` AND ${dbConstants.KEY_CREATEDBY}:${createdBy}`
        }else{
            filterObj = {
                [dbConstants.KEY_FILTERS]:`${dbConstants.KEY_CREATEDBY}:${createdBy}`
            }
        }
    }
    return feedTemplateFeedIndex.searchForFacetValues(dbConstants.VALUE_FACETS,searchStr,filterObj).then((successRes)=>{
        return ({
            [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
            [dbConstants.KEY_DATA]:successRes
        })
    }).catch((err)=>{
        printException("getFeedSearchSuggestion_v1",currentUser.email,err);
        return INTERNALSERVERERROR
    })
})
exports.getFavoritesSearchSuggestion_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let searchStr = data[dbConstants.KEY_SEARCHSTR]
    let filterObj={
        [dbConstants.KEY_FILTERS]:`${dbConstants.KEY_CREATEDBY}:${currentUser.userId}`
    }
    return favoriteTemplateIndex.searchForFacetValues(dbConstants.VALUE_FACETS,searchStr,filterObj).then((successRes)=>{
        return ({
            [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
            [dbConstants.KEY_DATA]:successRes
        })
    }).catch((err)=>{
        printException("getFavoritesSearchSuggestion_v1",currentUser.email,err);
        return INTERNALSERVERERROR
    })
})
exports.getMemes_v1=functions.https.onCall((data, context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let regionId=data[dbConstants.KEY_REGION_ID]
    let from=data[dbConstants.KEY_FROM]
    let createdBy=data[dbConstants.KEY_CREATEDBY];
    let limit
    let appVersion = data[dbConstants.KEY_APP_VERSION];
    if(appVersion && appVersion>=constants.INSTA_USERNAME_APP_VERSION && !createdBy){
            limit = constants.RECENT_MEMES_LIMIT
    }else{
        limit = data[dbConstants.KEY_LIMIT]
        limit = Math.min(limit||constants.TEMPLATE_LIST_LIMIT,constants.TEMPLATE_LIST_LIMIT)
    }
    let query = db.collection(`${collectionNames.MEMES}`)
    if(regionId){
        query=query.where(dbConstants.KEY_REGION_ID,"==",regionId)
    }

    if(from){
        if(data[dbConstants.KEY_QUERY_TYPE]===dbConstants.VALUE_QUERY_TYPE_BACKWARD){
            query=query.where(dbConstants.KEY_CREATEDTIME_ID,">",from)
        }else{
            query=query.where(dbConstants.KEY_CREATEDTIME_ID,"<",from)
        }
    }
    if(createdBy){
        query=query.where(dbConstants.KEY_CREATEDBY,"==",createdBy)
    }

    if(data[dbConstants.KEY_QUERY_TYPE]===dbConstants.VALUE_QUERY_TYPE_BACKWARD){
        query=query.orderBy(dbConstants.KEY_CREATEDTIME_ID,"asc").limit(limit)
    }else{
        query=query.orderBy(dbConstants.KEY_CREATEDTIME_ID,"desc").limit(limit)
    }
    if(appVersion && appVersion>=constants.INSTA_USERNAME_APP_VERSION){
        if(!createdBy){
            query=query.where(dbConstants.KEY_CREATEDTIME_ID,">=",getCurrentTimeMillis()-(daysToMillis(1))+currentUser.userId)
        }
        return  query.get().then((successRes)=>{
            if(successRes.empty){
                return NOCONTENT
            }

            let createdByIds=[]
            let responseData = successRes.docs.map((doc)=>{
                let docData=doc.data()
                createdByIds.push(docData[dbConstants.KEY_CREATEDBY])
                return docData
            })
            return getInstaUsernames(db,createdByIds,(instaUsernameMap)=>{
                responseData.map((memeObj)=>{
                    memeObj[dbConstants.KEY_INSTA_USERNAME]=instaUsernameMap[memeObj[dbConstants.KEY_CREATEDBY]]
                })
                return ({
                    [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                    [dbConstants.KEY_DATA]:responseData
                })
            },(err)=>{
                printException("getMemes_v1.getCreatedBy",currentUser.email,err)
                return INTERNALSERVERERROR
            })
        }).catch((err)=>{
            printException("getMemes_v1",currentUser.email,err)
            return INTERNALSERVERERROR
        })
    }else{
        return  query.get().then((successRes)=>{
            if(successRes.empty){
                return NOCONTENT
            }
            let responseData = successRes.docs.map((doc)=>{
                let docData=doc.data()
                return docData
            })
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                [dbConstants.KEY_DATA]:responseData
            })

        }).catch((err)=>{
            printException("getMemes_v1",currentUser.email,err)
            return INTERNALSERVERERROR
        })
    }
})
exports.getTemplateGroups_v1=functions.https.onCall((data, context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    return getTemplateGroups(db,data,(successRes)=>{
            if(successRes.length===0){
                return NOCONTENT
            }
            let responseData = successRes.map((doc)=>{
                return doc
            })
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                [dbConstants.KEY_DATA]:responseData
            })
        },
        (err)=>{
            printException("getTemplateGroups_v1",currentUser.email,err)
            return INTERNALSERVERERROR
    })
})

exports.getTemplateCredits_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let authorId = data[dbConstants.KEY_AUTHOR_ID]
    if(checkNull(authorId)){
        return BADREQUEST
    }
    //check whether the request is from fav
    return  getUserDetails(db,{[dbConstants.KEY_ID]:authorId},(userDetails)=>{
        return ({
            [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
            [dbConstants.KEY_DATA]:{
                [dbConstants.KEY_INSTA_USERNAME]:userDetails?userDetails[dbConstants.KEY_INSTA_USERNAME]:null
            }
        })
    },(err)=>{
        printException("getTemplateCredits_v1.getUserDetails",currentUser.email,err)
        return INTERNALSERVERERROR
    })
})

/*get operations end*/



/*post operations start*/
exports.signIn_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let email= currentUser.email
    let userDocRef = db.doc(`${collectionNames.USERS}/${userId}`)
    return userDocRef.get().then((successRes1)=>{
        let userDocRefRes = successRes1
        if (userDocRefRes.exists){
            let resData = successRes1.data()
            resData[dbConstants.KEY_NEWUSER]=false;
            return {[dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                [dbConstants.KEY_DATA]:resData
            }
        }else{
            let docData = {
                [dbConstants.KEY_STATUS]:constants.USER_STATUS_DEFAULT,
                [dbConstants.KEY_ID]:userId,
                email,[dbConstants.KEY_CREATEDTIME]:getCurrentTimeMillis()
            }
            return userDocRef.set(docData).then(()=>{
                return {[dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                    [dbConstants.KEY_DATA]:docData
                }
            }).catch((e)=>{
                printException("addUser_v1.setUserDoc",email,e)
                return INTERNALSERVERERROR
            })
        }
    }).catch((e)=>{
        printException("addUser_v1",email,e)
        return INTERNALSERVERERROR
    })
})
exports.updateNotificationToken_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let email= currentUser.email
    let token = data[dbConstants.KEY_NOTIFICATION_TOKEN]
    if(checkNull(token)){
        return BADREQUEST
    }
    let userDocRef = db.doc(`${collectionNames.USERS}/${userId}`)
    return userDocRef.get().then((successRes1)=>{
        let userDocRefRes = successRes1
        if (userDocRefRes.exists){
            let resData = successRes1.data()
            resData[dbConstants.KEY_NOTIFICATION_TOKEN]=[token]
            return successRes1.ref.set(resData).then(()=>{
                return ({
                    [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
                })
            }).catch((err)=>{
                printException("updateNotificationToken_v1.setNotificationToken",email,err)
            })
        }else{
            return BADREQUEST
        }
    }).catch((e)=>{
        printException("updateNotificationToken_v1.getUserDoc",email,e)
        return INTERNALSERVERERROR
    })
})
exports.setRegion_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let regionId = data[dbConstants.KEY_REGION_ID]
    if(!isValidRegion(regionId)){
        return  BADREQUEST
    }
    let userDocRef = db.doc(`${collectionNames.USERS}/${userId}`)
    return userDocRef.get().then((successRes1)=>{
        if (!successRes1.exists){
            return BADREQUEST
        }
        return userDocRef.update({[dbConstants.KEY_REGION_ID]:regionId}).then(()=>{
            return({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
            })
        }).catch((err)=>{
            printException("setRegion_v1.updateUserDoc",currentUser.email,err)
        })
    })
})
exports.addTemplate_v1=functions.https.onCall((data,context)=>{
    return(MESSAGE)


})
exports.updateTemplate_v1 = functions.https.onCall((data, context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let docId = data[dbConstants.KEY_ID]
    let searchTags = data[dbConstants.KEY_SEARCHTAGS]
    if(!templateValidation.isUpdateRequestValid(data)){
        return BADREQUEST
    }
    let templateType=data[dbConstants.KEY_TEMPLATE_TYPE]
    let docPath
    if(templateType===constants.FAV_TEMPLATETYPE){
        docPath=`${collectionNames.FAVORITES}/${docId}`
    }else{
        docPath=`${collectionNames.TEMPLATES}/${docId}`
    }
    return getUserDetails(db,{[dbConstants.KEY_ID]:userId},(userDoc)=>{
        if(userDoc===null){
            return UNAUTHORIZED
        }else if(userDoc[dbConstants.KEY_STATUS]===constants.USER_STATUS_BLOCKED){
            return  BLOCKED
        }else{
            return db.doc(docPath).get().then((successRes)=>{
                if(!successRes.exists){
                    return NOTFOUND
                }
                let responseData=successRes.data()
                let createdBy = responseData[dbConstants.KEY_CREATEDBY]
                if(createdBy!==userId){
                    return BADREQUEST
                }
                searchTags=getSearchTagsForServer(searchTags)
                let categoryId=data[dbConstants.KEY_CATEGORY_ID]
                return db.doc(docPath).update({
                    [dbConstants.KEY_SEARCHTAGS]:searchTags,
                    [dbConstants.KEY_SEARCH_TAGS_ARRAY]:getSearchTagsArr(searchTags),
                    [dbConstants.KEY_CATEGORY_ID]:categoryId
                }).then(()=>{
                    responseData[dbConstants.KEY_SEARCHTAGS]=getSearchTagsForClient(searchTags)
                    responseData[dbConstants.KEY_CATEGORY_ID]=categoryId
                    responseData[dbConstants.KEY_AUTHOR_ID]=createdBy
                    responseData[dbConstants.KEY_ISFAVORITE]=(templateType===constants.FAV_TEMPLATETYPE)
                    return ({
                        [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                        [dbConstants.KEY_DATA]:responseData
                    })
                }).catch((err)=>{
                    printException("updateTemplate_v1",currentUser.email,err)
                    return INTERNALSERVERERROR
                })

            })
        }
    },(err)=>{
        printException("updateTemplate_v1.getUser",currentUser.email,err)
        return INTERNALSERVERERROR
    })

})
exports.addTemplate_v2=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let imageUrl=data[dbConstants.KEY_IMAGEURL]
    let searchTags=data[dbConstants.KEY_SEARCHTAGS]
    if(!templateValidation.isValidRequest(data)){
        return BADREQUEST
    }
    let docId = generateRandomId();
    searchTags=getSearchTagsForServer(searchTags)
    let createdTime = getCurrentTimeMillis()
    return getUserDetails(db,{[dbConstants.KEY_ID]:userId},(userDoc)=>{
        if(userDoc===null){
            return UNAUTHORIZED
        }else if(userDoc[dbConstants.KEY_STATUS]===constants.USER_STATUS_BLOCKED){
            return  BLOCKED
        }else{
            let docData={
                [dbConstants.KEY_ID]:docId,
                [dbConstants.KEY_REGION_ID]:data[dbConstants.KEY_REGION_ID],
                [dbConstants.KEY_CATEGORY_ID]:data[dbConstants.KEY_CATEGORY_ID],
                [dbConstants.KEY_SEARCHTAGS]:searchTags,
                [dbConstants.KEY_SEARCH_TAGS_ARRAY]:getSearchTagsArr(searchTags),
                [dbConstants.KEY_IMAGEURL]:imageUrl,
                [dbConstants.KEY_CREATEDBY]:userId,
                [dbConstants.KEY_CREATEDTIME]:createdTime,
                [dbConstants.KEY_CREATEDTIME_ID]:`${createdTime}_${docId}`
            }
            let query
            if(adminConstants.USER_EMAILS.indexOf(currentUser.email)===-1){
                query=db.doc(`${collectionNames.UNVERIFIED_TEMPLATES}/${docId}`)
            }else{
                query=db.doc(`${collectionNames.TEMPLATES}/${docId}`)
            }
            return query.set(docData).then(()=>{
                docData[dbConstants.KEY_ISFAVORITE]=false
                docData[dbConstants.KEY_SEARCHTAGS]=getSearchTagsForClient(searchTags)
                docData[dbConstants.KEY_AUTHOR_ID]=userId
                return({
                    [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                    [dbConstants.KEY_DATA]:docData
                })
            }).catch((e)=>{
                printException("addTemplate_v1",currentUser.email,e)
                return INTERNALSERVERERROR
            })
        }
    },(err)=>{
        printException("addTemplate_v1.getUser",currentUser.email,err)
        return INTERNALSERVERERROR
    })


})

exports.deleteTemplate_v1 = functions.https.onCall((data, context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let docId = data[dbConstants.KEY_ID]
    if(checkNull(docId)){
        return BADREQUEST
    }
    return db.doc(`${collectionNames.TEMPLATES}/${docId}`).get().then((successRes)=>{
        if(!successRes.exists){
            return httpCodes.NOTFOUND
        }
        let responseData=successRes.data()
        let createdBy = responseData[dbConstants.KEY_CREATEDBY]
        if(createdBy!==userId){
            return BADREQUEST
        }
        return db.doc(`${collectionNames.TEMPLATES}/${docId}`).delete().then(()=>{
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                [dbConstants.KEY_DATA]:responseData
            })
        }).catch((err)=>{
            printException("deleteTemplate_v1",currentUser.email,err)
            return INTERNALSERVERERROR
        })

    })

})
exports.favoriteTemplate_v1=functions.https.onCall((data, context) => {
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let docId = data[dbConstants.KEY_ID]
    let imageUrl=data[dbConstants.KEY_IMAGEURL]
    let searchTags=data[dbConstants.KEY_SEARCHTAGS]

    if(!templateValidation.isFavRequestValid(data,userId)){
        return BADREQUEST
    }
    searchTags=getSearchTagsForServer(searchTags)

    let userAndTemplatePromise = [
        getUserDetails(db,{[dbConstants.KEY_ID]:userId,[dbConstants.KEY_RETURN_PROMISE]:true}),
        getTemplate(db,{[dbConstants.KEY_ID]:getTemplateIdFromFavId(docId),[dbConstants.KEY_RETURN_PROMISE]:true})
    ]

    return Promise.all(userAndTemplatePromise).then((responses)=>{
        let userPromiseRes = responses[0]
        if(userPromiseRes && userPromiseRes.exists){
            let userPromiseData=  userPromiseRes.data()
            if(userPromiseData[dbConstants.KEY_STATUS]===constants.USER_STATUS_BLOCKED){
                return  BLOCKED
            }else{
                let categoryId =data[dbConstants.KEY_CATEGORY_ID]
                let regionId = data[dbConstants.KEY_REGION_ID]
                let createdTime = getCurrentTimeMillis()
                let templatePromise= responses[1]
                let templateData
                if(templatePromise.exists){
                    templateData=templatePromise.data()
                }
                let docData={
                    [dbConstants.KEY_ID]:docId,
                    [dbConstants.KEY_REGION_ID]:regionId,
                    [dbConstants.KEY_CATEGORY_ID]:categoryId,
                    [dbConstants.KEY_SEARCHTAGS]:searchTags,
                    [dbConstants.KEY_IMAGEURL]:imageUrl,
                    [dbConstants.KEY_CREATEDBY]:userId,
                    [dbConstants.KEY_CREATEDTIME]:createdTime,
                    [dbConstants.KEY_CREATEDTIME_ID]:`${createdTime}_${docId}`,
                    [dbConstants.KEY_AUTHOR_ID]:templateData?templateData[dbConstants.KEY_CREATEDBY]:null
                }
                return db.doc(`${collectionNames.FAVORITES}/${docId}`).set(docData).then(()=>{
                    docData[dbConstants.KEY_ISFAVORITE]=true
                    docData[dbConstants.KEY_SEARCHTAGS]=getSearchTagsForClient(searchTags)
                    return({
                        [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                        [dbConstants.KEY_DATA]:docData
                    })
                }).catch((e)=>{
                    printException("favoriteTemplate_v1.set",currentUser.email,e)
                    return INTERNALSERVERERROR
                })
            }
        }else{
            return  UNAUTHORIZED
        }
    }).catch((err)=>{
        printException("favoriteTemplate_v1.getAllPromise",currentUser.email,err)
        return INTERNALSERVERERROR
    })

})
exports.unFavoriteTemplate_v1=functions.https.onCall((data, context) => {
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let docId = data[dbConstants.KEY_ID]
    if(!templateValidation.isValidFavDocId(docId,userId)){
        return  BADREQUEST
    }
    //TODO: unfavorite template blocked user verification
    return db.doc(`${collectionNames.FAVORITES}/${docId}`).delete().then(()=>{
        return({
            [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
        })
    }).catch((e)=>{
        printException("favoriteTemplate_v1.delete",currentUser.email,e)
        return INTERNALSERVERERROR
    })
})
exports.reportTemplate_v1=functions.https.onCall((data, context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let docId = data[dbConstants.KEY_ID]
    let reportType = data[dbConstants.KEY_REPORT_TYPE]

    return getUserDetails(db,{[dbConstants.KEY_ID]:userId},(userDoc)=>{
        if(userDoc===null){
            return UNAUTHORIZED
        }else if(userDoc[dbConstants.KEY_STATUS]===constants.USER_STATUS_BLOCKED){
            return  BLOCKED
        }else{
            return db.doc(`${collectionNames.TEMPLATES}/${docId}`).get().then((successRes)=>{
                if(successRes.exists){
                    let templateData = successRes.data()
                    return db.doc(`${collectionNames.REPORTS}/${docId}`).get().then((successRes)=>{
                        if(successRes.exists){
                            let userData = {
                                [dbConstants.KEY_ID]:userId,
                                [dbConstants.KEY_REPORT_TYPE]:reportType
                            }
                            return db.doc(`${collectionNames.REPORTS}/${docId}/${collectionNames.USERS}/${userId}`).set(userData).then(()=>{
                                return ({
                                    [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
                                })
                            }).catch((err)=>{
                                printException("reportTemplate_v1.setUserDoc",currentUser.email,err)
                            })
                        }else{
                            return db.doc(`${collectionNames.REPORTS}/${docId}`).set(templateData).then(()=>{
                                let userData = {
                                    [dbConstants.KEY_ID]:userId,
                                    [dbConstants.KEY_REPORT_TYPE]:reportType
                                }
                                return db.doc(`${collectionNames.REPORTS}/${docId}/${collectionNames.USERS}/${userId}`).set(userData).then(()=>{
                                    return ({
                                        [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
                                    })
                                }).catch((err)=>{
                                    printException("reportTemplate_v1.setUserDoc",currentUser.email,err)
                                    return INTERNALSERVERERROR
                                })

                            }).catch((err)=>{
                                printException("reportTemplate_v1.setTemplateDoc",currentUser.email,err)
                                return INTERNALSERVERERROR
                            })
                        }
                    }).catch((err)=>{
                        printException("reportTemplate_v1.getTemplateDoc",currentUser.email,err)
                        return INTERNALSERVERERROR
                    })

                }
                return ({
                    [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
                })
            }).catch((err)=>{
                printException("reportTemplate_v1.getUserDoc",currentUser.email,err)
            })
        }
    },(err)=>{
        printException("reportTemplate_v1.getUser",currentUser.email,err)
        return INTERNALSERVERERROR
    })

})
exports.sendFeedback_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    if(!isValidSendFeedbackRequest(data)){
        return BADREQUEST
    }
    let userId = currentUser.userId
    let email = currentUser.email
    let regionId = data[dbConstants.KEY_REGION_ID]
    let docId = generateRandomId();
    let createdTime = getCurrentTimeMillis()
    let description=data[dbConstants.KEY_DESCRIPTION]
    let docData={
        [dbConstants.KEY_ID]:docId,
        [dbConstants.KEY_REGION_ID]:regionId,
        [dbConstants.KEY_CREATEDBY]:userId,
        [dbConstants.KEY_EMAIL]:email,
        [dbConstants.KEY_CREATEDTIME]:createdTime,
        [dbConstants.KEY_CREATEDTIME_ID]:`${createdTime}_${docId}`,
        [dbConstants.KEY_DESCRIPTION]:description.trim(),
        [dbConstants.KEY_APP_VERSION]:data[dbConstants.KEY_APP_VERSION]||null
    }
    return db.doc(`${collectionNames.FEEDBACKS}/${docId}`).set(docData).then(()=>{
        return ({
            [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
        })
    }).catch((err)=>{
        printException("sendFeedback_v1",email,err)
        return INTERNALSERVERERROR
    })


})
exports.addMeme_v1=functions.https.onCall((data,context)=>{
    return(MESSAGE)
})

exports.addMeme_v2=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    if(!isValidMemeRequest(data)){
        return BADREQUEST
    }
    let userId = currentUser.userId
    return getUserDetails(db,{[dbConstants.KEY_ID]:userId},(userDoc)=>{
        if(userDoc===null){
            return UNAUTHORIZED
        }else if(userDoc[dbConstants.KEY_STATUS]===constants.USER_STATUS_BLOCKED){
            return  BLOCKED
        }else{
            let docId = generateRandomId();
            let createdTime = getCurrentTimeMillis()
            let docData={
                [dbConstants.KEY_ID]:docId,
                [dbConstants.KEY_REGION_ID]:data[dbConstants.KEY_REGION_ID],
                [dbConstants.KEY_IMAGEURL]:data[dbConstants.KEY_IMAGEURL],
                [dbConstants.KEY_CREATEDBY]:userId,
                [dbConstants.KEY_CREATEDTIME]:createdTime,
                [dbConstants.KEY_CREATEDTIME_ID]:`${createdTime}_${docId}`,
                [dbConstants.KEY_DOWNLOADS]:0,
                [dbConstants.KEY_SHARES]:0
            }
            let query
            if(adminConstants.USER_EMAILS.indexOf(currentUser.email)===-1){
                query=db.doc(`${collectionNames.UNVERIFIED_MEMES}/${docId}`)
            }else{
                query=db.doc(`${collectionNames.MEMES}/${docId}`)
            }
            return query.set(docData).then(()=>{
                return ({
                    [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                    [dbConstants.KEY_DATA]:docData
                })
            }).catch((err)=>{
                printException("addMeme_v1.addMeme",currentUser.email,err)
                return INTERNALSERVERERROR
            })
        }
    },(err)=>{
        printException("addMeme_v1.getUser",currentUser.email,err)
        return INTERNALSERVERERROR
    })
})

exports.deleteMeme_v1=functions.https.onCall((data,context)=>{

    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let docId = data[dbConstants.KEY_ID]
    if(checkNull(docId)){
        return BADREQUEST
    }
    return db.doc(`${collectionNames.MEMES}/${docId}`).get().then((successRes)=>{
        if (!successRes.exists){
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
            })
        }
        let docData = successRes.data()
        if(docData[dbConstants.KEY_CREATEDBY]!==currentUser.userId){
            return BADREQUEST
        }
        return db.doc(`${collectionNames.MEMES}/${docId}`).delete().then(()=>{
            return({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
            })
        }).catch((err)=>{
            printException("deleteMeme_v1.deleteDoc",currentUser.email,err)
            return INTERNALSERVERERROR
        })
    }).catch((err)=>{
        printException("deleteMeme_v1.getDoc",currentUser.email,err)
        return INTERNALSERVERERROR
    })

})
exports.memesActionCountPlusOne_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let docId = data[dbConstants.KEY_ID]
    if(checkNull(docId)){
        return BADREQUEST
    }

    return db.doc(`${collectionNames.MEMES}/${docId}`).get().then((successRes)=>{
        if (!successRes.exists){
            return NOCONTENT
        }
        let action = data[dbConstants.KEY_ACTION]
        let docData = successRes.data()
        let key,value
        if(action===dbConstants.KEY_SHARES){
            key = dbConstants.KEY_SHARES
            value = (docData[dbConstants.KEY_SHARES]||0)+1
        }else{
            key=dbConstants.KEY_DOWNLOADS
            value = (docData[dbConstants.KEY_DOWNLOADS]||0)+1
        }
        return db.doc(`${collectionNames.MEMES}/${docId}`).update({[key]:value}).then(()=>{
            return({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
            })
        }).catch((err)=>{
            printException("memesActionCountPlusOne_v1.updateDoc",currentUser.email,err)
            return INTERNALSERVERERROR
        })
    }).catch((err)=>{
        printException("memesActionCountPlusOne_v1.getDoc",currentUser.email,err)
        return INTERNALSERVERERROR
    })
})
exports.reportMeme_v1=functions.https.onCall((data, context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let docId = data[dbConstants.KEY_ID]
    let reportType = data[dbConstants.KEY_REPORT_TYPE]

    return getUserDetails(db,{[dbConstants.KEY_ID]:userId},(userDoc)=>{
        if(userDoc===null){
            return UNAUTHORIZED
        }else if(userDoc[dbConstants.KEY_STATUS]===constants.USER_STATUS_BLOCKED){
            return  BLOCKED
        }else{
            return db.doc(`${collectionNames.MEMES}/${docId}`).get().then((successRes)=>{
                if(successRes.exists){
                    let templateData = successRes.data()
                    return db.doc(`${collectionNames.REPORTS_MEME}/${docId}`).get().then((successRes)=>{
                        if(successRes.exists){
                            let userData = {
                                [dbConstants.KEY_ID]:userId,
                                [dbConstants.KEY_REPORT_TYPE]:reportType
                            }
                            return db.doc(`${collectionNames.REPORTS_MEME}/${docId}/${collectionNames.USERS}/${userId}`).set(userData).then(()=>{
                                return ({
                                    [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
                                })
                            }).catch((err)=>{
                                printException("reportMeme_v1.setUserDoc",currentUser.email,err)
                            })
                        }else{
                            return db.doc(`${collectionNames.REPORTS_MEME}/${docId}`).set(templateData).then(()=>{
                                let userData = {
                                    [dbConstants.KEY_ID]:userId,
                                    [dbConstants.KEY_REPORT_TYPE]:reportType
                                }
                                return db.doc(`${collectionNames.REPORTS_MEME}/${docId}/${collectionNames.USERS}/${userId}`).set(userData).then(()=>{
                                    return ({
                                        [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
                                    })
                                }).catch((err)=>{
                                    printException("reportMeme_v1.setUserDoc",currentUser.email,err)
                                    return INTERNALSERVERERROR
                                })

                            }).catch((err)=>{
                                printException("reportMeme_v1.setTemplateDoc",currentUser.email,err)
                                return INTERNALSERVERERROR
                            })
                        }
                    }).catch((err)=>{
                        printException("reportMeme_v1.getTemplateDoc",currentUser.email,err)
                        return INTERNALSERVERERROR
                    })

                }
                return ({
                    [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
                })
            }).catch((err)=>{
                printException("reportMeme_v1.getUserDoc",currentUser.email,err)
            })
        }
    },(err)=>{
        printException("reportMeme_v1.getUser",currentUser.email,err)
        return INTERNALSERVERERROR
    })

})
exports.addInstaUsername_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let username = data[dbConstants.KEY_INSTA_USERNAME]
    return getUserDetails(db,{[dbConstants.KEY_ID]:userId},(userDoc)=>{
        if(userDoc===null){
            return UNAUTHORIZED
        }
         username = sanitizeUsername(username)
        return db.doc(`${collectionNames.USERS}/${userId}`).update({
            [dbConstants.KEY_INSTA_USERNAME]:username
        }).then(()=>{
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                [dbConstants.KEY_DATA]:{
                    [dbConstants.KEY_INSTA_USERNAME]:username
                }
            })
        }).catch((err)=>{
            printException("addInstaUsername_v1.updateInstaUsername",currentUser.email,err)
            return INTERNALSERVERERROR
        })
    },(err)=>{
        printException("addInstaUsername_v1.getUser",currentUser.email,err)
        return INTERNALSERVERERROR
    })
})
/*post operations end*/




/*Admin operations start*/
exports.admin_signin_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let email= currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }
    let userDocRef = db.doc(`${collectionNames.USERS}/${userId}`)
    return userDocRef.get().then((successRes1)=>{
        let userDocRefRes = successRes1
        if (userDocRefRes.exists){
            let resData = successRes1.data()
            resData[dbConstants.KEY_NEWUSER]=false;
            return {[dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                [dbConstants.KEY_DATA]:resData
            }
        }else{
            return userDocRef.set({[dbConstants.KEY_ID]:userId,email,[dbConstants.KEY_CREATEDTIME]:getCurrentTimeMillis()}).then(()=>{
                return {[dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                    [dbConstants.KEY_DATA]:{[dbConstants.KEY_EMAIL]:email,
                        [dbConstants.KEY_ID]:userId,
                        [dbConstants.KEY_STATUS]:constants.USER_STATUS_DEFAULT
                    }
                }
            }).catch((e)=>{
                printException("admin_signin_v1.setUserDoc",email,e)
                return INTERNALSERVERERROR
            })
        }
    }).catch((e)=>{
        printException("admin_signin_v1",email,e)
        return INTERNALSERVERERROR
    })
})
exports.admin_deleteTemplate_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }
    let docId = data[dbConstants.KEY_ID]
    if(checkNull(docId)){
        return BADREQUEST
    }
    let promises=[]
    promises.push(db.doc(`${collectionNames.TEMPLATES}/${docId}`).delete())
    promises.push(db.doc(`${collectionNames.REPORTS}/${docId}`).delete())
    return Promise.all(promises).then(()=>{
        return ({[dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS})
    }).catch((err)=>{
        printException("admin_deleteTemplate__v1",email,err)
        return INTERNALSERVERERROR
    })
})
exports.admin_deleteMeme_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }
    let docId = data[dbConstants.KEY_ID]
    if(checkNull(docId)){
        return BADREQUEST
    }
    let promises=[]
    promises.push(db.doc(`${collectionNames.MEMES}/${docId}`).delete())
    promises.push(db.doc(`${collectionNames.REPORTS_MEME}/${docId}`).delete())
    return Promise.all(promises).then(()=>{
        return ({[dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS})
    }).catch((err)=>{
        printException("admin_deleteMeme_v1",email,err)
        return INTERNALSERVERERROR
    })
})
exports.admin_notaSpam_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }
    let docId = data[dbConstants.KEY_ID]
    if(checkNull(docId)){
        return BADREQUEST
    }
    return db.doc(`${collectionNames.REPORTS}/${docId}`).delete().then(()=>{
        return ({[dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS})
    }).catch((err)=>{
        printException("admin_notaSpam_v1",email,err)
        return INTERNALSERVERERROR
    })
})

exports.admin_memeNotSpam_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }
    let docId = data[dbConstants.KEY_ID]
    if(checkNull(docId)){
        return BADREQUEST
    }
    return db.doc(`${collectionNames.REPORTS_MEME}/${docId}`).delete().then(()=>{
        return ({[dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS})
    }).catch((err)=>{
        printException("admin_memeNotSpam_v1",email,err)
        return INTERNALSERVERERROR
    })
})
exports.admin_deleteTemplateAndBlockUser_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }
    let docId = data[dbConstants.KEY_ID]
    let createdBy = data[dbConstants.KEY_CREATEDBY]
    if(checkNull(docId) || checkNull(createdBy)){
        return BADREQUEST
    }
    let promises=[]
    promises.push(db.doc(`${collectionNames.TEMPLATES}/${docId}`).delete())
    promises.push(db.doc(`${collectionNames.REPORTS}/${docId}`).delete())
    promises.push(db.doc(`${collectionNames.USERS}/${createdBy}`).update({[dbConstants.KEY_STATUS]:constants.USER_STATUS_BLOCKED}))
    return Promise.all(promises).then(()=>{
        return ({[dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS})
    }).catch((err)=>{
        printException("admin_deleteTemplateAndBlockUser_v1",email,err)
        return INTERNALSERVERERROR
    })
})
exports.admin_getReportedTemplates_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }

    let from=data[dbConstants.KEY_FROM]
    let limit = data[dbConstants.KEY_LIMIT]
    limit = Math.min(limit||constants.TEMPLATE_LIST_LIMIT,constants.TEMPLATE_LIST_LIMIT)
    let query = db.collection(`${collectionNames.REPORTS}`).limit(limit).orderBy(dbConstants.KEY_CREATEDTIME_ID,"desc")
    if(from){
        query=query.where(dbConstants.KEY_CREATEDTIME_ID,"<",from)
    }

    return  query.get().then((successRes)=>{
        if(successRes.empty){
            return NOCONTENT
        }
        let response =successRes.docs.map((doc)=>{
            return doc.data()
        })
        return({
            [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
            [dbConstants.KEY_DATA]:response
        })
    }).catch((err)=>{
        printException("admin_getReportedTemplates_v1",email,err)
    })

})

exports.admin_addCategory_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }
    if(!isValidCategory(data)){
        return  BADREQUEST
    }
    let regionId = data[dbConstants.KEY_REGION_ID]
    let imageUrl = data[dbConstants.KEY_IMAGEURL]||null
    let name=generateCategoryName(data[dbConstants.KEY_NAME])
    let priority=Math.max(data[dbConstants.KEY_PRIORITY]||0,1)
    // return db.collection(`${}`
    return getCategories(db,data,(categories=[])=>{

        let categoryIds = categories.map((category)=>{
            return category[dbConstants.KEY_ID]
        })
        let currentCategoryId=generateCategoryId(name,regionId)
        if(categoryIds.indexOf(currentCategoryId)!==-1){
            return CONFLICT
        }
        let insertPosition;
        if(priority>categories.length){
            insertPosition=categories.length
        }else{
            insertPosition=priority-1
        }
        /*data[dbConstants.KEY_CAN_UPLOAD_TEMPLATE] may be undefined so put that true false check*/
        const categoryData = {
            [dbConstants.KEY_ID]:currentCategoryId,
            [dbConstants.KEY_NAME]:name,
            [dbConstants.KEY_IMAGEURL]:imageUrl,
            [dbConstants.KEY_REGION_ID]:regionId,
            [dbConstants.KEY_CAN_UPLOAD_TEMPLATE]:data[dbConstants.KEY_CAN_UPLOAD_TEMPLATE]?true:false
        }
        categories.splice(insertPosition,0,categoryData)
        let promiseArr = []
        for(let i=insertPosition;i<categories.length;i++){
            let currCategory = categories[i]
            currCategory[dbConstants.KEY_PRIORITY] = i+1;
            promiseArr.push(db.doc(`${collectionNames.TEMPLATE_CATEGORIES}/${currCategory[dbConstants.KEY_ID]}`).set(currCategory))
        }

        return Promise.all(promiseArr).then(()=>{
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                [dbConstants.KEY_DATA]:categoryData
            })
        }).catch((err)=>{
            printException("admin_addCategory.setCategory_v1",email,err)
            return INTERNALSERVERERROR
        })


    },(error)=>{
        printException("admin_addCategory_v1",email,error)
        return INTERNALSERVERERROR
    })

})


exports.admin_updateCategory_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(adminConstants.USER_EMAILS.indexOf(email)===-1){
        return UNAUTHORIZED
    }
    let categoryId = data[dbConstants.KEY_ID]
    if(!isValidCategory(data) || checkNull(categoryId)|| categoryId.trim()===""){
        return  BADREQUEST
    }


    let imageUrl = data[dbConstants.KEY_IMAGEURL]||null
    let regionId = data[dbConstants.KEY_REGION_ID]
    let name=generateCategoryName(data[dbConstants.KEY_NAME])
    let priority=Math.max(data[dbConstants.KEY_PRIORITY]||0,1)
    return getCategories(db,data,(categories=[])=>{

        let categoryIds = categories.map((category)=>{
            return category[dbConstants.KEY_ID]
        })
        let lastPosition =categoryIds.indexOf(categoryId)
        if(lastPosition===-1){
            return BADREQUEST
        }
        categories.splice(lastPosition,1)
        let insertPosition;
        if(priority>categories.length){
            insertPosition=categories.length
        }else{
            insertPosition=priority-1
        }
        /*data[dbConstants.KEY_CAN_UPLOAD_TEMPLATE] may be undefined so put that true false check*/
        const categoryData = {
            [dbConstants.KEY_ID]:categoryId,
            [dbConstants.KEY_NAME]:name,
            [dbConstants.KEY_IMAGEURL]:imageUrl,
            [dbConstants.KEY_REGION_ID]:regionId,
            [dbConstants.KEY_CAN_UPLOAD_TEMPLATE]:data[dbConstants.KEY_CAN_UPLOAD_TEMPLATE]?true:false
        }
        categories.splice(insertPosition,0,categoryData)
        let promiseArr = []
        for(let i=Math.min(lastPosition,insertPosition);i<categories.length;i++){
            let currCategory = categories[i]
            currCategory[dbConstants.KEY_PRIORITY] = i+1;
            promiseArr.push(db.doc(`${collectionNames.TEMPLATE_CATEGORIES}/${currCategory[dbConstants.KEY_ID]}`).set(currCategory))
        }

        return Promise.all(promiseArr).then(()=>{
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                [dbConstants.KEY_DATA]:categories
            })
        }).catch((err)=>{
            printException("admin_updateCategory_v1.setCategory_v1",email,err)
            return INTERNALSERVERERROR
        })


    },(error)=>{
        printException("admin_updateCategory_v1",email,error)
        return INTERNALSERVERERROR
    })
})
exports.admin_addTemplateGroup_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }

    if(!isValidTemplateGroupRequest(data)){
        return  BADREQUEST
    }
    let regionId = data[dbConstants.KEY_REGION_ID]
    let name=data[dbConstants.KEY_NAME].trim()
    let currentGroupId=generateCategoryId(name,regionId)
    return db.doc(`${collectionNames.TEMPLATE_GROUPS}/${currentGroupId}`).get().then((successRes)=>{
        if(successRes.exists){
            return CONFLICT
        }
        const groupData = {
            [dbConstants.KEY_ID]:currentGroupId,
            [dbConstants.KEY_NAME]:getSearchTagsForServer(name," "),
            [dbConstants.KEY_IMAGEURL]:data[dbConstants.KEY_IMAGEURL]||null,
            [dbConstants.KEY_REGION_ID]:regionId,
            [dbConstants.KEY_SEARCHSTR]:data[dbConstants.KEY_SEARCHSTR],
            [dbConstants.KEY_CREATEDTIME]:new Date().getTime()
        }

        return db.doc(`${collectionNames.TEMPLATE_GROUPS}/${currentGroupId}`).set(groupData).then(()=>{
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                [dbConstants.KEY_DATA]:groupData
            })
        }).catch((err)=>{
            printException("admin_addTemplateGroup_v1.addGroup",email,err)
            return INTERNALSERVERERROR
        })

    }).catch((err)=>{
        printException("admin_addTemplateGroup_v1.getGroup",email,err)
        return INTERNALSERVERERROR
    })

})
exports.admin_updateTemplateGroup_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }
    let groupId = data[dbConstants.KEY_ID]
    if(!isValidTemplateGroupRequest(data) || checkNull(groupId)|| groupId.trim()===""){
        return  BADREQUEST
    }
    let imageUrl = data[dbConstants.KEY_IMAGEURL]||null
    let regionId = data[dbConstants.KEY_REGION_ID]
    let name=data[dbConstants.KEY_NAME].trim()

    return db.doc(`${collectionNames.TEMPLATE_GROUPS}/${data[dbConstants.KEY_ID]}`).get().then((successRes)=>{
        if(!successRes.exists){
            return BADREQUEST
        }

        const groupData = {
            [dbConstants.KEY_ID]:groupId,
            [dbConstants.KEY_NAME]:getSearchTagsForServer(name," "),
            [dbConstants.KEY_IMAGEURL]:imageUrl,
            [dbConstants.KEY_REGION_ID]:regionId,
            [dbConstants.KEY_SEARCHSTR]:data[dbConstants.KEY_SEARCHSTR]
        }
        return db.doc(`${collectionNames.TEMPLATE_GROUPS}/${groupId}`).set(groupData).then(()=>{
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
                [dbConstants.KEY_DATA]:groupData
            })
        }).catch((err)=>{
            printException("admin_updateTemplateGroup_v1.updateGroup",email,err)
            return INTERNALSERVERERROR
        })
    }).catch((err)=>{
        printException("admin_updateTemplateGroup_v1.getGroup",email,err)
        return INTERNALSERVERERROR
    })

})

exports.admin_getUnverifiedTemplates_v1=functions.https.onCall((data, context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let regionId=data[dbConstants.KEY_REGION_ID]
    let categoryId=data[dbConstants.KEY_CATEGORY_ID]
    let from=data[dbConstants.KEY_FROM]
    let createdBy=data[dbConstants.KEY_CREATEDBY];
    let limit = data[dbConstants.KEY_LIMIT]
    limit = Math.min(limit||constants.TEMPLATE_LIST_LIMIT,constants.TEMPLATE_LIST_LIMIT)
    let query = db.collection(`${collectionNames.UNVERIFIED_TEMPLATES}`)
    if(regionId){
        query=query.where(dbConstants.KEY_REGION_ID,"==",regionId)
    }
    if(categoryId && categoryId!==`${constants.PREFIX_ALL}_${regionId}`){
        query=query.where(dbConstants.KEY_CATEGORY_ID,"==",categoryId)
    }
    if(from){
        if(data[dbConstants.KEY_QUERY_TYPE]===dbConstants.VALUE_QUERY_TYPE_BACKWARD){
            query=query.where(dbConstants.KEY_CREATEDTIME_ID,">",from)
        }else{
            query=query.where(dbConstants.KEY_CREATEDTIME_ID,"<",from)
        }
    }
    if(createdBy){
        query=query.where(dbConstants.KEY_CREATEDBY,"==",createdBy)
    }
    query=query.orderBy(dbConstants.KEY_CREATEDTIME_ID,"asc").limit(limit)
    return  query.get().then((successRes)=>{
        if(successRes.empty){
            return NOCONTENT
        }
        let responseData = successRes.docs.map((doc)=>{
            let docData=doc.data()
            docData[dbConstants.KEY_ISFAVORITE]=false
            return docData
        })
        return ({
            [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
            [dbConstants.KEY_DATA]:responseData
        })
    }).catch((err)=>{
        printException("getTemplates_v1",currentUser.email,err)
        return INTERNALSERVERERROR
    })
})

exports.admin_verifyTemplate_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }
    let docId = data[dbConstants.KEY_ID]
    return db.doc(`${collectionNames.UNVERIFIED_TEMPLATES}/${docId}`).get().then((successRes)=>{
        if(!successRes.exists){
            return BADREQUEST
        }
        let data= successRes.data()
        let promiseArr = [ db.doc(`${collectionNames.TEMPLATES}/${docId}`).set(data) , db.doc(`${collectionNames.UNVERIFIED_TEMPLATES}/${docId}`).delete()]
        return Promise.all(promiseArr).then(()=>{
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
            })
        }).catch((err)=>{
            printException("admin_verifyTemplates_v1.setTemplateData",email,err)
            return INTERNALSERVERERROR
        })
    }).catch((err)=>{
        printException("admin_verifyTemplates_v1.getUnVerifiedTemplate",email,err)
        return INTERNALSERVERERROR
    })

})

exports.admin_unVerifyTemplate_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }
    let docId = data[dbConstants.KEY_ID]
    return db.doc(`${collectionNames.UNVERIFIED_TEMPLATES}/${docId}`).get().then((successRes)=>{
        if(!successRes.exists){
            return BADREQUEST
        }
        let data= successRes.data()
        data[dbConstants.KEY_IS_SPAM]=true
        return db.doc(`${collectionNames.UNVERIFIED_TEMPLATES}/${docId}`).set(data).then(()=>{
            return db.doc(`${collectionNames.UNVERIFIED_TEMPLATES}/${docId}`).delete().then(()=>{
                return ({
                    [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
                })
            }).catch((err)=>{
                printException("admin_unVerifyTemplate_v1.deleteUnVerifiedTemplate",email,err)
                return INTERNALSERVERERROR
            })
        }).catch((err)=>{
            printException("admin_unVerifyTemplate_v1.setUnVerifiedAsSpam",email,err)
            return INTERNALSERVERERROR
        })

    }).catch((err)=>{
        printException("admin_unVerifyTemplate_v1.getUnVerifiedTemplate",email,err)
        return INTERNALSERVERERROR
    })

})

exports.admin_verifyMeme_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }
    let docId = data[dbConstants.KEY_ID]
    return db.doc(`${collectionNames.UNVERIFIED_MEMES}/${docId}`).get().then((successRes)=>{
        if(!successRes.exists){
            return BADREQUEST
        }
        let data= successRes.data()
        let promiseArr = [ db.doc(`${collectionNames.MEMES}/${docId}`).set(data) , db.doc(`${collectionNames.UNVERIFIED_MEMES}/${docId}`).delete()]
        return Promise.all(promiseArr).then(()=>{
            return ({
                [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
            })
        }).catch((err)=>{
            printException("admin_verifyMemes_v1.setTemplateData",email,err)
            return INTERNALSERVERERROR
        })
    }).catch((err)=>{
        printException("admin_verifyMemes_v1.getUnVerifiedMeme",email,err)
        return INTERNALSERVERERROR
    })

})


exports.admin_unVerifyMeme_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }
    let docId = data[dbConstants.KEY_ID]
    return db.doc(`${collectionNames.UNVERIFIED_MEMES}/${docId}`).get().then((successRes)=>{
        if(!successRes.exists){
            return BADREQUEST
        }
        let data= successRes.data()
        data[dbConstants.KEY_IS_SPAM]=true
        return db.doc(`${collectionNames.UNVERIFIED_MEMES}/${docId}`).set(data).then(()=>{
            return db.doc(`${collectionNames.UNVERIFIED_MEMES}/${docId}`).delete().then(()=>{
                return ({
                    [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
                })
            }).catch((err)=>{
                printException("admin_unVerifyMeme_v1.deleteUnVerifiedMeme",email,err)
                return INTERNALSERVERERROR
            })
        }).catch((err)=>{
            printException("admin_unVerifyMeme_v1.setUnVerifiedAsSpam",email,err)
            return INTERNALSERVERERROR
        })

    }).catch((err)=>{
        printException("admin_unVerifyMeme_v1.getUnVerifiedMeme",email,err)
        return INTERNALSERVERERROR
    })

})

exports.admin_getUnVerifiedMemes_v1=functions.https.onCall((data, context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let userId = currentUser.userId
    let regionId=data[dbConstants.KEY_REGION_ID]
    let categoryId=data[dbConstants.KEY_CATEGORY_ID]
    let from=data[dbConstants.KEY_FROM]
    let createdBy=data[dbConstants.KEY_CREATEDBY];
    let limit = data[dbConstants.KEY_LIMIT]
    limit = Math.min(limit||constants.TEMPLATE_LIST_LIMIT,constants.TEMPLATE_LIST_LIMIT)
    let query = db.collection(`${collectionNames.UNVERIFIED_MEMES}`)
    if(regionId){
        query=query.where(dbConstants.KEY_REGION_ID,"==",regionId)
    }

    if(from){
        if(data[dbConstants.KEY_QUERY_TYPE]===dbConstants.VALUE_QUERY_TYPE_BACKWARD){
            query=query.where(dbConstants.KEY_CREATEDTIME_ID,">",from)
        }else{
            query=query.where(dbConstants.KEY_CREATEDTIME_ID,"<",from)
        }
    }
    if(createdBy){
        query=query.where(dbConstants.KEY_CREATEDBY,"==",createdBy)
    }
    query=query.orderBy(dbConstants.KEY_CREATEDTIME_ID,"asc").limit(limit)
    return  query.get().then((successRes)=>{
        if(successRes.empty){
            return NOCONTENT
        }
        let responseData = successRes.docs.map((doc)=>{
            let docData=doc.data()
            return docData
        })
        return ({
            [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS,
            [dbConstants.KEY_DATA]:responseData
        })

    }).catch((err)=>{
        printException("admin_getUnVerifiedMemes_v1",currentUser.email,err)
        return INTERNALSERVERERROR
    })
})

exports.admin_sendNotification_v1=functions.https.onCall((data,context)=>{
    let currentUser = getContextUser(context)
    if(checkNull(currentUser)){
        return UNAUTHORIZED
    }
    let email=currentUser.email
    if(!isAdminAccount(email)){
        return UNAUTHORIZED
    }
    if(!isValidNotification(data)){
        if(BuildConfig.DEBUG){
            console.log("admin_sendNotification_v1===>invalid data")
        }
        return BADREQUEST
    }
    let notificationChannelId=data[dbConstants.KEY_FCM_ID];
    let message={
        data:{
            [dbConstants.KEY_FCM_ID]:notificationChannelId,
            [dbConstants.KEY_FCM_TITLE]:data[dbConstants.KEY_FCM_TITLE],
            [dbConstants.KEY_FCM_DESC]:data[dbConstants.KEY_FCM_DESC]
        },
        topic:data[dbConstants.KEY_FCM_TOPIC]
    }

    let imageUrl = data[dbConstants.KEY_FCM_IMAGEURL];
    if(!checkNull(imageUrl)){
        message.data[dbConstants.KEY_FCM_IMAGEURL] = imageUrl
    }
    let categoryId = data[dbConstants.KEY_FCM_CATEGORY_ID]
    if(!checkNull(categoryId)){
        message.data[dbConstants.KEY_FCM_CATEGORY_ID]=categoryId
    }
    if(notificationChannelId===dbConstants.VALUE_FCM_APP_UPDATE_CHANNEL){
        let link = data[dbConstants.KEY_FCM_LINK]
        if(!checkNull(link)){
            message.data[dbConstants.KEY_FCM_LINK]=link
        }
    }
    let versionCode = data[dbConstants.FCM_APP_VERSION_CODE]
    if(!checkNull(versionCode)){
        message.data[dbConstants.FCM_APP_VERSION_CODE]=versionCode
        message.data[dbConstants.FCM_VERSION_OPERATOR]=data[dbConstants.FCM_VERSION_OPERATOR]
    }

    return (admin.messaging().send(message).then((successRes)=>{

        return ({
            [dbConstants.KEY_STATUSCODE]:httpCodes.SUCCESS
        })
    }).catch((err)=>{
        printException("admin_sendNotification_v1",email,err)
        return INTERNALSERVERERROR
    }))

})
