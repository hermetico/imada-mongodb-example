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
                case 'help':
                    showHelp()
                    break
                case 'addSeenColumn':
                    addSeenColumn()
                    break
                case 'seenById':
                    seenById(sc)
                    break
                case 'tests':
                    tests()
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

    void tests(){
        Iterable result;
        println "Running all points of the README file "
        println "Loading database"
        pokemonService.setupData()
        println "Done"
        println ""
        println "Adding Seen column"
        pokemonService.addColumn("Seen", 0)
        println "Increasing seen of pokemon number 42 by 2"
        result = pokemonService.increaseFieldByNumber("42", "Seen", 2)
        showDetailedOutput(result)
        println "Querying pokemons seen more than 1"
        result  = pokemonService.getSeenMoreThan(1)
        println "Seen ${result.size()} pokemons more than 1"
        println "Querying pokemons seen less than 1"
        result  = pokemonService.getSeenLessThan(1)
        println "Seen ${result.size()} pokemons less than 1"

    }
    void showHelp(){
        println "These are the available commands:"
        println "load\n" +
                "id\n" +
                "scan\n" +
                "type\n" +
                "addSeenColumn\n" +
                "seenById\n" +
                "tests\n" +
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

    void addSeenColumn(){
        pokemonService.addColumn("Seen", 0)
        println "Done"
    }

    void seenById(Scanner sc) {
        println "Which number of pokemn have you seen?"
        String number = sc.nextLine()
        FindIterable iterable = pokemonService.increaseFieldByNumber(number, "Seen", 1)
        showDetailedOutput(iterable)
    }

    static void printHeader(String text) {
        println ""
        println "*"*80
        println text.center(80)
        println "*"*80
    }

}
