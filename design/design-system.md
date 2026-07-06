# QReport — Design System (Arancio e Technical Design)

Adattamento per QReport della direzione "Arancio e Technical Design" già scelta e
implementata in QuickStore (vedi `../../QuickStore/design/design-system.md`,
opzione 3). Stessa struttura, stessa tipografia, stessa spaziatura, stessa
codifica della severità per intensità di un unico accento — cambia solo la
**regola d'inchiostro** tra arancio e graphite.

**Stato: implementato nell'app**, non solo materiale di design — vedi
"Implementazione nel codice" più sotto per la mappatura completa componente ↔
file Kotlin e lo stato di avanzamento (17 fasi, tutte con build verde,
verifica visiva su device reale dopo ogni fase da parte dell'utente).

Anteprima: `design/mockup-orange-technical.html` · Token: `design/tokens-orange-technical.json`

## La regola d'inchiostro (unica differenza rispetto a QuickStore)

QuickStore usa l'arancio anche come colore di testo/icona direttamente su
pagina chiara (eyebrow, pulsante testuale, icone di scorciatoia, valori
numerici in evidenza), e il bianco come testo sopra i riempimenti arancioni
(pulsante primario, chip attivo, badge). Per QReport la regola è invertita:

- **Arancio come inchiostro** (testo o icona) → ammesso **solo su sfondo
  grafite** (tema scuro). Su pagina chiara i punti che in QuickStore erano
  arancio diventano grafite.
- **Grafite come inchiostro** → ammesso su **sfondo bianco** (il caso normale)
  **oppure sopra un riempimento arancione pieno** (pulsante primario, chip
  attivo, badge critico, segmented attivo): il testo su quei riempimenti è
  grafite, non più bianco.
- **I riempimenti pieni non cambiano**: pulsanti, chip, badge, strisce di
  stato e mire d'angolo restano arancio pieno anche su pagina chiara,
  esattamente come in QuickStore. La regola vincola solo l'inchiostro sopra
  quelle forme, non le forme stesse — altrimenti l'arancio sparirebbe dal
  tema chiaro e la severità per intensità (il punto centrale del design)
  non si leggerebbe più.

Motivo, verificato per contrasto (WCAG):

| Combinazione | Contrasto | Esito |
|---|---|---|
| Bianco su arancio (`#FFFFFF` su `#E88706`) | 2.66:1 | Sotto soglia AA (4.5:1) |
| Grafite su arancio, chiaro (`#333333` su `#E88706`) | 4.76:1 | Passa AA |
| Grafite su arancio, scuro (`#333333` su `#F0993A`) | 5.9:1 | Passa AA |
| Arancio su bianco (`#E88706` su `#FFFFFF`) | 2.66:1 | Sotto soglia AA |
| Grafite su bianco (`#333333` su `#FFFFFF`) | 12.6:1 | Ottimo (invariato) |

In pratica il bottone primario arancione con testo bianco di QuickStore era già
al limite della leggibilità; passare a testo grafite lo risolve e, applicato
in modo sistematico, dà a QReport un'identità visiva distinta pur restando
nella stessa famiglia.

**Eccezione deliberata (dopo verifica su device)**: sui riempimenti pieni più
grandi/ad alta enfasi — pulsante primario e "tertiary" (`onPrimary`/
`onTertiary` in `Theme.kt`) — il tema **chiaro** è tornato a testo bianco: il
grafite lì appesantiva troppo visivamente, secondo il giudizio dell'utente su
device reale. È un'eccezione consapevole alla soglia di contrasto AA (resta
sotto 4.5:1), applicata solo a quei due ruoli, solo in chiaro — chip, badge,
mire d'angolo e tutto il resto restano su testo grafite come da regola. Il
tema **scuro** resta su grafite per `onPrimary`/`onTertiary` (l'arancio più
chiaro usato in dark renderebbe il bianco ancora meno leggibile, ~2.1:1).

### Conseguenza pratica per i componenti "densità severità"

In QuickStore i valori numerici che segnalano attenzione/criticità (quantità,
statistiche) potevano comparire in arancio direttamente su card bianca. In
QReport quei valori restano di colore testo normale (grafite) in tema chiaro
— la severità resta comunque leggibile tramite striscia laterale, mira
d'angolo e badge, che sono riempimenti e non inchiostro. In tema scuro invece
quegli stessi valori possono tornare arancio, perché lì lo sfondo è grafite.

## Palette

| Nome | Hex | Ruolo |
|---|---|---|
| Orange | `#E88706` (chiaro) / `#F0993A` (scuro) | Riempimento sempre; inchiostro solo su sfondo grafite |
| Graphite | `#333333` | Inchiostro — su bianco o sopra riempimento arancione |
| Paper | `#F4F4F4` | Sfondo pagina (chiaro) |
| Border | `#E3E3E3` | Bordi/hairline (chiaro) |
| Slate | `#707070` | Testo secondario |

Tema scuro: sfondo `#1B1B1B`/superficie `#262626` (famiglia "grafite" —
è questo il contesto che abilita l'arancio come inchiostro), testo `#F2F2F2`,
testo secondario `#ABABAB`.

## Tipografia, spaziatura, mire d'angolo, severità per intensità

Invariati da QuickStore, ed **effettivamente implementati**:

- Display/headline su **font di sistema** (Roboto, solo pesi più alti),
  **IBM Plex Mono** su codici articolo/voce e timestamp. Big Shoulders
  Display esiste solo nell'opzione "Nameplate industriale" (opzione 2) di
  QuickStore: per l'opzione arancio/technical è stato provato e scartato —
  troppo condensato per restare leggibile su titoli reali (codici,
  descrizioni voce) — e il titolo/display è tornato al font di sistema. Vedi
  `Type.kt` per il commento che documenta questa scelta.
- **Title/body/label su Inter** (non più IBM Plex Sans, cambiato dopo
  verifica su device): Plex Sans era bundlato come font variabile ma
  caricato senza istanziare l'asse "wght" per i pesi richiesti, quindi
  renderizzava tutto al peso di default — troppo sottile, poco leggibile
  alle dimensioni piccole. Inter è pensato apposta per la leggibilità su
  schermo a dimensioni piccole; bundlato come due pesi statici reali
  (Regular 400, SemiBold 600, licenza SIL OFL) in `res/font/`. I ruoli che
  usavano peso Medium (500, non disponibile come faccia reale) sono passati
  esplicitamente a SemiBold, dando anche il tono leggermente più "bold"
  richiesto.
- Scala di spaziatura `xs=4 · sm=8 · md=12 · lg=16 · xl=24 · xxl=32` in
  `app/app/presentation/ui/theme/Spacing.kt` — componenti su xs/sm, titoli e
  ritmo tra sezioni su lg/xl/xxl.
- **Raggio angoli card: 9dp** (`QReportCard.kt`) — via di mezzo tra i 12dp
  del mockup originale (troppo arrotondato) e i 7dp di un tentativo
  intermedio (troppo squadrato), scelto dopo verifica su device.
- Mire d'angolo da 9×9px su card e stat — **sempre presenti**, non solo sulle
  righe con qualcosa da segnalare: colore neutro (`colorScheme.outline`,
  grigio) sulle righe normali, arancio (`colorScheme.primary`) solo su
  quelle con severità/attenzione. Prima versione (fase 9) le ometteva del
  tutto sulle righe neutre — corretto in fase 13 dopo verifica su device.
- Severità per intensità dell'unico accento (non per tinta): striscia neutra
  o mira grigia = normale, striscia/mira arancio piena = attenzione, badge
  arancio pieno (testo grafite) = critico. **Il criterio è sempre "questa
  riga ha una criticità/attenzione reale?", mai un colore di stato letterale
  o un altro dato secondario** — es. la striscia di un check-up recente in
  Home riflette se ha voci critiche NOK non risolte, non il colore
  configurato per il suo stato (Bozza/In corso/Completato), che sono concetti
  indipendenti (fase 12).

## Implementazione nel codice

### Componente condiviso principale: `QReportCard`

`app/app/presentation/components/QReportCard.kt` — wrapper unico per **tutte**
le card piatte dell'app (list-item, stat readout, header, sezioni di dialog):
bordo 1dp `colorScheme.outline`, elevazione 0dp (nessuna ombra Material), forma
`RoundedCornerShape(9.dp)`, mira d'angolo opzionale via `tickColor`.

Un dettaglio da NON ripetere se si tocca ancora questo file: `CardDefaults
.cardColors()` **senza argomenti** non usa `colorScheme.surface` come sfondo —
la Card "filled" di Material3 prende di default `surfaceContainerHighest` dal
token `FilledCardTokens.ContainerColor`, che qui coincideva col colore del
bordo (`outline`), rendendo il bordo invisibile e la card un blocco grigio
pieno (fase 11). `containerColor` va sempre passato esplicito.

Pattern "striscia laterale colorata" (righe lista con stato — check-up
recenti, isole recenti, clienti recenti, check item): `Box(Modifier.width(3.dp)
.fillMaxHeight())` dentro una `Row` **deve** avere `Modifier.height(IntrinsicSize.Min)`
sulla Row, altrimenti la striscia collassa a un'altezza invisibile — non è
vero che Compose fa un secondo passaggio di misura automatico per
`fillMaxHeight()` da solo in un contenitore "wrap content" (fase 13).

### Altri componenti condivisi

| Componente | File Kotlin | Note |
|---|---|---|
| Card piatta con bordo/mira | `app/app/presentation/components/QReportCard.kt` | vedi sopra — usato ovunque |
| Bottoni con inchiostro corretto | `app/app/presentation/components/QrButtons.kt` | `QrTextButton`/`QrOutlinedButton`, `contentColor` di default = `onSurface` invece del `primary` (arancio) di Material3 di default — agganciati ovunque via `import ... as TextButton/OutlinedButton`, zero modifiche ai call site |
| Sezione con titolo+icona nei dialog form | `app/app/presentation/components/SectionCard.kt` | delega a `QReportCard` |
| Campo form / dropdown | `app/app/presentation/components/QrFormField.kt` / `QrDropdownField.kt` | |
| Riga Annulla/Salva nei dialog | `app/app/presentation/components/QrFormActionsRow.kt` | |
| Stat readout (numero+etichetta, con o senza icona) | `app/app/presentation/components/QrListStatItem.kt` | orientamento verticale/orizzontale |
| Nameplate → riga check item / checkup | `checkup/checkup/presentation/components/CheckupCard.kt`, `CheckItemCardWithPhotos` in `CheckUpDetailScreen.kt` | |
| Empty state | `app/app/presentation/components/EmptyState.kt` | |
| Error state | `app/app/presentation/components/ErrorState.kt` | |
| Chip filtro stato | Filtro stato in `CheckUpListScreen` | |
| Sezione modulo collassabile (Sicurezza/Meccanico/…) | `ModuleSectionWithPhotos` in `CheckUpDetailScreen.kt` | |

### Home (`app/app/presentation/ui/home/`)

Tutta la Home usa lo stesso linguaggio piatto: sezione = etichetta + stat
card individuali bordate (pattern "stat readout 3-up", non più chip dentro un
unico contenitore con header "Apri") + eventuale lista con striscia laterale.
`DashboardSectionCard` (il vecchio contenitore con header cliccabile) è stato
eliminato — non serve più.

Le sezioni **Clienti** e **Isole** sono parametrizzabili da una nuova
schermata **Preferenze Home** (`HomePreferencesScreen.kt`, raggiungibile da
Impostazioni o dall'icona in alto nella barra titolo della Home): 4
interruttori — statistiche clienti/isole, clienti/isole recenti — tutti
**spenti di default**. Se entrambi gli interruttori di una sezione sono
spenti, l'intera sezione (etichetta inclusa) non compare. La sezione
Check-up non è parametrizzata, resta sempre visibile. Persistenza via
`AppSettingsDataStore` (Preferences DataStore, non Room — nessuna migrazione
DB richiesta).

### Contenuti di esempio nel mockup HTML

(solo `design/mockup-orange-technical.html`, non nell'app reale): moduli
Sicurezza/Meccanico/Elettrico da `strings.xml`, voci `SAF_001` (barriere
fotoelettriche, critico), `MEC_001`, `ELE_001`; isole tipo POLY Move/POLY
Weld; stati checkup Bozza/In Corso/Completato; stati voce OK/NOK/In
Attesa/N-A. Nell'app reale i "codici" dei check item vengono dall'id
(editabile) del template checklist che li ha generati, non da questi
placeholder — vedi `checkup/items/presentation/ui/CheckItemTemplateFormDialog.kt`.

## Come portarlo in Figma

Identico al procedimento di QuickStore:

1. Plugin gratuito **Tokens Studio for Figma**.
2. Import → incolla `design/tokens-orange-technical.json` di QReport.
3. "Create Styles/Variables".
4. Font: Inter (nativo in Figma, è uno dei font di default del prodotto) su
   title/body/label, IBM Plex Mono su dati/codici; display/headline restano
   sul font di sistema del progetto Figma (Roboto), non Big Shoulders Display.
5. Ricostruisci i componenti della sezione "Componenti" del mockup come
   componenti Figma (Auto Layout) usando gli Styles/Variables appena creati.

## Riuso per la futura web app

Le custom property CSS in cima a `mockup-orange-technical.html`
(`:root { --sp-lg: 16px; ... }`) sono la stessa fonte di verità del token
JSON — copiabili direttamente come base del CSS, incluse le tre variabili
che codificano la regola d'inchiostro: `--accent-fill`, `--accent-on`,
`--accent-ink`.

## Stato di implementazione / cosa resta aperto

Implementato e verificato su device reale dall'utente dopo ogni fase (17
fasi totali): tema chiaro/scuro (`Color.kt`/`Theme.kt`/`Type.kt`), Home,
CheckUpDetailScreen, tutte le card lista dell'app (Client/Facility/Island/
MechanicalUnit/Checkup/Contact/Document/Contract/TechnicalIntervention/
BackupItem), form condivisi, regola d'inchiostro sui bottoni/icone dirette,
schermata Preferenze Home.

Candidati non ancora fatti (nessuna richiesta esplicita finora):
- Unificare i bottoni Salva singoli di Contact/Contract/MechanicalUnit (form
  con un solo pulsante invece del pattern `QrFormActionsRow` standard).
- Estendere `QrFormField`/`QrDropdownField` alle eccezioni rimaste dalla
  centralizzazione dei form.
- Colore identificativo per modulo checklist (richiederebbe `colorHex` su
  `ModuleTypeMaster` — feature nuova con migrazione DB, non solo restyling).
- `CheckupItemStatusChip.kt` (chip di stato sui singoli check item, testo
  bianco su riempimento colorato incl. un arancio `#FF9800` per lo stato
  "In attesa") non è stato toccato: è un sistema di colori di stato
  pre-esistente e indipendente dal brand arancio/grafite (stessa scelta
  "ibrida" dell'inizio: i colori di stato OK/NOK/Pending restano fuori dalla
  regola d'inchiostro del brand), non un residuo dimenticato.

Nessuna modifica alla direzione "opzione 2" (nameplate industriale non
ricolorata) di QuickStore: questo documento copre solo la variante
arancio/graphite adottata per QReport.
