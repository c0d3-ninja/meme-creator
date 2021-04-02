const migrationKeys =require("./MigrationKeys")
const collectionNames=require("../constants/CollectionNames")

module.exports={
    [migrationKeys.PATH]:`${collectionNames.TEMPLATE_CATEGORIES}/:id`,
    [migrationKeys.VALUES]:[
        {id:"VADIVELU",name:"Vadivelu",regionId:"TN",priority:1,imageUrl:"https://firebasestorage.googleapis.com/v0/b/memeking-thugdroid.appspot.com/o/category_images%2Fvadivelu.jpg?alt=media&token=6f60ec60-cd7c-4809-ba52-f10fd59991e1"},
        {id:"GOUNDAMANI_SENTHIL",name:"Goundamani & Senthil",regionId:"TN",priority:2,imageUrl:"https://firebasestorage.googleapis.com/v0/b/memeking-thugdroid.appspot.com/o/category_images%2Fgoundamani_senthil.jpg?alt=media&token=405764b2-9b6a-491c-9c9e-548b663e4bb2"},
        {id:"SANTHANAM",name:"Santhanam",regionId:"TN",priority:3,imageUrl:"https://firebasestorage.googleapis.com/v0/b/memeking-thugdroid.appspot.com/o/category_images%2Fsanthanam.jpg?alt=media&token=d9bb7044-ace1-4c9e-b1ac-8a70e2697f88"},
        {id:"SOORI",name:"Soori",regionId:"TN",priority:4,imageUrl:"https://firebasestorage.googleapis.com/v0/b/memeking-thugdroid.appspot.com/o/category_images%2Fsoori.jpg?alt=media&token=fc853056-1ec0-4cdd-a01f-ccec6e73affa"},
        {id:"VIVEK",name:"Vivek",regionId:"TN",priority:5,imageUrl:""},
        {id:"YOGI_BABU",name:"Yogi Babu",regionId:"TN",priority:6,imageUrl:""},
        {id:"OTHER_COMEDIANS",name:"Other Comedians",regionId:"TN",priority:7,imageUrl:null},
        {id:"ACTORS",name:"Actors",regionId:"TN",priority:8,imageUrl:null},
        {id:"YOUTUBE",name:"Youtube",regionId:"TN",priority:9,imageUrl:"https://firebasestorage.googleapis.com/v0/b/memeking-thugdroid.appspot.com/o/category_images%2Fyoutube.png?alt=media&token=908b1d9a-7994-423e-a0f7-370f069c8945"},
        {id:"POLITICIANS",name:"Politicians",regionId:"TN",priority:10,imageUrl:null},
        {id:"ACTRESS",name:"Actress",regionId:"TN",priority:11,imageUrl:null},
        {id:"OTHERS",name:"Others",regionId:"TN",priority:12,imageUrl:null}
    ]
}