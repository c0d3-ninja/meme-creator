const constants = require("../constants/Constants")
const {checkNull,typeOf,types} = require("./CommonUtils")

const isValidCategoryImageUrl=(url="")=>{
    if(typeOf(constants.FIRESTORE_CATEGORIES_PATH)===types.STRING_TYPE){
        return  url.startsWith(constants.FIRESTORE_CATEGORIES_PATH)
    }else{
        let patternsArr = constants.FIRESTORE_CATEGORIES_PATH
        for(let i=0;i<patternsArr.length;i++){
            let pattern = patternsArr[i]
            if(url.startsWith(pattern)){
                return true
            }
        }
        return false
    }
}

const isValidFavDocId=(docId,contextuserId)=>{
    return docId.split("_")[1]===contextuserId
}

const isValidSearchTag =(searchTags="")=>{
    if(checkNull(searchTags)){
        return false
    }
    if(searchTags.trim().length===0){
        return false
    }
    return true
}

module.exports={
    isValidFavDocId,
    isValidSearchTag,
    isValidCategoryImageUrl
}