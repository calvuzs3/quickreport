package net.calvuz.qreport.app.app.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================
// QReport Color System - Industrial Theme
// =============================================

// =============================================
// Brand Chrome — Arancio e Technical Design
// (accento unico di brand; sostituisce il vecchio blu/arancio come primary/
// secondary/tertiary di MaterialTheme in Theme.kt. Status/Criticality/Module
// più sotto NON fanno parte di questo sistema, restano segnali semantici
// indipendenti — vedi design/design-system.md, sezione "regola d'inchiostro".)
// =============================================

// Orange — riempimento (pulsanti/chip/badge/strisce/mire). Come inchiostro
// (testo/icona) ammesso solo su sfondo grafite.
val TechnicalOrangeLight = Color(0xFFE88706)
val TechnicalOrangeDark = Color(0xFFF0993A)

// Sfumatura più scura dell'arancio (stesso hue, non un terzo colore) — usata
// per il ruolo Material3 "tertiary".
val TechnicalOrangeDarkerLight = Color(0xFFB96A05)
val TechnicalOrangeDarkerDark = Color(0xFFD68A3E)

// Graphite — inchiostro primario: su bianco, o sopra un riempimento arancione
// (in entrambi i temi: mai bianco su arancio, contrasto insufficiente).
val TechnicalGraphite = Color(0xFF333333)

// Vecchi colori brand — non più usati da Theme.kt, ma restano qui perché
// Status/Progress/Focus più sotto li usano come base: quei colori sono
// segnali semantici indipendenti dal brand chrome, non toccati dall'adozione
// arancio/grafite (vedi design/design-system.md).
val QReportBlue = Color(0xFF1976D2)
val QReportBlueLight = Color(0xFF63A4FF)
val QReportBlueDark = Color(0xFF004BA0)
val QReportOrange = Color(0xFFFF8F00)
val QReportOrangeLight = Color(0xFFFFC947)
val QReportOrangeDark = Color(0xFFC56000)

// Neutri tema chiaro
val TechnicalPaper = Color(0xFFF4F4F4)        // sfondo pagina
val TechnicalSurfaceLight = Color(0xFFFFFFFF) // card/superficie
val TechnicalBorder = Color(0xFFE3E3E3)       // bordi/hairline
val TechnicalSlate = Color(0xFF707070)        // testo secondario

// Neutri tema scuro (famiglia "grafite": qui l'arancio è ammesso come inchiostro)
val TechnicalBackgroundDark = Color(0xFF1B1B1B)
val TechnicalSurfaceDark = Color(0xFF262626)
val TechnicalBorderDark = Color(0xFF3A3A3A)
val TechnicalTextDark = Color(0xFFF2F2F2)
val TechnicalTextSecondaryDark = Color(0xFFABABAB)

// Status Colors
val QReportGreen = Color(0xFF388E3C)      // Success/OK status
val QReportGreenLight = Color(0xFF6ABF47) // Light green
val QReportGreenDark = Color(0xFF00600F)  // Dark green

val QReportRed = Color(0xFFD32F2F)        // Error/NOK status
val QReportRedLight = Color(0xFFFF6659)   // Light red
val QReportRedDark = Color(0xFF9A0007)    // Dark red

val QReportYellow = Color(0xFFFBC02D)     // Pending/Warning status
val QReportYellowLight = Color(0xFFFFF263) // Light yellow
val QReportYellowDark = Color(0xFFC49000) // Dark yellow

// Neutral Colors - Professional Grey Scale
val QReportWhite = Color(0xFFFFFFFF)
val QReportBlack = Color(0xFF000000)
val QReportGrey50 = Color(0xFFFAFAFA)
val QReportGrey100 = Color(0xFFF5F5F5)
val QReportGrey200 = Color(0xFFEEEEEE)
val QReportGrey300 = Color(0xFFE0E0E0)
val QReportGrey400 = Color(0xFFBDBDBD)
val QReportGrey500 = Color(0xFF9E9E9E)
val QReportGrey600 = Color(0xFF757575)
val QReportGrey700 = Color(0xFF616161)
val QReportGrey800 = Color(0xFF424242)
val QReportGrey900 = Color(0xFF212121)

// =============================================
// Status-Specific Colors
// =============================================

// Check Item Status Colors
val StatusOK = QReportGreen
val StatusNOK = QReportRed
val StatusPending = QReportOrange
val StatusNA = QReportGrey500

// Criticality Colors
val CriticalityHigh = QReportRed
val CriticalityMedium = QReportOrange
val CriticalityLow = QReportGreen
val CriticalityNA = QReportGrey500

// Urgency Colors (for spare parts)
val UrgencyCritical = QReportRed
val UrgencyHigh = QReportOrange
val UrgencyMedium = QReportYellow
val UrgencyLow = QReportGreen

// =============================================
// Surface Variations
// =============================================

// Card Backgrounds
val CardBackground = QReportWhite
val CardBackgroundElevated = Color(0xFFFAFAFA)
val CardBackgroundSelected = Color(0xFFE3F2FD)

// Module Colors (for check item grouping)
val ModuleSafety = Color(0xFFE53935)      // Red for safety
val ModuleMechanical = Color(0xFF3F51B5)   // Indigo for mechanical
val ModuleElectrical = Color(0xFFFF9800)   // Orange for electrical
val ModuleSoftware = Color(0xFF4CAF50)     // Green for software
val ModuleSpecific = Color(0xFF9C27B0)     // Purple for island-specific

// =============================================
// Semantic Colors
// =============================================

// Progress Indicators
val ProgressCompleted = QReportGreen
val ProgressInProgress = QReportBlue
val ProgressPending = QReportOrange
val ProgressBackground = QReportGrey200

// Photo/Media Colors
val PhotoBackground = QReportGrey100
val PhotoBorder = QReportGrey300
val PhotoSelected = QReportBlue

// Export Status
val ExportReady = QReportGreen
val ExportProcessing = QReportOrange
val ExportError = QReportRed

// =============================================
// Accessibility Colors
// =============================================

// High contrast variants for better accessibility
val HighContrastPrimary = Color(0xFF0D47A1)
val HighContrastSecondary = Color(0xFFE65100)
val HighContrastError = Color(0xFFB71C1C)
val HighContrastSuccess = Color(0xFF1B5E20)

// Focus indicators
val FocusIndicator = QReportBlue
val FocusIndicatorHigh = Color(0xFF0D47A1)

// Nota: le funzioni getCheckItemStatusColor/getCriticalityColor/getModuleColor
// (String -> Color) sono state rimosse: erano dead code (zero call site), in
// conflitto con la fonte di colore reale per lo stato check-item
// (CheckItemStatusExt.getColor(), su enum CheckItemStatus) e strutturalmente
// superate per criticità/modulo, ora a metà migrazione verso master data Room
// (CriticalityMaster/ModuleTypeMaster) — il colore da lì va letto tramite
// ColorUtils.toComposeColor() su un campo colorHex, non da un when su String.