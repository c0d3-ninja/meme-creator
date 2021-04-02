const {checkNull,typeOf,types} = require("../../utils/CommonUtils");
const constants = require("../Constants")
const dbConstants=require('../DbConstants');

const isValidRegion = (regionId)=>{
    return !checkNull(regionId) && typeOf(regionId)===types.STRING_TYPE
}

module.exports={
    isValidRegion
}