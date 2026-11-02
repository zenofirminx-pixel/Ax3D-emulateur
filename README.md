# AX3D Engine Alpha 0.4

Projet Android Kotlin + C++17. Cette version ajoute le format binaire **AX3D Binary Executable (ABX) v1** et son interpréteur sandboxé.

## Format ABX
- Signature obligatoire `AX3D` (octets `41 58 33 44`).
- En-tête 64 octets, version 1, UUID de jeu, tailles et CRC32 du bytecode.
- Bytecode interprété par la VM C++; aucun octet ABX n'est exécuté comme code machine ARM.
- Limites : fichier 64 MiB, code 16 MiB, ressources 48 MiB, pile et mémoire VM bornées.

## Créer un jeu ABX depuis Termux
Depuis la racine du dépôt :
```sh
mkdir -p mygame
printf '\\x01\\x2a\\x00\\x00\\x00\\x20\\xff' > mygame/hello.abxcode
python3 tools/create_abx.py mygame/hello.abxcode mygame/Hello-AX3D.abx
```
Ce programme pousse 42, l'affiche dans le journal de diagnostic puis s'arrête. Importer le `.abx` dans l'application et toucher « CHARGER ET EXÉCUTER ».

Opcodes v1 : `00` NOP, `01+i32` PUSH, `10` NEG, `11` ADD, `12` SUB, `13` MUL, `14` DIV, `20` PRINT, `21` LOAD via pile, `22+i32` STORE, `23+i32` LOAD direct, `30` MOVE_X diagnostic, `31` DRAW_OBJECT diagnostic, `32+i32` JMP, `40+i32` JNZ, `FF` HALT. Voir `ABX-FORMAT.md`.

## Construire
Ouvrir dans Android Studio ou lancer le workflow GitHub Actions `.github/workflows/android.yml`. La présence du workflow ne garantit pas le build : vérifier le résultat Actions et l'APK généré.

## Limites honnêtes
ABX v1 est une VM de bytecode minimaliste. Les événements MOVE_X/DRAW_OBJECT sont actuellement des diagnostics, pas un rendu 3D. Il ne s'agit pas d'un runtime de jeux 3D complet. L'émulation PSP ISO/CSO n'est pas prise en charge par ABX et reste expérimentale dans le code séparé.
