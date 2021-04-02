// const  CryptoJS = require("crypto-js");
const adminConstants = require("../constants/AdminConstants")
let types={
    ARRAY_TYPE:"[object Array]",
    STRING_TYPE:"[object String]",
    OBJECT_TYPE:"[object Object]",
    UNDEFINED:"undefined"
}
function typeOf(obj){
    return obj?Object.prototype.toString.call(obj):types.UNDEFINED;
}

function getContextUser(context){
    if(!context || !context.auth){
        return null
    }
    let userId=context.auth.uid
    let email = context.auth.token.email
    if(checkNull(userId)===null || checkNull(email)===null){
        return null
    }
    return {userId,email}
}
function checkNull(param){
    return (param===null || param ===undefined)
}
function getCurrentTimeMillis() {
    return new Date().getTime()
}
function daysToMillis(days=1) {
    return (1000*60*60*24*days)
}
function printException(functionName,email,execption){
    console.error(`Exception in - ${functionName} - ${email} - ${execption}`)
}
function generateRandomCharCodes(min,max){
    return Math.floor(Math.random() * (max - min + 1) + min);
}
function generateRandomName(length){
    let name="";
    for (let i=0;i<length;i++){
        //generate from a-z
        let charCode = generateRandomCharCodes(65,90)
        name += String.fromCharCode(charCode)
    }
    return (`${getCurrentTimeMillis()}${name}`)
}

// function encryptString(str,key){
//     return (CryptoJS.AES.encrypt(str.toString(),key).toString())
// }
// function decryptString(str,key){
//     return (CryptoJS.AES.decrypt(str,key).toString(CryptoJS.enc.Utf8))
// }
function normalize(paramArray,paramKey){
    let object = {}
    let arr = []
    let i=0
    paramArray.map((obj,index)=>{
        if(!object.hasOwnProperty(obj[paramKey])){
            object[obj[paramKey]]  = obj
            arr [i++] = obj[paramKey]
        }
    })
    return ( {data:object,order:arr} )
}
function generateRandomId() { // Public Domain/MIT
    var d = new Date().getTime();
    if (typeof performance !== 'undefined' && typeof performance.now === 'function'){
        d += performance.now();
    }
    return 'xxxxxxxxxxyxxxxxy'.replace(/[xy]/g, function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
}

function validateName(name){
return /^[a-zA-Z0-9]+$/.test(name)
}

function trimLeft(str){
    return str.replace(/^\s+/,"")
}
function titleCase(str) {
    var splitStr = str.toLowerCase().split(' ');
    for (var i = 0; i < splitStr.length; i++) {
        // You do not need to check if i is larger than splitStr length, as your for does that for you
        // Assign it back to the array
        splitStr[i] = splitStr[i].charAt(0).toUpperCase() + splitStr[i].substring(1);
    }
    // Directly return the joined string
    return splitStr.join(' ');
}
function getSearchTagsForServer(searchTagStr,splitter=","){
    let searchTags=replaceAll(searchTagStr,"#","")
    searchTags = searchTags.split(splitter)
    let searchTagsResult=[]
    for(let i=0;i<searchTags.length;i++){
        let currSearchTag = searchTags[i]
        currSearchTag=currSearchTag.trim()
        if(currSearchTag.length>0){
            searchTagsResult.push(titleCase(currSearchTag))
        }
    }
    return searchTagsResult.join(splitter)
}

function getSearchTagsArr(searchStr) {
    return searchStr.split(",")
}

function getSearchTagsForClient(searchTagArr){
    return searchTagArr
}

function isAdminAccount(email){
    if(adminConstants.USER_EMAILS.indexOf(email)!==-1){
        return true
    }
    return false
}

function sanitizeUsername(username){
    if(username===null){
        return username
    }
    if(username.includes("instagram.com") && /^(https?|chrome):\/\/[^\s$.?#].[^\s]*$/.test(username)){
        username  = username.replace("http://","").replace("https://","").split("/")[1].split("?")[0]
    }
    username=replaceAll(username,"@","")
    username=username.trim()
    return username
}

function replaceAll(str,searchValue,replaceValue){
    return str.split(searchValue).join(replaceValue)
}

function getTemplateIdFromFavId(favId){
    return favId.split("_")[0]
}

module.exports={
    getContextUser,
    checkNull,
    printException,
    getCurrentTimeMillis,
    normalize,
    generateRandomId,
    validateName,
    typeOf,
    types,
    getSearchTagsForServer,
    getSearchTagsForClient,
    getSearchTagsArr,
    isAdminAccount,
     sanitizeUsername,
    getTemplateIdFromFavId,
    daysToMillis
}