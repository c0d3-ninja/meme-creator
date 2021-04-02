const {checkNull,typeOf,types,getSearchTagsForServer} = require("../../utils/CommonUtils");
const constants = require("../Constants")
const dbConstants=require('../DbConstants');
const {isValidRegion}=require("./TemplateValidation")
const isValidImageUrl=(url="")=>{
    let patternsArr = constants.FIRESTORE_CATEGORIES_PATH
    for(let i=0;i<patternsArr.length;i++){
        let pattern = patternsArr[i]
        if(url.startsWith(pattern)){
            return true
        }
    }
    return false
}

const isValidTemplateGroupRequest = (data={})=>{
    return (
        isValidImageUrl(data[dbConstants.KEY_IMAGEURL])
        && isValidRegion(data[dbConstants.KEY_REGION_ID])
        &&(!checkNull(data[dbConstants.KEY_NAME])
        && (!checkNull(data[dbConstants.KEY_SEARCHSTR]))
        )
    )
}
module.exports={
    isValidTemplateGroupRequest
}