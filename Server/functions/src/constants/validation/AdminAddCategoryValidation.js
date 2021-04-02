const {checkNull,typeOf,types,getSearchTagsForServer} = require("../../utils/CommonUtils");
const dbConstants=require('../DbConstants');
const {isValidCategoryImageUrl} = require('../../utils/ValidationUtils')

const isValidCategory = (data)=>{
    let regionId = data[dbConstants.KEY_REGION_ID]
    let imageUrl = data[dbConstants.KEY_IMAGEURL]
    let name= data[dbConstants.KEY_NAME]
    if(checkNull(regionId)||checkNull(name)){
        return false
    }
    if(regionId.trim().length===0 || name.trim().length===0){
        return  false
    }
    if(imageUrl && !isValidCategoryImageUrl(imageUrl)){
        return false
    }
    return true;
}

const generateCategoryId = (name, regionId)=>{
    return (`${name}_${regionId}`).trim().toUpperCase().replace(/\s/g,"_")
}
const generateCategoryName = (name)=>{
    return (name[0].toUpperCase()+name.slice(1)).trim()
}
module.exports={
    isValidCategory,
    generateCategoryId,
    generateCategoryName
}