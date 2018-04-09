package net.grydeske

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.mongodb.client.AggregateIterable
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Projections
import jdk.nashorn.internal.runtime.FindProperty
import org.bson.types.ObjectId

import javax.xml.stream.Location

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Projections.*;
import com.mongodb.client.model.Sorts;
import org.bson.Document

class PokemonService {

    MongoClient mongoClient

    def host = "localhost" //your host name
    def port = 27017 //your port no.
    def databaseName = 'pokemon'
    def POKEMONS = 'pokemons'
    def LOCATIONS = 'locations'
    def APPEARANCES = 'appearances'

    MongoClient client() {
        mongoClient = mongoClient ?: new MongoClient(host, port)

        return mongoClient
    }

    MongoCollection collection(collectionName) {
        MongoDatabase db = client().getDatabase(databaseName)

        return db.getCollection(collectionName)
    }

    void closeClient() {
        client().close()
    }

    void setupData() {
        def pokemons = collection(POKEMONS)
        // restart the DB
        pokemons.drop()
        collection(LOCATIONS).drop()
        collection(APPEARANCES).drop()

        if( !pokemons.count()) {
            def items = []
            def first = true
            def headers

            this.getClass().getResource( '/Pokemon.csv' ).text.eachLine {
                def parts = it.split(',')
                if( first) {
                    headers = parts
                    first = false
                } else {
                    if( parts.size() == 13) {
                        Map pokemon = [:]
                        parts.eachWithIndex{ String entry, int i ->
                            pokemon[headers[i]] = parts[i]
                        }
                        items << pokemon
                    }
                }
            }

            def data = items.collect { it as Document }

            pokemons.insertMany( data )
        }

    }

    def getAllPokemon() {
        collection(POKEMONS).find()
    }

    def getAllAppearances(){
        collection(APPEARANCES).find()
    }

    def getByNumber(String number) {
        collection(POKEMONS).find(new Document("Number", number))
    }
    def getUniqueByNumber(String number) {
        collection(POKEMONS).find(new Document("Number", number)).first()
    }

    FindIterable getByKeyValue(String key,  String value){
        //Document query = new Document(key, value)
        //return collection('pokemons').find(query);
        return collection(POKEMONS).find(eq(key, value))
    }

    FindIterable getByType(String type){
        return getByKeyValue("Type1", type)

    }

    FindIterable getByQuery(Document query){
        return collection(POKEMONS).find(query)

    }

    void addColumn(String key, int value){
        // are equivalent
        // query composition with: exists("Seen", false)
        // and: new Document("Seen", new Document('$exists', false))

        //collection('pokemons').updateMany(
        //        exists("Seen", false),
        //        set(key, value))

        collection(POKEMONS).updateMany(
                    new Document("Seen", new Document('$exists', false)),
                    set(key, value))
    }

    FindIterable increaseFieldByNumber(String number, String field, int amount){
        collection(POKEMONS).updateOne(
                eq("Number", number),
                inc(field, amount))
        // returns the updated element
        return collection(POKEMONS).find(new Document("Number", number))

    }

    FindIterable getSeenMoreThan(int value){
        Document query = new Document("Seen", new Document('$gt', value))
        return getByQuery(query)
    }

    FindIterable getSeenLessThan(int value){
        Document query = new Document("Seen", new Document('$lt', value))
        return getByQuery(query)
    }

    void deleteByNumber(String number){
        collection('pokemons').deleteOne(eq("Number", number))
    }

    Document getLocationByName(String name){
        return collection(LOCATIONS).find(eq("Name", name)).first()
    }
    void addLocation(String location){
        int numLocations = collection(LOCATIONS).find().size()
        Document _new = new Document("Name", location)
                .append("Number", numLocations)
        collection(LOCATIONS).insertOne(_new)
    }

    void addLocations(String[] locations){
        def items = []
        int numLocations = collection(LOCATIONS).find().size()

        locations.each{
            items << new Document("Name", it)
                    .append("Number", numLocations++)
                    .append("Seen", Arrays.asList())

        }
        //def data = items.collect { it as Document }

        collection(LOCATIONS).insertMany( items )

    }

    FindIterable getAllLocations(){
        return collection(LOCATIONS).find()
    }


    Document _findAppearance(ObjectId what, ObjectId where, String who){
        return collection(APPEARANCES).find(
                new Document("pokemon_id", what)
                        .append("location_id", where)
                        .append("Who", who)
        ).first()
    }
    Document findAppearance(ObjectId what, ObjectId where, String who){
        return collection(APPEARANCES).find(
                new Document("pokemon_id", what)
                        .append("location_id", where)
                        .append("Who", who)
        ).first()
    }

    void addAppearance(ObjectId what, ObjectId where, String who){
        Document appearance = new Document("pokemon_id", what)
                .append("location_id", where)
                .append("Who", who)
                .append("Times", 1)
        collection(APPEARANCES).insertOne(appearance)
    }

    void _addAppearance(ObjectId what, ObjectId where, String who){
        Document appearance = new Document("pokemon_id", what)
                .append("location_id", where)
                .append("Who", who)
                .append("Times", 1)
        collection(APPEARANCES).insertOne(appearance)
    }

    void _increaseAppearance(ObjectId what, ObjectId where, String who){
        Document appearance = new Document("pokemon_id", what)
                .append("location_id", where)
                .append("Who", who)
        collection(APPEARANCES).updateOne(appearance, new Document('$inc', new Document("Times", 1)))
    }

    void increaseAppearance(ObjectId what, ObjectId where, String who){
        Document appearance = new Document("pokemon_id", what)
                .append("location_id", where)
                .append("Who", who)
        collection(APPEARANCES).updateOne(appearance, new Document('$inc', new Document("Times", 1)))
    }

    void _pokemonSeenAtBy(String pokemonNumber, String place, String who){
        Document pokemon = getUniqueByNumber(pokemonNumber)
        assert(pokemon != null) : "That pokemon number does not exist"
        Document location = getLocationByName(place)
        assert(location != null) : "That location does not exist"


        Document appearance = findAppearance(pokemon['_id'] as ObjectId, location['_id'] as ObjectId, who)

        if(appearance == null){
            addAppearance(pokemon['_id']as ObjectId, location['_id']as ObjectId, who)
        }else{
            increaseAppearance(pokemon['_id']as ObjectId, location['_id']as ObjectId, who)
        }
        // increasing the times the pokemon has been seen
        increaseFieldByNumber(pokemonNumber, "Seen", 1)

    }

    void pokemonSeenAtBy(String pokemonNumber, String place, String who){
        Document pokemon = getUniqueByNumber(pokemonNumber)
        assert(pokemon != null) : "That pokemon number does not exist"
        Document location = getLocationByName(place)
        assert(location != null) : "That location does not exist"

        Document seen = new Document("pokemon_id", pokemon['_id']).append("Who", who)

        Document appearance = collection(LOCATIONS).find(
                new Document("Name", place).append("Seen", new Document('$elemMatch', seen))

         ).first()

        if(appearance == null){
            println "appearance is null!"

            collection(LOCATIONS).updateOne(new Document("Name", place),
                    new Document('$push', new Document('Seen', seen.append("Times", 1)))
            )

        }else{
            println "appearance is not null!"
            collection(LOCATIONS).updateOne(
                    new Document("Name", place)
                            .append("Seen.pokemon_id", pokemon['_id'])
                            .append("Seen.Who", who),
                    new Document('$inc', new Document('Seen.$.Times', 1)))

        }
        println "now increasasing in the pokemon"
        // increasing the times the pokemon has been seen
        increaseFieldByNumber(pokemonNumber, "Seen", 1)

    }


    AggregateIterable getPokemonAppearances(String pokemonNumber){
        Document pokemon = getUniqueByNumber(pokemonNumber)
        assert(pokemon != null) : "That pokemon number does not exist"
        AggregateIterable response = collection(APPEARANCES).aggregate(
                Arrays.asList(
                        // querying by id, grouping by location and counting how many appearances
                        Aggregates.match(eq("pokemon_id", pokemon['_id'])),
                        Aggregates.group('$location_id',
                                Accumulators.sum("Times", 1))
                )
        )

        return response
    }

}
