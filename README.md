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
- [Format JSON de referència](#format-json-de-referència)
- [Errors comuns](#errors-comuns)
- [Llicència](#llicència)

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
