# QReport — Design System (Arancio e Technical Design)

Adattamento per QReport della direzione "Arancio e Technical Design" già scelta e
implementata in QuickStore (vedi `../../QuickStore/design/design-system.md`,
opzione 3). Stessa struttura, stessa tipografia, stessa spaziatura, stessa
codifica della severità per intensità di un unico accento — cambia solo la
**regola d'inchiostro** tra arancio e graphite.

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

Invariati da QuickStore:

- Display/headline su **font di sistema** (Roboto, solo pesi più alti),
  **IBM Plex Mono** su codici articolo/voce e timestamp. Big Shoulders
  Display esiste solo nell'opzione "Nameplate industriale" (opzione 2) di
  QuickStore: per l'opzione arancio/technical è stato provato e scartato —
  troppo condensato per restare leggibile su titoli reali (codici,
  descrizioni voce) — e il titolo/display è tornato al font di sistema. Vedi
  `Type.kt` di QuickStore per il commento che documenta questa scelta.
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
- Scala di spaziatura `xs=4 · sm=8 · md=12 · lg=16 · xl=24 · xxl=32` —
  componenti su xs/sm, titoli e ritmo tra sezioni su lg/xl/xxl. QReport non ha
  ancora un file token dedicato analogo a `Spacing.kt` di QuickStore: questa è
  una scala proposta, da introdurre se/quando si implementa la palette.
- Mire d'angolo da 9×9px su card e stat, colore = colore semantico della riga.
- Severità per intensità dell'unico accento (non per tinta): striscia neutra =
  normale, striscia arancio piena = attenzione, badge arancio pieno (testo
  grafite) = critico.

## Componenti — mappatura sul codice esistente

| Componente design | File Kotlin corrispondente |
|---|---|
| Nameplate → riga check item / checkup | `checkup/checkup/presentation/components/CheckupCard.kt` |
| Empty state | `app/app/presentation/components/EmptyState.kt` |
| Error state | `app/app/presentation/components/ErrorState.kt` |
| Stat readout (3-up) | `HomeScreen.kt`, sezioni Check-up/Isole |
| Chip filtro stato | Filtro stato in `CheckUpListScreen` |
| Sezione modulo collassabile (Sicurezza/Meccanico/…) | `CheckUpDetailScreen.kt` |

Contenuti di esempio usati nel mockup (reali, non placeholder): moduli
Sicurezza/Meccanico/Elettrico da `strings.xml`, voci `SAF_001` (barriere
fotoelettriche, critico), `MEC_001`, `ELE_001`; isole tipo POLY Move/POLY
Weld; stati checkup Bozza/In Corso/Completato; stati voce OK/NOK/In
Attesa/N-A.

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

## Deliberatamente fuori scope

- Nessuna modifica al codice Android in questo giro — questo è materiale di
  design (vedi `Color.kt`/`Theme.kt`/`Type.kt` attuali, che restano il tema
  blu industriale in uso oggi; l'adozione di questa palette è una scelta
  successiva, non presa qui).
- Non tutte le schermate dell'app sono mockate, solo le tre più
  rappresentative (Home, Check-up list, Check-up detail) — le altre seguono
  lo stesso linguaggio.
- Nessuna modifica alla direzione "opzione 2" (nameplate industriale non
  ricolorata) di QuickStore: questo documento copre solo la variante
  arancio/graphite richiesta per QReport.
