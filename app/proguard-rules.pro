# Standaard Compose/Kotlin regels zijn voldoende voor deze app; de
# woordenlijst zit als asset (niet als code) en wordt dus niet door
# R8/ProGuard aangeraakt.

# Extra zekerheid bovenop de consumer-proguard-regels die AndroidX Lifecycle
# zelf al meelevert voor ViewModel-constructie via reflectie: bewaar de
# ViewModel-class expliciet zodat een release-build (minify aan) 'm nooit
# per ongeluk verwijdert of hernoemt.
-keep class com.cryptora.app.CryptogramViewModel { *; }

# Deze data classes worden met .equals()/.copy() vergeleken en RecentSearch
# wordt bovendien handmatig (de)geserialiseerd naar SharedPreferences-tekst;
# bewaar ze zodat R8 velden/namen niet op een manier verandert die dat breekt.
-keep class com.cryptora.app.data.RecentSearch { *; }
-keep class com.cryptora.app.data.SearchEngine$Query { *; }
-keep class com.cryptora.app.data.SearchEngine$Result { *; }

# LET OP: dit bestand is met zorg samengesteld maar nog NIET getest tegen een
# echte release-build (de sandbox waarin dit project is opgezet heeft geen
# Android-SDK/Gradle-toegang). Bouw zelf een keer een release-APK/AAB
# (Build > Generate Signed App Bundle/APK) en test 'm op een toestel vóór
# publicatie, om zeker te weten dat minify/shrink niets breekt.
