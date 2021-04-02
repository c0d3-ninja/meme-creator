const {checkNull,typeOf,types} = require("../../utils/CommonUtils");
const constants = require("../Constants")
const dbConstants=require('../DbConstants');

const isValidNotification = (data)=>{
    return !checkNull(data[dbConstants.KEY_FCM_TOPIC]) &&
        !checkNull(data[dbConstants.KEY_FCM_TITLE]) && !checkNull(data[dbConstants.KEY_FCM_DESC])
    &&!checkNull(data[dbConstants.KEY_FCM_ID]) && !checkNull(data[dbConstants.KEY_FCM_IMAGEURL])
}

module.exports={
    isValidNotification
}