# TimeTrialPlugin
Plugin udělaný pro administrátory serverů, udělujicí jim možnost vyrobit time trialy pro hráče.


### Příkazy
`/timetrial help` - Zobrazí pomoc s příkazy  
`/timetrial create <název>` - Vytvoří nový time trial tam, kde stojíš  
`/timetrial set <id> end` - Nastaví konec time trialu tam, kde stojíš  
`/timetrial set <id> time <sekundy>` - Nastaví limit pro dokončení time trialu  
`/timetrial set <id> info <text>` - Nastaví info pro time trial  
`/timetrial set <id> reward add <příkaz>` - Přidá příkaz jako odměnu za dokončení time trialu (%plr% pro jméno hráče)  
`/timetrial set <id> reward list` - Vypíše veškeré odměnové příkazy time trialu a jejich id  
`/timetrial set <id> reward remove <id příkazu>` - Odebere odměnový příkaz  
`/timetrial list` - Vypíše všechny time trialy a jejich pozice  
`/timetrial remove <id>` - Odebere time trial  
`/timetrial refresh` - Opraví všechny time trialy  



### Práva
`timetrial.admin` - Právo vytvořit, nastavit a odstraňovat time trialy
