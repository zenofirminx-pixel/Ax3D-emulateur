# l'Infinity — Android

Application Android native Kotlin qui se connecte à `https://aur-x-backend.vercel.app/api/chat` avec une requête POST et lit les événements Server-Sent Events (SSE) au fil de l'eau.

## Contenu
- Interface de chat sombre, futuriste et spatiale.
- Bulles séparées pour l'utilisateur et l'assistant.
- Affichage progressif des fragments SSE.
- Historique de la conversation conservé en mémoire pendant la session.
- Icône vectorielle Infinity.
- Compilation APK debug par GitHub Actions.

## Contrat réseau utilisé
La requête POST envoie `message`, `history`, `messages` et `stream: true`, avec `Accept: text/event-stream, application/json`.
Le lecteur accepte les événements SSE `data:` avec `[DONE]`, les formats de fragments OpenAI (`choices[0].delta.content`) et plusieurs formats courants (`token`, `content`, `text`, `response`).

**À vérifier avec le backend :** le nom exact du champ d'entrée et le format SSE doivent correspondre à l'implémentation actuellement déployée. Si le serveur n'accepte pas `stream: true` ou attend un autre schéma, il faudra adapter `InfinityApi.kt`.

## Compilation
Ouvrir le dépôt avec Android Studio ou pousser sur GitHub. Le workflow `.github/workflows/android.yml` construit `app-debug.apk`.
