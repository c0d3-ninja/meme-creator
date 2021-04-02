const {checkNull} = require("../../utils/CommonUtils");
const dbConstants=require('../DbConstants');

const isValidSendFeedbackRequest=(data)=>{
    let region = data[dbConstants.KEY_REGION_ID]
    let version = data[dbConstants.KEY_APP_VERSION]
    if(checkNull(region) || checkNull(version)){
        return false
    }
    return true
}

module.exports={
    isValidSendFeedbackRequest
}