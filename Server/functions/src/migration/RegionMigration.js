const migrationKeys =require("./MigrationKeys")
const collectionNames=require("../constants/CollectionNames")

module.exports={
    [migrationKeys.PATH]:`${collectionNames.REGION}/:id`,
    [migrationKeys.VALUES]:[
        {id:"TN",name:"Kollywood",language:"Tamizh",priority:1},
        {id:"KL",name:"Mollywood",language:"Malayalam",priority:2},
        {id:"AP",name:"Tollywood",language:"Telugu",priority:3},
        {id:"KA",name:"Sandalwood",language:"Kannada",priority:4},
        {id:"DL",name:"Bollywood",language:"Hindi",priority:5},
        {id:"EN",name:"Hollywood",language:"English",priority:6},
    ]
}