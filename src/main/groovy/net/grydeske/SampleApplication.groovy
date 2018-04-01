package net.grydeske

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
                case 'scan':
                    scanPokemon()
                    break
                default:
                    println "Unknown input: ${line}"
            }
            line = sc.nextLine()
        }

        // close resources
        pokemonService.closeClient();
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
        pokemonService.getByNumber(number).each {
            println it
        }
    }

    static void printHeader(String text) {
        println ""
        println "*"*80
        println text.center(80)
        println "*"*80
    }

}
