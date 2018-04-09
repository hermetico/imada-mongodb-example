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
        println "Deleting pokemon 42"
        pokemonService.deleteByNumber("42")
        println "Requesting pokemon 42, should return empty collection"
        result = pokemonService.getByNumber("42")
        assert(result.size() == 0) : "Should return 0 but query returned ${result.size()}"
        println "Returned 0 as expected"
        println "Inserting location Barcelona"
        pokemonService.addLocation("Barcelona")
        println "Scanning al locations"
        showDetailedOutput(pokemonService.getAllLocations())
        println "Inserting locations Odense, Valencia"
        pokemonService.addLocations(["Odense", "Valencia"] as String[])
        println "Scanning al locations"
        showDetailedOutput(pokemonService.getAllLocations())
        println "Juan sees pokemon 40 at Barcelona"
        pokemonService.pokemonSeenAtBy("40", "Barcelona", "Juan")
        println "Juan sees pokemon 41 at Odense two times"
        pokemonService.pokemonSeenAtBy("41", "Odense", "Juan")
        pokemonService.pokemonSeenAtBy("41", "Odense", "Juan")
        println "Showing all appearances"
        pokemonService.getAllAppearances().each{println it}
        println "Peter sees pokemon 40 at Barcelona and Odense"
        pokemonService.pokemonSeenAtBy("40", "Odense", "Peter")
        pokemonService.pokemonSeenAtBy("40", "Barcelona", "Peter")
        println "Showing all appearances"
        //pokemonService.getAllAppearances().each{println it}
        showDetailedOutput(pokemonService.getAllLocations())
        println "Showing appearances of pokemon 40"
        //pokemonService.getPokemonAppearances("40").each{println it}

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
        pokemonService.allPokemon.each{
            println it
        }
        //showDetailedOutput(pokemonService.allPokemon)
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
