package net.grydeske

import com.mongodb.client.MongoCollection
import com.mongodb.client.FindIterable
import org.bson.Document

class SampleApplication {

    PokemonService pokemonService

    void runApplication() {
        pokemonService = new PokemonService()

        Scanner sc = new Scanner(System.in)

        printHeader("What do you want to do?")

        String line = sc.nextLine()
        while( line != 'exit' ) {
            switch (line) {
                case 'load':
                    pokemonService.setupData()
                    break
                case 'id':
                    getByNumer(sc)
                    break
                case 'type':
                    getByType(sc)
                    break
                case 'scan':
                    scanPokemon()
                    break
                case'help':
                    showHelp()
                    break
                default:
                    println "Unknown input: ${line}"
                    showHelp()

            }
            line = sc.nextLine()
        }

        // close resources
        pokemonService.closeClient();
    }

    void showHelp(){
        println "These are the available commands:"
        println "load\n" +
                "id\n" +
                "scan\n" +
                "type\n" +
                "help"
    }
    void scanPokemon() {
        printHeader('All Characters')
        pokemonService.allPokemon.each {
            println it
        }
    }

    void getByNumer(Scanner sc) {
        println "Which number should we look for?"
        String number = sc.nextLine()
        FindIterable iterable = pokemonService.getByNumber(number)
        showDetailedOutput(iterable)
    }

    void getByType(Scanner sc) {
        println "Which type should we look for?"
        String type = sc.nextLine()
        FindIterable iterable = pokemonService.getByType(type)
        showDetailedOutput(iterable)
    }


    void showDetailedInformation(Document pokemon){
        println "${pokemon['Name']}:"
        for(String key: pokemon.keySet()){
            if(key.equals("name")) continue
            println "- ${key} ${pokemon[key]}"
        }
        println ""

    }

    void showDetailedOutput(Iterable iterable){

        if (iterable.any()){
            for (Document doc : iterable) {
                showDetailedInformation(doc)
            }
        }else{
            println "The query did not return any output"
        }
    }
    static void printHeader(String text) {
        println ""
        println "*"*80
        println text.center(80)
        println "*"*80
    }

}
