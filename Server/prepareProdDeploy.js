const fs = require('fs')

const buildConfigContent = `module.exports={
    DEBUG:false,
    ISPRODUCTION:true,
    VALUE_FEED_SEARCH_INDEX:"prod_FEED_TEMPLATES",
    VALUE_FAV_SEARCH_INDEX:"prod_FAVORITE_TEMPLATES",
    FIRESTORE_MEMES_PATH:["https://firebasestorage.googleapis.com/v0/b/memeking-thugdroid.appspot.com/o/memes","https://firebasestorage.googleapis.com/v0/b/memeking-thugdroid-asia/o/memes"],
    FIRESTORE_TEMPLATES_PATH:["https://firebasestorage.googleapis.com/v0/b/memeking-thugdroid.appspot.com/o/templates","https://firebasestorage.googleapis.com/v0/b/memeking-thugdroid-asia/o/templates"],
    FIRESTORE_CATEGORIES_PATH:["https://firebasestorage.googleapis.com/v0/b/memeking-thugdroid.appspot.com/o/category_images","https://firebasestorage.googleapis.com/v0/b/memeking-thugdroid-asia/o/category_images"]
}`

const firebaseRcContent = `{
  "projects": {
    "default": "memeking-thugdroid"
  }
}`

try {
    console.log("Preparing Production Deploy=====>")
    console.log("Writing BuildConfig.js started")
    fs.writeFileSync('BuildConfig.js', buildConfigContent)
    console.log("Writing BuildConfig.js ended")
    console.log("Writing .firebaserc started")
    fs.writeFileSync('../.firebaserc', firebaseRcContent)
    console.log("Writing .firebaserc ended")
} catch (err) {
    console.error(err)
}
