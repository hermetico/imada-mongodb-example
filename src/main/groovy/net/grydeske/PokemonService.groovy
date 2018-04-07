package net.grydeske

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
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
        def pokemons = collection('pokemons')
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
        collection('pokemons').find()
    }

    def getByNumber(String number) {
        collection('pokemons').find(new Document("Number", number))
    }

    FindIterable getByKeyValue(String key,  String value){
        //Document query = new Document(key, value)
        //return collection('pokemons').find(query);
        return collection('pokemons').find(eq(key, value))
    }

    FindIterable getByType(String type){
        return getByKeyValue("Type1", type)

    }

    FindIterable getByQuery(Document query){
        return collection('pokemons').find(query)

    }

    void addColumn(String key, int value){
        // are equivalent
        // query composition with: exists("Seen", false)
        // and: new Document("Seen", new Document('$exists', false))

        //collection('pokemons').updateMany(
        //        exists("Seen", false),
        //        set(key, value))

        collection('pokemons').updateMany(
                    new Document("Seen", new Document('$exists', false)),
                    set(key, value))
    }

    FindIterable increaseFieldByNumber(String number, String field, Number amount){
        collection('pokemons').updateOne(
                eq("Number", number),
                inc(field, amount))
        // returns the updated element
        return collection('pokemons').find(new Document("Number", number))

    }

    FindIterable getSeenMoreThan(int value){
        Document query = new Document("Seen", new Document('$gt', value))
        return getByQuery(query)
    }

    FindIterable getSeenLessThan(int value){
        Document query = new Document("Seen", new Document('$lt', value))
        return getByQuery(query)
    }

}
