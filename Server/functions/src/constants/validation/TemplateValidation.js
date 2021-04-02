const {checkNull,typeOf,types,getSearchTagsForServer} = require("../../utils/CommonUtils");
const constants = require("../Constants")
const dbConstants=require('../DbConstants');
const isValidImageUrl=(url="")=>{
    if(typeOf(constants.FIRESTORE_TEMPLATES_PATH)===types.STRING_TYPE){
        return  url.startsWith(constants.FIRESTORE_TEMPLATES_PATH)
    }else{
        let patternsArr = constants.FIRESTORE_TEMPLATES_PATH
        for(let i=0;i<patternsArr.length;i++){
            let pattern = patternsArr[i]
            if(url.startsWith(pattern)){
                return true
            }
        }
        return false
    }
}
const isValidSearchTag =(searchTags="")=>{
    if(typeOf(searchTags)!==types.STRING_TYPE){
        return false
    }
    if(checkNull(searchTags)){
        return false
    }
    if(searchTags.trim().length===0 && searchTags.length>constants.SEARCH_TAGS_MAX_LENGTH){
        return false
    }
    return true
}
const isValidRegion = (regionId)=>{
    return (!checkNull(regionId) && typeOf(regionId)===types.STRING_TYPE)
}

const isValidCategory = (categoryId)=>{
    return typeOf(categoryId)===types.STRING_TYPE
}

function isValidRequest(request){
    return isValidImageUrl(request[dbConstants.KEY_IMAGEURL]) &&
        isValidSearchTag(request[dbConstants.KEY_SEARCHTAGS]) &&
        getSearchTagsForServer(request[dbConstants.KEY_SEARCHTAGS]).length>0
        &&isValidRegion(request[dbConstants.KEY_REGION_ID])
        && isValidCategory(request[dbConstants.KEY_CATEGORY_ID])
}

function isUpdateRequestValid(request){
    return typeOf(request[dbConstants.KEY_ID]) ===types.STRING_TYPE &&
        isValidSearchTag(request[dbConstants.KEY_SEARCHTAGS]) &&
        getSearchTagsForServer(request[dbConstants.KEY_SEARCHTAGS]).length>0
    &&!checkNull(request[dbConstants.KEY_CATEGORY_ID]) && typeOf(request[dbConstants.KEY_CATEGORY_ID]) ===types.STRING_TYPE
}

const isValidFavDocId=(docId,userId)=>{
    if(typeOf(docId)!==types.STRING_TYPE){
        return  false
    }
    let docIdArr = docId.split("_")
    if(docIdArr.length<1){
        return false
    }
    return  docIdArr[1]===userId
}

function isFavRequestValid(request,userId){
    return isValidFavDocId(request[dbConstants.KEY_ID],userId)
    && isValidRequest(request)
}

module.exports={
    isValidRequest,
    isUpdateRequestValid,
    isValidRegion,
    isValidCategory,
    isValidFavDocId,
    isFavRequestValid
}