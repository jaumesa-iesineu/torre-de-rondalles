# 🏰 Torre de Rondalles

> Joc roguelike semiobert desenvolupat en Java amb suport per a configuracions externes, modificacions (*mods*) i creació de contingut personalitzat.

---

## Taula de continguts

- [Requisits i instal·lació](#requisits-i-installació)
- [Executar el joc](#executar-el-joc)
- [Controls](#controls)
- [Arquitectura del motor](#arquitectura-del-motor)
- [Configuració externa](#configuració-externa)
- [Creació de contingut personalitzat](#creació-de-contingut-personalitzat)
- [Com crear un joc nou amb el motor](#com-crear-un-joc-nou-amb-el-motor)
- [Com crear un mod](#com-crear-un-mod)
- [Format JSON de referència](#format-json-de-referència)
- [Errors comuns](#errors-comuns)

---

## Requisits i instal·lació

### Requisits del sistema

| Requisit | Versió mínima |
|----------|--------------|
| Java | 24 o superior |
| Sistema operatiu | Qualsevol compatible amb Java |

### Instal·lació

Clona el repositori amb Git:

```bash
git clone https://github.com/jaumesa-iesineu/torre-de-rondalles
cd torre-de-rondalles
```

---
 
## Seguretat i protecció de dades
 
### Dades dels jugadors
 
Torre de Rondalles és un joc completament local: no es connecta a cap servidor extern ni recull cap dada personal dels jugadors. Tota la informació es queda a l'equip on s'executa el joc.
 
La base de dades `rondalles.db` es crea cada pic que empres el joc default dins el propi `joc.jar` i conté únicament la configuració interna del joc (equivalent al que es pot definir a `game.json`). Encara que es podria aconseguir modificar en temps d'execució del joc, com només conté configuracions del joc **no** és rellevant per a la seguretat dels usuaris.
 
### ⚠️ Integritat de les configuracions i mods
 
La principal consideració de seguretat del projecte és la **integritat de l'experiència de joc** quan s'utilitzen configuracions externes (`-game`) o mods (`-mod`).
 
El motor carrega i aplica els fitxers JSON sense cap validació de límits sobre els valors numèrics. Això significa que un fitxer de mod pot definir, per exemple, un jugador amb 10.000 punts de vida base o un enemic amb dany negatiu, alterant completament l'equilibri i la jugabilitat prevista.
 
Això **no és un error**, sinó una decisió de disseny: el sistema de mods és obert per naturalesa i permet total llibertat creativa. Cal tenir-ho en compte en els casos següents:
 
| Situació | Recomanació |
|----------|-------------|
| Partides en mode estàndard | Executar el joc sense `-mod` ni `-game` externs |
| Compartir mods amb altres jugadors | Revisar els valors del JSON abans de carregar-los |
| Crear contingut per distribuir | Documentar els valors que s'han modificat i el seu efecte |
| Torneigs o reptes entre jugadors | Acordar quins mods (si n'hi ha) són permesos |
 
> **Nota:** El joc no distingeix entre una configuració "oficial" i una de modded. Si es vol garantir una experiència no modificada, cal executar sempre `java -jar joc.jar` sense arguments addicionals.
 
---
## Executar el joc

### Execució bàsica

```bash
java -jar joc.jar
```

### Amb configuració externa (`-game`)

Permet substituir completament la configuració principal del joc per una de personalitzada:

```bash
java -jar joc.jar -game configuracio.json
```

### Amb mods (`-mod`)

Permet carregar un o més mods addicionals sobre la configuració base. Es poden encadenar múltiples fitxers:

```bash
java -jar joc.jar -mod mod1.json mod2.json
```

### Combinant opcions

```bash
java -jar joc.jar -game game.json -mod enemics.json objectes.json
```

---

## Controls

Exepte per el moviment del personatge, els controls es poden personalitzar des del fitxer de configuració. 
Els valors per defecte són:

| Tecla | Acció |
|-------|-------|
| `↑` `←` `↓` `→` | Moviment del personatge |
| `E` | Interactuar amb elements |
| `I` | Obrir inventari |
| `M` | Empènyer objectes (si és possible) |
| `ESC` | Menú / Pausa |

> **Nota:** Tots els controls excepte els de moviment del personatge es poden reconfigurar des del fitxer `game.json`.

---

## Arquitectura del motor

El joc s'organitza en mòduls independents que treballen conjuntament:

 ### 🛠️⚙️ Motor principal (`motor/`)

 <img width="50" height="50" alt="W12EngineGIF" src="https://github.com/user-attachments/assets/abb4ebfc-ce2b-4ca9-8355-d707ec2536c5" />


Gestiona el **bucle de joc per torns**: renderitza la pantalla, llegeix l'entrada de l'usuari i actualitza l'estat. Inclou suport per a animacions no bloquejants, com ara:
- Lliscament sobre gel
- Efecte *typewriter* a la pantalla de *game over*

### 🎮 Lògica de joc (`joc/`)

Controla els **estats del joc** i les transicions entre ells:
- MENU `pantalla inicial, menú principal`
- MENU_INICIAL `benvinguda abans de començar a jugar`
- PAUSA `menú de pausa, botons de sempre`
- MON `el jugador es mou pel mapa per torns`
- COMBAT `combat per torns, es mapa es queda congelat`
- GAME_OVER `el jugador ha mort, s'esborra sa partida`
- INVENTARI `inventari superposat damunt es mapa, perds un torn`
- VICTORIA `has guanyat! es drac ha caigut`
- COMERCIANT `pantalla de comerç amb es NPC`
- ENIGMA `pantalla de l'endevinalla del NPC`
- SELECCIO_PERSONATGE `tria de tipus de personatge`
- CREACIO_PERSONATGE `distribució de punts per al personatge personalitzat`

Carrega la configuració des de `game.json` i els seus subfitxers JSON. Gestiona el sistema de mods (`-mod`) i permet controls configurables.

### 👾 Entitats (`entitats/`)

Defineix totes les entitats del joc:

| Entitat | Descripció |
|---------|-----------|
| Jugador | Personatge controlat per l'usuari |
| Enemics | IA per torns: perseguir, guàrdia, estàtic, pacman |
| NPCs | Comerciants i altres personatges no jugadors |
| Portes | Elements interactius del mapa |

### ⚔️ Sistema de combat (`combat/`)

<img width="50" height="50" alt="DontMessWithMeCatBackOffCatGIF" src="https://github.com/user-attachments/assets/724221b0-c935-413e-83eb-29bd69d5ba31" />


Gestiona totes les mecàniques de combat:
- Càlcul de dany i esquivada
- Efectes d'estat: **verí**, **foc**, **gel**
- Atac sorpresa

### 🎒 Inventari i ítems (`inventari/`)

Sistema d'objectes amb suport per a:
- Armes
- Armadures per *slot*
- Pocions
- Claus
- Ítems al terra del mapa

### 🗺️ Mapa (`mapa/`)

- Càrrega de mapes des de fitxers `.map`
- Tipus de terreny configurables: **gel**, **aigua**, **punxes**, **buit**, amb velocitat d'entrada al terreny
- Terrenys amagats (traps)
- Boira de guerra amb algorisme de Bresenham
  >**Nota:** Els mapes son simple text com un `.txt` encara que la extensió realment és `.map`

### 🔊 Àudio (`audio/`)

Música de fons per pis, configurable des del JSON.

### 💾 Persistència (`db/`)

Configuració predeterminada emmagatzemada en base de dades **SQLite** (`rondalles.db`).

### 📁 Recursos (`src/main/resources/`)

| Carpeta | Contingut |
|---------|-----------|
| `mapes/` | Fitxers de mapa `.map` |
| `art/` | Art ASCII del joc |
| `gameover/` | Pantalles de *game over* en format JSON |

---

## Configuració externa

El fitxer `game.json` és el punt d'entrada principal per personalitzar el joc **sense modificar el codi font**.

### `-game` — Substituir la configuració principal

Permet crear experiències completament noves amb el mateix motor:

- Campanyes noves
- Conjunts de mapes personalitzats
- Mecàniques i textos diferents

```bash
java -jar joc.jar -game game.json
```

### `-mod` — Afegir contingut addicional

Permet ampliar el contingut base sense substituir-lo. Exemples d'ús:

- Nous enemics
- Nous objectes i armes
- Nous NPCs
- Nous mapes

```bash
java -jar joc.jar -mod enemics.json
```

> Els mods es carreguen **sobre** la configuració base (o la configuració `-game` si s'especifica). Es poden encadenar múltiples fitxers.

#### Mods d'exemple

A la carpeta `mods/` hi ha tres mods bàsics ja fets que es poden provar directament:

- `mod_facil.json` — baixa la vida i l'atac del Dimoni Boiet i la Bubota
- `mod_tresor.json` — afegeix pocions de vida i una espasa extra a planta1
- `mod_espasa_nova.json` — dona d'alta una arma nova al catàleg (Espasa de foc) i la posa al mapa

```bash
java -jar joc.jar -mod mods/mod_facil.json
java -jar joc.jar -mod mods/mod_tresor.json mods/mod_espasa_nova.json
```

---

## Creació de contingut personalitzat

### Guia pas a pas

1. **Crear el fitxer JSON** amb el contingut nou (veure [Format JSON](#format-json-de-referència))
2. **Afegir el contingut** seguint els camps requerits per a cada tipus d'entitat
3. **Executar el joc** amb `-mod` o `-game` apuntant al fitxer creat
4. **Comprovar errors** a la consola si alguna cosa no funciona (veure [Errors comuns](#errors-comuns))
> **Nota:** En la carpeta `resources` està un exemple de com ha de estar tot estructurat.
> **Nota:** Per fer els assets dels enemics(json) recomanam emprar la esguent pàgina: [ASCII Studio](https://asciistudio.app)

---

## Com crear un joc nou amb el motor

Pots fer servir el motor de Torre de Rondalles per crear el teu propi joc roguelike des de zero, sense tocar cap línia de codi Java. Només cal preparar uns fitxers JSON i els mapes en text pla.

### Estructura de carpetes recomanada

```
el-meu-joc/
├── game.json          ← punt d'entrada del teu joc
├── configuracio.json  ← opcions globals (mapa inicial, radis, etc.)
├── mapes.json         ← llista de mapes i l'ordre de progressió
├── enemics.json       ← tipus d'enemics i on estan al mapa
├── items.json         ← catàleg d'armes, armadures, pocions i posicions
├── npcs.json          ← comerciants i NPCs
├── jugador.json       ← stats base del jugador
├── controls.json      ← tecles configurables
├── musica.json        ← fitxers d'àudio per estat
├── texts.json         ← textos de la interfície
├── art/
│   └── enemic1.txt    ← art ASCII de cada enemic
└── mapes/
    ├── planta1.map    ← mapa en text pla
    └── planta2.map
```

### Pas a pas

**1. Crea el `game.json`**

Aquest fitxer és el punt d'entrada. Amb `$include:` pots separar cada secció en el seu propi fitxer:

```json
{
  "configuracio": "$include:configuracio.json",
  "controls":     "$include:controls.json",
  "musica":       "$include:musica.json",
  "npcs":         "$include:npcs.json",
  "jugador":      "$include:jugador.json",
  "texts":        "$include:texts.json"
}
```

O pots posar-ho tot dins el mateix fitxer, sense `$include:`, si el joc és petit.

**2. Configura les opcions globals (`configuracio.json`)**

```json
{
  "mapaInicial":   "pis1",
  "radiLlanterna": 8,
  "ampleHud":      30,
  "radiVisio":     10,
  "maxSlotsInventari": 4,
  "danyVeri":      3
}
```

| Camp | Descripció |
|------|-----------|
| `mapaInicial` | `id` del mapa on comença la partida |
| `radiLlanterna` | Quantes cel·les al voltant veu el jugador |
| `ampleHud` | Amplada del panell lateral en caràcters |
| `maxSlotsInventari` | Màxim d'objectes a l'inventari |
| `danyVeri` | Dany per torn quan el jugador està enverinat |

**3. Defineix els mapes (`mapes.json`)**

```json
{
  "mapes": {
    "ordre": ["pis1", "pis2"],
    "registres": [
      { "id": "pis1", "fitxer": "mapes/pis1.map" },
      { "id": "pis2", "fitxer": "mapes/pis2.map" }
    ]
  }
}
```

**4. Crea els fitxers de mapa (`.map`)**

Un mapa és un fitxer de text pla. La primera línia és el nom que es mostra. A partir de la segona, cada caràcter és una cel·la:

| Caràcter | Significat |
|----------|-----------|
| `#` | Paret |
| `.` | Terra buit |
| `~` | Aigua (ralenteix) |
| `*` | Gel (rellisca) |
| `≈` | Punxes (fan dany) |
| `<` | Escales (canvi de planta) |
| Lletra majúscula/minúscula | Enemic o NPC (la que defineixis a `enemics.json`) |

```
nom: Pis 1
################
#..............#
#...d..........#
#..............#
####....<...####
```

> **Nota:** L'amplada de totes les files ha de ser la mateixa. Si no, el motor pot donar errors de càrrega.

**5. Defineix els enemics (`enemics.json`)**

```json
{
  "enemics": {
    "tipus": [
      {
        "simbols": ["d"],
        "nom":     "Drac Petit",
        "vida":    20,
        "atac":    6,
        "radi":    5,
        "colorR":  220,
        "colorG":  30,
        "colorB":  30,
        "patroIA": "perseguir",
        "esBoss":  false,
        "artFitxer": "art/drac_petit.txt"
      }
    ],
    "posicions": [
      { "mapa": "pis1", "simbol": "d", "x": 5, "y": 3 }
    ]
  }
}
```

| Camp | Valors possibles | Descripció |
|------|-----------------|-----------|
| `patroIA` | `perseguir`, `guardia`, `static`, `pacman` | Comportament de l'enemic |
| `esBoss` | `true` / `false` | Si és boss, sona la música de boss en combat |
| `travessaParets` | `true` / `false` | Si pot travessar parets (com la Bubota) |
| `velocitat` | número enter | Caselles que es mou per torn (per defecte 1) |
| `artFitxer` | ruta relativa | Fitxer `.txt` amb l'art ASCII de l'enemic |

> **Nota:** Per fer l'art ASCII dels enemics recomanam [ASCII Studio](https://asciistudio.app). El fitxer és text pla: una línia del fitxer = una línia de l'art.

**6. Executa el teu joc**

```bash
java -jar joc.jar -game el-meu-joc/game.json
```

---

## Com crear un mod

Un mod modifica o amplia el joc base (o un `-game`) sense substituir-lo del tot. Només cal crear un fitxer JSON amb les seccions que vols canviar: el motor aplica els canvis per sobre de la configuració existent.

### Regles bàsiques

- Només poses al mod el que vols canviar. La resta queda igual.
- Si redefineixis un enemic pel seu `nom` o `simbols`, es sobreescriu el de base.
- Si afegeixes posicions noves, s'afegeixen sense eliminar les existents.
- Es poden encadenar múltiples mods: `java -jar joc.jar -mod mod1.json mod2.json`

### Exemple 1 — modificar els stats d'un enemic existent

```json
{
  "enemics": {
    "tipus": [
      {
        "simbols": ["d", "e"],
        "nom":   "DimoniBoiet",
        "vida":  5,
        "atac":  2
      }
    ]
  }
}
```

### Exemple 2 — afegir un enemic nou i col·locar-lo al mapa

```json
{
  "enemics": {
    "tipus": [
      {
        "simbols": ["V"],
        "nom":     "Vampir",
        "vida":    35,
        "atac":    8,
        "radi":    6,
        "colorR":  120,
        "colorG":  0,
        "colorB":  200,
        "patroIA": "perseguir",
        "esBoss":  false,
        "artFitxer": "art/vampir.txt"
      }
    ],
    "posicions": [
      { "mapa": "planta2", "simbol": "V", "x": 10, "y": 5 }
    ]
  }
}
```

> **Nota:** Si afegeixes un enemic nou amb un símbol nou (`V`), recorda posar aquell símbol al fitxer `.map` corresponent on vols que aparegui, o afegir la posició al JSON (com a l'exemple).

### Exemple 3 — afegir ítems al mapa

```json
{
  "items": {
    "posicions": [
      { "mapa": "planta1", "id": "pocio-vida",     "x": 10, "y": 4 },
      { "mapa": "planta1", "id": "espasa-cavaller", "x": 12, "y": 4 }
    ]
  }
}
```

### Exemple 4 — afegir una arma nova al catàleg i posar-la al mapa

```json
{
  "items": {
    "catalogItems": {
      "armes": [
        {
          "id":     "arc-estrelles",
          "nom":    "Arc de les Estrelles",
          "pes":    2,
          "simbol": "A",
          "atac":   11,
          "rang":   3,
          "tier":   2
        }
      ]
    },
    "posicions": [
      { "mapa": "planta2", "id": "arc-estrelles", "x": 8, "y": 6 }
    ]
  }
}
```

### Executar amb un mod

```bash
java -jar joc.jar -mod el-meu-mod.json
java -jar joc.jar -mod mods/mod_facil.json mods/el-meu-mod.json
java -jar joc.jar -game el-meu-joc/game.json -mod el-meu-mod.json
```

### Mods d'exemple inclosos

A la carpeta `mods/` tens tres exemples que pots estudiar i modificar:

| Fitxer | Què fa |
|--------|--------|
| `mod_facil.json` | Baixa la vida i l'atac del DimoniBoiet i la Bubota |
| `mod_tresor.json` | Afegeix pocions de vida i una espasa a planta1 |
| `mod_espasa_nova.json` | Dona d'alta una arma nova (Espasa de foc) i la col·loca al mapa |

---

## Format JSON de referència

### Exemple d'enemic

```json
{
  "id": "goblin",
  "vida": 50,
  "atac": 10,
  "defensa": 3,
  "velocitat": 1,
  "ia": "perseguir",
  "artFitxer": "goblin.txt"
}
```

### Camps disponibles

| Camp | Tipus | Descripció |
|------|-------|-----------|
| `id` | `string` | Identificador únic de l'entitat |
| `vida` | `int` | Punts de vida màxims |
| `atac` | `int` | Dany d'atac base |
| `defensa` | `int` | Reducció de dany rebut |
| `velocitat` | `int` | Velocitat de moviment per torn |
| `coordenades` | `[x, y]` | Posició inicial al mapa |
| `artFitxer` | `string` | Ruta al fitxer d'art ASCII |
| `ia` | `string` | Tipus d'IA: `perseguir`, `guardia`, `static`, `pacman` |

---

## Errors comuns

### Fitxer no trobat

```
No s'ha pogut carregar xxx.json
```

**Solucions possibles:**
- Comprova que la ruta al fitxer és correcta
- Comprova que l'extensió és `.json`
- Comprova que tens permisos de lectura sobre el fitxer

### JSON invàlid

```
Error carregant configuració
```

**Solucions possibles:**
- Comprova que totes les comes entre camps estan presents
- Comprova que tots els claudàtors i claus `{}` `[]` estan tancats correctament
- Comprova que tots els camps obligatoris hi són
- Utilitza un validador JSON en línia per detectar errors de sintaxi
