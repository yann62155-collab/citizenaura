# Citizen Aura (citizenaura) — NeoForge 21.1.215 / MC 1.21.1 / Java 21

Mod **serveur uniquement** : à mettre dans le dossier `mods` du serveur BoxToPlay, rien à installer chez les joueurs.
Particules vanilla envoyées par le serveur → visibles par les joueurs PC **et** console (Xbox via Geyser).

## Commandes (admin, permission niveau 2 / OP)
- `/aura donner <joueur> <couleur> [style]` — ex : `/aura donner Ishawakiii or orbite`
- `/aura retirer <joueur>`
- `/aura liste`
- `/aura couleurs`

Couleurs : rouge, bleu, vert, jaune, or, orange, violet, rose, cyan, blanc, noir, argent, arcenciel — ou hexa `ff8800`.
Styles : `orbite` (lueur + petites particules qui tournent, par défaut), `spirale`, `halo`.

Les auras sont sauvegardées dans le monde (`data/citizenaura.dat`) et cachées si le joueur est invisible ou en spectateur.

## Compilation
Pousser sur GitHub (ex : yann62155-collab/citizenaura) : GitHub Actions compile et publie le .jar en Release.
